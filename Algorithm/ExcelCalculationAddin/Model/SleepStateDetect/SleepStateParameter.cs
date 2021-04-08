using ExcelCalculationAddin.ListHelp;
using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepTimeClean;
using static ExcelCalculationAddin.Model.SleepStateDetect.SleepStateClean;
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
            public float soundClearSleepBorder;
            public float soundClearMotionBorder;
            public float modelMatchPercentage;

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
                    soundClearSleepBorder = 7,
                    soundClearMotionBorder = 1,
                    modelMatchPercentage = 98

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

            public static Drittel AddFactorToParameter(Drittel normal, Drittel factor, bool isNormal)
            {
                Drittel pm = Drittel.GetDefault();



                pm.sleepSleepBorder = normal.sleepSleepBorder * (isNormal ?  factor.sleepSleepBorder : 1/ factor.sleepSleepBorder );
                pm.deepSleepSleepBorder = normal.deepSleepSleepBorder * (isNormal ? factor.deepSleepSleepBorder : 1 / factor.deepSleepSleepBorder);
                pm.remSleepSleepBorder = normal.remSleepSleepBorder * (isNormal ? factor.remSleepSleepBorder : 1 / factor.remSleepSleepBorder);
                pm.sleepMotionBorder = normal.sleepMotionBorder * (isNormal ? factor.sleepMotionBorder : 1 / factor.sleepMotionBorder);
                pm.deepSleepMotionBorder = normal.deepSleepMotionBorder * (isNormal ? factor.deepSleepMotionBorder : 1 / factor.deepSleepMotionBorder);
                pm.remSleepMotionBorder = normal.remSleepMotionBorder * (isNormal ? factor.remSleepMotionBorder : 1 / factor.remSleepMotionBorder);
                pm.sleepLightBorder = normal.sleepLightBorder * (isNormal ? factor.sleepLightBorder : 1 / factor.sleepLightBorder);
                pm.deepSleepLightBorder = normal.deepSleepLightBorder * (isNormal ? factor.deepSleepLightBorder : 1 / factor.deepSleepLightBorder);
                pm.remSleepLightBorder = normal.remSleepLightBorder * (isNormal ? factor.remSleepLightBorder : 1 / factor.remSleepLightBorder);

                return pm;
            }



        }

        public static SleepStateParameter Combine(List<SleepStateParameter> ssp)
        {
            var paramdefault = SleepStateParameter.GetDefault();
            var params2 = SleepStateParameter.GetDefault();

            foreach (var item in ssp)
            {
                if (Math.Abs(item.first.sleepSleepBorder - paramdefault.first.sleepSleepBorder) > Math.Abs(params2.first.sleepSleepBorder - paramdefault.first.sleepSleepBorder))
                {
                    params2.first.sleepSleepBorder = item.first.sleepSleepBorder;
                }

                if (Math.Abs(item.first.deepSleepSleepBorder - paramdefault.first.deepSleepSleepBorder) > Math.Abs(params2.first.deepSleepSleepBorder - paramdefault.first.deepSleepSleepBorder))
                {
                    params2.first.deepSleepSleepBorder = item.first.deepSleepSleepBorder;
                }

                if (Math.Abs(item.first.remSleepSleepBorder - paramdefault.first.remSleepSleepBorder) > Math.Abs(params2.first.remSleepSleepBorder - paramdefault.first.remSleepSleepBorder))
                {
                    params2.first.remSleepSleepBorder = item.first.remSleepSleepBorder;
                }

                if (Math.Abs(item.first.sleepMotionBorder - paramdefault.first.sleepMotionBorder) > Math.Abs(params2.first.sleepMotionBorder - paramdefault.first.sleepMotionBorder))
                {
                    params2.first.sleepMotionBorder = item.first.sleepMotionBorder;
                }

                if (Math.Abs(item.first.deepSleepMotionBorder - paramdefault.first.deepSleepMotionBorder) > Math.Abs(params2.first.deepSleepMotionBorder - paramdefault.first.deepSleepMotionBorder))
                {
                    params2.first.deepSleepMotionBorder = item.first.deepSleepMotionBorder;
                }

                if (Math.Abs(item.first.remSleepMotionBorder - paramdefault.first.remSleepMotionBorder) > Math.Abs(params2.first.remSleepMotionBorder - paramdefault.first.remSleepMotionBorder))
                {
                    params2.first.remSleepMotionBorder = item.first.remSleepMotionBorder;
                }

                if (Math.Abs(item.first.sleepLightBorder - paramdefault.first.sleepLightBorder) > Math.Abs(params2.first.sleepLightBorder - paramdefault.first.sleepLightBorder))
                {
                    params2.first.sleepLightBorder = item.first.sleepLightBorder;
                }

                if (Math.Abs(item.first.deepSleepLightBorder - paramdefault.first.deepSleepLightBorder) > Math.Abs(params2.first.deepSleepLightBorder - paramdefault.first.deepSleepLightBorder))
                {
                    params2.first.deepSleepLightBorder = item.first.deepSleepLightBorder;
                }

                if (Math.Abs(item.first.remSleepLightBorder - paramdefault.first.remSleepLightBorder) > Math.Abs(params2.first.remSleepLightBorder - paramdefault.first.remSleepLightBorder))
                {
                    params2.first.remSleepLightBorder = item.first.remSleepLightBorder;
                }

            }

            params2.second = params2.third = params2.first;

            return params2;
        }

        public static SleepStateParameter AddFactorToParameter(SleepStateParameter normal, SleepStateParameter factor, bool isNormal)
        {
            SleepStateParameter pm = SleepStateParameter.GetDefault();

            pm.first = Drittel.AddFactorToParameter(normal.first, factor.first, isNormal);
            pm.second = Drittel.AddFactorToParameter(normal.second, factor.second, isNormal);
            pm.third = Drittel.AddFactorToParameter(normal.third, factor.third, isNormal);

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


        public static Dictionary<SleepStateCleanType, SleepStateParameter> CreateAllModels(bool isWhile)
        {
            Dictionary<SleepStateCleanType, SleepStateParameter> asss = new Dictionary<SleepStateCleanType, SleepStateParameter>();


            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet1 = (Worksheet)workbook.Worksheets["SleepAnalyse"];



            int finde = CellHelper.ExcelColumnNameToNumber("AN");

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

                var fValue = (int)CellHelper.GetCellValueFloat(5, finde, worksheet1);
                sp.first.sleepSleepBorder = fValue != 0 ? fValue : sp.first.sleepSleepBorder;
                fValue = (int)CellHelper.GetCellValueFloat(6, finde, worksheet1);
                sp.first.deepSleepSleepBorder = fValue != 0 ? fValue : sp.first.deepSleepSleepBorder;
                fValue = (int)CellHelper.GetCellValueFloat(7, finde, worksheet1);
                sp.first.remSleepSleepBorder = fValue != 0 ? fValue : sp.first.remSleepSleepBorder;
                fValue = (int)CellHelper.GetCellValueFloat(8, finde, worksheet1);
                sp.first.sleepMotionBorder = fValue != 0 ? fValue : sp.first.sleepMotionBorder;
                fValue = (int)CellHelper.GetCellValueFloat(9, finde, worksheet1);
                sp.first.deepSleepMotionBorder = fValue != 0 ? fValue : sp.first.deepSleepMotionBorder;
                fValue = (int)CellHelper.GetCellValueFloat(10, finde, worksheet1);
                sp.first.remSleepMotionBorder = fValue != 0 ? fValue : sp.first.remSleepMotionBorder;
                fValue = (int)CellHelper.GetCellValueFloat(11, finde, worksheet1);
                sp.first.sleepLightBorder = fValue != 0 ? fValue : sp.first.sleepLightBorder;
                fValue = (int)CellHelper.GetCellValueFloat(12, finde, worksheet1);
                sp.first.deepSleepLightBorder = fValue != 0 ? fValue : sp.first.deepSleepLightBorder;
                fValue = (int)CellHelper.GetCellValueFloat(13, finde, worksheet1);
                sp.first.remSleepLightBorder = fValue != 0 ? fValue : sp.first.remSleepLightBorder;

                sp.third = sp.second = sp.first;

                asss.Add((SleepStateCleanType)Convert.ToInt32(value), sp);
                finde++;
            }

            return asss;
        }

        public static Dictionary<SleepStateType, SleepStateParameter> CreateAllFactorModels(bool isWhile)
        {
            Dictionary<SleepStateType, SleepStateParameter> asss = new Dictionary<SleepStateType, SleepStateParameter>();

            //asss.Add(SleepStateType.light, SleepStateParameter.GetDefaultFactor());

            var workbook = (Workbook)Globals.ThisAddIn.Application.ActiveWorkbook;
            Worksheet worksheet1 = (Worksheet)workbook.Worksheets["SleepAnalyse"];



            int finde = CellHelper.ExcelColumnNameToNumber("AO");

            bool available = true;
            while (available)
            {
                string value = CellHelper.GetCellValue(20, finde, worksheet1);
                if (value == null)
                {
                    available = false;
                    break;
                }

                var sp = SleepStateParameter.GetDefaultFactor();

                var fValue = (int)CellHelper.GetCellValueFloat(21, finde, worksheet1);
                sp.first.sleepSleepBorder = fValue != 0 ? fValue : sp.first.sleepSleepBorder;
                fValue = (int)CellHelper.GetCellValueFloat(22, finde, worksheet1);
                sp.first.deepSleepSleepBorder = fValue != 0 ? fValue : sp.first.deepSleepSleepBorder;
                fValue = (int)CellHelper.GetCellValueFloat(23, finde, worksheet1);
                sp.first.remSleepSleepBorder = fValue != 0 ? fValue : sp.first.remSleepSleepBorder;
                fValue = (int)CellHelper.GetCellValueFloat(24, finde, worksheet1);
                sp.first.sleepMotionBorder = fValue != 0 ? fValue : sp.first.sleepMotionBorder;
                fValue = (int)CellHelper.GetCellValueFloat(25, finde, worksheet1);
                sp.first.deepSleepMotionBorder = fValue != 0 ? fValue : sp.first.deepSleepMotionBorder;
                fValue = (int)CellHelper.GetCellValueFloat(26, finde, worksheet1);
                sp.first.remSleepMotionBorder = fValue != 0 ? fValue : sp.first.remSleepMotionBorder;
                fValue = (int)CellHelper.GetCellValueFloat(27, finde, worksheet1);
                sp.first.sleepLightBorder = fValue != 0 ? fValue : sp.first.sleepLightBorder;
                fValue = (int)CellHelper.GetCellValueFloat(28, finde, worksheet1);
                sp.first.deepSleepLightBorder = fValue != 0 ? fValue : sp.first.deepSleepLightBorder;
                fValue = (int)CellHelper.GetCellValueFloat(29, finde, worksheet1);
                sp.first.remSleepLightBorder = fValue != 0 ? fValue : sp.first.remSleepLightBorder;

                sp.third = sp.second = sp.first;

                asss.Add((SleepStateType)Convert.ToInt32(value), sp);
                finde += 2;
            }

            return asss;
        }
        
    }
}
