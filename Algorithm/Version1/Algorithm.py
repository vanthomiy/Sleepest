

# Caller for new input information
from ParamsObject import Parameter
from asyncio.windows_events import NULL
from RecordDataObject import RecordDataObject
import os


recordData = []
SleepThreshold = NULL
LightThreshold = NULL
MotionThreshold = NULL
MinAwakeDuration = NULL
MinSleepDuration = NULL
AwakeBackCalculation = NULL
MinBackCalculation = NULL
MaxDataSetsForCaluclation = NULL


def clearData():
  recordData = []
  SleepThreshold = NULL
  LightThreshold = NULL
  MotionThreshold = NULL
  MinAwakeDuration = NULL
  MinSleepDuration = NULL
  AwakeBackCalculation = NULL
  MinBackCalculation = NULL
  MaxDataSetsForCaluclation = NULL

#algorythm values
timesSleep = 0


async def importNewSleepDataSet(sleepDataSet, _actualParams):
    
    #save list and params
    recordData.append(sleepDataSet)

    #set params
    SleepThreshold = _actualParams[0].set
    SleepThresholdFaktor = _actualParams[0].faktor
    SleepThresholdOffset = _actualParams[0].offset
    LightThreshold = _actualParams[1].set
    LightThresholdFaktor = _actualParams[1].faktor
    LightThresholdOffset = _actualParams[1].offset
    MotionThreshold = _actualParams[2].set
    MotionThresholdFaktor = _actualParams[2].faktor
    MotionThresholdOffset = _actualParams[2].offset
    MinAwakeDuration = _actualParams[3].set
    MinSleepDuration = _actualParams[4].set
    MinSleepDurationFaktor = _actualParams[4].faktor
    MinSleepDurationOffset = _actualParams[4].offset
    AwakeBackCalculation = _actualParams[5].set
    MinBackCalculation = _actualParams[6].set
    MaxDataSetsForCaluclation = _actualParams[7].set

    lenData = len(recordData)
    #check if list is to big.. then remove values
    if (lenData > MaxDataSetsForCaluclation):
       del recordData[0]

    #check if enough data else return
    #if(lenData < MinAwakeDuration and  lenData < MinSleepDuration):
    #  return NULL

    #start the algorithm

    calculation = []

    #foreach dataset in recorddata
    for dataset in recordData:
      e1 = ((dataset.sleep/(SleepThreshold*2+0.00001)) if dataset.sleep < SleepThreshold else (0.5+(dataset.sleep-SleepThreshold) * (1/((2 * 100 - SleepThreshold)+0.00001))))
      e2 = 1 - ((dataset.light/(LightThreshold*2+0.00001)) if dataset.light > LightThreshold else (0.5+(dataset.light-LightThreshold) * (1/((2 * 5 - LightThreshold)+0.00001))))
      e3 = 1 - ((dataset.motion/(MotionThreshold*2+0.00001)) if dataset.motion > MotionThreshold else (0.5+(dataset.motion-MotionThreshold) * (1/((2 * 5 - MotionThreshold)+0.00001))))

      #calculate the result without e4
      maxPrevSleep = (SleepThresholdFaktor +  SleepThresholdOffset) + (LightThresholdFaktor +  LightThresholdOffset) + (MotionThresholdFaktor +  MotionThresholdOffset)
      sleepPrevSum = (e1 * SleepThresholdFaktor +  SleepThresholdOffset) + (e2 * LightThresholdFaktor +  LightThresholdOffset) + (e3 * MotionThresholdFaktor +  MotionThresholdOffset)
      sleepPrevCalc = sleepPrevSum / maxPrevSleep
      timesPrevSleep = timesSleep - (1/(MinSleepDuration+0.00001)) if sleepPrevCalc < 0.5 else timesSleep + (1/((MinSleepDuration+0.00001)))
      if(timesPrevSleep < 0):
        timesPrevSleep = 0
      elif(timesPrevSleep > 1):
        timesPrevSleep = 1

      #Calculate the full value
      maxSleep = (SleepThresholdFaktor +  SleepThresholdOffset) + (LightThresholdFaktor +  LightThresholdOffset) + (MotionThresholdFaktor +  MotionThresholdOffset) + (MinSleepDurationFaktor +  MinSleepDurationOffset)
      sleepSum = (e1 * SleepThresholdFaktor +  SleepThresholdOffset) + (e2 * LightThresholdFaktor +  LightThresholdOffset) + (e3 * MotionThresholdFaktor +  MotionThresholdOffset) + (timesPrevSleep * MinSleepDurationFaktor +  MinSleepDurationOffset)
      sleepCalc = sleepSum / maxSleep

      #put all in a list
      calculation.append(sleepCalc)

      #print("At: " + " Calculated Raw: %.2f" % sleepCalc + " RealSleep: %.2f" % dataset.realSleep)


    return calculation, recordData



