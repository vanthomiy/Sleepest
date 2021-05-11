import tensorflow as tf
import numpy as np


def outputdataclassifiSmall(predictions1):
    class_names = ['awake', 'sleep']
    prediction_class1 = np.argmax(predictions1)
    prediction_class_name1 = class_names[prediction_class1]
    return prediction_class_name1


def outputdataclassifi(index, predictions1, predictions2):
    print('Prediction ' + str(index) +':')
    class_names = ['awake', 'sleep']
    prediction_class1 = np.argmax(predictions1)
    prediction_class_name1 = class_names[prediction_class1]
    prediction_class2 = np.argmax(predictions2)
    prediction_class_name2 = class_names[prediction_class2]

    print('Pre: ' + str(prediction_class_name1) + "/ Fea: " + str(prediction_class_name2))



def loadData():

    lines = []

    lines.append('1,1,2,94,1,3,94,1,5,94,3,5,94,1,5,94,1,6,94,1,6,94,1,6,94,2,5,94,2,6,94')
    lines.append('1,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1')
    lines.append('1,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28')
    lines.append('1,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4')
    lines.append('1,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11')
    lines.append('1,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11')
    lines.append('1,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35')
    lines.append('0,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4,2,6,8,2,5,35')
    lines.append('0,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4,2,6,8')
    lines.append('0,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4')
    lines.append('0,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1')
    lines.append('0,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4')
    lines.append('0,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1')
    lines.append('1,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1')
    lines.append('1,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28')
    lines.append('1,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4')

    data = []

    for line in lines:
        linedata = line.split(',')

        input = np.array(
            [
             float(linedata[3]),
             float(linedata[2]),
             float(linedata[1]), 
             float(linedata[6]), 
             float(linedata[5]), 
             float(linedata[4]), 
             float(linedata[9]), 
             float(linedata[8]), 
             float(linedata[7]), 
             float(linedata[12]), 
             float(linedata[11]), 
             float(linedata[10]), 
             float(linedata[15]), 
             float(linedata[14]), 
             float(linedata[13]), 
             float(linedata[18]), 
             float(linedata[17]), 
             float(linedata[16]), 
             float(linedata[21]), 
             float(linedata[20]), 
             float(linedata[19]), 
             float(linedata[24]), 
             float(linedata[23]), 
             float(linedata[22]), 
             float(linedata[27]), 
             float(linedata[26]), 
             float(linedata[25]), 
             float(linedata[30]), 
             float(linedata[29]), 
             float(linedata[28]), 
            ], dtype=np.float32)

        

        data.append(input)

    return data

def loadDataReversed():

    lines = []

    lines.append('1,1,2,94,1,3,94,1,5,94,3,5,94,1,5,94,1,6,94,1,6,94,1,6,94,2,5,94,2,6,94')
    lines.append('1,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1')
    lines.append('1,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28')
    lines.append('1,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4')
    lines.append('1,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11')
    lines.append('1,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11')
    lines.append('1,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35')
    lines.append('0,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4,2,6,8,2,5,35')
    lines.append('0,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4,2,6,8')
    lines.append('0,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4')
    lines.append('0,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1')
    lines.append('0,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4')
    lines.append('0,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1')
    lines.append('1,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1')
    lines.append('1,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28')
    lines.append('1,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4')

    data = []

    for line in lines:
        linedata = line.split(',')
        linedata.reverse()
        input = np.array(
            [
             float(linedata[0]),
             float(linedata[1]),
             float(linedata[2]), 
             float(linedata[3]), 
             float(linedata[4]), 
             float(linedata[5]), 
             float(linedata[6]), 
             float(linedata[7]), 
             float(linedata[8]), 
             float(linedata[9]), 
             float(linedata[10]), 
             float(linedata[11]), 
             float(linedata[12]), 
             float(linedata[13]), 
             float(linedata[14]), 
             float(linedata[15]), 
             float(linedata[16]), 
             float(linedata[17]), 
             float(linedata[18]), 
             float(linedata[19]), 
             float(linedata[20]), 
             float(linedata[21]), 
             float(linedata[22]), 
             float(linedata[23]), 
             float(linedata[24]), 
             float(linedata[25]), 
             float(linedata[26]), 
             float(linedata[27]), 
             float(linedata[28]), 
             float(linedata[29]), 
            ], dtype=np.float32)

        

        data.append(input)

    return data

