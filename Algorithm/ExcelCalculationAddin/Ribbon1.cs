using CsvHelper;
using ExcelCalculationAddin.Calclulate;
using ExcelCalculationAddin.Export;
using ExcelCalculationAddin.Live;
using ExcelCalculationAddin.Model;
using ExcelCalculationAddin.Model.SleepStateDetect;
using Microsoft.Office.Tools.Ribbon;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using static ExcelCalculationAddin.Model.SleepStateDetect.SleepStateClean;
using static ExcelCalculationAddin.Model.SleepTimeClean;
using static ExcelCalculationAddin.Model.SleepType;

namespace ExcelCalculationAddin
{
    public partial class Ribbon1
    {
        private void Ribbon1_Load(object sender, RibbonUIEventArgs e)
        {

        }

        private async void btnCalculieren_Click(object sender, RibbonControlEventArgs e)
        {


            // Kalkulieren und abspeichern der daten
            ReadParameter.GetAlarmSettings();
            ReadParameter.ReadSleepTypeParameter();

            
            await CalculateSleep.CalcAllSleepData();

        }

        private async void btnEinlesen_Click(object sender, RibbonControlEventArgs e)
        {
            // Kalkulieren und abspeichern der daten
            ReadParameter.GetAlarmSettings();
            ReadParameter.ReadSleepTypeParameter();
           // SleepTimeClean.sleepCleanParamsAfter = SleepTimeParameter.CreateAllModels(false);
            //SleepTimeClean.sleepCleanModelsAfter = SleepTimeModel.CreateAllModels(false);
            //SleepType.sleepTypeParamsAfter = SleepTimeParameter.CreateAllFactorModels(false);

            SleepTimeClean.sleepTimeParamsWhile= SleepTimeParameter.CreateAllModels(true);
            SleepTimeClean.sleepTimeModelsWhile = SleepTimeModel.CreateAllModels(true);
            SleepType.sleepTimeParameter = SleepTimeParameter.CreateAllFactorModels(true);

            SleepStateClean.sleepStateParams = SleepStateParameter.CreateAllModels(true);
            SleepStateClean.sleepStateModels = SleepStateModel.CreateAllModels(true);
           // SleepType.sleepStateParameter= SleepStateParameter.CreateAllFactorModels(true);

            await ReadParameter.GetAllUserData();
        }

        private async void btnCalcLive_Click(object sender, RibbonControlEventArgs e)
        {
            // Kalkulieren und abspeichern der daten
            ReadParameter.GetAlarmSettings();
            ReadParameter.ReadSleepTypeParameter();


            await CalcSleepLive.CalcAllSleepData();
        }

