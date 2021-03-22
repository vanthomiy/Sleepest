using ExcelCalculationAddin.Read;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class AlarmSettings
    {
        public TimeSpan SleepTimeStart;
        public TimeSpan SleepTimeEnd; 
        
        public TimeSpan SleepWakeUpStart;
        public TimeSpan SleepWakeUpEnd;

        public TimeSpan sleepTime;
        public TimeSpan sleepTimeAdd;


        public void AddValue(string value, AlarmPoints ap)
        {
            double d = double.Parse(value);

            switch (ap)
            {

                case AlarmPoints.WakeUpStart:
                    SleepWakeUpStart = DateTime.FromOADate(d).TimeOfDay; 
                    break;
                case AlarmPoints.WakeUpEnd:
                    SleepWakeUpEnd = DateTime.FromOADate(d).TimeOfDay;
                    break;
                case AlarmPoints.SleepTime:
                    sleepTime = DateTime.FromOADate(d).TimeOfDay;
                    break;
                case AlarmPoints.SleepTimeLight:
                    break;
                case AlarmPoints.SleepTimeDeep:
                    break;
                case AlarmPoints.SleepTimeRem:
                    break;
                case AlarmPoints.SleepAdd:
                    sleepTimeAdd = DateTime.FromOADate(d).TimeOfDay;
                    break;
                case AlarmPoints.SleepTimeEnd:
                    SleepTimeEnd = DateTime.FromOADate(d).TimeOfDay;
                    break;
                case AlarmPoints.SleepTimeStart:
                    SleepTimeStart = DateTime.FromOADate(d).TimeOfDay;
                    break;
                default:
                    break;
            }
        }
    }
}
