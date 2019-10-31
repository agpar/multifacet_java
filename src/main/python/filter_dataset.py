#!/usr/bin/python3

from yelp_interface.data_interface import read_reviews, read_businesses, read_users, read_tips
from os import path
import settings
import json
from tools.id_index_map import IDIndexMap

"""
Filter files, only retaining users who have reviewed at least 20 restaurant
or food related reviews.

Maps each user id to a integer, which is also used as the index to refer to that
user in any matrix in later processing.
"""

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
        if review['business_id'] in businesses:
            if 'text' in review:
                del review['text']
            return review
        else:
            return None
    reviews_by_userid, _ = read_reviews(REVIEW_FILE, filter=review_filter)

    def tip_filter(tip):
        if tip['business_id'] in businesses:
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


def filter_friend_list(user, userIdMap):
    return ", ".join([str(userIdMap.get_int(u)) for u in user['friends'].split(", ")])


def write_filtered(users_by_id, reviews_by_userid, tips_by_userid, businesses):
    business_filtered = path.join(settings.DATA_DIR, 'business_filtered.json')
    review_filtered = path.join(settings.DATA_DIR, 'review_filtered.json')
    user_filtered = path.join(settings.DATA_DIR, 'user_filtered.json')
    tip_filtered = path.join(settings.DATA_DIR, 'tip_filtered.json')

    businessIdMap = IDIndexMap()
    reviewIdMap = IDIndexMap()
    userIdMap = IDIndexMap()

    # Assign sequential IDs to primary users
    for user in users_by_id.values():
        user['true_user_id'] = user['user_id']
        user['user_id'] = userIdMap.get_int(user['user_id'])

    with open(user_filtered, 'w') as f:
        for user in users_by_id.values():
            user['friends'] = filter_friend_list(user, userIdMap)
            f.write(f"{json.dumps(user)}\n")

    with open(review_filtered, 'w') as f:
        for user_id, reviews in reviews_by_userid.items():
            for review in reviews:
                review['true_review_id'] = review['review_id']
                review['review_id'] = reviewIdMap.get_int(review['review_id'])
                review['business_id'] = businessIdMap.get_int(review['business_id'])
                review['user_id'] = userIdMap.get_int(review['user_id'])
                f.write(f"{json.dumps(review)}\n")

    with open(tip_filtered, 'w') as f:
        for user_id, tips in tips_by_userid.items():
            for tip in tips:
                tip['business_id'] = businessIdMap.get_int(tip['business_id'])
                tip['user_id'] = userIdMap.get_int(tip['user_id'])
                f.write(f"{json.dumps(tip)}\n")

    with open(business_filtered, 'w') as f:
        for business in businesses.values():
            business['true_business_id'] = business['business_id']
            business['business_id'] = businessIdMap.get_int(business['business_id'])
            f.write(f"{json.dumps(business)}\n")


if __name__ == '__main__':
    users_by_id, reviews_by_userid, tips_by_userid, businesses = read_all()
    write_filtered(users_by_id, reviews_by_userid, tips_by_userid, businesses)


