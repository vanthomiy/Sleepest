using ExcelCalculationAddin.Calclulate;
using ExcelCalculationAddin.Live;
using ExcelCalculationAddin.Model;
using Microsoft.Office.Tools.Ribbon;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

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
            SleepClean.sleepCleanParamsAfter = SleepParameter.CreateAllModels(false);
            SleepClean.sleepCleanModelsAfter = SleepCleanModel.CreateAllModels(false);
            SleepType.sleepTypeParamsAfter = SleepParameter.CreateAllFactorModels(false);

            SleepClean.sleepCleanParamsWhile= SleepParameter.CreateAllModels(true);
            SleepClean.sleepCleanModelsWhile = SleepCleanModel.CreateAllModels(true);
            SleepType.sleepTypeParamsWhile = SleepParameter.CreateAllFactorModels(true);

            await ReadParameter.GetAllUserData();
        }

        private async void btnCalcLive_Click(object sender, RibbonControlEventArgs e)
        {
            // Kalkulieren und abspeichern der daten
            ReadParameter.GetAlarmSettings();
            ReadParameter.ReadSleepTypeParameter();


            await CalcSleepLive.CalcAllSleepData();
        }
    }
}
