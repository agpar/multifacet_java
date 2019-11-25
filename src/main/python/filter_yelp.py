#!/usr/bin/python3
from tools.user_reviews import UserReviews
from yelp_interface.data_interface import read_reviews, read_businesses, read_users, read_tips
from os import path
import settings
import json
from tools.id_index_map import IDIndexMap
import numpy as np
import math
from collections import defaultdict, Counter
import matplotlib.pyplot as plt

"""
Filter users. Only look at users who have rated at least MIN_REVIEWS restaurants. Pick
NUM_USERS randomly from the set. Reviews are deduped, using only the latest review for
each item for each user.

Splits reviews into train/test set using leave k out strategy.
 
Maps each user id to a integer, which is also used as the index to refer to that
user in any matrix in later processing.
"""

MIN_USER_REVIEWS = 10
MIN_ITEM_REVIEWS = 10
LEAVE_OUT = 1
NUM_USERS = 30_000
BUSINESS_FILE = path.join(settings.YELP_DATA_DIR, 'business.json')
REVIEW_FILE = path.join(settings.YELP_DATA_DIR, 'review.json')
USER_FILE = path.join(settings.YELP_DATA_DIR, 'user.json')
TIP_FILE = path.join(settings.YELP_DATA_DIR, 'tip.json')


def read_all(min_user_reviews=MIN_USER_REVIEWS):
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
    for user in reviews_by_userid:
        reviews_by_userid[user] = UserReviews.linear_dupe_removal(reviews_by_userid[user])

    def tip_filter(tip):
        if tip['business_id'] in businesses:
            return tip
        else:
            return None

    tips_by_userid = read_tips(TIP_FILE, filter=tip_filter)

    def user_filter(user):
        if len(reviews_by_userid.get(user['user_id'], [])) >= min_user_reviews:
            return user
        else:
            return None
    users_by_id = read_users(USER_FILE, filter=user_filter)

    return users_by_id, reviews_by_userid, tips_by_userid, businesses


def choose_sample(users_by_id, reviews_by_userid):
    # Choose a random subset of users, with dist based on log number of reviews.
    keys = list(users_by_id.keys())
    probs = np.array([math.log(len(reviews_by_userid[key])) for key in keys])
    probs = probs / np.sum(probs)
    random_ids = np.random.choice(keys, size=NUM_USERS, replace=False, p=probs)
    random_users_by_id = dict()
    for random_id in random_ids:
        random_users_by_id[random_id] = users_by_id[random_id]

    return random_users_by_id


def write_stats(nat_users, sampled_users, reviews):
    stats_file = path.join(settings.YELP_DATA_DIR, 'sample_stat.json')
    nat_review_lens = [len(reviews[user]) for user in nat_users]
    sam_review_lens = [len(reviews[user]) for user in sampled_users]
    stats = {}
    stats['nat_mean_review_len'] = float(np.mean(nat_review_lens))
    stats['nat_median_review_len'] = float(np.median(nat_review_lens))
    stats['nat_min_review_len'] = float(np.min(nat_review_lens))
    stats['nat_max_review_len'] = float(np.max(nat_review_lens))
    stats['sam_mean_review_len'] = float(np.mean(sam_review_lens))
    stats['sam_median_review_len'] = float(np.median(sam_review_lens))
    stats['sam_min_review_len'] = float(np.min(sam_review_lens))
    stats['sam_max_review_len'] = float(np.max(sam_review_lens))

    with open(stats_file, 'w') as f:
        json.dump(stats, f)


def filter_friend_list(user, userIdMap):
    return ", ".join([str(userIdMap.get_int(u)) for u in user['friends'].split(", ")])


def split_reviews(selected_users, reviews_by_user_id):
    test_set = defaultdict(list)
    for user_id in selected_users:
        user_reviews = reviews_by_user_id[user_id]
        for i in range(LEAVE_OUT):
            selected_idx = np.random.choice(len(user_reviews))
            selected_review = user_reviews.pop(selected_idx)
            test_set[user_id].append(selected_review)
    return reviews_by_userid, test_set


def write_filtered(users_by_id, review_train, review_test, tips_by_userid, businesses):
    business_filtered = path.join(settings.YELP_DATA_DIR, 'business_filtered.json')
    review_train_filtered = path.join(settings.YELP_DATA_DIR, 'review_train_filtered.json')
    review_test_filtered = path.join(settings.YELP_DATA_DIR, 'review_test_filtered.json')
    user_filtered = path.join(settings.YELP_DATA_DIR, 'user_filtered.json')
    tip_filtered = path.join(settings.YELP_DATA_DIR, 'tip_filtered.json')

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

    for fname, data in [(review_train_filtered, review_train), (review_test_filtered, review_test)]:
        with open(fname, 'w') as f:
            for user_id, reviews in data.items():
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


def plot_review_counts(user_ids, reviews_by_userid):
    lens = [len(reviews_by_userid[user]) for user in user_ids]
    lens.sort()
    counts = Counter(lens)
    plt.plot(list(counts.keys()), list(counts.values()))
    plt.yscale('log')
    plt.xlabel("Review #")
    plt.ylabel("User #")


if __name__ == '__main__':
    users_by_id, reviews_by_userid, tips_by_userid, businesses = read_all()
    random_users = choose_sample(users_by_id, reviews_by_userid)
    write_stats(users_by_id, random_users, reviews_by_userid)
    review_train, review_test = split_reviews(list(random_users.keys()), reviews_by_userid)
    write_filtered(random_users, review_train, review_test, tips_by_userid, businesses)


