"""
Methods for pulling data out of YELP dataset.

To reduce the amount of data pulled into memory, the current
plan is to specify some number of users (NUM_USERS) then only
pull in the reviews/businesses/tips that are relevant to those
users. In the case of a user having a friend that is not in
the selected set, that data will be retained but the system
will not recurse into importing that user.

Since these sets are so large, it might be good to write out
a subset before doing all the dev work.
"""
from os import path
from collections import defaultdict
from collections import namedtuple
import json
import math

from tools.user_reviews import UserReviews

try:
    import settings
except ImportError as e:
    print("Failed to import settings. Did you create a src/settings.py file?")
    raise e


RatingTuple = namedtuple('Rating', 'user_id item_id score')


class YelpData:
    def __init__(self, users, reviews, tips, businesses):
        self._users = users
        self._reviews = reviews
        self._tips = tips
        self._businesses = businesses
        self.review_avg = sum(r['stars'] for rlist in self._reviews.values() for r in rlist) / sum(
            len(rlist) for rlist in self._reviews.values())

        self._rating_tuples = []
        self.reviews_by_item = defaultdict(list)
        for review_list in self._reviews.values():
            for review in review_list:
                self.reviews_by_item[review['business_id']].append(review)

        self.tips_by_item = defaultdict(list)
        for tip_list in self._tips.values():
            for tip in tip_list:
                self.tips_by_item[tip['business_id']].append(tip)

    def get_user(self, user_id):
        user = self._users[user_id]
        if user.get('reviews'):
            return user
        reviews = self._reviews.get(user_id, [])
        tips = self._tips.get(user_id, [])
        user['reviews'], user['tips'] = [], []
        user['friends'] = self.parse_friends(user)
        for review in reviews:
            business = self._businesses.get(review['business_id'])
            review['business'] = business
            user['reviews'].append(review)
        for tip in tips:
            business = self._businesses.get(tip['business_id'])
            tip['business'] = business
            user['tips'].append(tip)
        user['reviews'] = UserReviews(user['reviews'], self.reviews_by_item)

        return user

    def get_reviews_for_item(self, business):
        if isinstance(business, dict):
            key = business['business_id']
        elif isinstance(business, str):
            key = business
        else:
            raise Exception("'business' is not a string or a dict")

        return self.reviews_by_item[key]

    def get_tips_for_item(self, business):
        if isinstance(business, dict):
            key = business['business_id']
        elif isinstance(business, str):
            key = business
        else:
            raise Exception("'business' is not a string or a dict")

        return self.tips_by_item[key]

    def users(self):
        for user in self._users.values():
            yield self.get_user(user['user_id'])

    def rating_tuples(self):
        if self._rating_tuples:
            return self.self._rating_tuples

        rating_tuples = []
        for user in self._users.values():
            for review in self._reviews[user['user_id']]:
                user_id = user['user_id']
                item_id = review['business_id']
                score = review['stars']
                rating_tuples.append(RatingTuple(user_id, item_id, score))
        self._rating_tuples = rating_tuples
        return rating_tuples

    @staticmethod
    def parse_friends(user):
        if not user['friends']:
            return set()
        else:
            return set(u.strip() for u in user['friends'].split(","))


def read_data(user_range=(0, settings.DATA_NUM_USERS), read_sample=settings.DATA_READ_SAMPLE,
              user_filter=None, review_filter=None, tip_filter=None,
              business_filter=None):
    """Read data from yelp data set. Return a YelpData object with contents.

    :param user_range: A tuple specifying the indexes of the first and last user to read.
    :param user_filter: An optional f: dict -> dict which can be used to modify users as they are read.
    :param review_filter: An optional f: dict -> dict which can be used to modify reviews as they are read.
    :param tip_filter:  An optional f: dict -> dict which can be used to modify tips as they are read.
    :param business_filter:  An optional f: dict -> dict which can be used to modify businesses as they are read.
    :param read_sample: A boolean flag. Set to true to load a predefined sample (for testing)
    :return: A YelpData with the data read from text files.
    """
    if not read_sample:
        USERS_FILE = path.join(settings.YELP_DATA_DIR, 'user_filtered.json')
        BUSINESS_FILE = path.join(settings.YELP_DATA_DIR, 'business_filtered.json')
        REVIEW_FILE = path.join(settings.YELP_DATA_DIR, 'review_train_filtered.json')
        TIP_FILE = path.join(settings.YELP_DATA_DIR, 'tip_filtered.json')
    else:
        USERS_FILE = path.join(settings.YELP_DATA_DIR, 'user_sample.json')
        BUSINESS_FILE = path.join(settings.YELP_DATA_DIR, 'business_sample.json')
        REVIEW_FILE = path.join(settings.YELP_DATA_DIR, 'review_sample.json')
        TIP_FILE = path.join(settings.YELP_DATA_DIR, 'tip_sample.json')

    if not user_filter:
        user_filter = lambda x: x
    if not review_filter:
        review_filter = lambda x: x
    if not tip_filter:
        tip_filter = lambda x: x
    if not business_filter:
        business_filter = lambda x: x

    # Indexed by user_id.
    users = read_users(USERS_FILE, user_range, filter=user_filter)
    reviews, reviewed_business_ids = read_reviews(REVIEW_FILE, filter=review_filter)
    businesses = read_businesses(BUSINESS_FILE, filter=business_filter)
    tips = read_tips(TIP_FILE, filter=tip_filter)

    return YelpData(users, reviews, tips, businesses)


def read_tips(tip_file, filter=lambda x: x):
    tips = defaultdict(list)
    with open(tip_file, 'r') as f:
        for line in f:
            tip = json.loads(line)
            tip['text'] = ''
            filtered_tip = filter(tip)
            if filtered_tip:
                tips[tip['user_id']].append(filtered_tip)
    return tips


def read_users(user_file, user_range=(0, math.inf), filter=lambda x: x):
    users = {}
    with open(user_file, 'r') as f:
        line_no = 0
        for line in f:
            if line_no >= user_range[0]:
                user = json.loads(line)
                filtered_user = filter(user)
                if filtered_user:
                    users[user['user_id']] = filtered_user
            line_no += 1
            if line_no >= user_range[1]:
                break
    return users


def read_reviews(review_file, filter=lambda x: x):
    # Indexed by user_id
    reviews = defaultdict(list)
    with open(review_file, 'r') as f:
        for line in f:
            review = json.loads(line)
            if review.get('text'):
                del review['text']
            filtered_review = filter(review)
            if filtered_review:
                reviews[review['user_id']].append(filtered_review)
    reviewed_business_ids = set([r['business_id'] for rlist in reviews.values() for r in rlist])
    return reviews, reviewed_business_ids


def read_businesses(business_file, filter=lambda x: x):
    # Indexed by review_id and tip id
    businesses = {}
    with open(business_file) as f:
        for line in f:
            business = json.loads(line)
            filtered_business = filter(business)
            if filtered_business:
                businesses[business['business_id']] = filtered_business
    return businesses

