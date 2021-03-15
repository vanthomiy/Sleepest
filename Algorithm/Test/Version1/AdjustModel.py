#Create history
import json
import random


def historyCreater(amountAll,percentageAll,percentageAllSleep,percentageAllAwake, actualParams):

    
    newData = {}

    with open('ParamsHistory.txt') as json_file:
        data = json.load(json_file)
        #if actual is better then that
        if(data['percentageAll'] > percentageAll):
           return

        newData = {}
        myList = []

        #change when update
        for i in range(len(actualParams)):
            value = {}
            value['art'] = actualParams[i].art
            value['min'] = actualParams[i].min
            value['max'] = actualParams[i].max
            value['set'] = actualParams[i].set
            value['offset'] = actualParams[i].offset
            value['faktor'] = actualParams[i].faktor
            myList.append(value.copy())


        newData['actualParameter'] = myList
        newData['amountAll'] = amountAll
        newData['percentageAll'] = percentageAll
        newData['percentageAllSleep'] = percentageAllSleep
        newData['percentageAllAwake'] = percentageAllAwake


    with open('ParamsHistory.txt', 'w') as json_file:
        sco_instance_pkl = json.dumps(newData)
        json_file.write(sco_instance_pkl)
        json_file.close()


#Adjust the params random
def adjustParamsRandom(actualParams):
     #set params
    _actualParams = actualParams
    _actualParams[0].set = random.randint(_actualParams[0].min, _actualParams[0].max)
    _actualParams[0].faktor = random.randint(0, 100) / 100
    _actualParams[0].offset = random.randint(0, 1) / 100
    _actualParams[1].set = random.randint(_actualParams[1].min, _actualParams[1].max)
    _actualParams[1].faktor = random.randint(0, 100) / 100
    _actualParams[1].offset = random.randint(0, 1) / 100
    _actualParams[2].set = random.randint(_actualParams[2].min, _actualParams[2].max)
    _actualParams[2].faktor = random.randint(0, 100) / 100
    _actualParams[2].offset = random.randint(0, 1) / 100
    _actualParams[4].set = random.randint(_actualParams[4].min, _actualParams[4].max)
    _actualParams[4].faktor = random.randint(0, 100) / 100
    _actualParams[4].offset = random.randint(0, 1) / 100
    _actualParams[7].set = random.randint(_actualParams[7].min, _actualParams[7].max)
    return _actualParams

#Adjust the params specific with curve
def adjustParamsSpecific():
    a = 1


