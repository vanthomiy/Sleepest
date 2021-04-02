using ExcelCalculationAddin.ListHelp;
using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepClean;
using static ExcelCalculationAddin.Model.SleepType;

namespace ExcelCalculationAddin.Model
{
    public class SleepParameter
    {
        public TimeSpan awakeTime; // wie lange soll die awake zeit gezählt werden (Zukunft)
        public TimeSpan sleepTime; // wie lange sollen vergangenheitswerte für einschlaf gezählt werden
        public TimeSpan wakeUpTime; // wie lange sollen vergangenheitswerte für aufwachen gezählt werden

        public float sleepSleepBorder; // grenze zum einschlafen
        public float awakeSleepBorder; // grenze zum aufwachen
        public float sleepMotionBorder; // grenze zum einschlafen
        public float awakeMotionBorder; // grenze zum aufwachen
        public float sleepMedianOverTime; // grenze welche der median für einschlafen haben sollte
        public float diffSleep; // grenze welche die diff im median für einschlafen haben sollte
        public float diffSleepFuture;  // grenze welche die diff im median für einschlafen in zukunft haben sollte
        public float awakeMedianOverTime; // grenze welche der median für aufwachen haben sollte
        public float diffAwake; // grenze welche die diff im median für aufwachen haben sollte

        public float modelMatchPercentage;

        public static SleepParameter AddFactorToParameter(SleepParameter normal, SleepParameter factor)
        {
            SleepParameter pm = SleepParameter.GetDefault();


            pm.sleepSleepBorder = normal.sleepSleepBorder * factor.sleepSleepBorder;
            pm.awakeSleepBorder = normal.awakeSleepBorder * factor.awakeSleepBorder;
            pm.sleepMotionBorder = normal.sleepMotionBorder * factor.sleepMotionBorder;
            pm.awakeMotionBorder = normal.awakeMotionBorder * factor.awakeMotionBorder;
            pm.sleepMedianOverTime = normal.sleepMedianOverTime * factor.sleepMedianOverTime;
            pm.diffSleep = normal.diffSleep * factor.diffSleep;
            pm.diffSleepFuture = normal.diffSleepFuture * factor.diffSleepFuture;
            pm.awakeMedianOverTime = normal.awakeMedianOverTime * factor.awakeMedianOverTime;
            pm.diffAwake = normal.diffAwake * factor.diffAwake;

            return pm;
        }

        public static SleepParameter GetDefault()
        {
            return new SleepParameter()
            {
                awakeTime = new TimeSpan(00, 30, 00),
                sleepTime = new TimeSpan(00, 50, 00),
                wakeUpTime = new TimeSpan(01, 30, 00),

                sleepSleepBorder = 50,
                awakeSleepBorder = 20,
                sleepMotionBorder = 4,
                awakeMotionBorder = 0,
                sleepMedianOverTime = 75,
                diffSleep = 50,
                diffSleepFuture = 0,
                awakeMedianOverTime = 30,
                diffAwake = -5,
                modelMatchPercentage = 95
            };
        }

        public static SleepParameter GetDefaultFactor()
        {
            return new SleepParameter()
            {
                awakeTime = new TimeSpan(0),
                sleepTime = new TimeSpan(0),
                wakeUpTime = new TimeSpan(0),

                sleepSleepBorder = 1,
                awakeSleepBorder = 1,
                sleepMotionBorder = 1,
                awakeMotionBorder = 1,
                sleepMedianOverTime = 1,
                diffSleep = 1,
                diffSleepFuture = 1,
                awakeMedianOverTime = 1,
                diffAwake = 1,
                modelMatchPercentage = 1
            };
        }

