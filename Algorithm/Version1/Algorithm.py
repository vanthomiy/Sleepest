

# Caller for new input information
from asyncio.windows_events import NULL
from RecordDataObject import RecordDataObject


recordData = []
actualParams = NULL

def clearData():
  recordData = []
  actualParams = NULL


async def importNewSleepDataSet(sleepDataSet:RecordDataObject, _actualParams):
    #save list and params
    recordData.append(sleepDataSet)
    actualParams = _actualParams
    maxListCount = actualParams[7]['set']
    print(maxListCount)
    #check if list is to big.. then remove values
    if (len(recordData) > maxListCount):
      del recordData[0]

    # start the algorithm





