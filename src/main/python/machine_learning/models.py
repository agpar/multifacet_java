from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import confusion_matrix

import settings


class ChosenClassifier:
    def __init__(self):
        self.model_str = settings.ML_MODEL
        self.train_fn = self._parse_model()

    def _parse_model(self):
        if self.model_str == 'RandomForest':
            return learn_rf
        if self.model_str == 'LogisticRegression':
            return learn_logit

        print(f'Could not parse settings.ml_model: "{self.model_str}", defaulting to RandomForest')
        return learn_rf

    def fit(self, X, Y):
        return self.train_fn(X, Y)


def learn_logit(X, Y):
    clf = LogisticRegression(
        class_weight='balanced',
        penalty='l2',
        solver='saga',
        max_iter=1000).fit(X, Y)
    return clf


#def learn_boost(X, Y):
#    clf = xgb.XGBClassifier(random_state=1,learning_rate=0.01).fit(X, Y)
#    return clf

def learn_rf(X, Y):
    clf = RandomForestClassifier(
        bootstrap=True,
        criterion='gini',
        max_depth=4,
        max_features=0.5,
        max_leaf_nodes=None,
        min_impurity_decrease=0.0,
        min_samples_leaf=1,
        min_samples_split=2,
        min_weight_fraction_leaf=0,
        n_estimators=100)
    clf.fit(X, Y)
    return clf


def evaluate(clf, X, Y):
    predictions = clf.predict(X)
    confusion = confusion_matrix(Y, predictions)
    fp = confusion[0][1] / (confusion[0][1] + confusion[0][0])
    fn = confusion[1][0] / (confusion[1][0] + confusion[1][1])
    return confusion, fp, fn
