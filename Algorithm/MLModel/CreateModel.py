import tensorflow as tf

# Convert the model
'''dir = 'E:\\Dokumente\\Programmieren\\Projekte\\Sleepest\\Algorithm\\MLModel\\my_pet_classifier'
converter = tf.lite.TFLiteConverter.from_saved_model(dir) # path to the SavedModel directory
tflite_model = converter.convert()'''

model = 'E:\\Dokumente\\Programmieren\\Projekte\\Sleepest\\Algorithm\\MLModel\\my_pet_classifier\\saved_model.pb'


# Converting a tf.Keras model to a TensorFlow Lite model.
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save the model.
with open('model.tflite', 'wb') as f:
  f.write(tflite_model)


def tfLiteModel(newmodel):
  # Converting a tf.Keras model to a TensorFlow Lite model.
  converter = tf.lite.TFLiteConverter.from_keras_model(newmodel)
  tflite_model = converter.convert()