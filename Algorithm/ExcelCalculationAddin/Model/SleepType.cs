using ExcelCalculationAddin.Model.SleepStateDetect;
using ExcelCalculationAddin.Read;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class SleepType
    {

        public enum SleepUserType
        {
            heavy = 2,
            standard = 5,
            light = 1,

        }

        public enum SleepStateType
        {
            light = 1,
            heavy = 2,
            superheavy = 3,

        }


        // public static Dictionary<SleepUserType, SleepCleanModel> sleepTypeModels;
        public static Dictionary<SleepUserType, SleepParameter> sleepTypeParamsWhile;
        public static Dictionary<SleepUserType, SleepParameter> sleepTypeParamsAfter;

        public static Dictionary<SleepStateType, SleepStateParameter> sleepStateParameter;

    }
}
