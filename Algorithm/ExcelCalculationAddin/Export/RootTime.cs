using ExcelCalculationAddin.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepTimeClean;
using static ExcelCalculationAddin.Model.SleepType;

namespace ExcelCalculationAddin.Export
{
    // Root myDeserializedClass = JsonConvert.DeserializeObject<Root>(myJsonResponse); 
    public class DataSetter
    {
        public float Average { get; set; }
        public float Factor { get; set; }
        public float Max { get; set; }
        public float Median { get; set; }
        public float Min { get; set; }
    }

    public class ValuesTimeModel
    {
        public DataSetter light { get; set; }
        public DataSetter motion { get; set; }
        public DataSetter sleep { get; set; }
    }

    public class SleepTimeModelMaxMin
    {
        public ValuesTimeModel valuesAwake { get; set; }
        public ValuesTimeModel valuesDiff { get; set; }
        public ValuesTimeModel valuesSleep { get; set; }
    }

    public class RootTime
    {
        public string id { get; set; }
        public SleepTimeModelMaxMin sleepTimeModelMax { get; set; }
        public SleepTimeModelMaxMin sleepTimeModelMin { get; set; }
        //public SleepTimeParameter sleepTimeParameter { get; set; }
        public SleepTimeCleanType sleepTimePattern { get; set; }
    }

    public class RootTimeParameter
    {
        public string id { get; set; } // SleepStateCleanType index + UserFactorPattern index

        public SleepTimeCleanType sleepTimePattern { get; set; }

        public UserFactorPattern userFactorPattern { get; set; }

        public SleepTimeParameter sleepTimeParameter { get; set; }
    }

}
