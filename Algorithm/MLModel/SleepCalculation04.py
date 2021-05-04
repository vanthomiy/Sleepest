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


csv_file = 'datasets/sleepdata04v1.csv' 
dataframe = pd.read_csv(csv_file)

dataframe.head()


# Drop un-used columns.
#dataframe = dataframe.drop(columns=['time'])

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

'''
sleep_type = feature_column.categorical_column_with_vocabulary_list(
      'real', ['awake', 'sleeping'])
'''

length = 10
headers = []

for i in range(0,length):
    headers.append('sleep'+ str(i))
    headers.append('motion'+ str(i))
    headers.append('brigthness'+ str(i))



feature_columns = []

# numeric cols
for header in headers:
  feature_columns.append(feature_column.numeric_column(header))

sleepBuckets = []

# bucketized cols
for index in range(0,length):
  motion = feature_column.numeric_column('motion'+str(index))
  motion_buckets = feature_column.bucketized_column(motion, boundaries=[1, 2, 3, 4, 5, 6])
  feature_columns.append(motion_buckets)

  sleep = feature_column.numeric_column('sleep'+str(index))
  sleep_buckets = feature_column.bucketized_column(sleep, boundaries=[4,100])
  feature_columns.append(sleep_buckets)

  sleepBuckets.append(sleep_buckets)

  light = feature_column.numeric_column('brigthness'+str(index))
  light_buckets = feature_column.bucketized_column(light, boundaries=[1, 2, 3, 4, 5, 6])
  feature_columns.append(light_buckets)
  
  
  # crossed columns
  motionSleepFeature = feature_column.crossed_column([motion_buckets, sleep_buckets], hash_bucket_size=100)
  ligthSleepFeature = feature_column.crossed_column([light_buckets, sleep_buckets], hash_bucket_size=100)
  feature_columns.append(feature_column.indicator_column(motionSleepFeature))
  feature_columns.append(feature_column.indicator_column(ligthSleepFeature))


# Sleep Sleep cross buckets
for index in range(0, len(sleepBuckets)-2):
  sleepSleep = feature_column.crossed_column([sleepBuckets[index], sleepBuckets[index+1]], hash_bucket_size=100)
  feature_columns.append(feature_column.indicator_column(sleepSleep))




feature_layer = tf.keras.layers.DenseFeatures(feature_columns)



batch_size = 32
train_ds = df_to_dataset(train, batch_size=batch_size)
val_ds = df_to_dataset(val, shuffle=False, batch_size=batch_size)
test_ds = df_to_dataset(test, shuffle=False, batch_size=batch_size)
test_val_ds = df_to_dataset(trainTest, shuffle=False, batch_size=batch_size)


num_classes = 2
model = tf.keras.Sequential([
  feature_layer,
  layers.Dense(128, activation='relu'),
  layers.Dense(128, activation='relu'),
  layers.Dropout(.1),
  layers.Dense(num_classes)
])

#----
class_names=['sleep', 'awake']

def log_confusion_matrix(epoch, logs):
    
    # Use the model to predict the values from the test_images.
    test_pred_raw = model.predict(val_ds)
    
    test_pred = np.argmax(test_pred_raw, axis=1)
    
    # Calculate the confusion matrix using sklearn.metrics
    cm = sklearn.metrics.confusion_matrix([0,1], test_pred)
    
    figure = cfm.plot_confusion_matrix(cm, class_names=class_names)
    cm_image = cfm.plot_to_image(figure)
    
    # Log the confusion matrix as an image summary.
    with file_writer_cm.as_default():
        tf.summary.image("Confusion Matrix", cm_image, step=epoch)



pathbefore = '.\\logs\\sleep04\\'
path = pathbefore + datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir=path, histogram_freq=1)

file_writer_cm = tf.summary.create_file_writer(path + '/cm')

cm_callback = tf.keras.callbacks.LambdaCallback(on_epoch_end=log_confusion_matrix)

#--
'''
model.compile(optimizer='adam',
              loss=tf.keras.losses.BinaryCrossentropy(from_logits=True),
              metrics=['accuracy'])
'''
model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
              metrics=['sparse_categorical_accuracy'])

model.fit(train_ds,
          validation_data=val_ds,
          epochs=3,
          callbacks=[tensorboard_callback])


y_pred = model.predict(val_ds)
predicted_categories = tf.argmax(y_pred, axis=1)
true_categories = tf.concat([y for x, y in val_ds], axis=0)
actmatrix = metrics.confusion_matrix(predicted_categories, true_categories)

print(actmatrix)

loss, accuracy = model.evaluate(test_ds)

model.save('sleep04_features')

convertSaveModelWithCustomOps('sleep04_features', 'sleep04_features.tflite', False)

print("Accuracy", accuracy)

sample = {}

for header in headers:
    sample[header] = 1

print(len(headers))
sample['sleep0'] = 95
sample['sleep1'] = 95
sample['sleep2'] = 95
sample['sleep3'] = 95

input_dict = {name: tf.convert_to_tensor([value]) for name, value in sample.items()}
predictions = model.predict(input_dict)
print(predictions)