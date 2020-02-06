import os.path as path
import csv
from collections import defaultdict, namedtuple

import settings
from tools.id_index_map import IDIndexMap

"""
Filter files, only retaining users who have reviewed at least 20 items.
Also dedupes reviews (retaining only the latest review a user left for an item), which
may or may not be necessary for epinions.

Maps each ID to a contiguous ascending integer, which can later be used (without mapping)
to refer to that user in a matrix of user x item reviews.
"""

MIN_REVIEWS = 10
LEAVE_OUT = 1
ITEM_REVIEW_FILE = path.join(settings.EPINIONS_DATA_DIR, 'rating.txt')
USER_REVIEW_FILE = path.join(settings.EPINIONS_DATA_DIR, 'user_rating.txt')
CONTENT_FILE = path.join(settings.EPINIONS_DATA_DIR, 'mc.txt')

Review = namedtuple('Review', ['object_id', 'user_id', 'rating', 'status', 'creation_date', 'date', 'type', 'vert_id'])
TrustLink = namedtuple('TrustLink', ['user_id', 'other_id', 'rating', 'date'])
Content = namedtuple('Content', ['content_id', 'author_id', 'subject_id'])


def read_all():
    # Load reviews and take note of which users have > 20 reviews
    reviews_by_user = defaultdict(list)
    with open(ITEM_REVIEW_FILE, 'r') as f:
        reader = csv.reader(f, delimiter='\t')
        for row in reader:
            review = Review(*row)
            if int(review.rating) > 5:
                review = review._replace(rating='5')
            reviews_by_user[review.user_id].append(review)

    relevant_users = set()
    relevant_reviews = []
    for key in reviews_by_user.keys():
        if len(reviews_by_user[key]) >= MIN_REVIEWS:
            relevant_users.add(key)
            relevant_reviews.extend(reviews_by_user[key])

    print(f"{len(relevant_users)} relevant users")
    print(f"{len(relevant_reviews)} relevant reviews")

    # Load the user-to-user trust links for relevant users.
    trust_links = []
    with open(USER_REVIEW_FILE, 'r') as f:
        reader = csv.reader(f, delimiter='\t')
        for row in reader:
            trust_link = TrustLink(*row)
            if trust_link.user_id in relevant_users or trust_link.other_id in relevant_users:
                trust_links.append(trust_link)

    # Load up the content, in case the subjects and numbers of content are worth considering.
    content_list = []
    with open(CONTENT_FILE, 'r') as f:
        reader = csv.reader(f, delimiter='|')
        for row in reader:
            content = Content(*row)
            if content.author_id in relevant_users:
                content_list.append(content)

    return relevant_users, reviews_by_user, trust_links, content_list


def write_filtered(relevant_users, reviews, trust_links, content):
    ITEM_REVIEW_FILTERED = path.join(settings.EPINIONS_DATA_DIR, 'rating_filtered.txt')
    USER_REVIEW_FILTERED = path.join(settings.EPINIONS_DATA_DIR, 'user_rating_filtered.txt')
    CONTENT_FILTERED = path.join(settings.EPINIONS_DATA_DIR, 'mc_filtered.txt')
    RELEVANT_USERS = path.join(settings.EPINIONS_DATA_DIR, "users.txt")

    itemIdMap= IDIndexMap()
    userIdMap = IDIndexMap()

    with open(RELEVANT_USERS, 'w') as f:
        for user in relevant_users:
            f.write(f"{userIdMap.get_int(user)}\n")

    with open(USER_REVIEW_FILTERED, 'w') as f:
        writer = csv.writer(f)
        writer.writerow(trust_links[0]._fields)
        for link in trust_links:
            link = link._replace(user_id=userIdMap.get_int(link.user_id))
            link = link._replace(other_id=userIdMap.get_int(link.other_id))
            writer.writerow(link)

    with open(ITEM_REVIEW_FILTERED, 'w') as f:
        writer = csv.writer(f)
        writer.writerow(next(iter(reviews.items()))[1][0]._fields)
        for user_id, reviews in reviews.items():
            for review in reviews:
                review = review._replace(user_id=userIdMap.get_int(review.user_id))
                review = review._replace(object_id=itemIdMap.get_int(review.object_id))
                writer.writerow(review)

    with open(CONTENT_FILTERED, 'w') as f:
        writer = csv.writer(f)
        writer.writerow(content[0]._fields)
        for cont in content:
            cont = cont._replace(author_id=userIdMap.get_int(cont.author_id))
            writer.writerow(cont)


def run():
    users, reviews, links, content = read_all()
    write_filtered(users, reviews, links, content)


if __name__ == '__main__':
    run()
