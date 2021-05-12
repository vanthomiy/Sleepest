﻿using ExcelCalculationAddin.Export;
using ExcelCalculationAddin.ListHelp;
using ExcelCalculationAddin.Model.SleepStateDetect;
using ExcelCalculationAddin.Read;
using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepStateDetect.SleepStateParameter;
using static ExcelCalculationAddin.Model.SleepType;

namespace ExcelCalculationAddin.Model
{
    public class SleepSession
    {
        public UserFactorPattern sleepUserType = UserFactorPattern.NORMAL;

        public static int actualRow = 3;
        public DateTime dateTime;

        public bool foundSleep = false;
        public int times = 0;

        public List<SleepDataEntry> sleepDataEntrieSleepTime;
        public List<SleepDataEntry> sleepDataEntrieSleepTimeAll;

        public Dictionary<int,List<SleepDataEntry>> sleepDataEntrieSleep;
        public Dictionary<int, List<SleepDataEntry>> sleepDataEntrieAwake;

        public Strukture structureSleep;
        public Strukture structureAwake;
        public Strukture diffrence;

        public List<SleepDataEntry> modelSleepDataEntrieSleep;
        public List<SleepDataEntry> modelSleepDataEntrieAwake;
        public Strukture modelStructureSleep;
        public Strukture modelStructureAwake;
        public Strukture modelDiffrence;

        public string rw11 = "",rw21="", rw12 = "", rw2 = "", nf1 = "", nf2 = "";
        public string rws1 = "", rws2 = "", rws3 = "", f1 = "";
        public string rwas = "";
        // Find the sleep points and the wakeup points
        public Task<bool> CalcSleepTimesRealTime(SleepTimeParameter parameters, int count = 0)
        {
            List<int> awakeF1 = new List<int>(); // median awake over last x time
            List<int> sleepF1 = new List<int>(); // median sleep over last x time
            List<int> wakeUpF1 = new List<int>(); // median sleep over nect x times 
            List<int> diffSleepF1 = new List<int>(); // Median sleep diff between sleep and wakeup
            List<int> diffSleepF2 = new List<int>(); // Future Median sleep diff between sleep and wakeup
            List<int> diffAwakeF1 = new List<int>(); // Median sleep diff between awake and wakeup

            TimeSpan awakeTime = TimeSpan.FromSeconds(parameters.awakeTime);
            TimeSpan sleepTime = TimeSpan.FromSeconds(parameters.sleepTime);
            TimeSpan wakeUpTime = TimeSpan.FromSeconds(parameters.wakeUpTime);

            int sleepSleep = (int)parameters.sleepSleepBorder, sleepAwake = (int)parameters.awakeSleepBorder;
            int motionSleep = (int)parameters.sleepMotionBorder, sleep = (int)parameters.sleepMedianOverTime, diffSleep = (int)parameters.diffSleep, diffSleepFuture = (int)parameters.diffSleepFuture;
            int awake = (int)parameters.awakeMedianOverTime, diffAwake = (int)parameters.diffAwake;
            int motionAwake = (int)parameters.awakeMotionBorder;

            List<int> sleepPoint = new List<int>();
            List<int> wakeupPoint = new List<int>();

            for (int i = 0; i < sleepDataEntrieSleepTime.Count; i++)
            {
                DateTime actualTime = sleepDataEntrieSleepTime[i].time;

                var list = sleepDataEntrieSleepTime.Where(x => x.time >= actualTime && x.time < actualTime.Add(awakeTime)).ToList();
                awakeF1.Add(list.Sum(x => x.sleep) / list.Count);

                list = sleepDataEntrieSleepTime.Where(x => x.time >= actualTime && x.time < actualTime.Add(sleepTime)).ToList();
                sleepF1.Add(list.Sum(x => x.sleep) / list.Count);


                list = sleepDataEntrieSleepTime.Where(x => x.time <= actualTime && x.time > actualTime.Subtract(wakeUpTime)).ToList();
                wakeUpF1.Add(list.Sum(x => x.sleep) / list.Count);


                if (i != 0)
                {
                    diffAwakeF1.Add(awakeF1[i] - wakeUpF1[i - 1]);
                    diffSleepF1.Add(sleepF1[i] - wakeUpF1[i - 1]);
                }
                else
                {
                    diffAwakeF1.Add(0);
                    diffSleepF1.Add(0);
                    
                }


            }

            for (int i = 0; i < sleepDataEntrieSleepTime.Count; i++)
            {
                if (sleepDataEntrieSleepTime.Count < i + 8)
                {
                    diffSleepF2.Add(0);
                }
                else
                {
                    diffSleepF2.Add(awakeF1[i + 5] - wakeUpF1[i + 4]);
                }
            }

            // set sleep points for each
            for (int i = 0; i < diffSleepF1.Count; i++)
            {
                // Check if fall asleep
                if (sleepPoint.Count <= wakeupPoint.Count && sleepDataEntrieSleepTime[i].sleep > sleepSleep && sleepDataEntrieSleepTime[i].motion < motionSleep && sleepF1[i] > sleep && diffSleepF1[i] > diffSleep && diffSleepF2[i] > diffSleepFuture)
                {
                    sleepPoint.Add(i);
                }
                else if (sleepPoint.Count > wakeupPoint.Count && sleepDataEntrieSleepTime[i].sleep < sleepAwake && awakeF1[i] < awake && diffAwakeF1[i] < diffAwake && sleepDataEntrieSleepTime[i].motion > motionAwake)
                {
                    wakeupPoint.Add(i);
                }
            }

            if (count == 0)
            {
                sleepDataEntrieSleep = new Dictionary<int, List<SleepDataEntry>>();
                sleepDataEntrieAwake = new Dictionary<int, List<SleepDataEntry>>();
            }

            sleepDataEntrieSleep.Add(count, new List<SleepDataEntry>());
            sleepDataEntrieAwake.Add(count, new List<SleepDataEntry>());

            bool sleeping = false;

            for (int i = 0; i < sleepDataEntrieSleepTime.Count; i++)
            {
                if (sleepPoint.Contains(i))
                {
                    if (!sleeping)
                    {
                        times++;
                    }

                    sleeping = true;
                }
                else if (wakeupPoint.Contains(i))
                {
                    sleeping = false;
                }

                if (sleeping)
                {
                    foundSleep = true;
                    sleepDataEntrieSleep[count].Add(sleepDataEntrieSleepTime[i]);
                }
                else
                {
                    sleepDataEntrieAwake[count].Add(sleepDataEntrieSleepTime[i]);
                }
            }

            return Task.FromResult(true);

        }

