# Personalized multi facet trust
This repository contains code for testing the possibility of personalizing trust link predictions
in a multi facet framework.

## How to run an experiment

1. Set $MULTIFACET_ROOT to point to the project root directory (where this file is.)
2. Follow the directions in ./settings.properties to override local settings.
2. Download the Yelp data set and filter it's contents using src/python/filter_dataset. This
has the effect of filtering out sparse users and assigning each entity a unique, consecutive integer
ID. Rename the filtered files to overshadow the names of the original files and use the filtered 
versions going forward.
3. Use src/python/generate_single_vects.py to generate the set of single user feature vectors for
users in the data set.
4. Generate pairwise comparison vectors. This can be done by running the java project with the 
--genPairs flag and the path to the desired output file.
5. Generate clusters of users (if desired). This can be accomplished with the src/main/python/cluster.py
file and the pairwise vects generated in step 4.
6. Generate predictions for user trust links. This can be accomplished with the src/main/python/predict.py
file, and the pairwise and single vect files generated in steps 3 and 4.
7. A recommender system can be trained and evaluated by calling the main java project with
the path to an experiment description json file as input. See example_experiment.json for
inspiration.

## Experiment dir setup

The experiment directory (`settings.properties:experiment_dir`) will be used to store intermediate files and
output experiment results. `experiment_dir` should contain the predictions file you will use for your experiment 
(the one referenced in the `predictionFile` field of a json experiment description). 

Results are placed in `experiment_dir/{name}/results.txt`. If multiple threads are running predictions with the
same name, results are appended to this file in arbitrary order (but containing a full description of the
experiment they are associated with.) 


