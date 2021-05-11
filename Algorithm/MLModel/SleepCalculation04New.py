import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from tensorflow.keras import layers
from tensorflow.keras.layers.experimental import preprocessing
from tensorflow import feature_column

from ConvertModel import *

import pathlib

tf.keras.backend.floatx()


csv_file = 'datasets/combined04data.csv' 
dataframe = pd.read_csv(csv_file)

dataframe.head()

class_names = ['awake', 'sleeping']

# In the original dataset "4" indicates the pet was not adopted.
dataframe['target'] = dataframe['real']

# Drop un-used columns.
#dataframe = dataframe.drop(columns=['time'])
dataframe = dataframe.drop(columns=['real', 'time'])

'''
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
'''

headers = []

for i in range(0,10):
    headers.append('sleep'+ str(i))
    headers.append('motion'+ str(i))
    headers.append('light'+ str(i))

# Attention we need to reverse the headers... otherwise we will have to write them backwards
headers.reverse()
headersSleep = []

for i in range(0,10):
    headersSleep.append('sleep'+ str(i))

headersMotion = []

for i in range(0,10):
    headersMotion.append('motion'+ str(i))

headersLight = []

for i in range(0,10):
    headersLight.append('light'+ str(i))



train, test = train_test_split(dataframe, test_size=0.2)
train, val = train_test_split(train, test_size=0.2)
print(len(train), 'train examples')
print(len(val), 'validation examples')
print(len(test), 'test examples')


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


batch_size = 5
train_ds = df_to_dataset(train, batch_size=batch_size)

[(train_features, label_batch)] = train_ds.take(1)

def get_normalization_layer(name, dataset):
  # Create a Normalization layer for our feature.
  normalizer = preprocessing.Normalization()

  # Prepare a Dataset that only yields our feature.
  feature_ds = dataset.map(lambda x, y: x[name])

  # Learn the statistics of the data.
  normalizer.adapt(feature_ds)

  return normalizer



batch_size = 256
train_ds = df_to_dataset(train, batch_size=batch_size)
val_ds = df_to_dataset(val, shuffle=False, batch_size=batch_size)
test_ds = df_to_dataset(test, shuffle=False, batch_size=batch_size)

all_inputs = []
encoded_features = []

featuresSleep = []
featuresMotion = []
featuresLight = []

# Numeric features. 
#sleep, motion, light ...!
for header in headers:

    feature = tf.feature_column.numeric_column(
        header, shape=(1,), default_value=None, dtype=tf.dtypes.float32, normalizer_fn=None
    )

    encoded_features.append(feature)


num_classes = 2

feature_layer = tf.keras.layers.DenseFeatures(encoded_features)

model = tf.keras.Sequential([
  feature_layer,
  layers.Dense(128, activation='relu'),
  layers.Dense(128, activation='relu'),
  layers.Dropout(.1),
  layers.Dense(1)
])

model.input()

model.compile(optimizer='adam',
              loss= tf.keras.losses.BinaryCrossentropy(from_logits=True),
              metrics=['accuracy'])

model.fit(train_ds,
          validation_data=val_ds,
          epochs=10)

model.summary()

loss, accuracy = model.evaluate(test_ds)
print("Accuracy", accuracy)


model.save('sleep_classifier_features')
reloaded_model = tf.keras.models.load_model('sleep_classifier_features')

convertSaveModelWithCustomOps('sleep_classifier_features', 'sleep_classifier_features.tflite', False)

dot_img_file = 'sleep_classifier_features/classification04model.png'
# rankdir='LR' is used to make the graph horizontal.
tf.keras.utils.plot_model(model, to_file=dot_img_file, show_shapes=True, rankdir="LR")