        public static Dictionary<SleepCleanType, SleepParameter> CreateAllModels(bool isWhile)
        {
            Dictionary<SleepCleanType, SleepParameter> asss = new Dictionary<SleepCleanType, SleepParameter>();


            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet1 = isWhile ? (Worksheet)workbook.Worksheets["SleeptypesWhile"] : (Worksheet)workbook.Worksheets["SleeptypesAfter"];



            int finde = CellHelper.ExcelColumnNameToNumber("AV");

            bool available = true;
            while (available)
            {
                string value = CellHelper.GetCellValue(4, finde, worksheet1);
                if (value == null)
                {
                    available = false;
                    break;
                }

                var sp = SleepParameter.GetDefault();

                var fValue = (int)CellHelper.GetCellValueFloat(8, finde, worksheet1);
                sp.sleepSleepBorder = fValue != 0 ? fValue: sp.sleepSleepBorder;
                fValue = (int)CellHelper.GetCellValueFloat(9, finde, worksheet1);
                sp.awakeSleepBorder = fValue != 0 ? fValue : sp.awakeSleepBorder;
                fValue = (int)CellHelper.GetCellValueFloat(10, finde, worksheet1);
                sp.sleepMotionBorder = fValue != 0 ? fValue : sp.sleepMotionBorder;
                fValue = (int)CellHelper.GetCellValueFloat(11, finde, worksheet1);
                sp.awakeMotionBorder = fValue != 0 ? fValue : sp.awakeMotionBorder;
                fValue = (int)CellHelper.GetCellValueFloat(12, finde, worksheet1);
                sp.sleepMedianOverTime = fValue != 0 ? fValue : sp.sleepMedianOverTime;
                fValue = (int)CellHelper.GetCellValueFloat(13, finde, worksheet1);
                sp.awakeMedianOverTime = fValue != 0 ? fValue : sp.awakeMedianOverTime;
                fValue = (int)CellHelper.GetCellValueFloat(14, finde, worksheet1);
                sp.diffSleep = fValue != 0 ? fValue : sp.diffSleep;
                fValue = (int)CellHelper.GetCellValueFloat(15, finde, worksheet1);
                sp.diffSleepFuture = fValue != 0 ? fValue : sp.diffSleepFuture;
                fValue = (int)CellHelper.GetCellValueFloat(16, finde, worksheet1);
                sp.diffAwake = fValue != 0 ? fValue : sp.diffAwake;

                asss.Add((SleepCleanType)Convert.ToInt32(value), sp);
                finde++;
            }

            return asss;
        }

        public static Dictionary<SleepUserType, SleepParameter> CreateAllFactorModels(bool isWhile)
        {
            Dictionary<SleepUserType, SleepParameter> asss = new Dictionary<SleepUserType, SleepParameter>();

            asss.Add(SleepUserType.standard, SleepParameter.GetDefaultFactor());

            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet1 = isWhile ? (Worksheet)workbook.Worksheets["SleeptypesWhile"] : (Worksheet)workbook.Worksheets["SleeptypesAfter"];



            int finde = CellHelper.ExcelColumnNameToNumber("AX");

            bool available = true;
            while (available)
            {
                string value = CellHelper.GetCellValue(20, finde, worksheet1);
                if (value == null)
                {
                    available = false;
                    break;
                }

                var sp = SleepParameter.GetDefaultFactor();

                var fValue = CellHelper.GetCellValueFloat(24, finde, worksheet1);
                sp.sleepSleepBorder = fValue != 0 ? fValue : sp.sleepSleepBorder;
                fValue = CellHelper.GetCellValueFloat(25, finde, worksheet1);
                sp.awakeSleepBorder = fValue != 0 ? fValue : sp.awakeSleepBorder;
                fValue = CellHelper.GetCellValueFloat(26, finde, worksheet1);
                sp.sleepMotionBorder = fValue != 0 ? fValue : sp.sleepMotionBorder;
                fValue = CellHelper.GetCellValueFloat(27, finde, worksheet1);
                sp.awakeMotionBorder = fValue != 0 ? fValue : sp.awakeMotionBorder;
                fValue = CellHelper.GetCellValueFloat(28, finde, worksheet1);
                sp.sleepMedianOverTime = fValue != 0 ? fValue : sp.sleepMedianOverTime;
                fValue = CellHelper.GetCellValueFloat(29, finde, worksheet1);
                sp.awakeMedianOverTime = fValue != 0 ? fValue : sp.awakeMedianOverTime;
                fValue = CellHelper.GetCellValueFloat(30, finde, worksheet1);
                sp.diffSleep = fValue != 0 ? fValue : sp.diffSleep;
                fValue = CellHelper.GetCellValueFloat(31, finde, worksheet1);
                sp.diffSleepFuture = fValue != 0 ? fValue : sp.diffSleepFuture;
                fValue = CellHelper.GetCellValueFloat(32, finde, worksheet1);
                sp.diffAwake = fValue != 0 ? fValue : sp.diffAwake;

                asss.Add((SleepUserType)Convert.ToInt32(value), sp);
                finde+=2;
            }

            return asss;
        }


    }
}
