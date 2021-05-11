using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class SleepTimeClean
    {
        [JsonConverter(typeof(StringEnumConverter))]
        public enum SleepTimeCleanType
        {
            NONE,
            WAKEUPTOLATE,
            WAKEUPTOEARLY,
            ASLEEPTOLATE,
            ASLEEPTOEARLY
            //startToEarlyStoppedToEarly = 42,
            //startToEarlyStoppedToLate = 43,
            //startToLateStoppedToEarly = 52,
            //startToLateStoppedToLate = 53
        }

        

        public static Dictionary<SleepTimeCleanType,SleepTimeModel> sleepCleanModelsAfter;
        public static Dictionary<SleepTimeCleanType, SleepTimeParameter> sleepCleanParamsAfter;

        public static Dictionary<SleepTimeCleanType, SleepTimeModel> sleepTimeModelsWhile;
        public static Dictionary<SleepTimeCleanType, SleepTimeParameter> sleepTimeParamsWhile;



    }
}
