import tensorflow as tf
import numpy as np

tflite_file = 'sleep_classifier_model.tflite' 

sample = {}

headers = []

for i in range(0,10):
    headers.append('sleep'+ str(i))
    headers.append('motion'+ str(i))
    headers.append('light'+ str(i))

for header in headers:
    sample[header] = 1


sample['sleep0'] = 95
sample['sleep1'] = 95
sample['sleep2'] = 95
sample['sleep3'] = 95


input_dict = {name: tf.convert_to_tensor([value]) for name, value in sample.items()}


interpreter = tf.lite.Interpreter(model_path=tflite_file)
interpreter.allocate_tensors()


# Get input and output tensors.
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# Test the model on random input data.
input_shape = input_details[0]['shape']
input_data = np.array(np.random.random_sample(input_shape), dtype=np.float32)



#input_data1 = np.array([90.,91.,91.,91.,93.,91.,87.,95.,94.,91.,1.,1.,1.,4.,1.,1.,3.,1.,1.,1.,1.,1.,3.,1.,1.,3.,1.,1.,3.,1.], dtype=np.float32)
input_data1 = np.array([95.,1.,1.,95.,1.,1.,95.,1.,1.,95.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.], dtype=np.float32)
input_data2 = np.array([91.,1.,1.,60.,1.,1.,70.,1.,1.,80.,1.,1.,20.,1.,1.,30.,1.,1.,20.,1.,1.,30.,1.,1.,20.,1.,1.,60.,1.,1.,1.,1.,1.], dtype=np.float32)
input_data3 = np.array([90.,1.,1.,90.,1.,1.,90.,1.,1.,90.,1.,1.,20.,1.,1.,30.,1.,1.,14.,1.,1.,80.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.], dtype=np.float32)

input_data11 = np.array([1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,3.,1.,1.,3.,1.,1.,3.,1.,95.,1.,1.,95.,1.,1.,95.,1.,1.,95.,1.,1.,95.,1.,1.], dtype=np.float32)
input_data21 = np.array([1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,3.,1.,1.,3.,1.,1.,3.,1.,1.,95.,1.,1.,95.,1.,1.,95.,1.,1.,95.,1.,1.,95.,1.], dtype=np.float32)
input_data31 = np.array([1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,3.,1.,1.,3.,1.,1.,3.,1.,1.,1.,1.,1.,1.,95.,1.,1.,95.,1.,1.,95.,1.,1.,95.], dtype=np.float32)


for i in range(0,len(input_details)):
    input_i = np.array([[input_data1[i]]], dtype=np.float32)
    interpreter.set_tensor(input_details[i]['index'], input_i)

interpreter.invoke()

# The function `get_tensor()` returns a copy of the tensor data.
# Use `tensor()` in order to get a pointer to the tensor.
output_data = interpreter.get_tensor(output_details[0]['index'])
prob = tf.nn.sigmoid(output_data)
print(output_data)
print(prob)

class_names = ["awake", "sleeping"]

prediction_class = np.argmax(output_data)
prediction_class_name = class_names[prediction_class]
print(prediction_class)
print(prediction_class_name)


for i in range(0,len(input_details)):
    input_i = np.array([[input_data2[i]]], dtype=np.float32)
    interpreter.set_tensor(input_details[i]['index'], input_i)

interpreter.invoke()

# The function `get_tensor()` returns a copy of the tensor data.
# Use `tensor()` in order to get a pointer to the tensor.
output_data = interpreter.get_tensor(output_details[0]['index'])
prob = tf.nn.sigmoid(output_data)
print(output_data)
print(prob)


prediction_class = np.argmax(output_data)
prediction_class_name = class_names[prediction_class]
print(prediction_class)
print(prediction_class_name)

for i in range(0,len(input_details)):
    input_i = np.array([[input_data3[i]]], dtype=np.float32)
    interpreter.set_tensor(input_details[i]['index'], input_i)

interpreter.invoke()

# The function `get_tensor()` returns a copy of the tensor data.
# Use `tensor()` in order to get a pointer to the tensor.
output_data = interpreter.get_tensor(output_details[0]['index'])
prob = tf.nn.sigmoid(output_data)
print(output_data)
print(prob)


prediction_class = np.argmax(output_data)
prediction_class_name = class_names[prediction_class]
print(prediction_class)
print(prediction_class_name)

for i in range(0,len(input_details)):
    input_i = np.array([[input_data11[i]]], dtype=np.float32)
    interpreter.set_tensor(input_details[i]['index'], input_i)

interpreter.invoke()

# The function `get_tensor()` returns a copy of the tensor data.
# Use `tensor()` in order to get a pointer to the tensor.
output_data = interpreter.get_tensor(output_details[0]['index'])
prob = tf.nn.sigmoid(output_data)
print(output_data)
print(prob)


prediction_class = np.argmax(output_data)
prediction_class_name = class_names[prediction_class]
print(prediction_class)
print(prediction_class_name)

for i in range(0,len(input_details)):
    input_i = np.array([[input_data21[i]]], dtype=np.float32)
    interpreter.set_tensor(input_details[i]['index'], input_i)

interpreter.invoke()

# The function `get_tensor()` returns a copy of the tensor data.
# Use `tensor()` in order to get a pointer to the tensor.
output_data = interpreter.get_tensor(output_details[0]['index'])
prob = tf.nn.sigmoid(output_data)
print(output_data)
print(prob)


prediction_class = np.argmax(output_data)
prediction_class_name = class_names[prediction_class]
print(prediction_class)
print(prediction_class_name)

for i in range(0,len(input_details)):
    input_i = np.array([[input_data31[i]]], dtype=np.float32)
    interpreter.set_tensor(input_details[i]['index'], input_i)

interpreter.invoke()

# The function `get_tensor()` returns a copy of the tensor data.
# Use `tensor()` in order to get a pointer to the tensor.
output_data = interpreter.get_tensor(output_details[0]['index'])
prob = tf.nn.sigmoid(output_data)
print(output_data)
print(prob)


prediction_class = np.argmax(output_data)
prediction_class_name = class_names[prediction_class]
print(prediction_class)
print(prediction_class_name)
