import numpy as np
import pandas as pd

import tensorflow as tf
import ImportCsv as csv

from ConvertModel import *
from tensorflow import feature_column
from tensorflow.keras import layers
from sklearn.model_selection import train_test_split

import pathlib

'''#dataset_url = 'http://storage.googleapis.com/download.tensorflow.org/data/petfinder-mini.zip'
csv_file = 'datasets/petfinder-mini/petfinder-mini.csv'
csv_file = csv.LoadCsv()

tf.keras.utils.get_file('petfinder_mini.zip', dataset_url,
                        extract=True, cache_dir='.')'''

csv_file = 'datasets/combined04data.csv' 
dataframe = pd.read_csv(csv_file)

dataframe.head()



# In the original dataset "4" indicates the pet was not adopted.
#dataframe['target'] = np.where(dataframe['AdoptionSpeed']==4, 0, 1)

#dataframe['target'] = 'real'
# Drop un-used columns.
dataframe = dataframe.drop(columns=['time'])

train, test = train_test_split(dataframe, test_size=0.2)
train, val = train_test_split(train, test_size=0.2)
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

'''
sleep_type = feature_column.categorical_column_with_vocabulary_list(
      'real', ['awake', 'sleeping'])
'''


feature_columns = []

# numeric cols
for header in ['motion0', 'sleep0', 'light0',
'motion1', 'sleep1', 'light1','motion2', 'sleep2', 'light2',
'motion3', 'sleep3', 'light3','motion4', 'sleep4', 'light4',
'motion5', 'sleep5', 'light5',
'motion6', 'sleep6', 'light6','motion7', 'sleep7', 'light7',
'motion8', 'sleep8', 'light8','motion9', 'sleep9', 'light9',
'motion10', 'sleep10', 'light10']:
  feature_columns.append(feature_column.numeric_column(header))

sleepBuckets = []

# bucketized cols
for index in range(0,10):
  motion = feature_column.numeric_column('motion'+str(index))
  motion_buckets = feature_column.bucketized_column(motion, boundaries=[1, 2, 3, 4, 5, 6])
  feature_columns.append(motion_buckets)

  sleep = feature_column.numeric_column('sleep'+str(index))
  sleep_buckets = feature_column.bucketized_column(sleep, boundaries=[4,100])
  feature_columns.append(sleep_buckets)

  sleepBuckets.append(sleep_buckets)

  light = feature_column.numeric_column('light'+str(index))
  light_buckets = feature_column.bucketized_column(light, boundaries=[1, 2, 3, 4, 5, 6])
  feature_columns.append(light_buckets)
  
  
  # crossed columns
  motionSleepFeature = feature_column.crossed_column([motion_buckets, sleep_buckets], hash_bucket_size=100)
  ligthSleepFeature = feature_column.crossed_column([light_buckets, sleep_buckets], hash_bucket_size=100)
  feature_columns.append(feature_column.indicator_column(motionSleepFeature))
  feature_columns.append(feature_column.indicator_column(ligthSleepFeature))


'''
# indicator_columns
indicator_column_names = ['real']


for col_name in indicator_column_names:
  categorical_column = feature_column.categorical_column_with_vocabulary_list(
      col_name, dataframe[col_name].unique())
  indicator_column = feature_column.indicator_column(categorical_column)
  feature_columns.append(indicator_column)
'''


# Sleep Sleep cross buckets
for index in range(0, len(sleepBuckets)-2):
  sleepSleep = feature_column.crossed_column([sleepBuckets[index], sleepBuckets[index+1]], hash_bucket_size=100)
  feature_columns.append(feature_column.indicator_column(sleepSleep))




feature_layer = tf.keras.layers.DenseFeatures(feature_columns)



batch_size = 32
train_ds = df_to_dataset(train, batch_size=batch_size)
val_ds = df_to_dataset(val, shuffle=False, batch_size=batch_size)
test_ds = df_to_dataset(test, shuffle=False, batch_size=batch_size)

model = tf.keras.Sequential([
  feature_layer,
  layers.Dense(128, activation='relu'),
  layers.Dense(128, activation='relu'),
  layers.Dropout(.1),
  layers.Dense(1)
])

model.compile(optimizer='adam',
              loss=tf.keras.losses.BinaryCrossentropy(from_logits=True),
              metrics=['accuracy'])

model.fit(train_ds,
          validation_data=val_ds,
          epochs=10)


loss, accuracy = model.evaluate(test_ds)

model.save('sleep04_classifier')

convertSaveModel('sleep04_classifier')

print("Accuracy", accuracy)
