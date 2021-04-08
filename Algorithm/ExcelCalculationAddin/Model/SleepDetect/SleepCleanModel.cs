using ExcelCalculationAddin.ListHelp;
using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepTimeClean;
using static ExcelCalculationAddin.Model.SleepType;

namespace ExcelCalculationAddin.Model
{
    public class SleepTimeModel
    {

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

        public bool CheckIfIsTypeModel(SleepTimeParameter sleepParam, Strukture awake, Strukture sleep, Strukture diff)
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

            if ((isright * 100) / amount > sleepParam.modelMatchPercentage)
            {
                return true;
            }
           


            return false;
        }


        public static Dictionary<SleepTimeCleanType, SleepTimeModel> CreateAllModels(bool isWhile)
        {
            Dictionary<SleepTimeCleanType, SleepTimeModel> asss = new Dictionary<SleepTimeCleanType, SleepTimeModel>();


            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet1 = isWhile ? (Worksheet)workbook.Worksheets["SleeptypesWhile"] : (Worksheet)workbook.Worksheets["SleeptypesAfter"];



            int finde = CellHelper.ExcelColumnNameToNumber("AN");
            int offWach = 1, offSleep = 4, offDiff = 7;

            for (int i = 4; i < 200; i+=12)
            {
                string value = CellHelper.GetCellValue(i, finde, worksheet1);
                if (value == null)
                {
                    break; 
                }

                SleepTimeCleanType cleanModelType;
                try
                {
                    cleanModelType = (SleepTimeCleanType)Convert.ToInt32(value);

                    asss.Add(cleanModelType, new SleepTimeModel());
                }
                catch (Exception)
                {

                    continue;
                }

                foreach (var item in asss[cleanModelType].valuesWach)
                {
                    for (int k = 0; k < 3; k++)
                    {
                        float vv = CellHelper.GetCellValueFloat(i+k+ offWach, finde+1+(int)item.Key, worksheet1);
                        asss[cleanModelType].valuesWach[item.Key].maxmintype[(MaxMinHelperType)k] = (float)vv;
                    }

                    for (int k = 0; k < 3; k++)
                    {
                        float vv = CellHelper.GetCellValueFloat(i + k + offSleep, finde + 1 + (int)item.Key, worksheet1);
                        asss[cleanModelType].valuesSleep[item.Key].maxmintype[(MaxMinHelperType)k] = (float)vv;
                    }

                    for (int k = 0; k < 3; k++)
                    {
                        float vv = CellHelper.GetCellValueFloat(i + k + offDiff, finde + 1 + (int)item.Key, worksheet1);
                        asss[cleanModelType].valuesDiff[item.Key].maxmintype[(MaxMinHelperType)k] = (float)vv;
                    }
                }
            }

            return asss;
        }

        public enum SleepCleanModelType
        {
            MaxSchlaf,
            MinSchlaf,
            MaxLicht,
            MinLicht,
            MaxMotion,
            MinMotion,
            Schlaf,
            Licht,
            Motion
                
        }

    }
}
