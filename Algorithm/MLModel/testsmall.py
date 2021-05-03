import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from tensorflow.keras import layers
from tensorflow.keras.layers.experimental import preprocessing
from tensorflow import feature_column

from ConvertModel import *

import pathlib

csv_file = 'datasets/combined04data.csv' 
dataframe = pd.read_csv(csv_file)

dataframe.head()

class_names = ['awake', 'sleeping']

a = dataframe['real']
count1 = 0
count2 = 0
for i in a.values:
  if(i == 0):
    count1 = count1+1
  else:
    count2 = count2+1


print(count1)
print(count2)
