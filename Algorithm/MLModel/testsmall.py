import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from tensorflow.keras import layers
from tensorflow.keras.layers.experimental import preprocessing
from tensorflow import feature_column

from ConvertModel import *

import pathlib

reloaded_model = tf.keras.models.load_model('sleep_classifier_model')

headers = []

for i in range(0,10):
    headers.append('sleep'+ str(i))
    headers.append('motion'+ str(i))
    headers.append('brigthness'+ str(i))

sample = {}

for header in headers:
    sample[header] = 1


sample['sleep0'] = 95
sample['sleep1'] = 95
sample['sleep2'] = 95
sample['sleep3'] = 95

input_dict = {name: tf.convert_to_tensor([value]) for name, value in sample.items()}
predictions = reloaded_model.predict(input_dict)
prob = tf.nn.sigmoid(predictions[0])

print(
    "This particular sleep has a %.1f percent probability of sleep "
    % (100 * prob)
)