        public Task<bool> CalcSleepTimesRealTimeForModel(SleepTimeParameter parameters)
        {
            List<int> awakeF1 = new List<int>(); // median awake over last x time
            List<int> sleepF1 = new List<int>(); // median sleep over last x time
            List<int> wakeUpF1 = new List<int>(); // median sleep over nect x times 
            List<int> diffSleepF1 = new List<int>(); // Median sleep diff between sleep and wakeup
            List<int> diffSleepF2 = new List<int>(); // Future Median sleep diff between sleep and wakeup
            List<int> diffAwakeF1 = new List<int>(); // Median sleep diff between awake and wakeup

            TimeSpan awakeTime = TimeSpan.FromSeconds(parameters.awakeTime);
            TimeSpan sleepTime = TimeSpan.FromSeconds(parameters.sleepTime);
            TimeSpan wakeUpTime = TimeSpan.FromSeconds(parameters.wakeUpTime);

            int sleepSleep = (int)parameters.sleepSleepBorder, sleepAwake = (int)parameters.awakeSleepBorder;
            int motionSleep = (int)parameters.sleepMotionBorder, sleep = (int)parameters.sleepMedianOverTime, diffSleep = (int)parameters.diffSleep, diffSleepFuture = (int)parameters.diffSleepFuture;
            int awake = (int)parameters.awakeMedianOverTime, diffAwake = (int)parameters.diffAwake;
            int motionAwake = (int)parameters.awakeMotionBorder;

            List<int> sleepPoint = new List<int>();
            List<int> wakeupPoint = new List<int>();

            for (int i = 0; i < sleepDataEntrieSleepTimeAll.Count; i++)
            {
                DateTime actualTime = sleepDataEntrieSleepTimeAll[i].time;

                var list = sleepDataEntrieSleepTimeAll.Where(x => x.time >= actualTime && x.time < actualTime.Add(awakeTime)).ToList();
                awakeF1.Add(list.Sum(x => x.sleep) / list.Count);

                list = sleepDataEntrieSleepTimeAll.Where(x => x.time >= actualTime && x.time < actualTime.Add(sleepTime)).ToList();
                sleepF1.Add(list.Sum(x => x.sleep) / list.Count);


                list = sleepDataEntrieSleepTimeAll.Where(x => x.time <= actualTime && x.time > actualTime.Subtract(wakeUpTime)).ToList();
                wakeUpF1.Add(list.Sum(x => x.sleep) / list.Count);


                if (i != 0)
                {
                    diffAwakeF1.Add(awakeF1[i] - wakeUpF1[i - 1]);
                    diffSleepF1.Add(sleepF1[i] - wakeUpF1[i - 1]);
                }
                else
                {
                    diffAwakeF1.Add(0);
                    diffSleepF1.Add(0);

                }


            }

            for (int i = 0; i < sleepDataEntrieSleepTimeAll.Count; i++)
            {
                if (sleepDataEntrieSleepTimeAll.Count < i + 8)
                {
                    diffSleepF2.Add(0);
                }
                else
                {
                    diffSleepF2.Add(awakeF1[i + 5] - wakeUpF1[i + 4]);
                }
            }

            // set sleep points for each
            for (int i = 0; i < diffSleepF1.Count; i++)
            {
                // Check if fall asleep
                if (sleepPoint.Count <= wakeupPoint.Count && sleepDataEntrieSleepTimeAll[i].sleep > sleepSleep && sleepDataEntrieSleepTimeAll[i].motion < motionSleep && sleepF1[i] > sleep && diffSleepF1[i] > diffSleep && diffSleepF2[i] > diffSleepFuture)
                {
                    sleepPoint.Add(i);
                }
                else if (sleepPoint.Count > wakeupPoint.Count && sleepDataEntrieSleepTimeAll[i].sleep < sleepAwake && awakeF1[i] < awake && diffAwakeF1[i] < diffAwake && sleepDataEntrieSleepTimeAll[i].motion > motionAwake)
                {
                    wakeupPoint.Add(i);
                }
            }


            modelSleepDataEntrieSleep = new List<SleepDataEntry>();
            modelSleepDataEntrieAwake = new List<SleepDataEntry>();


            bool sleeping = false;

            for (int i = 0; i < sleepDataEntrieSleepTimeAll.Count; i++)
            {
                if (sleepPoint.Contains(i))
                {
                    if (!sleeping)
                    {
                        //times++;
                    }

                    sleeping = true;
                }
                else if (wakeupPoint.Contains(i))
                {
                    sleeping = false;
                }

                if (sleeping)
                {
                    foundSleep = true;
                    modelSleepDataEntrieSleep.Add(sleepDataEntrieSleepTimeAll[i]);
                }
                else
                {
                    modelSleepDataEntrieAwake.Add(sleepDataEntrieSleepTimeAll[i]);
                }
            }

            return Task.FromResult(true);

        }