def loadDataLikeSummary():

    lines = []

    lines.append('1,1,2,94,1,3,94,1,5,94,3,5,94,1,5,94,1,6,94,1,6,94,1,6,94,2,5,94,2,6,94')
    lines.append('1,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1')
    lines.append('1,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28')
    lines.append('1,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4')
    lines.append('1,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11')
    lines.append('1,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11')
    lines.append('1,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35')
    lines.append('0,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4,2,6,8,2,5,35')
    lines.append('0,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4,2,6,8')
    lines.append('0,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4')
    lines.append('0,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1')
    lines.append('0,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4')
    lines.append('0,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1')
    lines.append('1,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1')
    lines.append('1,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28')
    lines.append('1,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4')

    data = []

    for line in lines:
        linedata = line.split(',')

        input = np.array(
            [
             float(linedata[28]),
             float(linedata[29]),
             float(linedata[30]), 
             float(linedata[25]), 
             float(linedata[26]), 
             float(linedata[27]), 
             float(linedata[22]), 
             float(linedata[23]), 
             float(linedata[24]), 
             float(linedata[19]), 
             float(linedata[20]), 
             float(linedata[21]), 
             float(linedata[16]), 
             float(linedata[17]), 
             float(linedata[18]), 
             float(linedata[13]), 
             float(linedata[14]), 
             float(linedata[15]), 
             float(linedata[10]), 
             float(linedata[11]), 
             float(linedata[12]), 
             float(linedata[7]), 
             float(linedata[8]), 
             float(linedata[9]), 
             float(linedata[4]), 
             float(linedata[5]), 
             float(linedata[6]), 
             float(linedata[1]), 
             float(linedata[2]), 
             float(linedata[3]), 
            ], dtype=np.float32)

        

        data.append(input)

    return data

def loadDataLikeExpected():

    lines = []

    lines.append('1,1,2,94,1,3,94,1,5,94,3,5,94,1,5,94,1,6,94,1,6,94,1,6,94,2,5,94,2,6,94')
    lines.append('1,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1')
    lines.append('1,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28')
    lines.append('1,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4')
    lines.append('1,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11')
    lines.append('1,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11')
    lines.append('1,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21')
    lines.append('1,1,1,95,1,1,95,1,1,95,1,1,95,1,1,95,1,1,82,1,1,84,1,5,66,1,2,88,1,3,35')
    lines.append('0,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4,2,6,8,2,5,35')
    lines.append('0,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4,2,6,8')
    lines.append('0,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1,2,6,4')
    lines.append('0,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4,3,6,1')
    lines.append('0,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1,2,6,4')
    lines.append('0,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1,2,6,1')
    lines.append('1,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28,2,6,1')
    lines.append('1,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4,2,5,28')
    lines.append('1,1,1,84,1,5,66,1,2,88,1,3,35,1,5,21,3,5,15,1,5,54,1,6,11,1,6,11,1,6,4')

    data = []

    for line in lines:
        linedata = line.split(',')
        linedata.reverse()
        input = np.array(
            [
             float(linedata[0]),
             float(linedata[3]),
             float(linedata[6]), 
             float(linedata[9]), 
             float(linedata[12]), 
             float(linedata[15]), 
             float(linedata[18]), 
             float(linedata[21]), 
             float(linedata[24]), 
             float(linedata[27]), 
             float(linedata[1]), 
             float(linedata[4]), 
             float(linedata[7]), 
             float(linedata[10]), 
             float(linedata[13]), 
             float(linedata[16]), 
             float(linedata[19]), 
             float(linedata[22]), 
             float(linedata[25]), 
             float(linedata[28]), 
             float(linedata[2]), 
             float(linedata[5]), 
             float(linedata[8]), 
             float(linedata[11]), 
             float(linedata[14]), 
             float(linedata[17]), 
             float(linedata[20]), 
             float(linedata[23]), 
             float(linedata[26]), 
             float(linedata[29]), 
            ], dtype=np.float32)

        

        data.append(input)

    return data

tflite_file = 'sleep_classifier_model.tflite' 


interpreter = tf.lite.Interpreter(model_path=tflite_file)
interpreter.allocate_tensors()


# Get input and output tensors.
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# Test model on random input data.
input_shape = input_details[0]['shape']
input_data_auto = np.array(np.random.random_sample(input_shape), dtype=np.float32)

allinputs = loadDataLikeExpected()


result = []
j = 0
for input_data in allinputs:

    j = j+1

    for i in range(0,len(input_details)):
        input_i = np.array([[input_data[i]]], dtype=np.float32)
        #input_index = interpreter.get_input_details()[i]["index"]
        interpreter.set_tensor(input_details[i]["index"], input_i)

    interpreter.invoke()

    # The function `get_tensor()` returns a copy of the tensor data.
    # Use `tensor()` in order to get a pointer to the tensor.
    output_data = interpreter.get_tensor(output_details[0]['index'])

    result.append(outputdataclassifiSmall(output_data))


print('Prediction: ')
print(result)
