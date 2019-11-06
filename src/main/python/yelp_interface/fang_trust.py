from collections import defaultdict
from tools.review_similarity import review_pcc, review_cos, pcc, cos
from tools.user_reviews import avg_user_score, avg_item_score


AVG_REVIEW_SCORE = 3.7161

class FangTrust():
    """Trust indicators from Fang et al"""

    def __init__(self, reviews_by_item):
        self._reviews_by_item = reviews_by_item
        self._cache = defaultdict(dict)

    def _put_cache(self, user, indicator_title, indicator_value):
        self._cache[user['user_id']][indicator_title] = indicator_value

    def _get_cache(self, user, indicator_title):
        return self._cache[user['user_id']].get(indicator_title, None)

    def get_vector(self, truster, trustee):
        vect = [self.integrity_pcc(trustee), self.integrity_cos(trustee), self.integrity_pcc(truster),
                self.integrity_cos(truster), self.competence(trustee), self.competence(truster)]
        return vect

    def get_solo_vector(self, truster):
        vect = [self.integrity_pcc(truster), self.competence(truster)]
        return vect

    def vector_labels(self):
        return [
            'trustee_integrity_pcc',
            'trustee_integrity_cos',
            'truster_integrity_pcc',
            'truster_integrity_cos',
            'trustee_competence',
            'truster_competence',
        ]

    def solo_vecto_labels(self):
        return [
            'integrity',
            'competence',
        ]

    def benevolence_pcc(self, truster, trustee):
        reviews1 = truster['reviews']
        reviews2 = trustee['reviews']
        return review_pcc(reviews1, reviews2, avg_mode='OVERALL')

    def benevolence_cos(self, truster, trustee):
        reviews1 = truster['reviews']
        reviews2 = trustee['reviews']
        return review_cos(reviews1, reviews2)

    def integrity_pcc(self, trustee):
        cached_val = self._get_cache(trustee, 'integrity_pcc')
        if cached_val:
            return cached_val

        reviews = trustee['reviews']
        avg_reviews = []
        for r in reviews:
            revs = self._reviews_by_item[r['business_id']]
            avg = avg_item_score(r['business_id'], revs)
            avg_reviews.append(avg)

        trustee_scores = [r['stars'] for r in reviews]
        trustee_avg = avg_user_score(trustee['user_id'], reviews)
        trustee_avgs = [trustee_avg for i in range(len(reviews))]
        global_avgs = [AVG_REVIEW_SCORE for i in range(len(reviews))]
        val = pcc(trustee_scores, trustee_avgs, avg_reviews, global_avgs)
        self._put_cache(trustee, 'integrity_pcc', val)
        return val

    def integrity_cos(self, trustee):
        cached_val = self._get_cache(trustee, 'integrity_cos')
        if cached_val:
            return cached_val

        reviews = trustee['reviews']
        avg_reviews = []
        for r in reviews:
            revs = self._reviews_by_item[r['business_id']]
            avg = avg_item_score(r['business_id'], revs)
            avg_reviews.append(avg)

        trustee_scores = [r['stars'] for r in reviews]
        val = cos(trustee_scores, avg_reviews)
        self._put_cache(trustee, 'integrity_cos', val)
        return val

    def competence(self, trustee):
        cached_val = self._get_cache(trustee, 'competence')
        if cached_val:
            return cached_val

        e = 0.5
        numer = 0
        denom = 0
        for review in trustee['reviews']:
            other_reviews = self._reviews_by_item[review['business_id']]
            numer += len([r for r in other_reviews if abs(r['stars'] - review['stars']) < e])
            denom += len(other_reviews)
        val = numer / denom

        self._put_cache(trustee, 'competence', val)
        return val