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
            NONE,
            TOMANYSLEEP,
            TOMANYLIGHT,
            TOMANYDEEP,
            TOMANYREM,
            TOLESSSLEEP,
            TOLESSLIGHT,
            TOLESSDEEP,
            TOLESSREM
        }



        public static Dictionary<string, SleepStateModel> sleepStateModels;
        public static Dictionary<string, SleepStateParameter> sleepStateParams;

    }
}