        public async Task<bool> CalcData()
        {
            
            try
            {
                if (sleepDataEntrieSleep[0].Count == 0)
                {

                }

                if (sleepDataEntrieSleep[0].Count != 0)
                {
                    dateTime = sleepDataEntrieSleep[0].Where(x => x.time != null).FirstOrDefault().time;

                    structureSleep = new Strukture();
                    await structureSleep.CalcData(sleepDataEntrieSleep[0]);
                }

                if (sleepDataEntrieAwake[0].Count != 0)
                {
                    dateTime = sleepDataEntrieAwake[0].Where(x => x.time != null).FirstOrDefault().time;

                    structureAwake = new Strukture();
                    await structureAwake.CalcData(sleepDataEntrieAwake[0]);
                }

                if (structureSleep != null && structureAwake != null)
                {
                    dateTime = sleepDataEntrieAwake[0].Where(x => x.time != null).FirstOrDefault().time;

                    diffrence = new Strukture();
                    await diffrence.CalcData(structureSleep, structureAwake);
                }
            }
            catch (Exception ex)
            {

            }

            try
            {



                var sleepList = modelSleepDataEntrieSleep.Where(x => x.time < modelSleepDataEntrieSleep[modelSleepDataEntrieSleep.Count()-1].time.AddHours(-1)).ToList();
                modelStructureSleep = new Strukture();
                await modelStructureSleep.CalcData(sleepList);




                var awakeList = modelSleepDataEntrieAwake.Where(x => x.time < modelSleepDataEntrieSleep[0].time && x.time > modelSleepDataEntrieSleep[0].time.AddHours(-2)).ToList();
                modelStructureAwake = new Strukture();
                await modelStructureAwake.CalcData(awakeList);


                if (modelStructureSleep != null && modelStructureAwake != null)
                {

                    modelDiffrence = new Strukture();
                    await modelDiffrence.CalcData(modelStructureSleep, modelStructureAwake);
                }
            }
            catch (Exception ex)
            {

            }


            return true;
        }

