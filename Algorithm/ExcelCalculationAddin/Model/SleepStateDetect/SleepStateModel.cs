﻿using ExcelCalculationAddin.ListHelp;
using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepCleanModel;
using static ExcelCalculationAddin.Model.SleepStateDetect.SleepStateClean;
using static ExcelCalculationAddin.Model.SleepType;

namespace ExcelCalculationAddin.Model.SleepStateDetect
{
    public class SleepStateModel
    {
        public SleepStateType sleepStateType;
        public SleepStateCleanType sleepStateModel;

        public Dictionary<SleepCleanModelType, MaxMinHelper> valuesWach = new Dictionary<SleepCleanModelType, MaxMinHelper>() {
            { SleepCleanModelType.MaxLicht, new MaxMinHelper() },
            { SleepCleanModelType.MinLicht, new MaxMinHelper() },
            { SleepCleanModelType.MaxMotion, new MaxMinHelper() },
            { SleepCleanModelType.MinMotion, new MaxMinHelper() },
            { SleepCleanModelType.MaxSchlaf, new MaxMinHelper() },
            { SleepCleanModelType.MinSchlaf, new MaxMinHelper() },

            };
        public Dictionary<SleepCleanModelType, MaxMinHelper> valuesSleep = new Dictionary<SleepCleanModelType, MaxMinHelper>() {
            { SleepCleanModelType.MaxLicht, new MaxMinHelper() },
            { SleepCleanModelType.MinLicht, new MaxMinHelper() },
            { SleepCleanModelType.MaxMotion, new MaxMinHelper() },
            { SleepCleanModelType.MinMotion, new MaxMinHelper() },
            { SleepCleanModelType.MaxSchlaf, new MaxMinHelper() },
            { SleepCleanModelType.MinSchlaf, new MaxMinHelper() },

            };
        public Dictionary<SleepCleanModelType, MaxMinHelper> valuesDiff = new Dictionary<SleepCleanModelType, MaxMinHelper>() {
            { SleepCleanModelType.MaxLicht, new MaxMinHelper() },
            { SleepCleanModelType.MinLicht, new MaxMinHelper() },
            { SleepCleanModelType.MaxMotion, new MaxMinHelper() },
            { SleepCleanModelType.MinMotion, new MaxMinHelper() },
            { SleepCleanModelType.MaxSchlaf, new MaxMinHelper() },
            { SleepCleanModelType.MinSchlaf, new MaxMinHelper() },

            };

