import numpy as np
import pandas as pd
import matplotlib as plt
import seaborn as sns

from sklearn import metrics
import io

import tensorflow as tf
from tensorflow.python.keras.callbacks import TensorBoard
import ImportCsv as csv
import datetime

from ConvertModel import *
from tensorflow import feature_column
from tensorflow.keras import layers
from sklearn.model_selection import train_test_split
import Metrics.ConfusionMatrix as cfm
import sklearn

import pathlib


csv_file = 'datasets/combined04data.csv' 
dataframe = pd.read_csv(csv_file)

dataframe.head()


# Drop un-used columns.
dataframe = dataframe.drop(columns=['time'])
dataframe = dataframe.drop(columns=['sleep1'])
dataframe = dataframe.drop(columns=['sleep2'])
dataframe = dataframe.drop(columns=['sleep3'])
dataframe = dataframe.drop(columns=['sleep4'])
dataframe = dataframe.drop(columns=['sleep5'])
dataframe = dataframe.drop(columns=['sleep6'])
dataframe = dataframe.drop(columns=['sleep7'])
dataframe = dataframe.drop(columns=['sleep8'])
dataframe = dataframe.drop(columns=['sleep9'])
dataframe = dataframe.drop(columns=['sleep10'])

dataframe = dataframe.drop(columns=['motion1'])
dataframe = dataframe.drop(columns=['motion2'])
dataframe = dataframe.drop(columns=['motion3'])
dataframe = dataframe.drop(columns=['motion4'])
dataframe = dataframe.drop(columns=['motion5'])
dataframe = dataframe.drop(columns=['motion6'])
dataframe = dataframe.drop(columns=['motion7'])
dataframe = dataframe.drop(columns=['motion8'])
dataframe = dataframe.drop(columns=['motion9'])
dataframe = dataframe.drop(columns=['motion10'])

dataframe = dataframe.drop(columns=['light1'])
dataframe = dataframe.drop(columns=['light2'])
dataframe = dataframe.drop(columns=['light3'])
dataframe = dataframe.drop(columns=['light4'])
dataframe = dataframe.drop(columns=['light5'])
dataframe = dataframe.drop(columns=['light6'])
dataframe = dataframe.drop(columns=['light7'])
dataframe = dataframe.drop(columns=['light8'])
dataframe = dataframe.drop(columns=['light9'])
dataframe = dataframe.drop(columns=['light10'])


trainTest, test = train_test_split(dataframe, test_size=0.2)
train, val = train_test_split(trainTest, test_size=0.2)
print(len(train), 'train examples')
print(len(val), 'validation examples')
print(len(test), 'test examples')

# A utility method to create a tf.data dataset from a Pandas Dataframe
def df_to_dataset(dataframe, shuffle=True, batch_size=32):
  dataframe = dataframe.copy()
  labels = dataframe.pop('real')
  ds = tf.data.Dataset.from_tensor_slices((dict(dataframe), labels))
  if shuffle:
    ds = ds.shuffle(buffer_size=len(dataframe))
  ds = ds.batch(batch_size)
  return ds

def sleepNom(col):
  std = 100
  return (col/std)

def moliNom(col):
  std = 6
  return (col/std)

feature_columns = []


# numeric cols
motion = feature_column.numeric_column('motion0')
feature_columns.append(motion)

sleep = feature_column.numeric_column('sleep0')
feature_columns.append(sleep)

light = feature_column.numeric_column('light0')
feature_columns.append(light)


feature_layer = tf.keras.layers.DenseFeatures(feature_columns)


batch_size = 32
train_ds = df_to_dataset(train, batch_size=batch_size)
val_ds = df_to_dataset(val, shuffle=False, batch_size=batch_size)
test_ds = df_to_dataset(test, shuffle=False, batch_size=batch_size)
test_val_ds = df_to_dataset(trainTest, shuffle=False, batch_size=batch_size)


model = tf.keras.Sequential([
  feature_layer,
  layers.Dense(128, activation='relu'),
  layers.Dense(128, activation='relu'),
  layers.Dropout(.1),
  layers.Dense(1)
])

#----

pathbefore = '.\\logs\\sleep04\\'
path = pathbefore + datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir=path, histogram_freq=1)

#--

model.compile(optimizer='adam',
              loss=tf.keras.losses.BinaryCrossentropy(from_logits=True),
              metrics=['accuracy'])

model.fit(train_ds,
          validation_data=val_ds,
          epochs = 3)


loss, accuracy = model.evaluate(test_ds)

model.save('sleep04_classifier')

convertSaveModel('sleep04_classifier', 'sleep04classifier.tflite', False)

print("Accuracy", accuracy)
