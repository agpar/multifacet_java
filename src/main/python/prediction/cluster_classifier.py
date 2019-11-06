from multiprocessing import Pool, Queue

from prediction.classifier_trainer import ClassifierTrainer
from prediction_tools import combined_headers
from typing import List


def _train_classifier(i, single_path, pairwise_path, header, combiner, clf_trainer, userIds=None, numVects=None):
    training_set = combiner(single_path, pairwise_path, userIds=userIds, numVects=numVects)
    clf, score = clf_trainer.learn_classifier(training_set, header, 1.0)
    if score > 0.6:
        return clf, score, i
    else:
        return None, score, i


class ClusterClassifier:
    SINGLE_PATH = ""
    PAIRWISE_PATH = ""
    CLUSTERED = True

    def __init__(self, cluster_labels: List[int], combiner, trainer: ClassifierTrainer):
        self.cluster_labels = cluster_labels
        self.combiner = combiner
        self.trainer = trainer
        self.NUM_CLUSTERS = len(set(self.cluster_labels))
        self.clusters = []
        self.user_clusters = {}
        self.classifiers = []
        self.overall_classifier = None

    def init_clusters(self):
        self.clusters = [set() for x in range(self.NUM_CLUSTERS)]
        for i, clust in enumerate(self.cluster_labels):
            self.clusters[clust].add(i)
            self.user_clusters[i] = clust

    def train_classifiers(self):
        header = combined_headers(self.SINGLE_PATH, self.PAIRWISE_PATH)
        self.classifiers = [None for i in range(self.NUM_CLUSTERS)]

        # Start a process pool of workers
        pool = Pool(self.NUM_CLUSTERS + 1)
        results = []
        args_base = (self.SINGLE_PATH, self.PAIRWISE_PATH, header, self.combiner, self.trainer)
        if self.NUM_CLUSTERS > 1:
            for i in range(self.NUM_CLUSTERS):
                if len(self.clusters[i]) > 100:
                    args = (i, ) + args_base
                    kwds = {'userIds': self.clusters[i]}
                    results.append(pool.apply_async(_train_classifier, args=args, kwds=kwds))

        # Compute the "general" classifier at the same time
        args = (-1, ) + args_base
        kwds = {'numVects': 500_000}
        results.append(pool.apply_async(_train_classifier, args=args, kwds=kwds))
        pool.close()
        for res in results:
            clf, score, i = res.get()
            if i == -1:
                self.overall_classifier = clf
                print(f"Overall classifier score: {score}")
            else:
                self.classifiers[i] = clf
                print(f"Classifier {i} score: {score}")
        pool.join()

    def fit(self):
        self.init_clusters()
        self.train_classifiers()

    def predict(self, user_id, lines):
        clf_idx = self.user_clusters[user_id]
        clf = self.classifiers[clf_idx]
        if clf is None:
            clf = self.overall_classifier

        return clf.predict(lines)