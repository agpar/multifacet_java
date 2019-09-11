import numpy as np

from clustering.iter_cluster import iter_cluster_avg
from clustering.id_index_map import IDIndexMap
from prediction_tools import combined_headers
from settings import NUM_CLUSTERS


class ClusterClassifier:
    SINGLE_PATH = ""
    PAIRWISE_PATH = ""
    CLUSTERED = True

    def __init__(self, dist_array: np.array, index_map: IDIndexMap):
        self.dist_array = dist_array
        self.index_map = index_map
        self.user_clusters = {}
        self.clusters = []
        self.classifiers = []
        self.overall_classifier = None

    def init_clusters(self):
        labels = iter_cluster_avg(self.dist_array, int(len(self.dist_array)/ NUM_CLUSTERS))
        self.clusters = [set() for x in range(NUM_CLUSTERS)]
        for i in range(len(labels)):
            cluster_idx = labels[i]
            self.clusters[cluster_idx].add(self.index_map.get_str(i))
            self.user_clusters[self.index_map.get_str(i)] = cluster_idx
        cluster_lens = [len(c) for c in self.clusters]
        print(f"Cluster lengths: {cluster_lens}")

    def train_classifiers(self, combiner, clf_trainer):
        header = combined_headers(self.SINGLE_PATH, self.PAIRWISE_PATH)
        self.classifiers = [None for x in range(NUM_CLUSTERS)]
        for i in range(NUM_CLUSTERS):
            if len(self.clusters[i]) > 100:
                training_set = combiner(self.SINGLE_PATH, self.PAIRWISE_PATH, userIds=self.clusters[i])
                clf, score = clf_trainer(training_set, header, 1.0)
                print(score)
                print(clf.coef_)
                if score > 0.6:
                    self.classifiers[i] = clf
                else:
                    print("Score to low. Using generic classifier.")
        overall_set = combiner(self.SINGLE_PATH, self.PAIRWISE_PATH, numVects=300_000)
        self.overall_classifier, score = clf_trainer(overall_set, header, 1.0)
        print("Generic classifier.")
        print(score)
        print(self.overall_classifier.coef_)

    def fit(self, combiner, clf_trainer):
        self.init_clusters()
        self.train_classifiers(combiner, clf_trainer)

    def predict(self, user_id, lines):
        clf_idx = self.user_clusters[user_id]
        clf = self.classifiers[clf_idx]
        if clf is None:
            clf = self.overall_classifier

        return clf.predict(lines)