using ExcelCalculationAddin.Model.SleepStateDetect;
using ExcelCalculationAddin.Read;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class SleepType
    {

        [JsonConverter(typeof(StringEnumConverter))]
        public enum UserFactorPattern
        {
            NONE,
            SUPERLIGHT,
            LIGHT,
            NORMAL,
            HEAVY,
            SUPERHEAVY


        }

        //public enum SleepStateType
        //{
        //    light = 1,
        //    heavy = 2,
        //    superheavy = 3,

        //}


        // public static Dictionary<SleepUserType, SleepCleanModel> sleepTypeModels;
        public static Dictionary<UserFactorPattern, SleepTimeParameter> sleepTimeParameter;
        public static Dictionary<UserFactorPattern, SleepTimeParameter> sleepTypeParamsAfter;

        public static Dictionary<UserFactorPattern, SleepStateParameter> sleepStateParameter;

    }
}
