import json
from collections import defaultdict
import matplotlib.pyplot as plt
from itertools import cycle

"""Tools for parsing the json files generated as results."""

def load(path):
    with open(path, 'r') as f:
        return [json.loads(line) for line in f]

def group_results(results):
    grouped = defaultdict(list)
    for result in results:
        if result['name'] == 'Cluster':
            key = result['predictionFile'].replace(".txt", "")
        else:
            key = result['name']
        grouped[key].append(result)
    return grouped

def avg_groups(grouped):
    averaged = []
    for key, val in grouped.items():
        avg_mae = sum([v['MAE'] for v in val])/len(val)
        avg_mse = sum([v['MSE'] for v in val])/len(val)
        averaged.append({
                'MSE': avg_mse,
                'MAE': avg_mae,
                'NAME': key
                })
    return averaged

def min_groups(grouped, key):
    mins = {}
    for k, vals in grouped.items():
        for v in vals:
            if k not in mins or mins[k] > v[key]:
                mins[k] = v[key]
    return list(mins.items())

def avg_at_reg(vals, key, reg):
    s = 0
    l = 0
    for val in vals:
        if val['socialReg'] == reg:
            s += val[key]
            l += 1
    return s / l

def all_regs(vals):
    regs = set()
    for val in vals:
        regs.add(val['socialReg'])
    return regs


def filter_tests(results, tests):
    filtered = []
    for r in results:
        if r['name'] == "Cluster":
            key = r['predictionFile']
        else:
            key = r['name']
        if key in tests:
            filtered.append(r)
    return filtered

def combine_regs(grouped, key):
    data_points = {}
    for group_key, group_vals in grouped.items():
        regs = all_regs(group_vals)
        data = []
        for reg in regs:
            data.append((reg, avg_at_reg(group_vals, key, reg)))
        data_points[group_key] = data
    return data_points

def min_combined(combined):
    mins = {}
    for key, vals in combined.items():
        mins[key] = min([v[1] for v in vals])
    return mins

def all_tests(results):
    tests = set()
    for r in results:
        if r['name'] == "Cluster":
            key = r['predictionFile']
        else:
            key = r['name']
        tests.add(key)
    return tests


def plot_mae(grouped):
    graph(grouped, "MAE")
    plt.xlabel("Social Regulation")
    plt.ylabel("MAE")
    plt.title("Mean Absolute Error")


def plot_mse(grouped):
    graph(grouped, "MSE")
    plt.xlabel("Social Regulation")
    plt.ylabel("MSE")
    plt.title("Mean Squared Error")


def graph(grouped, key):
    fig = plt.figure()
    data_points = combine_regs(grouped, key)
    lines = ["-","--",":","-."]
    linecycler = cycle(lines)

    for key, values in data_points.items():
        values.sort(key=lambda x: x[0])
        regs = [v[0] for v in values]
        vals = [v[1] for v in values]
        plt.plot(regs, vals, next(linecycler), label=key)

    plt.legend()
