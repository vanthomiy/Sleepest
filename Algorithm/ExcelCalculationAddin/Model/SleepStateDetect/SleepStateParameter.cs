using ExcelCalculationAddin.ListHelp;
using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepClean;
using static ExcelCalculationAddin.Model.SleepType;

namespace ExcelCalculationAddin.Model.SleepStateDetect
{
    public class SleepStateParameter
    {
        public Drittel first;
        public Drittel second;
        public Drittel third;


        public class Drittel
        {
            public float sleepSleepBorder; // grenze zum einschlafen
            public float deepSleepSleepBorder; // grenze zum aufwachen
            public float remSleepSleepBorder; // grenze zum einschlafen
            public float sleepMotionBorder; // grenze zum einschlafen
            public float deepSleepMotionBorder; // grenze zum aufwachen
            public float remSleepMotionBorder; // grenze zum einschlafen
            public float sleepLightBorder; // grenze zum einschlafen
            public float deepSleepLightBorder; // grenze zum aufwachen
            public float remSleepLightBorder; // grenze zum einschlafen

            public static Drittel GetDefault()
            {
                return new Drittel()
                {

                    sleepSleepBorder = 20,
                    deepSleepSleepBorder = 90,
                    remSleepSleepBorder = 95,
                    sleepMotionBorder = 5,
                    deepSleepMotionBorder = 3,
                    remSleepMotionBorder = 1,
                    sleepLightBorder = 5,
                    deepSleepLightBorder = 3,
                    remSleepLightBorder = 1,

                };
            }

            public static Drittel GetDefaulFactor()
            {
                return new Drittel()
                {

                    sleepSleepBorder = 1,
                    deepSleepSleepBorder = 1,
                    remSleepSleepBorder = 1,
                    sleepMotionBorder = 1,
                    deepSleepMotionBorder = 1,
                    remSleepMotionBorder = 1,
                    sleepLightBorder = 1,
                    deepSleepLightBorder = 1,
                    remSleepLightBorder = 1,

                };
            }

            public static Drittel AddFactorToParameter(Drittel normal, Drittel factor)
            {
                Drittel pm = Drittel.GetDefault();


                pm.sleepSleepBorder = normal.sleepSleepBorder * factor.sleepSleepBorder;
                pm.sleepSleepBorder = normal.sleepSleepBorder * factor.sleepSleepBorder;
                pm.remSleepSleepBorder = normal.remSleepSleepBorder * factor.remSleepSleepBorder;
                pm.sleepMotionBorder = normal.sleepMotionBorder * factor.sleepMotionBorder;
                pm.deepSleepMotionBorder = normal.deepSleepMotionBorder * factor.deepSleepMotionBorder;
                pm.remSleepMotionBorder = normal.remSleepMotionBorder * factor.remSleepMotionBorder;
                pm.sleepLightBorder = normal.sleepLightBorder * factor.sleepLightBorder;
                pm.deepSleepLightBorder = normal.deepSleepLightBorder * factor.deepSleepLightBorder;
                pm.remSleepLightBorder = normal.remSleepLightBorder * factor.remSleepLightBorder;

                return pm;
            }

        }


        public static SleepStateParameter AddFactorToParameter(SleepStateParameter normal, SleepStateParameter factor)
        {
            SleepStateParameter pm = SleepStateParameter.GetDefault();

            pm.first = Drittel.AddFactorToParameter(normal.first, factor.first);
            pm.second = Drittel.AddFactorToParameter(normal.second, factor.second);
            pm.third = Drittel.AddFactorToParameter(normal.third, factor.third);

            return pm;
        }


        public static SleepStateParameter GetDefault()
        {
            return new SleepStateParameter()
            {

                first = Drittel.GetDefault(),
                second = Drittel.GetDefault(),
                third = Drittel.GetDefault()

            };
        }

        public static SleepStateParameter GetDefaultFactor()
        {
            return new SleepStateParameter()
            {
                first = Drittel.GetDefaulFactor(),
                second = Drittel.GetDefaulFactor(),
                third = Drittel.GetDefaulFactor()
            };
        }


        /*

        public static Dictionary<SleepCleanType, SleepStateParameter> CreateAllModels(bool isWhile)
        {
            Dictionary<SleepCleanType, SleepStateParameter> asss = new Dictionary<SleepCleanType, SleepStateParameter>();


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

                var sp = SleepStateParameter.GetDefault();

                var fValue = (int)CellHelper.GetCellValueFloat(8, finde, worksheet1);
                sp.sleepSleepBorder = fValue != 0 ? fValue : sp.sleepSleepBorder;
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
                finde += 2;
            }

            return asss;
        }
        */
    }
}
