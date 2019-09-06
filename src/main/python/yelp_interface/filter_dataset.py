from yelp_interface.data_interface import read_reviews, read_businesses, read_users, read_tips
from os import path
import settings
import json

"""Filter files, only retaining users who have reviewed at least 20 restaurant
of food things"""

MIN_REVIEWS = 20
BUSINESS_FILE = path.join(settings.DATA_DIR, 'business.json')
REVIEW_FILE = path.join(settings.DATA_DIR, 'review.json')
USER_FILE = path.join(settings.DATA_DIR, 'user.json')
TIP_FILE = path.join(settings.DATA_DIR, 'tip.json')


def read_all():
    def business_filter(business):
        if not business.get('categories'):
            return None
        categories = set(business['categories'].split(', '))
        if 'Food' in categories or 'Restaurants' in categories:
            return business
        else:
            return None
    businesses = read_businesses(BUSINESS_FILE, filter=business_filter)

    def review_filter(review):
        business_id = review['business_id']
        if business_id in businesses:
            if 'text' in review:
                del review['text']
            return review
        else:
            return None
    reviews_by_userid, _ = read_reviews(REVIEW_FILE, filter=review_filter)

    def tip_filter(tip):
        business_id = tip['business_id']
        if business_id in businesses:
            return tip
        else:
            return None

    tips_by_userid = read_tips(TIP_FILE, filter=tip_filter)

    def user_filter(user):
        if len(reviews_by_userid.get(user['user_id'], [])) >= MIN_REVIEWS:
            return user
        else:
            return None
    users_by_id = read_users(USER_FILE, filter=user_filter)

    print(len(users_by_id))

    return users_by_id, reviews_by_userid, tips_by_userid, businesses


def write_filtered(users_by_id, reviews_by_userid, tips_by_userid, businesses):
    business_filtered = path.join(settings.DATA_DIR, 'business_filtered.json')
    review_filtered = path.join(settings.DATA_DIR, 'review_filtered.json')
    user_filtered = path.join(settings.DATA_DIR, 'user_filtered.json')
    tip_filtered = path.join(settings.DATA_DIR, 'tip_filtered.json')

    with open(user_filtered, 'w') as f:
        for user in users_by_id.values():
            f.write(f"{json.dumps(user)}\n")

    with open(review_filtered, 'w') as f:
        for user_id, review in reviews_by_userid.items():
            if user_id in users_by_id:
                f.write(f"{json.dumps(review)}\n")

    with open(tip_filtered, 'w') as f:
        for user_id, tip in tips_by_userid.items():
            if user_id in users_by_id:
                f.write(f"{json.dumps(tip)}\n")

    with open(business_filtered, 'w') as f:
        for business in businesses:
            f.write(f"{json.dumps(business)}")


if __name__ == '__main__':
    users_by_id, reviews_by_userid, tips_by_userid, businesses = read_all()
    write_filtered(users_by_id, reviews_by_userid, tips_by_userid, businesses)


