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
        public List<SleepSession> sleepSessionAfter;
        public List<SleepSession> sleepSessionWhile;

        public async Task<bool> CreateSleepSessionsAfter()
        {
            sleepSessionAfter = new List<SleepSession>();
            SleepSession ss = null;
            bool isSleepTime;

            DateTime day = DateTime.Now;

            for (int i = 0; i < allSleepData.Count; i++)
            {
                SleepDataEntry item = allSleepData[i];

                if (item.time == default || item.time == null)
                {
                    continue;
                }

                if (ss == null)
                {
                    ss = new SleepSession();
                    ss.sleepDataEntrieSleepTime = new List<SleepDataEntry>();
                    ss.sleepDataEntrieSleepTimeAll = new List<SleepDataEntry>();

                    day = item.time;
                }

                if (item.time.Day == day.Day && item.time.TimeOfDay > new TimeSpan(15, 0, 0))
                {
                    ss.sleepDataEntrieSleepTimeAll.Add(item);
                }
                else if (item.time.Day >= day.AddDays(1).Day && item.time.TimeOfDay < new TimeSpan(15, 0, 0))
                {
                    ss.sleepDataEntrieSleepTimeAll.Add(item);
                }

                if(item.time.Day >= day.AddDays(1).Day && item.time.TimeOfDay >= new TimeSpan(15, 0, 0))
                {

                    ss.sleepDataEntrieSleepTime = ss.sleepDataEntrieSleepTimeAll.Where(x => x.time.TimeOfDay > ReadParameter.alarmSetttings.SleepTimeStart || x.time.TimeOfDay < ReadParameter.alarmSetttings.SleepTimeEnd).ToList();
                    sleepSessionAfter.Add(ss);
                    ss = null;
                }



                /*
                if (ss == null && ())
                {
                    ss = new SleepSession();
                    ss.sleepDataEntrieSleepTime = new List<SleepDataEntry>();
                    ss.sleepDataEntrieSleepTimeAll = new List<SleepDataEntry>();
                }

                if (item.time.TimeOfDay < new TimeSpan(12, 0, 0) ||  item.time.TimeOfDay > new TimeSpan(18, 0, 0))
                {
                    ss.sleepDataEntrieSleepTimeAll.Add(item);
                }


                if (item.time.TimeOfDay > ReadParameter.alarmSetttings.SleepTimeStart || item.time.TimeOfDay < ReadParameter.alarmSetttings.SleepTimeEnd)
                {
                    isSleepTime = true;
                    if (ss == null)
                    {
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
                    sleepSessionAfter.Add(ss);
                    ss = null;
                }
                */

            }

            return true;
        }

        public async Task<bool> CreateSleepSessionsWhile()
        {
            sleepSessionWhile = new List<SleepSession>();
            SleepSession ss = null;
            bool isSleepTime = false;

            DateTime day = DateTime.Now;


            int nomcount = 0, idcount = 0;
          
            for (int i = 0; i < allSleepData.Count; i++)
            {
                SleepDataEntry item = allSleepData[i];

              

                if (item.time == default || item.time == null)
                {
                    continue;
                }

                if (ss == null)
                {
                    ss = new SleepSession();
                    ss.sleepDataEntrieSleepTime = new List<SleepDataEntry>();
                    ss.sleepDataEntrieSleepTimeAll = new List<SleepDataEntry>();

                    day = item.time;
                }

                if (ss != null && (item.time.TimeOfDay > ReadParameter.alarmSetttings.SleepTimeStart || item.time.TimeOfDay < ReadParameter.alarmSetttings.SleepWakeUpStart))
                {
                    ss.sleepDataEntrieSleepTime.Add(item);
                }

                if (item.time.Day == day.Day && item.time.TimeOfDay > new TimeSpan(15, 0, 0))
                {
                    ss.sleepDataEntrieSleepTimeAll.Add(item);
                }
                else if (item.time.Day == (day + new TimeSpan(24, 0, 0)).Day && item.time.TimeOfDay < new TimeSpan(15, 0, 0))
                {
                    ss.sleepDataEntrieSleepTimeAll.Add(item);
                }
                else if (item.time.Day >= (day + new TimeSpan(24, 0, 0)).Day)
                {
                    if (ss != null)
                    {
                        //ss.sleepDataEntrieSleepTime = ss.sleepDataEntrieSleepTimeAll.Where(x => x.time.TimeOfDay > ReadParameter.alarmSetttings.SleepTimeStart || x.time.TimeOfDay < ReadParameter.alarmSetttings.SleepTimeEnd).ToList();
                        sleepSessionWhile.Add(ss);
                        ss = null;
                    }
                }



            }



            return true;
        }

    }
}
