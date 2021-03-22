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

            foreach (var item in allSleepData)
            {
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
                        ss.sleepDataEntrie = new List<SleepDataEntry>();
                    }
                    ss.sleepDataEntrie.Add(item);
                }
                else
                {
                    isSleepTime = false;
                }


                if (ss != null && !isSleepTime)
                {

                    await ss.CalcData();

                    sleepSession.Add(ss);
                    ss = null;
                }

            }

            return true;
        }
    }
}
