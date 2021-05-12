﻿using ExcelCalculationAddin.Read;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class SleepDataEntry
    {
        public int row;

        public DateTime time;
        public int sleep;
        public int motion;
        public int light;
        public SleepState realSleepState;
        public SleepState[] calcSleepState = new SleepState[2];

        // für calculation
        public bool isPersonSleeping;
        public bool isSoundCleanup;
        public int sleepStartValue;
        public float timeOverCleanup;

        public bool issecond;

        public Task<bool> AddValue(string value, DataPoints dp)
        {
            try
            {
                switch (dp)
                {
                    case DataPoints.Date:
                        double d = double.Parse(value);
                        time = DateTime.FromOADate(d);
                        break;
                    case DataPoints.Time:
                        double dd = double.Parse(value);
                        var a = DateTime.FromOADate(dd);
                        time = time.AddHours(a.Hour);
                        time = time.AddMinutes(a.Minute);
                        time = time.AddSeconds(a.Second);
                        break;
                    case DataPoints.Sleep:
                        sleep = Convert.ToInt32(value);
                        break;
                    case DataPoints.Light:
                        light = Convert.ToInt32(value);
                        break;
                    case DataPoints.Motion:
                        motion = Convert.ToInt32(value);
                        break;
                    case DataPoints.Real:
                        var rs = Convert.ToInt32(value);
                        realSleepState = (SleepState)rs;
                        break;
                    case DataPoints.Caculated:
                        break;
                    case DataPoints.GeneralParam:
                        break;
                    case DataPoints.SleepType:
                        break;
                    case DataPoints.SleepTypePercentage:
                        break;
                    default:
                        break;
                }
            }
            catch (Exception)
            {
                return Task.FromResult(false);
            }

            return Task.FromResult(true);
        }


    }

    public enum SleepState
    {
        awake,
        light,
        deep,
        rem,
        sleeping
    }
}