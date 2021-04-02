using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model.SleepStateDetect
{
    public class SleepStateClean
    {

        public enum SleepStateCleanType
        {
            zero = 0,
            zero1 = 1,
            TOLESSAWAKE = 2,
            TOMUCHAWAKE = 3,
            TOLESSDEEP = 4,
            TOMUCHDEEP = 5,
            TOLESSREM = 6,
            TOMUCHREM = 7,
        }



        public static Dictionary<string, SleepStateModel> sleepStateModels;
        public static Dictionary<SleepStateCleanType, SleepStateParameter> sleepStateParams;

    }
}
