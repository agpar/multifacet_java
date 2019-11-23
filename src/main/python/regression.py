from sklearn.linear_model import LogisticRegression
from sklearn.metrics import confusion_matrix
import xgboost as xgb
import numpy as np


def learn_logit(X, Y):
    clf = LogisticRegression(class_weight='balanced',
            penalty='l2', solver='saga').fit(X, Y)
    return clf


def learn_boost(X, Y):
    clf = xgb.XGBClassifier(random_state=1,learning_rate=0.01).fit(X, Y)
    return clf

def evaluate(clf, X, Y):
    predictions = clf.predict(X)
    confusion = confusion_matrix(Y, predictions)
    fp = confusion[0][1] / (confusion[0][1] + confusion[0][0])
    fn = confusion[1][0] / (confusion[1][0] + confusion[1][1])
    return confusion, fp, fn
