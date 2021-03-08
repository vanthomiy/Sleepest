from io import StringIO
from RecordDataObject import RecordDataObject
from ParamsObject import ParamsObject
import json
from openpyxl import load_workbook
import asyncio
from Algorithm import importNewSleepDataSet


#Lade die Excelliste mit allen verfügbaren Daten
def LoadExcelSheetsData():
  workbook = load_workbook(filename="Schlafdaten.xlsx")
  
  sheets = workbook.sheetnames
  
  sheetData = {}

  for sheetName in sheets:
    sheet = workbook[sheetName]
    maxRow = sheet.max_row
    for i in range(2,maxRow+1):

      datum=sheet.cell(row=i,column=1)
      time=sheet.cell(row=i,column=2)
      sleep=sheet.cell(row=i,column=3)
      light=sheet.cell(row=i,column=4)
      motion=sheet.cell(row=i,column=5)
      real=sheet.cell(row=i,column=6)

      record = RecordDataObject(datum, time, sleep, light, motion, real)
      sheetData[sheetName] = record

  return sheetData



  #merge data of all


  return workbook._sheets

#Create history
def HistoryCreater():
  print("Function is called")

#Lade die Aktuellen und die Startparameter in das File
def LoadParams(values):
    with open('Params.txt') as json_file:
         data = json.load(json_file)
         return data[values]

#Vergleiche richtig mit falsch und speichere es in der history mitsamt den parametern
def evaluateCalculation(calculation):
    d =1  

#Program main
async def main():

    sheetData = LoadExcelSheetsData()
    actualParams = LoadParams('actualParameter')

    #for each persondatasets
    for person in sheetData:
        #for each sleepdataset
        for dataSet in person:
            # can simply be awaited to wait until it is complete:
            task = asyncio.create_task(importNewSleepDataSet(dataSet, actualParams))
            calculation = await task
            #evaluateCalculation(calculation, actualParams)


   
asyncio.run(main())