        public Task<bool> WriteCalcData(bool isWhile, string user)
        {
            if (sleepDataEntrieSleep == null || sleepDataEntrieSleep.Count == 0 || !sleepDataEntrieSleep.ContainsKey(0) ||sleepDataEntrieSleep[0].Count == 0)
            {
                return Task.FromResult(false);
            }

            if (sleepDataEntrieAwake == null || sleepDataEntrieAwake.Count == 0 || !sleepDataEntrieAwake.ContainsKey(0) ||  sleepDataEntrieAwake[0].Count == 0)
            {
                return Task.FromResult(false);
            }

            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet1 = isWhile ? (Worksheet)workbook.Worksheets["SleeptypesWhile"] : (Worksheet)workbook.Worksheets["SleeptypesAfter"];
            Worksheet worksheet2 = (Worksheet)workbook.Worksheets["SleepAnalyse"];

            Worksheet worksheet = (Worksheet)workbook.Worksheets[user];

            int off = isWhile ? 4 : 0;

            int[] realSleep = new int[5];
            int[] caclSleep = new int[5];
            int[] caclSleep1 = new int[5];

            bool isjust4 = false;

            foreach (var list in sleepDataEntrieSleep)
            {
                realSleep = new int[5];
                caclSleep = new int[5];
                caclSleep1 = new int[5];

                foreach (var item in list.Value)
                {
                   
                    // find if right or wrong
                    if (item == list.Value.FirstOrDefault())
                    {
                        var same = (int)CellHelper.GetCellValueFloat(item.row, 6, worksheet);
                        var before = (int)CellHelper.GetCellValueFloat(item.row-1, 6, worksheet);
                        var before1 = (int)CellHelper.GetCellValueFloat(item.row-2, 6, worksheet);
                        if (same == 0)
                        {
                            if (list.Key == 0)
                                rw12 = "4";
                            else
                                rw21 += "4";
                        }
                        if (before > 0 || before1 > 0)
                        {
                            if (list.Key == 0)
                                rw12 = "3";
                            else
                                rw21 += "3";
                        }

                    }
                    else if (item == list.Value.LastOrDefault())
                    {
                        var same = (int)CellHelper.GetCellValueFloat(item.row, 6, worksheet);
                        var after = (int)CellHelper.GetCellValueFloat(item.row + 1, 6, worksheet);
                        var after1 = (int)CellHelper.GetCellValueFloat(item.row + 2, 6, worksheet);
                        if (same ==0)
                        {
                            if (list.Key == 0)
                                rw11 = "1";
                            else
                                rw21 += "1";
                        }
                        if ((after > 0 || after1 > 0) && sleepDataEntrieSleepTime.LastOrDefault().time > item.time)
                        {
                            if (list.Key == 0)
                                rw11 = "2";
                            else
                                rw21 += "2";
                        }
                    }

                    if (isWhile)
                    {
                        var sl = (int)CellHelper.GetCellValueFloat(item.row, 6, worksheet);
                        realSleep[sl]++;

                        caclSleep[(int)item.calcSleepState[0]]++;
                        caclSleep1[(int)item.calcSleepState[1]]++;

                        if ((int)item.calcSleepState[0] != 4 && (int)item.calcSleepState[0] != 0)
                        {
                            caclSleep[4]++;
                        }

                        if ((int)item.calcSleepState[1] != 4 && (int)item.calcSleepState[1] != 0)
                        {
                            caclSleep1[4]++;
                        }


                        if ((int)item.realSleepState != 4 && (int)item.realSleepState != 0)
                        {
                            realSleep[4]++;
                        }

                        if (!isjust4 && (int)item.realSleepState == 4)
                        {
                            isjust4 = true;
                        }
                    }
                    

                    //ListHelp.CellHelper.WriteCellValue("Sleeping", item.row, DataSetup.dataSetPoints[DataPoints.Caculated], worksheet);
                    ListHelp.CellHelper.WriteCellValue("Sleeping", item.row, CellHelper.GetColumnName(7+list.Key + off) , worksheet);
                    ListHelp.CellHelper.WriteCellValue((int)item.calcSleepState[0], item.row, CellHelper.GetColumnName(15 + list.Key + off), worksheet);
                    if (item.issecond)
                    {
                        ListHelp.CellHelper.WriteCellValue((int)item.calcSleepState[1], item.row, CellHelper.GetColumnName(16 + list.Key + off), worksheet);
                    }
                }
            }

            foreach (var list in sleepDataEntrieAwake)
            {
                foreach (var item in list.Value)
                {
                    ListHelp.CellHelper.WriteCellValue("", item.row, CellHelper.GetColumnName(7 + list.Key + off), worksheet);
                   // ListHelp.CellHelper.WriteCellValue("", item.row, CellHelper.GetColumnName(15 + list.Key + off), worksheet);
                }
            }



            var row = sleepDataEntrieSleep[0].FirstOrDefault().row;

            ListHelp.CellHelper.WriteCellValue(row.ToString(), actualRow, "A", worksheet1);
            ListHelp.CellHelper.WriteCellValue(user, actualRow, "B", worksheet1);
            ListHelp.CellHelper.WriteCellValue(rw11, actualRow, "AX", worksheet1);
            ListHelp.CellHelper.WriteCellValue(rw12, actualRow, "AY", worksheet1);
            ListHelp.CellHelper.WriteCellValue((rw11 != "" || rw12 != "") ? rw2:"", actualRow, "AZ", worksheet1);
            ListHelp.CellHelper.WriteCellValue((rw11 != "" || rw12 != "") ? rw21:"", actualRow, "BC", worksheet1);
            ListHelp.CellHelper.WriteCellValue(nf1, actualRow, "BA", worksheet1);
            ListHelp.CellHelper.WriteCellValue(nf2, actualRow, "BB", worksheet1);

            if (isWhile)
            {

                ListHelp.CellHelper.WriteCellValue(row.ToString(), actualRow, "A", worksheet2);
                ListHelp.CellHelper.WriteCellValue(user, actualRow, "B", worksheet2);

                if (realSleep[0] < caclSleep[0])
                {
                    rws1 = "5";
                }
                else if (realSleep[0] > caclSleep[0])
                {
                    rws1 = "1";
                }

                if (realSleep[2] < caclSleep[2] && !isjust4)
                {
                    rws2 = "3";
                }
                else if (realSleep[2] > caclSleep[2] && !isjust4)
                {
                    rws2 = "7";
                }

                if (realSleep[3] < caclSleep[3] && !isjust4)
                {
                    rws3 = "4";
                }
                else if (realSleep[3] > caclSleep[3] && !isjust4)
                {
                    rws3 = "8";
                }

                if (realSleep[0] < caclSleep1[0])
                {
                    rwas += "5";
                }
                else if (realSleep[0] > caclSleep1[0])
                {
                    rwas += "1";
                }

                if (realSleep[2] < caclSleep1[2] && !isjust4)
                {
                    rwas += "3";
                }
                else if (realSleep[2] > caclSleep1[2] && !isjust4)
                {
                    rwas += "7";
                }

                if (realSleep[3] < caclSleep1[3] && !isjust4)
                {
                    rwas += "4";
                }
                else if (realSleep[3] > caclSleep1[3] && !isjust4)
                {
                    rwas += "8";
                }


                ListHelp.CellHelper.WriteCellValue(rws1, actualRow, "V", worksheet2);
                ListHelp.CellHelper.WriteCellValue(rws2, actualRow, "W", worksheet2);
                ListHelp.CellHelper.WriteCellValue(rws3, actualRow, "X", worksheet2);
                ListHelp.CellHelper.WriteCellValue(f1, actualRow, "Y", worksheet2);
                ListHelp.CellHelper.WriteCellValue(rwas, actualRow, "Z", worksheet2);

                for (int i = 0; i < 5; i++)
                {
                    ListHelp.CellHelper.WriteCellValue(realSleep[i], actualRow, ListHelp.CellHelper.GetColumnName(i * 2 + 3), worksheet2);
                    ListHelp.CellHelper.WriteCellValue(caclSleep[i], actualRow, ListHelp.CellHelper.GetColumnName(i * 2 + 4), worksheet2);

                    if (isjust4 && i > 0)
                    {
                        ListHelp.CellHelper.WriteCellValue(0, actualRow, ListHelp.CellHelper.GetColumnName(i + 13), worksheet2);
                        continue;
                    }

                    ListHelp.CellHelper.WriteCellValue(realSleep[i] - caclSleep[i], actualRow, ListHelp.CellHelper.GetColumnName(i + 13), worksheet2);

                }
            }



            modelDiffrence?.WriteData(actualRow, worksheet1, 10);
            modelStructureSleep?.WriteData(actualRow, worksheet1, 5);
            modelStructureAwake?.WriteData(actualRow, worksheet1, 0);
            actualRow++;
            return Task.FromResult(true);
        }


