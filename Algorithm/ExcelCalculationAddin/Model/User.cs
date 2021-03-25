using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class User
    {
        public string sheetname;
        public List<SleepDataEntry> allSleepData;
        public List<SleepSession> sleepSession;

        public async Task<bool> CreateSleepSessions()
        {
            sleepSession = new List<SleepSession>();
            SleepSession ss = null;
            bool isSleepTime;

            for (int i = 0; i < allSleepData.Count; i++)
            {
                SleepDataEntry item = allSleepData[i];

                if (item.time == default || item.time == null)
                {
                    continue;
                }

                if (item.time.TimeOfDay > ReadParameter.alarmSetttings.SleepTimeStart || item.time.TimeOfDay < ReadParameter.alarmSetttings.SleepTimeEnd)
                {
                    isSleepTime = true;
                    if (ss == null)
                    {
                        ss = new SleepSession();
                        ss.sleepDataEntrieSleepTime = new List<SleepDataEntry>();
                    }
                    ss.sleepDataEntrieSleepTime.Add(item);
                }
                else
                {
                    isSleepTime = false;
                }


                if (ss != null && (!isSleepTime || i+1 == allSleepData.Count))
                {
                    sleepSession.Add(ss);
                    ss = null;
                }

            }

            return true;
        }
    }
}
