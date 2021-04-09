using ExcelCalculationAddin.Model.SleepStateDetect;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepStateDetect.SleepStateClean;
using static ExcelCalculationAddin.Model.SleepStateDetect.SleepStateParameter;
using static ExcelCalculationAddin.Model.SleepType;

namespace ExcelCalculationAddin.Export
{

    // Root myDeserializedClass = JsonConvert.DeserializeObject<Root>(myJsonResponse); 


    public class RootState
    {
        public string id { get; set; }
        public SleepTimeModelMaxMin sleepStateModelMax { get; set; }
        public SleepTimeModelMaxMin sleepStateModelMin { get; set; }
       // public SleepStateParameter sleepStateParameter { get; set; }
        public SleepStateCleanType sleepStatePattern { get; set; }
        public UserFactorPattern userFactorPattern { get; set; }
    }

    public class RootStateParameter
    {
        public string id { get; set; } // SleepStateCleanType index + UserFactorPattern index

        public SleepStateCleanType sleepStatePattern { get; set; }

        public UserFactorPattern userFactorPattern { get; set; }

        public Drittel sleepStateParameter { get; set; }
    }
}
