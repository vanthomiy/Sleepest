import tensorflow as tf
import numpy as np
import pickle as pkl
import json

def outputdataclassifiSmall(predictions1, class_names):
    prediction_class1 = np.argmax(predictions1)
    prediction_class_name1 = class_names[prediction_class1]
    return prediction_class_name1

def loadDataFor045():

    lines = []

    lines.append('0,1,6,9,2,6,3,1,5,13,2,6,7,2,6,7,2,6,8,2,6,5,2,6,2,1,6,5,1,6,5,1,6,4,2,5,28,2,6,7,1,6,7,1,6,4,2,5,28,2,6,1,2,6,1,1,6,3,4,1,21,4,6,24,3,6,2,3,6,2,3,6,1')
    lines.append('0,2,6,1,1,6,9,2,6,3,1,5,13,1,5,13,2,6,7,2,6,8,2,6,5,2,6,5,2,6,2,1,6,5,1,6,4,2,5,28,2,6,7,1,6,7,1,6,4,2,5,28,2,5,28,2,6,1,1,6,3,4,1,21,4,6,24,4,6,24,3,6,2')
    lines.append('0,2,6,23,2,6,1,1,6,9,2,6,3,2,6,3,1,5,13,2,6,7,2,6,8,2,6,8,2,6,5,2,6,2,1,6,5,1,6,4,2,5,28,2,6,7,2,6,7,1,6,7,1,6,4,2,5,28,2,6,1,1,6,3,4,1,21,4,6,24,4,6,24')
    lines.append('0,1,6,4,2,6,23,2,6,1,1,6,9,2,6,3,2,6,3,1,5,13,2,6,7,2,6,8,2,6,8,2,6,5,2,6,2,1,6,5,1,6,4,2,5,28,2,6,7,2,6,7,1,6,7,1,6,4,2,5,28,2,6,1,1,6,3,4,1,21,4,6,24')
    lines.append('0,1,6,5,1,6,4,2,6,23,2,6,1,1,6,9,2,6,3,2,6,3,1,5,13,2,6,7,2,6,7,2,6,8,2,6,5,2,6,2,1,6,5,1,6,4,1,6,4,2,5,28,2,6,7,1,6,7,1,6,4,2,5,28,2,6,1,1,6,3,1,6,3')
    lines.append('0,1,4,31,1,6,5,1,6,4,2,6,23,2,6,1,1,6,9,1,6,9,2,6,3,1,5,13,2,6,7,2,6,7,2,6,8,2,6,5,2,6,2,1,6,5,1,6,5,1,6,4,2,5,28,2,6,7,1,6,7,1,6,4,2,5,28,2,6,1,2,6,1')
    lines.append('0,1,4,60,1,4,31,1,6,5,1,6,4,2,6,23,2,6,1,2,6,1,1,6,9,2,6,3,1,5,13,1,5,13,2,6,7,2,6,8,2,6,5,2,6,5,2,6,2,1,6,5,1,6,4,2,5,28,2,6,7,1,6,7,1,6,4,2,5,28,2,5,28')
    lines.append('1,1,3,88,1,4,60,1,4,31,1,6,5,1,6,4,2,6,23,2,6,1,2,6,1,1,6,9,2,6,3,2,6,3,1,5,13,2,6,7,2,6,8,2,6,5,2,6,5,2,6,2,1,6,5,1,6,4,2,5,28,2,6,7,1,6,7,1,6,7,1,6,4')
    lines.append('1,1,4,78,1,3,88,1,3,88,1,4,60,1,4,31,1,6,5,1,6,4,2,6,23,2,6,1,2,6,1,1,6,9,2,6,3,2,6,3,1,5,13,2,6,7,2,6,8,2,6,5,2,6,5,2,6,2,1,6,5,1,6,4,2,5,28,2,6,7,1,6,7')
    lines.append('1,1,3,88,1,4,78,1,3,88,1,3,88,1,4,60,1,4,31,1,6,5,1,6,4,2,6,23,2,6,1,2,6,1,1,6,9,2,6,3,2,6,3,1,5,13,2,6,7,2,6,8,2,6,5,2,6,5,2,6,2,1,6,5,1,6,4,2,5,28,2,6,7')    
    lines.append('1,1,3,90,1,3,88,1,4,78,1,3,88,1,3,88,1,4,60,1,4,31,1,6,5,1,6,4,1,6,4,2,6,23,2,6,1,1,6,9,2,6,3,2,6,3,1,5,13,2,6,7,2,6,8,2,6,8,2,6,5,2,6,2,1,6,5,1,6,4,2,5,28')
    lines.append('1,1,1,91,1,3,90,1,3,88,1,4,78,1,3,88,1,3,88,1,4,60,1,4,31,1,4,31,1,6,5,1,6,4,2,6,23,2,6,1,1,6,9,1,6,9,2,6,3,1,5,13,2,6,7,2,6,7,2,6,8,2,6,5,2,6,2,1,6,5,1,6,5')
    lines.append('1,1,1,91,1,1,91,1,3,90,1,3,88,1,3,88,1,4,78,1,3,88,1,3,88,1,4,60,1,4,31,1,6,5,1,6,4,2,6,23,2,6,1,2,6,1,1,6,9,2,6,3,2,6,3,1,5,13,2,6,7,2,6,8,2,6,5,2,6,5,2,6,2')

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