        public Task<bool> CalcSleepStatesWhileSleep(SleepStateParameter parameters, int index1)
        {
           

            if (sleepDataEntrieSleep == null || sleepDataEntrieSleep.Count == 0)
            {
                return Task.FromResult(false);
            }


            Drittel actualParam = parameters.first;
            int index = sleepDataEntrieSleep.Count > 1 ? 1 : 0;

            for (int i = 0; i < sleepDataEntrieSleep[index].Count; i++)
            {
                if (index1 == 1)
                {
                    sleepDataEntrieSleep[index][i].issecond = true;
                }

                // is awake?
                if (sleepDataEntrieSleep[index][i].sleep <= actualParam.sleepSleepBorder ||
                    sleepDataEntrieSleep[index][i].motion >= actualParam.sleepMotionBorder ||
                    sleepDataEntrieSleep[index][i].light >= actualParam.sleepLightBorder)
                {

                    if (sleepDataEntrieSleep[index][i].sleep <= actualParam.soundClearSleepBorder ||
                    sleepDataEntrieSleep[index][i].motion <= actualParam.soundClearMotionBorder)
                    {
                        sleepDataEntrieSleep[index][i].calcSleepState[index1] = SleepState.light;
                    }
                    else
                    {
                        sleepDataEntrieSleep[index][i].calcSleepState[index1] = SleepState.awake;
                    }
                }
                // is normal?
                else if (sleepDataEntrieSleep[index][i].sleep <= actualParam.deepSleepSleepBorder ||
                   sleepDataEntrieSleep[index][i].motion >= actualParam.deepSleepMotionBorder ||
                   sleepDataEntrieSleep[index][i].light >= actualParam.deepSleepLightBorder)
                {
                    sleepDataEntrieSleep[index][i].calcSleepState[index1] = SleepState.light;
                }
                // is deep
                else if (sleepDataEntrieSleep[index][i].sleep <= actualParam.remSleepSleepBorder ||
                  sleepDataEntrieSleep[index][i].motion >= actualParam.remSleepMotionBorder ||
                  sleepDataEntrieSleep[index][i].light >= actualParam.remSleepLightBorder)
                {
                    sleepDataEntrieSleep[index][i].calcSleepState[index1] = SleepState.deep;
                }
                // is rem
                else
                {
                    sleepDataEntrieSleep[index][i].calcSleepState[index1] = SleepState.rem;
                }
            }


            return Task.FromResult(true);
        }

    }

