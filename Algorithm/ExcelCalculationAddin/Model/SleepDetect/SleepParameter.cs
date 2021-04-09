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
    public class SleepTimeParameter
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


        public static SleepTimeParameter Combine(List<SleepTimeParameter> ssp)
        {
            var paramdefault = SleepTimeParameter.GetDefault();
            var params2 = SleepTimeParameter.GetDefault();

            foreach (var item in ssp)
            {
                if (Math.Abs(item.sleepSleepBorder - paramdefault.sleepSleepBorder) > Math.Abs(params2.sleepSleepBorder - paramdefault.sleepSleepBorder))
                {
                    params2.sleepSleepBorder = item.sleepSleepBorder;
                }

                if (Math.Abs(item.awakeSleepBorder - paramdefault.awakeSleepBorder) > Math.Abs(params2.awakeSleepBorder - paramdefault.awakeSleepBorder))
                {
                    params2.awakeSleepBorder = item.awakeSleepBorder;
                }

                if (Math.Abs(item.sleepMotionBorder - paramdefault.sleepMotionBorder) > Math.Abs(params2.sleepMotionBorder - paramdefault.sleepMotionBorder))
                {
                    params2.sleepMotionBorder = item.sleepMotionBorder;
                }

                if (Math.Abs(item.sleepMotionBorder - paramdefault.sleepMotionBorder) > Math.Abs(params2.sleepMotionBorder - paramdefault.sleepMotionBorder))
                {
                    params2.sleepMotionBorder = item.sleepMotionBorder;
                }

                if (Math.Abs(item.awakeMotionBorder - paramdefault.awakeMotionBorder) > Math.Abs(params2.awakeMotionBorder - paramdefault.awakeMotionBorder))
                {
                    params2.awakeMotionBorder = item.awakeMotionBorder;
                }

                if (Math.Abs(item.sleepMedianOverTime - paramdefault.sleepMedianOverTime) > Math.Abs(params2.sleepMedianOverTime - paramdefault.sleepMedianOverTime))
                {
                    params2.sleepMedianOverTime = item.sleepMedianOverTime;
                }

                if (Math.Abs(item.diffSleep - paramdefault.diffSleep) > Math.Abs(params2.diffSleep - paramdefault.diffSleep))
                {
                    params2.diffSleep = item.diffSleep;
                }

                if (Math.Abs(item.diffSleepFuture - paramdefault.diffSleepFuture) > Math.Abs(params2.diffSleepFuture - paramdefault.diffSleepFuture))
                {
                    params2.diffSleepFuture = item.diffSleepFuture;
                }

                if (Math.Abs(item.awakeMedianOverTime - paramdefault.awakeMedianOverTime) > Math.Abs(params2.awakeMedianOverTime - paramdefault.awakeMedianOverTime))
                {
                    params2.awakeMedianOverTime = item.awakeMedianOverTime;
                }

                if (Math.Abs(item.diffAwake - paramdefault.diffAwake) > Math.Abs(params2.diffAwake - paramdefault.diffAwake))
                {
                    params2.diffAwake = item.diffAwake;
                }

            }

            return params2;
        }


        public static SleepTimeParameter AddFactorToParameter(SleepTimeParameter normal, SleepTimeParameter factor, bool isNormal)
        {
            SleepTimeParameter pm = SleepTimeParameter.GetDefault();


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

        public static SleepTimeParameter GetDefault()
        {
            return new SleepTimeParameter()
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

        public static SleepTimeParameter GetDefaultFactor()
        {
            return new SleepTimeParameter()
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

        public static Dictionary<SleepTimeCleanType, SleepTimeParameter> CreateAllModels(bool isWhile)
        {
            Dictionary<SleepTimeCleanType, SleepTimeParameter> asss = new Dictionary<SleepTimeCleanType, SleepTimeParameter>();


            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet1 = isWhile ? (Worksheet)workbook.Worksheets["SleeptypesWhile"] : (Worksheet)workbook.Worksheets["SleeptypesAfter"];



            int finde = CellHelper.ExcelColumnNameToNumber("AW");

            bool available = true;
            while (available)
            {
                string value = CellHelper.GetCellValue(4, finde, worksheet1);
                if (value == null)
                {
                    available = false;
                    break;
                }

                var sp = SleepTimeParameter.GetDefault();

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

                asss.Add((SleepTimeCleanType)Convert.ToInt32(value), sp);
                finde++;
            }

            return asss;
        }

        public static Dictionary<UserFactorPattern, SleepTimeParameter> CreateAllFactorModels(bool isWhile)
        {
            Dictionary<UserFactorPattern, SleepTimeParameter> asss = new Dictionary<UserFactorPattern, SleepTimeParameter>();

            asss.Add(UserFactorPattern.standard, SleepTimeParameter.GetDefaultFactor());

            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet1 = isWhile ? (Worksheet)workbook.Worksheets["SleeptypesWhile"] : (Worksheet)workbook.Worksheets["SleeptypesAfter"];



            int finde = CellHelper.ExcelColumnNameToNumber("AY");

            bool available = true;
            while (available)
            {
                string value = CellHelper.GetCellValue(20, finde, worksheet1);
                if (value == null)
                {
                    available = false;
                    break;
                }

                var sp = SleepTimeParameter.GetDefaultFactor();

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

                asss.Add((UserFactorPattern)Convert.ToInt32(value), sp);
                finde+=2;
            }

            return asss;
        }


    }
}
