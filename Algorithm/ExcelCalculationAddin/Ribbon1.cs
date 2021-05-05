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

           // SleepTimeClean.sleepTimeParamsWhile= SleepTimeParameter.CreateAllModels(true);
           // SleepTimeClean.sleepTimeModelsWhile = SleepTimeModel.CreateAllModels(true);
           // SleepType.sleepTimeParameter = SleepTimeParameter.CreateAllFactorModels(true);

           // SleepStateClean.sleepStateParams = SleepStateParameter.CreateAllModels(true);
           // SleepStateClean.sleepStateModels = SleepStateModel.CreateAllModels(true);
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

        private async void btnJsonExport_Click1(object sender, RibbonControlEventArgs e)
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
            /*

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

            */
            List<List<RootRawApi>> mrral = new List<List<RootRawApi>>();
            List<List<RootRawApiTrue>> mrraltrue = new List<List<RootRawApiTrue>>();

            for (int i = 1; i < ReadParameter.values.Count; i++)
            {
                for (int j = 1; j < ReadParameter.values[i].sleepSessionWhile.Count; j++)
                {
                    List<RootRawApi> rral = new List<RootRawApi>();
                    List<RootRawApiTrue> rraltrue = new List<RootRawApiTrue>();

                    foreach (var item in ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll)
                    {
                        RootRawApi rra = new RootRawApi();
                        RootRawApiTrue rratrue = new RootRawApiTrue();

                        rratrue.confidence = rra.confidence = item.sleep;
                        rratrue.motion = rra.motion = item.motion;
                        rratrue.light = rra.light = item.light;
                        rratrue.real = item.realSleepState;

                        TimeSpan span = item.time.Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc));
                        rra.timestampSeconds = (int)span.TotalSeconds;
                        rratrue.timestampSeconds = (int)span.TotalSeconds;

                        rral.Add(rra);
                        rraltrue.Add(rratrue);
                    }

                    mrral.Add(rral);
                    mrraltrue.Add(rraltrue);
                }
            }
          
            var rawSleepApiDataFiles = JsonConvert.SerializeObject(mrral);
            var rawSleepApiDataFilesTrue = JsonConvert.SerializeObject(mrraltrue);
            ExportFile.Export(rawSleepApiDataFiles, "SleepValues", folder);
            ExportFile.Export(rawSleepApiDataFilesTrue, "SleepValuesTrue", folder);

            List<string> csvData = new List<string>();
         
            for (int i = 0; i < ReadParameter.values.Count; i++)
            {
                for (int j = 1; j < ReadParameter.values[i].sleepSessionWhile.Count; j++)
                {
                    string data = "time, timeraw,light,motion,sleep,real\n";
                    string data1 = "time, timeraw,light,motion,sleep,real\n";
                    string data2 = "time, timeraw,light,motion,sleep,real\n";

                    string data11 = "real,brigthness,motion,sleep,brigthness1,motion1,sleep1,brigthness2,motion2,sleep2,brigthness3,motion3,sleep3,brigthness4,motion4,sleep4,brigthness5,motion5,sleep5,brigthness6,motion6,sleep6,brigthness7,motion7,sleep7,brigthness8,motion8,sleep8,brigthness9,motion9,sleep9\n";
                    string data12 = "real,brigthness,motion,sleep,brigthness1,motion1,sleep1,brigthness2,motion2,sleep2,brigthness3,motion3,sleep3,brigthness4,motion4,sleep4,brigthness5,motion5,sleep5,brigthness6,motion6,sleep6,brigthness7,motion7,sleep7,brigthness8,motion8,sleep8,brigthness9,motion9,sleep9\n";
                    string data13 = "real,brigthness,motion,sleep,brigthness1,motion1,sleep1,brigthness2,motion2,sleep2,brigthness3,motion3,sleep3,brigthness4,motion4,sleep4,brigthness5,motion5,sleep5,brigthness6,motion6,sleep6,brigthness7,motion7,sleep7,brigthness8,motion8,sleep8,brigthness9,motion9,sleep9\n";
                    
                    List<RootRawApiFull> rraltrue = new List<RootRawApiFull>();
                    string time = "";

                    for (int k = 0; k < ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll.Count; k++)
                    {
                        var actualTime = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time;
                        //var listOfDataBevore = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll.Where(x => x.time <= actualTime && x.time > actualTime.AddMinutes(-60)).OrderByDescending(y=> y.time).ToList();
                        var listOfDataBevore = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll.Where(x => x.time <= actualTime).OrderByDescending(y => y.time).ToList();
                        string timea = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time.Year.ToString() + "-" + ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time.Month.ToString() + "-" + ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time.Day.ToString() + " " + ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time.TimeOfDay;
                        TimeSpan span = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time.Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc));
                        time = span.TotalSeconds.ToString();
                        string val1 = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].realSleepState.ToString();

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
                        val1 = item.light + "," + item.motion + "," + item.sleep + "," + (int)item.realSleepState + "\n";// + ReadParameter.values[i].sheetname + "\n";
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


        public void Split<T>(T[] array, int index, out T[] first, out T[] second)
        {
            first = array.Take(index).ToArray();
            second = array.Skip(index).ToArray();
        }

        public void Split<T>(T[] array, int index, out T[] first, out T[] second, out T[] third)
        {
            first = array.Take(index).ToArray();
            var puffer = array.Skip(index).ToArray();
            second = puffer.Take(index).ToArray();
            third = puffer.Skip(index).ToArray();
        }

        public void SplitMidPoint<T>(T[] array, out T[] first, out T[] second)
        {
            Split(array, array.Length / 2, out first, out second);
        }

        public void SplitThirdPoint<T>(T[] array, out T[] first, out T[] second, out T[] third)
        {
            Split(array, array.Length / 3, out first, out second, out third);
        }

        private async void btnJsonExport_Click(object sender, RibbonControlEventArgs e)
        {

            string folder = await ExportFile.GetFolder();

            if (folder == null)
            {
                return;
            }



            List<List<RootRawApi>> mrral = new List<List<RootRawApi>>();
            List<List<RootRawApiTrue>> mrraltrue = new List<List<RootRawApiTrue>>();

            for (int i = 1; i < ReadParameter.values.Count; i++)
            {
                for (int j = 1; j < ReadParameter.values[i].sleepSessionWhile.Count; j++)
                {
                    List<RootRawApi> rral = new List<RootRawApi>();
                    List<RootRawApiTrue> rraltrue = new List<RootRawApiTrue>();

                    foreach (var item in ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll)
                    {
                        RootRawApi rra = new RootRawApi();
                        RootRawApiTrue rratrue = new RootRawApiTrue();

                        rratrue.confidence = rra.confidence = item.sleep;
                        rratrue.motion = rra.motion = item.motion;
                        rratrue.light = rra.light = item.light;
                        rratrue.real = item.realSleepState;

                        TimeSpan span = item.time.Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc));
                        rra.timestampSeconds = (int)span.TotalSeconds;
                        rratrue.timestampSeconds = (int)span.TotalSeconds;

                        rral.Add(rra);
                        rraltrue.Add(rratrue);
                    }

                    mrral.Add(rral);
                    mrraltrue.Add(rraltrue);
                }
            }

            var rawSleepApiDataFiles = JsonConvert.SerializeObject(mrral);
            var rawSleepApiDataFilesTrue = JsonConvert.SerializeObject(mrraltrue);
            ExportFile.Export(rawSleepApiDataFiles, "SleepValues", folder);
            ExportFile.Export(rawSleepApiDataFilesTrue, "SleepValuesTrue", folder);


            List<int> times = new List<int>{5, 10, 30};
            int minutes = 2*60;

            List<string> csvData = new List<string>();
            foreach (var time in times)
            {
                string sleep04file = "real";
                string sleep12file = "real";
                string tablebedfile = "real,brigthnessMax,motionMax,sleepMax,brigthnessMin,motionMin,sleepMin,brigthnessMedian,motionMedian,sleepMedian,brigthnessAverage,motionAverage,sleepAverage";
                string wakeuplightfile = "real";

                for (int r = 0; r < minutes / time; r++)
                {
                    sleep04file += $",brigthness{r},motion{r},sleep{r}";
                    sleep12file += $",brigthness{r},motion{r},sleep{r}";
                }

                for (int r = 1; r < minutes / time; r++)
                {
                    wakeuplightfile += $",brigthness{r},motion{r},sleep{r}";
                }

                for (int i = 0; i < ReadParameter.values.Count; i++)
                {
                    var userdatabucket04 = "";
                    var userdatabucket12 = "";
                    var userdatabucketWakeup = "";
                    var userdatatablebedbucket = "";

                    for (int j = 0; j < ReadParameter.values[i].sleepSessionWhile.Count; j++)
                    {
                        List<RootRawApiFull> rraltrue = new List<RootRawApiFull>();

                        var databucket04 = "";
                        var databucket12 = "";
                        var databucketWakeup = "";
                        var tablebedbucket = "";


                        if (ReadParameter.values[i].sheetname != "Table")
                        {
                            for (int k = 0; k < ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll.Count; k++)
                            {
                                try
                                {

                                    var startTime = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].time;
                                    var listOfDataBevore = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll.OrderByDescending(y => y.time).ToList();
                                    var listOfDataAfter = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll.OrderBy(y => y.time).ToList();

                                    string dataset04 = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].realSleepState.ToString();
                                    string datasetWakeup = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].realSleepState.ToString();
                                    string dataset12 = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].realSleepState.ToString();

                                    for (int l = 0; l < minutes; l += time)
                                    {
                                        var actualTime = startTime.AddMinutes(-l);

                                        var actualItem = listOfDataBevore.First(x => x.time <= actualTime);

                                        if (listOfDataBevore.Count() > l)
                                        {
                                            dataset04 += "," + actualItem.light + "," + actualItem.motion + "," + actualItem.sleep;
                                        }
                                        else
                                        {
                                            var item = listOfDataBevore.Last();
                                            dataset04 += "," + item.light + "," + item.motion + "," + item.sleep;
                                        }
                                    }

                                    for (int l = 1; l < minutes; l += time)
                                    {
                                        var actualTime = startTime.AddMinutes(-l);

                                        var actualItem = listOfDataBevore.First(x => x.time <= actualTime);

                                        if (listOfDataBevore.Count() > l)
                                        {
                                            datasetWakeup += "," + actualItem.light + "," + actualItem.motion + "," + actualItem.sleep;
                                        }
                                        else
                                        {
                                            var item = listOfDataBevore.Last();
                                            datasetWakeup += "," + item.light + "," + item.motion + "," + item.sleep;
                                        }
                                    }

                                    for (int l = 0; l < minutes / 2; l += time)
                                    {
                                        var actualTimeMin = startTime.AddMinutes(-l);
                                        var actualTimeMax = startTime.AddMinutes(l);

                                        var actualItemBefore = listOfDataBevore.First(x => x.time <= actualTimeMin);
                                        var actualItemAfter = listOfDataAfter.First(x => x.time >= actualTimeMax);

                                        if (listOfDataBevore.Count() > l)
                                        {
                                            dataset12 += "," + actualItemBefore.light + "," + actualItemBefore.motion + "," + actualItemBefore.sleep;
                                        }
                                        else
                                        {
                                            var item = listOfDataBevore.Last();
                                            dataset12 += "," + item.light + "," + item.motion + "," + item.sleep;
                                        }

                                        if (listOfDataAfter.Count() > l)
                                        {
                                            dataset12 += "," + actualItemAfter.light + "," + actualItemAfter.motion + "," + actualItemAfter.sleep;
                                        }
                                        else
                                        {
                                            var item = listOfDataAfter.Last();
                                            dataset12 += "," + item.light + "," + item.motion + "," + item.sleep;
                                        }
                                    }



                                    databucket04 += "\n" + dataset04.Replace("rem", "sleeping").Replace("deep", "sleeping").Replace("light", "sleeping");

                                    if (!ReadParameter.values[i].sheetname.ToLower().Contains("fabi") && ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll[k].realSleepState != SleepState.awake)
                                    {
                                        databucketWakeup += "\n" + datasetWakeup.Replace("rem", "light");
                                        databucket12 += "\n" + dataset12.Replace("rem", "light");
                                    }
                                }
                                catch (Exception ex)
                                {

                                }
                            }

                            userdatabucket04 += "\n" + databucket04;
                            userdatabucket12 += "\n" + databucket12;
                            userdatabucketWakeup += "\n" + databucketWakeup;
                        }

                        var sleepingListFull = ReadParameter.values[i].sleepSessionWhile[j].sleepDataEntrieSleepTimeAll.Where(x=>x.realSleepState != SleepState.awake).ToArray();
                        if (sleepingListFull != null && sleepingListFull.Count() > 6)
                        {
                            List<SleepDataEntry[]> data = new List<SleepDataEntry[]>();
                            SleepDataEntry[] sleepingListFirstHalf, sleepingListSecondHalf, sleepingList1of3, sleepingList2of3, sleepingList3of3;
                            SplitMidPoint(sleepingListFull, out sleepingListFirstHalf, out sleepingListSecondHalf);
                            SplitThirdPoint(sleepingListFull, out sleepingList1of3, out sleepingList2of3, out sleepingList3of3);
                            data.Add(sleepingListFirstHalf);
                            data.Add(sleepingListSecondHalf);
                            data.Add(sleepingList1of3);
                            data.Add(sleepingList2of3);
                            data.Add(sleepingList3of3);

                            foreach (var item in data)
                            {


                                var maxLight = item.Max(x => x.light);
                                var minLight = item.Min(x => x.light);
                                var averageLight = item.Average(x => x.light);
                                var medianLight = item.Sum(x => x.light) / item.Count();

                                var maxMotion = item.Max(x => x.motion);
                                var minMotion = item.Min(x => x.motion);
                                var averageMotion = item.Average(x => x.motion);
                                var medianMotion = item.Sum(x => x.motion) / item.Count();

                                var maxSleep = item.Max(x => x.sleep);
                                var minSleep = item.Min(x => x.sleep);
                                var averageSleep = item.Average(x => x.sleep);
                                var medianSleep = item.Sum(x => x.sleep) / item.Count();

                                tablebedbucket += $"\n{(ReadParameter.values[i].sheetname == "Table" ? 0:1)}," +
                                    $"{(int)maxLight},{(int)maxMotion},{(int)maxSleep}," +
                                    $"{(int)minLight},{(int)minMotion},{(int)minSleep}," +
                                    $"{(int)averageLight},{(int)averageMotion},{(int)averageSleep}," +
                                    $"{(int)medianLight},{(int)medianMotion},{(int)medianSleep}";

                            }
                        }

                        
                        userdatatablebedbucket += "\n" + tablebedbucket;               

                    }

                    sleep04file += "\n" + userdatabucket04;
                    sleep12file += "\n" + userdatabucket12;
                    wakeuplightfile += "\n" + userdatabucketWakeup;
                    tablebedfile += "\n" + userdatatablebedbucket;

                }

                sleep04file = sleep04file.Replace("awake", "0").Replace("sleeping", "1");
                wakeuplightfile = wakeuplightfile.Replace("light", "0").Replace("deep", "1");
                sleep12file = sleep12file.Replace("light", "0").Replace("deep", "1");

                ExportFile.ExportCSV(sleep04file, "sleep04"+time, folder, @"Datasets\sleep04");
                ExportFile.ExportCSV(sleep12file, "sleep12" + time, folder, @"Datasets\sleep12");
                ExportFile.ExportCSV(wakeuplightfile, "wakeuplightfile" + time, folder, @"Datasets\wakeuplight");
                ExportFile.ExportCSV(tablebedfile, "tablebedfile" + time, folder, @"Datasets\tablebed");
            }
        }
    }
}
