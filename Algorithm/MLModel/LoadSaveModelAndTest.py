import numpy as np
import tensorflow as tf
from ConvertModel import *


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

        sample = {}
        sample['sleep0'] = float(linedata[3])
        sample['sleep1'] = float(linedata[6])
        sample['sleep2'] = float(linedata[9])
        sample['sleep3'] = float(linedata[12])
        sample['sleep4'] = float(linedata[15])
        sample['sleep5'] = float(linedata[18])
        sample['sleep6'] = float(linedata[21])
        sample['sleep7'] = float(linedata[24])
        sample['sleep8'] = float(linedata[27])
        sample['sleep9'] = float(linedata[30])
        sample['brigthness0'] = float(linedata[1])
        sample['brigthness1'] = float(linedata[4])
        sample['brigthness2'] = float(linedata[7])
        sample['brigthness3'] = float(linedata[10])
        sample['brigthness4'] = float(linedata[13])
        sample['brigthness5'] = float(linedata[16])
        sample['brigthness6'] = float(linedata[19])
        sample['brigthness7'] = float(linedata[22])
        sample['brigthness8'] = float(linedata[25])
        sample['brigthness9'] = float(linedata[28])
        sample['motion0'] = float(linedata[2])
        sample['motion1'] = float(linedata[5])
        sample['motion2'] = float(linedata[8])
        sample['motion3'] = float(linedata[11])
        sample['motion4'] = float(linedata[14])
        sample['motion5'] = float(linedata[17])
        sample['motion6'] = float(linedata[20])
        sample['motion7'] = float(linedata[23])
        sample['motion8'] = float(linedata[26])
        sample['motion9'] = float(linedata[29])

        data.append(sample)

    return data


length = 10
headers = []

for i in range(0,length):
    headers.append('sleep'+ str(i))
    headers.append('motion'+ str(i))
    headers.append('brigthness'+ str(i))

reloaded_model_pre = tf.keras.models.load_model('sleep_classifier_model')
reloaded_model_fea = tf.keras.models.load_model('sleep04_features')


alldata = loadData()

i = 0
for sample in alldata:

    i = i+1
    input_dict = {name: tf.convert_to_tensor([value]) for name, value in sample.items()}
    predictions1 = reloaded_model_pre.predict(input_dict)
    predictions2 = reloaded_model_fea.predict(input_dict)
    outputdataclassifi(i, predictions1, predictions2)