        private async void btnJsonExport_Click(object sender, RibbonControlEventArgs e)
        {
            if (SleepStateClean.sleepStateParams == null)
            {
                return;
            }


            string folder = await ExportFile.GetFolder();

            if (folder == null)
            {
                return;
            }


            // first we have to create the classes
            List<RootTime> rootTime = new List<RootTime>();
            foreach (var item in SleepTimeClean.sleepTimeModelsWhile)
            {
                //SleepTimeClean.sleepTimeParamsWhile

                RootTime rt = new RootTime();

                //rt.id = ((int)item.Key).ToString();
                rt.id = item.Key.ToString();
                //rt.sleepTimeParameter = SleepTimeClean.sleepTimeParamsWhile[item.Key];
                rt.sleepTimePattern = item.Key;

                rt.sleepTimeModelMax = item.Value.getMaxValues();
                rt.sleepTimeModelMin = item.Value.getMinValues();

                rootTime.Add(rt);
            }

            var jsonTimeFile = JsonConvert.SerializeObject(rootTime);

            ExportFile.Export(jsonTimeFile, "TimeModel", folder);

            // first we have to create the classes
            List<RootState> rootState = new List<RootState>();
            foreach (var item in SleepStateClean.sleepStateModels)
            {
                //SleepTimeClean.sleepTimeParamsWhile

                RootState rt = new RootState();

                var a = Char.GetNumericValue(item.Key[0]);
                var b = Char.GetNumericValue(item.Key[1]);

                rt.sleepStatePattern = (SleepStateCleanType)((int)(a));
                rt.userFactorPattern = (UserFactorPattern)((int)(b));
                rt.id = rt.sleepStatePattern.ToString() + rt.userFactorPattern.ToString();

                //rt.sleepStateParameter = SleepStateClean.sleepStateParams[rt.sleepStatePattern];

                rt.sleepStateModelMax = item.Value.getMaxValues();
                rt.sleepStateModelMin = item.Value.getMinValues();

                rootState.Add(rt);
            }

            var jsonStateFile = JsonConvert.SerializeObject(rootState);
            ExportFile.Export(jsonStateFile, "StateModel", folder);

            // first we have to create the classes
            List<RootStateParameter> rootStateParameter = new List<RootStateParameter>();
            foreach (var item in SleepStateClean.sleepStateParams)
            {
                //SleepTimeClean.sleepTimeParamsWhile

                RootStateParameter rst = new RootStateParameter();

                //rst.id = ((int)item.Key).ToString() + "0";
                rst.id = item.Key.ToString();
                rst.sleepStatePattern = item.Value.stc;
                rst.userFactorPattern = item.Value.ufp;

                //rt.sleepStateParameter = SleepStateClean.sleepStateParams[rt.sleepStatePattern];

                rst.sleepStateParameter = item.Value.first;

                rootStateParameter.Add(rst);
            }

            var jsonStateParamsFile = JsonConvert.SerializeObject(rootStateParameter);

            ExportFile.Export(jsonStateParamsFile, "StateParameter", folder);

            // first we have to create the classes
            List<RootTimeParameter> rootTimeParameter = new List<RootTimeParameter>();
            foreach (var item in SleepTimeClean.sleepTimeParamsWhile)
            {
                //SleepTimeClean.sleepTimeParamsWhile

                RootTimeParameter rst = new RootTimeParameter();

                //rst.id = ((int)item.Key).ToString() + "0";
                rst.id = item.Key.ToString();
                rst.sleepTimePattern = item.Key;
                rst.userFactorPattern = (UserFactorPattern)0;

                //rt.sleepStateParameter = SleepStateClean.sleepStateParams[rt.sleepStatePattern];

                rst.sleepTimeParameter = item.Value;

                rootTimeParameter.Add(rst);
            }
            foreach (var item in SleepType.sleepTimeParameter)
            {
                //SleepTimeClean.sleepTimeParamsWhile

                RootTimeParameter rst = new RootTimeParameter();

                rst.id = item.Key.ToString();
                //rst.id = "0" + ((int)item.Key).ToString();
                rst.sleepTimePattern = (SleepTimeCleanType)0;
                rst.userFactorPattern = item.Key;

                //rt.sleepStateParameter = SleepStateClean.sleepStateParams[rt.sleepStatePattern];

                rst.sleepTimeParameter = item.Value;

                rootTimeParameter.Add(rst);
            }

            var jsonTimeParamsFile = JsonConvert.SerializeObject(rootTimeParameter);
            ExportFile.Export(jsonTimeParamsFile, "TimeParameter", folder);


            List<List<RootRawApi>> mrral = new List<List<RootRawApi>>();

            for (int i = 1; i < ReadParameter.values.Count; i++)
            {
                for (int j = 1; j < ReadParameter.values[i].sleepSessionWhile.Count; j++)
                {
                    List<RootRawApi> rral = new List<RootRawApi>();

                    foreach (var item in ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll)
                    {
                        RootRawApi rra = new RootRawApi();

                        rra.confidence = item.sleep;
                        rra.motion = item.motion;
                        rra.light = item.light;

                        TimeSpan span = item.time.Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc));
                        rra.timestampSeconds = (int)span.TotalSeconds;

                        rral.Add(rra);
                    }

                    mrral.Add(rral);
                }
            }
          
            var rawSleepApiDataFiles = JsonConvert.SerializeObject(mrral);
            ExportFile.Export(rawSleepApiDataFiles, "SleepValues", folder);

            List<string> csvData = new List<string>();
         
