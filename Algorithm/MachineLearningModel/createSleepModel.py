import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from tensorflow._api.v2 import data
from tensorflow.keras import layers
from tensorflow.keras.layers.experimental import preprocessing
from tensorflow import feature_column
import datetime
import tensorboard
from tfliteconverter import *
import sklearn
import pathlib


def loadDataFrame(csv_file):

  dataframe = pd.read_csv(csv_file)

  # In the original dataset "4" indicates the pet was not adopted.
  dataframe['target'] = dataframe['real']

  # Drop un-used columns.
  #dataframe = dataframe.drop(columns=['time'])
  dataframe = dataframe.drop(columns=['real'])

  return dataframe

def createHeaders(length):
  headers = []

  for i in range(0,length):
    headers.append('sleep'+ str(i))
    headers.append('motion'+ str(i))
    headers.append('brigthness'+ str(i))

  return headers

# A utility method to create a tf.data dataset from a Pandas Dataframe
def df_to_dataset(dataframe, shuffle=True, batch_size=32):
  dataframe = dataframe.copy()
  labels = dataframe.pop('target')
  ds = tf.data.Dataset.from_tensor_slices((dict(dataframe), labels))
  if shuffle:
    ds = ds.shuffle(buffer_size=len(dataframe))
  ds = ds.batch(batch_size)
  ds = ds.prefetch(batch_size)
  return ds

def get_normalization_layer(name, dataset):
  # Create a Normalization layer for our feature.
  normalizer = preprocessing.Normalization()

  # Prepare a Dataset that only yields our feature.
  feature_ds = dataset.map(lambda x, y: x[name])

  # Learn the statistics of the data.
  normalizer.adapt(feature_ds)

  return normalizer

def createFeatures(dataframe, headers):

  train, test = train_test_split(dataframe, test_size=0.2)
  train, val = train_test_split(train, test_size=0.2)

  batch_size = 256
  train_ds = df_to_dataset(train, batch_size=batch_size)
  val_ds = df_to_dataset(val, shuffle=False, batch_size=batch_size)
  test_ds = df_to_dataset(test, shuffle=False, batch_size=batch_size)

  all_inputs = []
  encoded_features = []

  featuresSleep = []

  # Numeric features. 
  #sleep, motion, light ...!
  for header in headers:
    numeric_col = tf.keras.Input(shape=(1,), name=header, dtype = tf.dtypes.float32)
    normalization_layer = get_normalization_layer(header, train_ds)
    encoded_numeric_col = normalization_layer(numeric_col)
    all_inputs.append(numeric_col)
    encoded_features.append(encoded_numeric_col)
    featuresSleep.append(numeric_col)

  return all_inputs, encoded_features, val_ds,train_ds, test_ds

def createModel(num_classes, encoded_features, all_inputs):

  all_features = tf.keras.layers.concatenate(encoded_features)
  x = tf.keras.layers.Dense(32, activation="relu")(all_features)
  x = tf.keras.layers.Dropout(0.5)(x)#overfitting avoiding
  output = tf.keras.layers.Dense(num_classes)(x)
  model = tf.keras.Model(all_inputs, output)

  model.compile(optimizer='adam',
                loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
                metrics=['sparse_categorical_accuracy'])

  model.summary()

  return model

def trainAndSaveModel(model, val_ds, train_ds, test_ds, modelname, class_weights):
  
  pathbefore = '.\\logs\\' + modelname + '\\'
  path = pathbefore + datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
  tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir=path, histogram_freq=1)

  model.fit(train_ds, epochs=10, validation_data=val_ds, class_weight= class_weights, callbacks=[tensorboard_callback])

  loss, accuracy = model.evaluate(test_ds)

  model.save(modelname)

  dot_img_file = modelname + '/model.png'
  tf.keras.utils.plot_model(model, to_file=dot_img_file, show_shapes=True, rankdir="LR")

  convertSaveModelWithCustomOps(modelname, 'litemodels/'+ modelname + '.tflite', False)

  return loss, accuracy

def start04(time, length):

  csv_file = 'Datasets/sleep04'+ str(time) +'.csv' 

  # Weight the sleep 2 times more then the awake !
  class_weights = {
      0: 1,
      1: 2,
  }

  dataframe = loadDataFrame(csv_file)
  headers = createHeaders(length)
  all_inputs, encoded_features, val_ds,train_ds, test_ds = createFeatures(dataframe, headers)
  model = createModel(2,encoded_features, all_inputs)
  loss, accuracy = trainAndSaveModel(model, val_ds,train_ds, test_ds, 'sleep04'+ str(time), class_weights)
  return loss, accuracy

def start12(time, length):

  csv_file = 'Datasets/sleep12'+ str(time) +'.csv' 

  # Weight the sleep 2 times more then the awake !
  class_weights = {
      0: 1,
      1: 3,
  }

  dataframe = loadDataFrame(csv_file)
  headers = createHeaders(length)
  all_inputs, encoded_features, val_ds,train_ds, test_ds = createFeatures(dataframe, headers)
  model = createModel(2,encoded_features, all_inputs)
  loss, accuracy = trainAndSaveModel(model, val_ds,train_ds, test_ds, 'sleep12'+ str(time), class_weights)
  return loss, accuracy


 


