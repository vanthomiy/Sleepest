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

# In the original dataset "4" indicates the pet was not adopted.
dataframe['target'] = dataframe['real']

# Drop un-used columns.
#dataframe = dataframe.drop(columns=['time'])
dataframe = dataframe.drop(columns=['real'])

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
print('Every feature:', list(train_features.keys()))
print('A batch of ages:', train_features['sleep0'])
print('A batch of targets:', label_batch )


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
for header in headersSleep:
  numeric_col = tf.keras.Input(shape=(1,), name=header)
  normalization_layer = get_normalization_layer(header, train_ds)
  encoded_numeric_col = normalization_layer(numeric_col)
  all_inputs.append(numeric_col)
  encoded_features.append(encoded_numeric_col)
  featuresSleep.append(numeric_col)

for header in headersMotion:
  numeric_col = tf.keras.Input(shape=(1,), name=header)
  normalization_layer = get_normalization_layer(header, train_ds)
  encoded_numeric_col = normalization_layer(numeric_col)
  all_inputs.append(numeric_col)
  encoded_features.append(encoded_numeric_col)
  featuresMotion.append(numeric_col)

for header in headersLight:
  numeric_col = tf.keras.Input(shape=(1,), name=header)
  normalization_layer = get_normalization_layer(header, train_ds)
  encoded_numeric_col = normalization_layer(numeric_col)
  all_inputs.append(numeric_col)
  encoded_features.append(encoded_numeric_col)
  featuresLight.append(numeric_col)



'''
# Sleep Sleep cross buckets
for index in range(0, len(featuresSleep)-2):
  sleepSleep = feature_column.crossed_column([featuresSleep[index], featuresSleep[index+1]], hash_bucket_size=100)
  encoded_features.append(feature_column.indicator_column(sleepSleep))


# Motion Sleep Light cross buckets
for index in range(0, len(featuresSleep)-2):
  sleepSleep = feature_column.crossed_column([featuresSleep[index], featuresMotion[index+1], featuresLight[index+1]], hash_bucket_size=100)
  encoded_features.append(feature_column.indicator_column(sleepSleep))
'''


'''
# Categorical features encoded as integers.
age_col = tf.keras.Input(shape=(1,), name='Age', dtype='int64')
encoding_layer = get_category_encoding_layer('Age', train_ds, dtype='int64',
                                             max_tokens=5)
encoded_age_col = encoding_layer(age_col)
all_inputs.append(age_col)
encoded_features.append(encoded_age_col)
'''
num_classes = 2

all_features = tf.keras.layers.concatenate(encoded_features)
x = tf.keras.layers.Dense(32, activation="relu")(all_features)
#x = tf.keras.layers.Dropout(0.5)(x)
#output = tf.keras.layers.Dense(1)(x)
output = layers.Dense(num_classes)(x)
model = tf.keras.Model(all_inputs, output)
#model.add(layers.Dense(3, activation='softmax'))
#model.compile(optimizer='sgd', loss=tf.keras.losses.CategoricalCrossentropy())



'''
model.compile(optimizer='adam',
              loss=tf.keras.losses.categorical_crossentropy(),
              #loss=tf.keras.losses.BinaryCrossentropy(from_logits=True),
              metrics=["accuracy"])
'''
#model.compile(optimizer='sgd', loss='categorical_crossentropy', metrics=['acc']) 
model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
              metrics=['accuracy'])
model.summary()


# rankdir='LR' is used to make the graph horizontal.
tf.keras.utils.plot_model(model, show_shapes=True, rankdir="LR")

model.fit(train_ds, epochs=10, validation_data=val_ds)


loss, accuracy = model.evaluate(test_ds)
print("Accuracy", accuracy)


model.save('sleep_classifier_model')
reloaded_model = tf.keras.models.load_model('sleep_classifier_model')

sample = {}

for header in headers:
    sample[header] = 1


sample['sleep0'] = 94
sample['sleep1'] = 94
sample['sleep2'] = 92
sample['sleep3'] = 91
sample['sleep4'] = 90
sample['sleep5'] = 83
sample['sleep6'] = 77
sample['motion1'] = 2
sample['motion3'] = 2
sample['motion4'] = 3
sample['motion5'] = 4
sample['motion6'] = 3

input_dict = {name: tf.convert_to_tensor([value]) for name, value in sample.items()}
predictions = reloaded_model.predict(input_dict)
prob = tf.nn.sigmoid(predictions[0])

print(np.argmax(predictions[0]))
print(np.argmax(predictions[0], axis=-1))
score = tf.nn.softmax(predictions[0])
print(
    "This sleep most likely belongs to {} with a {:.2f} percent confidence."
    .format(class_names[np.argmax(score)], 100 * np.max(score))
)


convertSaveModelWithCustomOps('sleep_classifier_model', 'sleep_classifier_model.tflite', False)
