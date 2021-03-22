using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Read
{
    public static class DataSetup
    {
        public static Dictionary<DataPoints, string> dataSetPoints;
        public static Dictionary<ParamsPoints, int> paramsSetPoints;
        public static Dictionary<AlarmPoints, int> alarmSetPoints;


        static DataSetup()
        {

            dataSetPoints = new Dictionary<DataPoints, string>()
            {
            {DataPoints.Date, "A"},
            {DataPoints.Time, "B"},
            {DataPoints.Sleep, "C"},
            {DataPoints.Light, "D"},
            {DataPoints.Motion, "E"},
            {DataPoints.Real, "F"},
            {DataPoints.Caculated, "G"},
            {DataPoints.GeneralParam, "H"},
            {DataPoints.SleepType, "I"},
            {DataPoints.SleepTypePercentage, "J"}
        };

            alarmSetPoints = new Dictionary<AlarmPoints, int>()
            {
            {AlarmPoints.WakeUpStart, 3},
            {AlarmPoints.WakeUpEnd, 4},
            {AlarmPoints.SleepTime, 6},
            {AlarmPoints.SleepTimeLight, 7 },
            {AlarmPoints.SleepTimeDeep, 8},
            {AlarmPoints.SleepTimeRem, 9},
            {AlarmPoints.SleepAdd, 11},
            {AlarmPoints.SleepTimeStart, 13},
            {AlarmPoints.SleepTimeEnd, 14}
        };

            paramsSetPoints = new Dictionary<ParamsPoints, int>()
            {
            {ParamsPoints.GeneralSleep, 7},
            {ParamsPoints.GeneralLight, 8},
            {ParamsPoints.GeneralMotion, 9},
            {ParamsPoints.DeepSleep, 11 },
            {ParamsPoints.DeepLight, 12},
            {ParamsPoints.DeepMotion, 13},
            {ParamsPoints.RemSleep, 15},
            {ParamsPoints.RemLight, 16},
            {ParamsPoints.RemMotion, 17},
            {ParamsPoints.SoundHelpBorder, 19},
            {ParamsPoints.SoundHelpMotion, 21},
            {ParamsPoints.SoundHelpDuration, 22},
            {ParamsPoints.DurationOverX, 24 },
            {ParamsPoints.DurationBorder, 25},
            {ParamsPoints.SleepStartClearBorder, 27},
            {ParamsPoints.SleepStartClearMotion, 28},
            {ParamsPoints.SleepStartClearSleep, 29},
            {ParamsPoints.SleepStartDecrease, 32},
            {ParamsPoints.SleepStartBorder, 33}
        };
        }
    }

    public enum DataPoints
    {
        Date,
        Time,
        Sleep,
        Light,
        Motion,
        Real,
        Caculated,
        GeneralParam,
        SleepType,
        SleepTypePercentage
    }
    public enum AlarmPoints
    {
        WakeUpStart,
        WakeUpEnd,
        SleepTimeStart,
        SleepTimeEnd,
        SleepTime,
        SleepTimeLight,
        SleepTimeDeep,
        SleepTimeRem,
        SleepAdd
    }

    public enum ParamsPoints
    {
        GeneralSleep,
        GeneralLight,
        GeneralMotion,
        DeepSleep,
        DeepLight,
        DeepMotion,
        RemSleep,
        RemLight,
        RemMotion,
        SoundHelpBorder,
        SoundHelpMotion,
        SoundHelpDuration,
        DurationOverX,
        DurationBorder,
        SleepStartClearBorder,
        SleepStartClearMotion,
        SleepStartClearSleep,
        SleepStartDecrease,
        SleepStartBorder,
    }
}