        public bool CheckIfIsTypeModel(SleepStateParameter sleepParam, Strukture awake, Strukture sleep, Strukture diff)
        {
            // es muss gecheckt werden ob alles in den bounds ist
            bool isAType = true;
            int isright = 0;
            int iswrong = 0;


            try
            {
                foreach (var item in awake.data)
                {
                    List<SleepCleanModelType> types = new List<SleepCleanModelType>();
                    if (item.Key == SleepCleanModelType.Schlaf)
                    {
                        types.Add(SleepCleanModelType.MaxSchlaf);
                        types.Add(SleepCleanModelType.MinSchlaf);
                    }
                    else if (item.Key == SleepCleanModelType.Motion)
                    {
                        types.Add(SleepCleanModelType.MaxMotion);
                        types.Add(SleepCleanModelType.MinMotion);
                    }
                    else
                    {
                        types.Add(SleepCleanModelType.MaxLicht);
                        types.Add(SleepCleanModelType.MinLicht);
                    }

                    foreach (var verlgiech in item.Value.maxmintype)
                    {
                        if (verlgiech.Value <= valuesWach[types[0]].maxmintype[verlgiech.Key] && verlgiech.Value >= valuesWach[types[1]].maxmintype[verlgiech.Key])
                        {
                            isright++;
                        }
                        else
                        {
                            //isAType = false;
                            //return false;
                            iswrong++;
                        }
                    }

                }

                foreach (var item in sleep.data)
                {
                    List<SleepCleanModelType> types = new List<SleepCleanModelType>();
                    if (item.Key == SleepCleanModelType.Schlaf)
                    {
                        types.Add(SleepCleanModelType.MaxSchlaf);
                        types.Add(SleepCleanModelType.MinSchlaf);
                    }
                    else if (item.Key == SleepCleanModelType.Motion)
                    {
                        types.Add(SleepCleanModelType.MaxMotion);
                        types.Add(SleepCleanModelType.MinMotion);
                    }
                    else
                    {
                        types.Add(SleepCleanModelType.MaxLicht);
                        types.Add(SleepCleanModelType.MinLicht);
                    }

                    foreach (var verlgiech in item.Value.maxmintype)
                    {
                        if (verlgiech.Value <= valuesSleep[types[0]].maxmintype[verlgiech.Key] && verlgiech.Value >= valuesSleep[types[1]].maxmintype[verlgiech.Key])
                        {
                            isright++;
                        }
                        else
                        {
                            //isAType = false;
                            //return false;
                            iswrong++;

                        }

                    }

                }

                foreach (var item in diff.data)
                {
                    List<SleepCleanModelType> types = new List<SleepCleanModelType>();
                    if (item.Key == SleepCleanModelType.Schlaf)
                    {
                        types.Add(SleepCleanModelType.MaxSchlaf);
                        types.Add(SleepCleanModelType.MinSchlaf);
                    }
                    else if (item.Key == SleepCleanModelType.Motion)
                    {
                        types.Add(SleepCleanModelType.MaxMotion);
                        types.Add(SleepCleanModelType.MinMotion);
                    }
                    else
                    {
                        types.Add(SleepCleanModelType.MaxLicht);
                        types.Add(SleepCleanModelType.MinLicht);
                    }

                    foreach (var verlgiech in item.Value.maxmintype)
                    {
                        if (verlgiech.Value <= valuesDiff[types[0]].maxmintype[verlgiech.Key] && verlgiech.Value >= valuesDiff[types[1]].maxmintype[verlgiech.Key])
                        {
                            isright++;

                        }
                        else
                        {
                            //isAType = false;
                            //return false;
                            iswrong++;

                        }
                    }

                }
            }
            catch (Exception)
            {

                throw;
            }


            int amount = isright + iswrong;

            if ((isright * 100) / amount > sleepParam.first.modelMatchPercentage)
            {
                return true;
            }



            return false;
        }


        public static Dictionary<string, SleepStateModel> CreateAllModels(bool isWhile)
        {
            Dictionary<string, SleepStateModel> asss = new Dictionary<string, SleepStateModel>();


            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet1 = (Worksheet)workbook.Worksheets["SleepAnalyse"];



            int finde = CellHelper.ExcelColumnNameToNumber("AE");
            int offWach = 1, offSleep = 4, offDiff = 7;

            string key = "";
            for (int i = 4; i < 200; i += 12)
            {

                string value = CellHelper.GetCellValue(i, finde, worksheet1);
                string value1 = CellHelper.GetCellValue(i - 1, finde, worksheet1);

                if (value == null || value1 == null)
                {
                    break;
                }

                try
                {
                    key = (value).ToString() + (value1).ToString();
                    asss.Add(key, new SleepStateModel());
                }
                catch (Exception)
                {

                    continue;
                }



                asss[key].sleepStateType = (SleepStateType)Convert.ToInt32(value1);
                asss[key].sleepStateModel = (SleepStateCleanType)Convert.ToInt32(value);
                foreach (var item in asss[key].valuesWach)
                {
                    for (int k = 0; k < 3; k++)
                    {
                        float vv = CellHelper.GetCellValueFloat(i + k + offWach, finde + 1 + (int)item.Key, worksheet1);
                        asss[key].valuesWach[item.Key].maxmintype[(MaxMinHelperType)k] = (float)vv;
                    }

                    for (int k = 0; k < 3; k++)
                    {
                        float vv = CellHelper.GetCellValueFloat(i + k + offSleep, finde + 1 + (int)item.Key, worksheet1);
                        asss[key].valuesSleep[item.Key].maxmintype[(MaxMinHelperType)k] = (float)vv;
                    }

                    for (int k = 0; k < 3; k++)
                    {
                        float vv = CellHelper.GetCellValueFloat(i + k + offDiff, finde + 1 + (int)item.Key, worksheet1);
                        asss[key].valuesDiff[item.Key].maxmintype[(MaxMinHelperType)k] = (float)vv;
                    }
                }
            }

            return asss;
        }
    }
}
