from prediction.classifier_trainer import ClassifierTrainer
from prediction_tools import combined_headers
from typing import List


class ClusterClassifier:
    SINGLE_PATH = ""
    PAIRWISE_PATH = ""
    CLUSTERED = True

    def __init__(self, cluster_labels: List[int]):
        self.cluster_labels = cluster_labels
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

    def train_classifiers(self, combiner, clf_trainer: ClassifierTrainer):
        header = combined_headers(self.SINGLE_PATH, self.PAIRWISE_PATH)
        self.classifiers = [None for i in range(self.NUM_CLUSTERS)]
        for i in range(self.NUM_CLUSTERS):
            if len(self.clusters[i]) > 100:
                training_set = combiner(self.SINGLE_PATH, self.PAIRWISE_PATH, userIds=self.clusters[i])
                clf, score = clf_trainer.learn_classifier(training_set, header, 1.0)
                print(score)
                print(clf.coef_)
                if score > 0.6:
                    self.classifiers[i] = clf
                else:
                    print("Score too low. Using generic classifier.")
        overall_set = combiner(self.SINGLE_PATH, self.PAIRWISE_PATH, numVects=500_000)
        self.overall_classifier, score = clf_trainer.learn_classifier(overall_set, header, 1.0)
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