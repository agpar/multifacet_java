from multiprocessing import Pool, Queue

from prediction.classifier_trainer import ClassifierTrainer
from prediction_tools import combined_headers
from typing import List
import settings


def _train_classifier(i, single_path, pairwise_path, header, combiner, clf_trainer, userIds=None, numVects=None):
    training_set = combiner(single_path, pairwise_path, userIds=userIds, numVects=numVects)
    X, Y = clf_trainer.to_dataset(training_set, header)
    if len(X) < 1000:
        return None, f"Not learned, only len(X) examples", i
    clf, score = clf_trainer.learn_classifier(X, Y, header, 1.0)
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
        args, kwargs = [], []
        args_base = (self.SINGLE_PATH, self.PAIRWISE_PATH, header, self.combiner, self.trainer)
        if self.NUM_CLUSTERS > 1:
            for i in range(self.NUM_CLUSTERS):
                if len(self.clusters[i]) > 100:
                    args.append((i, ) + args_base)
                    kwargs.append({'userIds': self.clusters[i]})

        # Compute the "general" classifier at the same time
        args.append((-1, ) + args_base)
        kwargs.append({'numVects': 1_000_000})
        if settings.MULTIPROCESS_PREDICTIONS:
            self._train_with_pool(args, kwargs)
        else:
            self._train_consecutively(args, kwargs)

    def _train_with_pool(self, arg_list, kwarg_list):
        results = []
        pool = Pool(self.NUM_CLUSTERS + 1)

        for args, kwds in zip(arg_list, kwarg_list):
            results.append(pool.apply_async(_train_classifier, args=args, kwds=kwds))

        pool.close()
        for res in results:
            clf, score, i = res.get()
            self._apply_result(clf, score, i)
        pool.join()

    def _train_consecutively(self, arg_list, kwarg_list):
        for args, kwds in zip(arg_list, kwarg_list):
            clf, score, i = _train_classifier(*args, **kwds)
            self._apply_result(clf, score, i)

    def _apply_result(self, clf, score, i):
        if i == -1:
            self.overall_classifier = clf
            print(f"Overall classifier score: {score}")
        else:
            self.classifiers[i] = clf
            print(f"Classifier {i} score: {score}")

    def fit(self):
        self.init_clusters()
        self.train_classifiers()

    def predict(self, user_id, lines):
        clf_idx = self.user_clusters[user_id]
        clf = self.classifiers[clf_idx]
        if clf is None:
            clf = self.overall_classifier

        return clf.predict(lines)

    def get_clf(self, i):
        if self.classifiers[i] is not None:
            return self.classifiers[i]
        else:
            return self.overall_classifier

    def get_cluster(self, i):
        return self.clusters[i]
