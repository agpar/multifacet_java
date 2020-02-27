"""Simply reads the settings.json file at root and makes its contents available."""
import math
import os

multifacet_root = os.environ.get('MULTIFACET_ROOT')
if not multifacet_root:
    raise EnvironmentError("$MULTIFACET_ROOT is not set!")

default_settings_path = os.path.join(multifacet_root, "settings.properties")
local_settings_path = os.path.join(multifacet_root, "settings_local.properties")
if os.path.exists(local_settings_path):
    settings_path = local_settings_path
else:
    settings_path = default_settings_path

settings = {}
with open(settings_path) as f:
    for line in f:
        line = line.strip()
        if not line or (line and line[0] == '#'):
            continue
        else:
            key, val = line.split('=')
            settings[key] = val

YELP_DATA_DIR = settings['yelp_data_dir']
EPINIONS_DATA_DIR = settings['epinions_data_dir']
ML_MODEL = settings['ml_model']

DATA_NUM_USERS = math.inf  # legacy, used to control the max number of users read from data files.
DATA_READ_SAMPLE = False  # legacy, used when checking to see if a sample of users should be taken
if settings['multiprocess_predictions'].lower() == 'true':
    MULTIPROCESS_PREDICTIONS = True
else:
    MULTIPROCESS_PREDICTIONS = False
