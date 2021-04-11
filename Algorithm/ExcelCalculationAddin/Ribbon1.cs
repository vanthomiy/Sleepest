using ExcelCalculationAddin.Calclulate;
using ExcelCalculationAddin.Export;
using ExcelCalculationAddin.Live;
using ExcelCalculationAddin.Model;
using ExcelCalculationAddin.Model.SleepStateDetect;
using Microsoft.Office.Tools.Ribbon;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
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

            SleepTimeClean.sleepTimeParamsWhile= SleepTimeParameter.CreateAllModels(true);
            SleepTimeClean.sleepTimeModelsWhile = SleepTimeModel.CreateAllModels(true);
            SleepType.sleepTimeParameter = SleepTimeParameter.CreateAllFactorModels(true);


            SleepStateClean.sleepStateParams = SleepStateParameter.CreateAllModels(true);
            SleepStateClean.sleepStateModels = SleepStateModel.CreateAllModels(true);
            SleepType.sleepStateParameter= SleepStateParameter.CreateAllFactorModels(true);

            await ReadParameter.GetAllUserData();
        }

        private async void btnCalcLive_Click(object sender, RibbonControlEventArgs e)
        {
            // Kalkulieren und abspeichern der daten
            ReadParameter.GetAlarmSettings();
            ReadParameter.ReadSleepTypeParameter();


            await CalcSleepLive.CalcAllSleepData();
        }

        private void btnJsonExport_Click(object sender, RibbonControlEventArgs e)
        {
            if (SleepStateClean.sleepStateParams == null)
            {
                return;
            }


            // first we have to create the classes
            List<RootTime> rootTime = new List<RootTime>();
            foreach (var item in SleepTimeClean.sleepTimeModelsWhile)
            {
                //SleepTimeClean.sleepTimeParamsWhile

                RootTime rt = new RootTime();

                rt.id = ((int)item.Key).ToString();
                //rt.sleepTimeParameter = SleepTimeClean.sleepTimeParamsWhile[item.Key];
                rt.sleepTimePattern = item.Key;

                rt.sleepTimeModelMax = item.Value.getMaxValues();
                rt.sleepTimeModelMin = item.Value.getMinValues();

                rootTime.Add(rt);
            }

            var jsonTimeFile = JsonConvert.SerializeObject(rootTime);


            // first we have to create the classes
            List<RootState> rootState = new List<RootState>();
            foreach (var item in SleepStateClean.sleepStateModels)
            {
                //SleepTimeClean.sleepTimeParamsWhile

                RootState rt = new RootState();

                rt.id = item.Key;
                var a = Char.GetNumericValue(item.Key[0]);
                var b = Char.GetNumericValue(item.Key[1]);
                rt.sleepStatePattern = (SleepStateCleanType)((int)(a));
                rt.userFactorPattern = (UserFactorPattern)((int)(b));

                //rt.sleepStateParameter = SleepStateClean.sleepStateParams[rt.sleepStatePattern];

                rt.sleepStateModelMax = item.Value.getMaxValues();
                rt.sleepStateModelMin = item.Value.getMinValues();

                rootState.Add(rt);
            }

            var jsonStateFile = JsonConvert.SerializeObject(rootState);

            // first we have to create the classes
            List<RootStateParameter> rootStateParameter = new List<RootStateParameter>();
            foreach (var item in SleepStateClean.sleepStateParams)
            {
                //SleepTimeClean.sleepTimeParamsWhile

                RootStateParameter rst = new RootStateParameter();

                rst.id = ((int)item.Key).ToString() + "0";
                rst.sleepStatePattern = item.Key;
                rst.userFactorPattern = (UserFactorPattern)0;

                //rt.sleepStateParameter = SleepStateClean.sleepStateParams[rt.sleepStatePattern];

                rst.sleepStateParameter = item.Value.first;

                rootStateParameter.Add(rst);
            }
            foreach (var item in SleepType.sleepStateParameter)
            {
                //SleepTimeClean.sleepTimeParamsWhile

                RootStateParameter rst = new RootStateParameter();

                rst.id = "0" + ((int)item.Key).ToString();
                rst.sleepStatePattern = (SleepStateCleanType)0;
                rst.userFactorPattern = item.Key;

                //rt.sleepStateParameter = SleepStateClean.sleepStateParams[rt.sleepStatePattern];

                rst.sleepStateParameter = item.Value.first;

                rootStateParameter.Add(rst);
            }

            var jsonStateParamsFile = JsonConvert.SerializeObject(rootStateParameter);


            // first we have to create the classes
            List<RootTimeParameter> rootTimeParameter = new List<RootTimeParameter>();
            foreach (var item in SleepTimeClean.sleepTimeParamsWhile)
            {
                //SleepTimeClean.sleepTimeParamsWhile

                RootTimeParameter rst = new RootTimeParameter();

                rst.id = ((int)item.Key).ToString() + "0";
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

                rst.id = "0" + ((int)item.Key).ToString();
                rst.sleepTimePattern = (SleepTimeCleanType)0;
                rst.userFactorPattern = item.Key;

                //rt.sleepStateParameter = SleepStateClean.sleepStateParams[rt.sleepStatePattern];

                rst.sleepTimeParameter = item.Value;

                rootTimeParameter.Add(rst);
            }

            var jsonTimeParamsFile = JsonConvert.SerializeObject(rootTimeParameter);

        }
    }
}
