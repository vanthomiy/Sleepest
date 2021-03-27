using ExcelCalculationAddin.Model;
using ExcelCalculationAddin.Read;
using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Parameters = ExcelCalculationAddin.Model.Parameters;

namespace ExcelCalculationAddin
{
    class ReadParameter
    {
        public static List<User> values;
        public static List<SleepType> sleepTypes;
        public static AlarmSettings alarmSetttings;
        public static Parameters parameters;

        public async static Task<bool> GetAllUserData()
        {
            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;

            values = new List<User>();


            foreach (Worksheet worksheet in workbook.Worksheets)
            {

                if (worksheet.Name == "Berechnung" || worksheet.Name == "WeckerParameter" || worksheet.Name == "SleeptypesWhile" || worksheet.Name == "SleeptypesAfter")
                {
                    continue;
                }

                User user = new User();
                user.sheetname = worksheet.Name;
                user.allSleepData = new List<SleepDataEntry>();

                int count = ListHelp.SheetParams.GetRowsCount(worksheet);

                for (int i = 2; i < count; i++)
                {
                    SleepDataEntry sde = new SleepDataEntry();

                    foreach (var set in DataSetup.dataSetPoints)
                    {
                        var value = ListHelp.CellHelper.GetCellValue(i, set.Value, worksheet);
                        if (value == null)
                        {
                            continue;
                        }
                        sde.row = i;
                        await sde.AddValue(value, set.Key);
                    }

                    user.allSleepData.Add(sde);
                }

                await user.CreateSleepSessionsAfter();
                await user.CreateSleepSessionsWhile();

                values.Add(user);
            }

            return true;
        }

        public static void ReadSleepTypeParameter()
        {
            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet = (Worksheet)workbook.Worksheets["Berechnung"];

            parameters = new Parameters();

            foreach (var set in DataSetup.paramsSetPoints)
            {
                var value = ListHelp.CellHelper.GetCellValue(set.Value, "C", worksheet);
                if (value == null)
                {
                    continue;
                }

                //parameters.AddValue(value, set.Key);
            }
        }

        public static void GetAlarmSettings()
        {
            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet = (Worksheet)workbook.Worksheets["WeckerParameter"];

            alarmSetttings = new AlarmSettings();

            foreach (var set in DataSetup.alarmSetPoints)
            {
                var value = ListHelp.CellHelper.GetCellValue(set.Value, "C" , worksheet);
                if (value == null)
                {
                    continue;
                }

                alarmSetttings.AddValue(value, set.Key);
            }
        }
    }
}
