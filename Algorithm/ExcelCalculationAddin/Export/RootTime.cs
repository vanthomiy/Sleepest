using ExcelCalculationAddin.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepTimeClean;

namespace ExcelCalculationAddin.Export
{
    // Root myDeserializedClass = JsonConvert.DeserializeObject<Root>(myJsonResponse); 
    public class DataSetter
    {
        public int Average { get; set; }
        public int Factor { get; set; }
        public int Max { get; set; }
        public int Median { get; set; }
        public int Min { get; set; }
    }


    public class ValuesTimeModel
    {
        public DataSetter maxLicht { get; set; }
        public DataSetter maxMotion { get; set; }
        public DataSetter maxSchlaf { get; set; }
        public DataSetter minLicht { get; set; }
        public DataSetter minMotion { get; set; }
        public DataSetter minSchlaf { get; set; }
    }


    public class SleepTimeModelMaxMin
    {
        public ValuesTimeModel valuesAwake { get; set; }
        public ValuesTimeModel valuesDiff { get; set; }
        public ValuesTimeModel valuesSleep { get; set; }
    }


    //public class SleepTimeParameter
    //{
    //    public int awakeMedianOverTime { get; set; }
    //    public int awakeMotionBorder { get; set; }
    //    public int awakeSleepBorder { get; set; }
    //    public int awakeTime { get; set; }
    //    public int diffAwake { get; set; }
    //    public int diffSleep { get; set; }
    //    public int diffSleepFuture { get; set; }
    //    public int modelMatchPercentage { get; set; }
    //    public int sleepMedianOverTime { get; set; }
    //    public int sleepMotionBorder { get; set; }
    //    public int sleepSleepBorder { get; set; }
    //    public int sleepTime { get; set; }
    //    public int wakeUpTime { get; set; }
    //}

    public class RootTime
    {
        public int id { get; set; }
        public SleepTimeModelMaxMin sleepTimeModelMax { get; set; }
        public SleepTimeModelMaxMin sleepTimeModelMin { get; set; }
        public SleepTimeParameter sleepTimeParameter { get; set; }
        public SleepTimeCleanType sleepTimePattern { get; set; }
    }
}