def testLiteModel(tflite_file, allinputs):
    interpreter = tf.lite.Interpreter(model_path=tflite_file)
    interpreter.allocate_tensors()

    # Get input and output tensors.
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    result = []
    for input_data in allinputs:
        for i in range(0,len(input_details)):
            input_i = np.array([[input_data[i]]], dtype=np.float32)
            interpreter.set_tensor(input_details[i]["index"], input_i)

        interpreter.invoke()

        # The function `get_tensor()` returns a copy of the tensor data.
        # Use `tensor()` in order to get a pointer to the tensor.
        output_data = interpreter.get_tensor(output_details[0]['index'])

        result.append(outputdataclassifiSmall(output_data))


    return result

def saveModelInputDetails(tflite_file):
    interpreter = tf.lite.Interpreter(model_path=tflite_file+'.tflite')
    interpreter.allocate_tensors()

    # Get input and output tensors.
    input_details = interpreter.get_input_details()

    list1 = []
    for input in input_details:
        data = {}
        
        data['index'] = str(input['index'])
        data['name'] = str(input['name'])
        data['shape'] = str(input['shape'])
        data['dtype'] = str(input['shape'].dtype)

        list1.append(data)


    file = tflite_file+'Inputs.json'


    with open(file, 'w') as fp:
        json.dump(list1,fp)
            
'''
tflite_file = 'litemodels/sleep045' 
saveModelInputDetails(tflite_file)

tflite_file = 'litemodels/sleep0410' 
saveModelInputDetails(tflite_file)

tflite_file = 'litemodels/sleep0430' 
saveModelInputDetails(tflite_file)


tflite_file = 'litemodels/sleep125' 
saveModelInputDetails(tflite_file)

tflite_file = 'litemodels/sleep1210' 
saveModelInputDetails(tflite_file)

tflite_file = 'litemodels/sleep1230' 
saveModelInputDetails(tflite_file)

tflite_file = 'litemodels/sleep125' 
saveModelInputDetails(tflite_file)

tflite_file = 'litemodels/sleep1210' 
saveModelInputDetails(tflite_file)

tflite_file = 'litemodels/sleep1230' 
saveModelInputDetails(tflite_file)
'''
'''
results = [] 

tflite_file = 'litemodels/sleep045.tflite' 
allinputs = loadDataFor045()
results.append(testLiteModel(tflite_file, allinputs))


####

j = 0

for result in results:
    j = j+1
    print(str(j) + result)

'''
