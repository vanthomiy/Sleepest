import tensorflow as tf

# Convert the model

dir = 'E:\\Dokumente\\Programmieren\\Projekte\\Sleepest\\Algorithm\\MLModel\\models\\Google\\tf_saved_model-combined04_20210426111459-2021-04-26T12_32_21.913276Z'
converter = tf.lite.TFLiteConverter.from_saved_model(dir) # path to the SavedModel directory
tflite_model = converter.convert()

# Save the model.
with open('model.tflite', 'wb') as f:
  f.write(tflite_model)