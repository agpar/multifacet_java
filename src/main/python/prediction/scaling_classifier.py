from sklearn.preprocessing import StandardScaler

"""
Learns on normalized data, then can normalize the later stuff later.
"""


class ScalingClassifier:
    def __init__(self, training_fn):
        self.training_fn = training_fn
        self.clf = None
        self.scaler = None

    def fit(self, X, Y):
        self.scaler = StandardScaler().fit(X)
        X_scaled = self.scaler.transform(X)
        self.clf = self.training_fn(X_scaled, Y)
        return self

    def predict(self, X):
        X_scaled = self.scaler.transform(X)
        return self.clf.predict(X_scaled)

    def score(self, X, Y):
        X_scaled = self.scaler.transform(X)
        return self.clf.score(X_scaled, Y)