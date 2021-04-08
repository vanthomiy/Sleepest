using ExcelCalculationAddin.Calclulate;
using ExcelCalculationAddin.Live;
using ExcelCalculationAddin.Model;
using ExcelCalculationAddin.Model.SleepStateDetect;
using Microsoft.Office.Tools.Ribbon;
using Newtonsoft.Json;

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
            SleepTimeClean.sleepCleanParamsAfter = SleepTimeParameter.CreateAllModels(false);
            SleepTimeClean.sleepCleanModelsAfter = SleepTimeModel.CreateAllModels(false);
            SleepType.sleepTypeParamsAfter = SleepTimeParameter.CreateAllFactorModels(false);

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

            //JsonConvert.SerializeObject()

            
        }
    }
}
