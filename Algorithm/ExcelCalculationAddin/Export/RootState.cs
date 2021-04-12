using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
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

        [JsonConverter(typeof(StringEnumConverter))]
        public SleepStateCleanType sleepStatePattern { get; set; }

        [JsonConverter(typeof(StringEnumConverter))]
        public UserFactorPattern userFactorPattern { get; set; }

    }

    public class RootStateParameter
    {
        public string id { get; set; } // SleepStateCleanType index + UserFactorPattern index

        [JsonConverter(typeof(StringEnumConverter))]
        public SleepStateCleanType sleepStatePattern { get; set; }

        [JsonConverter(typeof(StringEnumConverter))]
        public UserFactorPattern userFactorPattern { get; set; }

        public Drittel sleepStateParameter { get; set; }
    }

    public class RootRawApi
    {
        public int timestampSeconds { get; set; }

        public int confidence { get; set; }

        public int motion { get; set; }

        public int light { get; set; }
    }
}
