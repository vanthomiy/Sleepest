using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class SleepClean
    {
        public enum SleepCleanType
        {
            stoppedToLate = 3,
            stoppedToEarly = 2,
            startToEarly = 4,
            startToLate = 5,
            startToEarlyStoppedToEarly = 42,
            startToEarlyStoppedToLate = 43,
            startToLateStoppedToEarly = 52,
            startToLateStoppedToLate = 53
        }

        

        public static Dictionary<SleepCleanType,SleepCleanModel> sleepCleanModelsAfter;
        public static Dictionary<SleepCleanType, SleepParameter> sleepCleanParamsAfter;

        public static Dictionary<SleepCleanType, SleepCleanModel> sleepCleanModelsWhile;
        public static Dictionary<SleepCleanType, SleepParameter> sleepCleanParamsWhile;

    }    
}
