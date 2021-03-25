using ExcelCalculationAddin.ListHelp;
using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepClean;

namespace ExcelCalculationAddin.Model
{
    public class SleepCleanModel
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

        public bool CheckIfIsTypeModel(Strukture awake, Strukture sleep, Strukture diff)
        {
            // es muss gecheckt werden ob alles in den bounds ist
            bool isAType = true;

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

                        }
                        else
                        {
                            isAType = false;
                            return false;
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

                        }
                        else
                        {
                            isAType = false;
                            return false;
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

                        }
                        else
                        {
                            isAType = false;
                            return false;
                        }
                    }

                }
            }
            catch (Exception)
            {

                throw;
            }


           


            return true;
        }


        public static Dictionary<SleepCleanType, SleepCleanModel> CreateAllModels()
        {
            Dictionary<SleepCleanType, SleepCleanModel> asss = new Dictionary<SleepCleanType, SleepCleanModel>();


            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet1 = (Worksheet)workbook.Worksheets["Sleeptypes"];



            int finde = CellHelper.ExcelColumnNameToNumber("AJ");
            int offWach = 1, offSleep = 4, offDiff = 7;

            for (int i = 4; i < 200; i+=12)
            {
                string value = CellHelper.GetCellValue(i, finde, worksheet1);
                if (value == null)
                {
                    break; 
                }

                SleepCleanType cleanModelType;
                try
                {
                    cleanModelType = (SleepCleanType)Convert.ToInt32(value);

                    asss.Add(cleanModelType, new SleepCleanModel());
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
