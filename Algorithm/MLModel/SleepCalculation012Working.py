import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from tensorflow.keras import layers
from tensorflow.keras.layers.experimental import preprocessing
from tensorflow import feature_column
import datetime
import tensorboard
from ConvertModel import *
import Metrics.ConfusionMatrix as cfm
import sklearn
import pathlib

csv_file = 'datasets/sleepdata04v1.csv' 
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

length = 10
headers = []

for i in range(0,length):
    headers.append('sleep'+ str(i))
    headers.append('motion'+ str(i))
    headers.append('brigthness'+ str(i))

# Attention we need to reverse the headers... otherwise we will have to write them backwards
headers.reverse()
headersSleep = []

for i in range(0,length):
    headersSleep.append('sleep'+ str(i))

headersMotion = []

for i in range(0,length):
    headersMotion.append('motion'+ str(i))

headersLight = []

for i in range(0,length):
    headersLight.append('brigthness'+ str(i))



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
  numeric_col = tf.keras.Input(shape=(1,), name=header)
  normalization_layer = get_normalization_layer(header, train_ds)
  encoded_numeric_col = normalization_layer(numeric_col)
  all_inputs.append(numeric_col)
  encoded_features.append(encoded_numeric_col)
  featuresSleep.append(numeric_col)

'''
for header in headersSleep.reverse():
  numeric_col = tf.keras.Input(shape=(1,), name=header)
  normalization_layer = get_normalization_layer(header, train_ds)
  encoded_numeric_col = normalization_layer(numeric_col)
  all_inputs.append(numeric_col)
  encoded_features.append(encoded_numeric_col)
  featuresSleep.append(numeric_col)

for header in headersMotion.reverse():
  numeric_col = tf.keras.Input(shape=(1,), name=header)
  normalization_layer = get_normalization_layer(header, train_ds)
  encoded_numeric_col = normalization_layer(numeric_col)
  all_inputs.append(numeric_col)
  encoded_features.append(encoded_numeric_col)
  featuresMotion.append(numeric_col)

for header in headersLight.reverse():
  numeric_col = tf.keras.Input(shape=(1,), name=header)
  normalization_layer = get_normalization_layer(header, train_ds)
  encoded_numeric_col = normalization_layer(numeric_col)
  all_inputs.append(numeric_col)
  encoded_features.append(encoded_numeric_col)
  featuresLight.append(numeric_col)
'''


num_classes = 2

all_features = tf.keras.layers.concatenate(encoded_features)
x = tf.keras.layers.Dense(32, activation="relu")(all_features)
x = tf.keras.layers.Dropout(0.5)(x)#overfitting avoiding
output = tf.keras.layers.Dense(num_classes)(x)
#output = layers.Dense(num_classes, activation=tf.nn.softmax)(x)
model = tf.keras.Model(all_inputs, output)

#model.add(layers.Dense(3, activation='softmax'))
#model.compile(optimizer='sgd', loss=tf.keras.losses.CategoricalCrossentropy())



'''
model.compile(optimizer='adam',
              #loss=tf.keras.losses.categorical_crossentropy(),
              loss=tf.keras.losses.BinaryCrossentropy(from_logits=True),
              metrics=["accuracy"])
'''

#model.compile(optimizer='sgd', loss='categorical_crossentropy', metrics=['acc']) 

model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
              metrics=['sparse_categorical_accuracy'])

'''
model.compile(optimizer=tf.keras.optimizers.Adam(),
              loss=tf.keras.losses.BinaryCrossentropy(from_logits=True), # default from_logits=False
              metrics=[tf.keras.metrics.BinaryAccuracy()])
'''
model.summary()

def log_confusion_matrix(epoch, logs):
    
    # Use the model to predict the values from the test_images.
    test_pred_raw = model.predict(val_ds)
    
    test_pred = np.argmax(test_pred_raw, axis=1)
    
    # Calculate the confusion matrix using sklearn.metrics
    cm = sklearn.metrics.confusion_matrix([0,1], test_pred)
    
    figure = cfm.plot_confusion_matrix(cm, class_names=['awake', 'sleeping'])
    cm_image = cfm.plot_to_image(figure)
    
    # Log the confusion matrix as an image summary.
    with file_writer_cm.as_default():
        tf.summary.image("Confusion Matrix", cm_image, step=epoch)



pathbefore = '.\\logs\\sleep04pp\\'
path = pathbefore + datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir=path, histogram_freq=1)

file_writer_cm = tf.summary.create_file_writer(path + '/cm')

cm_callback = tf.keras.callbacks.LambdaCallback(on_epoch_end=log_confusion_matrix)

# Weight the sleep 2 times more then the awake !
class_weights = {
    0: 1,
    1: 2,
}

model.fit(train_ds, epochs=10, validation_data=val_ds, class_weight= class_weights, callbacks=[tensorboard_callback])


loss, accuracy = model.evaluate(test_ds)
print("Accuracy", accuracy)

model.save('sleep_classifier_model')
reloaded_model = tf.keras.models.load_model('sleep_classifier_model')

convertSaveModelWithCustomOps('sleep_classifier_model', 'sleep_classifier_model.tflite', False)

dot_img_file = 'sleep_classifier_model/classification04model.png'
# rankdir='LR' is used to make the graph horizontal.
tf.keras.utils.plot_model(model, to_file=dot_img_file, show_shapes=True, rankdir="LR")


sample = {}

for header in headers:
    sample[header] = 1

print(len(headers))
sample['sleep0'] = 95
sample['sleep1'] = 95
sample['sleep2'] = 95
sample['sleep3'] = 95

input_dict = {name: tf.convert_to_tensor([value]) for name, value in sample.items()}
predictions = reloaded_model.predict(input_dict)
print(predictions)