    public class MaxMinHelper
    {

        public Dictionary<MaxMinHelperType, float> maxmintype = new Dictionary<MaxMinHelperType, float>();

        //public float Median;
        //public float Average;
        //public float Factor;

        public void WriteData(int row, int column, Worksheet worksheet)
        {
            // ListHelp.CellHelper.WriteCellValue((Max).ToString(), row, CellHelper.GetColumnName(column), worksheet);
            // ListHelp.CellHelper.WriteCellValue((Min).ToString(), row, "L", worksheet);
            if (maxmintype.ContainsKey(MaxMinHelperType.Median))
            {
                ListHelp.CellHelper.WriteCellValue((maxmintype[MaxMinHelperType.Median]), row, CellHelper.GetColumnName(column), worksheet);

            }
            if (maxmintype.ContainsKey(MaxMinHelperType.Max))
            {
                ListHelp.CellHelper.WriteCellValue((maxmintype[MaxMinHelperType.Max]), row, CellHelper.GetColumnName(column+1), worksheet);

            }
            if (maxmintype.ContainsKey(MaxMinHelperType.Min))
            {
                ListHelp.CellHelper.WriteCellValue((maxmintype[MaxMinHelperType.Min]), row, CellHelper.GetColumnName(column+2), worksheet);

            }
            if (maxmintype.ContainsKey(MaxMinHelperType.Average))
            {
                ListHelp.CellHelper.WriteCellValue((maxmintype[MaxMinHelperType.Average]), row, CellHelper.GetColumnName(column + 3), worksheet);

            }
            if (maxmintype.ContainsKey(MaxMinHelperType.Factor))
            {
                ListHelp.CellHelper.WriteCellValue((maxmintype[MaxMinHelperType.Factor]), row, CellHelper.GetColumnName(column + 4), worksheet);

            }


        }