            for (int i = 0; i < ReadParameter.values.Count; i++)
            {
                for (int j = 1; j < ReadParameter.values[i].sleepSessionWhile.Count; j++)
                {
                    string data = "time, timeraw,light,motion,sleep,real\n";
                    string data1 = "time, timeraw,light,motion,sleep,real\n";
                    string data2 = "time, timeraw,light,motion,sleep,real\n";
                    
                    string data11 = "time,real,light,motion,sleep,light1,motion1,sleep1,light2,motion2,sleep2,light3,motion3,sleep3,light4,motion4,sleep4,light5,motion5,sleep5,light6,motion6,sleep6,light7,motion7,sleep7,light8,motion8,sleep8,light9,motion9,sleep9,light10,motion10,sleep10\n";
                    string data12 = "time,real,light,motion,sleep,light1,motion1,sleep1,light2,motion2,sleep2,light3,motion3,sleep3,light4,motion4,sleep4,light5,motion5,sleep5,light6,motion6,sleep6,light7,motion7,sleep7,light8,motion8,sleep8,light9,motion9,sleep9,light10,motion10,sleep10\n";
                    string data13 = "time,real,light,motion,sleep,light1,motion1,sleep1,light2,motion2,sleep2,light3,motion3,sleep3,light4,motion4,sleep4,light5,motion5,sleep5,light6,motion6,sleep6,light7,motion7,sleep7,light8,motion8,sleep8,light9,motion9,sleep9,light10,motion10,sleep10\n";
                    List<RootRawApiFull> rraltrue = new List<RootRawApiFull>();
                    string time = "";

                    for (int k = 0; k < ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll.Count; k++)
                    {
                        var actualTime = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time;
                        var listOfDataBevore = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll.Where(x => x.time <= actualTime && x.time > actualTime.AddMinutes(-60)).OrderByDescending(y=> y.time).ToList();
                        string timea = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time.Year.ToString() + "-" + ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time.Month.ToString() + "-" + ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time.Day.ToString() + " " + ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time.TimeOfDay;
                        TimeSpan span = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time.Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc));
                        time = span.TotalSeconds.ToString();
                        string val1 = timea + "," + ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].realSleepState;

                        for (int l = 0; l < 10; l++)
                        {
                            if (listOfDataBevore.Count() > l)
                            {
                                val1 += "," + listOfDataBevore[l].light + ","+ listOfDataBevore[l].motion + ","+ listOfDataBevore[l].sleep;
                            }
                            else
                            {
                                val1 += ",0,0,0";
                            }
                        }

                        data11 += val1+"\n";
                        data12 = data11.Replace("rem", "sleeping").Replace("deep", "sleeping").Replace("light", "sleeping");
                        if (!ReadParameter.values[i].sheetname.ToLower().Contains("fabi"))
                        {
                            data13 += val1.Replace("rem", "light") + "\n";
                        }

                    }

                    ExportFile.ExportCSV(data11, ReadParameter.values[i].sheetname + time, folder, "CombinedCsvFiles");
                    ExportFile.ExportCSV(data12, ReadParameter.values[i].sheetname + time, folder, @"CombinedCsvFiles\Combined04");
                    if (!ReadParameter.values[i].sheetname.ToLower().Contains("fabi"))
                    {
                        ExportFile.ExportCSV(data13, ReadParameter.values[i].sheetname + time, folder, @"CombinedCsvFiles\Combined012");
                    }


                    foreach (var item in ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll)
                    {
                        string val1 = "";

                        TimeSpan span = item.time.Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc));
                        string timea = item.time.Year.ToString() + "-" + item.time.Month.ToString() + "-" + item.time.Day.ToString() + " " + item.time.TimeOfDay;
                        val1 = timea + ","+ span.TotalSeconds.ToString() + "," + item.light + "," + item.motion + "," + item.sleep + "," + (int)item.realSleepState + "\n";// + ReadParameter.values[i].sheetname + "\n";
                        time = span.TotalSeconds.ToString();
                        
                        data += val1;
                        if (!ReadParameter.values[i].sheetname.ToLower().Contains("fabi"))
                        {
                            data2 += val1.Replace("rem", "light");
                        }

                        RootRawApiFull rrf = new RootRawApiFull();

                        rrf.light = item.light;
                        rrf.motion = item.motion;
                        rrf.sleep = item.sleep;
                        rrf.real = (int)item.realSleepState;
                        rrf.user = ReadParameter.values[i].sheetname;
                        rrf.time = timea;

                        rraltrue.Add(rrf);

                    }

                    data1 = data.Replace("rem", "sleeping").Replace("deep", "sleeping").Replace("light", "sleeping");

                    ExportFile.ExportCSV(data, ReadParameter.values[i].sheetname + time, folder, "SingleCsvFiles");
                    ExportFile.ExportCSV(data1, ReadParameter.values[i].sheetname + time, folder, @"SingleCsvFiles\Combined04");
                    if (!ReadParameter.values[i].sheetname.ToLower().Contains("fabi"))
                    {
                        ExportFile.ExportCSV(data2, ReadParameter.values[i].sheetname + time, folder, @"SingleCsvFiles\Combined012");
                    }

                    var jsondaza = JsonConvert.SerializeObject(rraltrue);

                    ExportFile.ExportJSON(jsondaza, ReadParameter.values[i].sheetname + time, folder);

                }
            }


        }
    }
}
