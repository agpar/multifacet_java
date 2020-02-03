import json
import matplotlib.pyplot as plt

with open('k_social_iter.json', 'r') as f:
    soc_res_iter = json.load(f)
    soc_res_iter.sort(key=lambda x: x[0])

with open('k_social_greedy.json', 'r') as f:
    soc_res_greedy = json.load(f)
    soc_res_greedy.sort(key=lambda x: x[0])

with open('k_pcc_iter.json', 'r') as f:
    pcc_res_iter = json.load(f)
    pcc_res_iter.sort(key=lambda x: x[0])

with open('k_pcc_greedy.json', 'r') as f:
    pcc_res_greedy = json.load(f)
    pcc_res_greedy.sort(key=lambda x: x[0])

pairs = [
    (soc_res_greedy, soc_res_iter, 'Social'),
    (pcc_res_greedy, pcc_res_iter, 'Pref')
]

for greedy, iters, title in pairs:
    fig, ax = plt.subplots()
    ax.plot([s[0] for s in greedy], [s[1][0] for s in greedy], label="greedy")
    ax.plot([s[0] for s in iters], [s[1][0] for s in iters], label='iterative')
    ax.set(xlabel='k', ylabel='silhouette', title=f'Yelp Cluster {title} Silhouette Scores')
    ax.legend()
    fig.savefig(f'yelp_{title}_silhouette.png', dpi=300)

    fig, ax = plt.subplots()
    ax.plot([s[0] for s in greedy], [s[1][1] for s in greedy], label="greedy")
    ax.plot([s[0] for s in iters], [s[1][1] for s in iters], label='iterative')
    ax.set(xlabel='k', ylabel='mean instra dist', title=f'Yelp Cluster {title} Mean Intra Cluster Dist')
    ax.legend()
    fig.savefig(f'yelp_{title}_intra.png', dpi=300)