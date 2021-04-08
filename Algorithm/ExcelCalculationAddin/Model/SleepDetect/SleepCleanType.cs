using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class SleepTimeClean
    {
        public enum SleepTimeCleanType
        {
            stoppedToLate = 3,
            stoppedToEarly = 2,
            startToEarly = 4,
            startToLate = 5,
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
