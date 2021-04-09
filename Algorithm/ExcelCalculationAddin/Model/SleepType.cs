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

        public enum UserFactorPattern
        {
            standard = 0,
            superLight = 1,
            light = 2,
            heavy = 3,
            superHeavy = 4,

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