        public DataSetter getMaxValues()
        {
            DataSetter ds = new DataSetter();

            ds.Average = maxmintype[MaxMinHelperType.Average];
            ds.Factor = maxmintype[MaxMinHelperType.Factor];
            ds.Max = maxmintype[MaxMinHelperType.Max]; 
            ds.Min = maxmintype[MaxMinHelperType.Min]; 
            ds.Median = maxmintype[MaxMinHelperType.Median];

            return ds;
        }

        public static MaxMinHelper GetMax()
        {
            return new MaxMinHelper()
            {
                maxmintype = new Dictionary<MaxMinHelperType, float>()
                {
                    { MaxMinHelperType.Average, 1000 },
                    { MaxMinHelperType.Median, 1000 },
                    { MaxMinHelperType.Factor, 1000 },
                    { MaxMinHelperType.Max, 1000 },
                    { MaxMinHelperType.Min, 1000 }

                }

            };
        }

        public static MaxMinHelper GetMin()
        {
            return new MaxMinHelper()
            {
                maxmintype = new Dictionary<MaxMinHelperType, float>()
                {
                    { MaxMinHelperType.Average, -1000 },
                    { MaxMinHelperType.Median, -1000 },
                    { MaxMinHelperType.Factor, -1000 },
                    { MaxMinHelperType.Max, -1000 },
                    { MaxMinHelperType.Min, -1000 }

                }
            };
        }
    }

    public enum MaxMinHelperType
    {
        Median,
        Average,
        Factor,
        Max,
        Min
    }

    public enum MobilePhonePlace
    {
        inBed,
        onTable,
        unidentified
    }

    public enum SleepTime
    {
        onDay,
        inNight
    }

    public enum Shutters
    {
        closed,
        open
    }

}