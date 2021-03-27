using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepCleanModel;

namespace ExcelCalculationAddin.Model
{
    public class Strukture
    {

        public MobilePhonePlace mobilePhonePlace;
        public SleepTime sleepTime;
        public Shutters shutters;

        public Dictionary<SleepCleanModelType, MaxMinHelper> data = new Dictionary<SleepCleanModelType, MaxMinHelper>();

        public MaxMinHelper sleepApiDataTime;
        //public MaxMinHelper sleepLikely;
        //public MaxMinHelper motion;
        //public MaxMinHelper light;


        public Task<bool> CalcData(Strukture awake, Strukture sleep)
        {
            try
            {


                sleepApiDataTime = new MaxMinHelper();
                data.Add(SleepCleanModelType.Schlaf, new MaxMinHelper());
                data.Add(SleepCleanModelType.Motion, new MaxMinHelper());
                data.Add(SleepCleanModelType.Licht, new MaxMinHelper());
                //motion = new MaxMinHelper();
                //light = new MaxMinHelper();

                foreach (var item in awake.data[SleepCleanModelType.Schlaf].maxmintype)
                {
                    data[SleepCleanModelType.Schlaf].maxmintype[item.Key] = sleep.data[SleepCleanModelType.Schlaf].maxmintype[item.Key] - awake.data[SleepCleanModelType.Schlaf].maxmintype[item.Key];
                    data[SleepCleanModelType.Motion].maxmintype[item.Key] = sleep.data[SleepCleanModelType.Motion].maxmintype[item.Key] - awake.data[SleepCleanModelType.Motion].maxmintype[item.Key];
                    data[SleepCleanModelType.Licht].maxmintype[item.Key] = sleep.data[SleepCleanModelType.Licht].maxmintype[item.Key] - awake.data[SleepCleanModelType.Licht].maxmintype[item.Key];


                    //sleepLikely.maxmintype[item.Key] = sleep.sleepLikely.maxmintype[item.Key] - awake.sleepLikely.maxmintype[item.Key];
                    //motion.maxmintype[item.Key] = sleep.motion.maxmintype[item.Key] - awake.motion.maxmintype[item.Key];
                    //light.maxmintype[item.Key] = sleep.light.maxmintype[item.Key] - awake.light.maxmintype[item.Key];
                }

                /*sleepLikely.Median = sleep.sleepLikely.Median - awake.sleepLikely.Median;
                sleepLikely.Average = sleep.sleepLikely.Average - awake.sleepLikely.Average;
                sleepLikely.Factor = sleepLikely.Median == 0 ? 1 : (sleepLikely.Average / sleepLikely.Median) * 100;

                motion.Median = sleep.motion.Median - awake.motion.Median;
                motion.Average = sleep.motion.Average - awake.motion.Average;
                motion.Factor = motion.Median == 0 ? 1 : (motion.Average / motion.Median) * 100;

                light.Median = sleep.light.Median - awake.light.Median;
                light.Average = sleep.light.Average - awake.light.Average;
                light.Factor = light.Median == 0 ? 1 : (light.Average /light.Median) * 100;*/
            }
            catch (Exception ex)
            {

                throw;
            }
            return Task.FromResult(true);
        }


        public Task<bool> CalcData(List<SleepDataEntry> sleepDataEntrie)
        {

            //sleepApiDataTime = new MaxMinHelper();
            //sleepLikely = new MaxMinHelper();
            //motion = new MaxMinHelper();
            //light = new MaxMinHelper();

            sleepApiDataTime = new MaxMinHelper();
            data.Add(SleepCleanModelType.Schlaf, new MaxMinHelper());
            data.Add(SleepCleanModelType.Motion, new MaxMinHelper());
            data.Add(SleepCleanModelType.Licht, new MaxMinHelper());


            DateTime lastDatetime = default;
            List<int> sleepTime = new List<int>();

            foreach (var sleepEntrie in sleepDataEntrie)
            {
                if (lastDatetime == default)
                {
                    lastDatetime = sleepEntrie.time;
                    continue;
                }

                int ts = (int)(sleepEntrie.time - lastDatetime).TotalSeconds;
                sleepTime.Add(ts);

                lastDatetime = sleepEntrie.time;
            }


            sleepApiDataTime.maxmintype.Add(MaxMinHelperType.Average, (float)sleepTime.Average());
            data[SleepCleanModelType.Schlaf].maxmintype.Add(MaxMinHelperType.Average, (float)sleepDataEntrie.Average(x => x.sleep));
            data[SleepCleanModelType.Motion].maxmintype.Add(MaxMinHelperType.Average, (float)sleepDataEntrie.Average(x => x.motion));
            data[SleepCleanModelType.Licht].maxmintype.Add(MaxMinHelperType.Average, (float)sleepDataEntrie.Average(x => x.light));

            sleepApiDataTime.Max = sleepTime.Max();
            data[SleepCleanModelType.Schlaf].Max = sleepDataEntrie.Max(x => x.sleep);
            data[SleepCleanModelType.Motion].Max = sleepDataEntrie.Max(x => x.motion);
            data[SleepCleanModelType.Licht].Max = sleepDataEntrie.Max(x => x.light);

            sleepApiDataTime.Min = sleepTime.Min();
            data[SleepCleanModelType.Schlaf].Min = sleepDataEntrie.Min(x => x.sleep);
            data[SleepCleanModelType.Motion].Min = sleepDataEntrie.Min(x => x.motion);
            data[SleepCleanModelType.Licht].Min = sleepDataEntrie.Min(x => x.light);

            sleepApiDataTime.maxmintype.Add(MaxMinHelperType.Median, sleepTime.OrderByDescending(x => x).ToList()[sleepTime.Count / 2]);
            data[SleepCleanModelType.Schlaf].maxmintype.Add(MaxMinHelperType.Median, sleepDataEntrie.OrderByDescending(x => x.sleep).ToList()[sleepDataEntrie.Count / 2].sleep);
            data[SleepCleanModelType.Motion].maxmintype.Add(MaxMinHelperType.Median, sleepDataEntrie.OrderByDescending(x => x.motion).ToList()[sleepDataEntrie.Count / 2].motion);
            data[SleepCleanModelType.Licht].maxmintype.Add(MaxMinHelperType.Median, sleepDataEntrie.OrderByDescending(x => x.light).ToList()[sleepDataEntrie.Count / 2].light);

            sleepApiDataTime.maxmintype.Add(MaxMinHelperType.Factor, sleepApiDataTime.maxmintype[MaxMinHelperType.Average] / sleepApiDataTime.maxmintype[MaxMinHelperType.Median]);
            data[SleepCleanModelType.Schlaf].maxmintype.Add(MaxMinHelperType.Factor, data[SleepCleanModelType.Schlaf].maxmintype[MaxMinHelperType.Average] / data[SleepCleanModelType.Schlaf].maxmintype[MaxMinHelperType.Median]);
            data[SleepCleanModelType.Motion].maxmintype.Add(MaxMinHelperType.Factor, data[SleepCleanModelType.Motion].maxmintype[MaxMinHelperType.Average] / data[SleepCleanModelType.Motion].maxmintype[MaxMinHelperType.Median]);
            data[SleepCleanModelType.Licht].maxmintype.Add(MaxMinHelperType.Factor, data[SleepCleanModelType.Licht].maxmintype[MaxMinHelperType.Average] / data[SleepCleanModelType.Licht].maxmintype[MaxMinHelperType.Median]);


            return Task.FromResult(true);
        }


        public void WriteData(int row, Worksheet worksheet, int offset)
        {
            //sleepApiDataTime.WriteData(row, , worksheet);
            data[SleepCleanModelType.Schlaf].WriteData(row, 3 + offset, worksheet);
            data[SleepCleanModelType.Motion].WriteData(row, 21 + offset, worksheet);
            data[SleepCleanModelType.Licht].WriteData(row, 12 + offset, worksheet);
        }


        public static Strukture GetMax(MobilePhonePlace mobilePhonePlace)
        {
            return new Strukture()
            {
                mobilePhonePlace = mobilePhonePlace,
                sleepApiDataTime = MaxMinHelper.GetMax(),
                data = new Dictionary<SleepCleanModelType, MaxMinHelper>()
                {
                    { SleepCleanModelType.Schlaf, MaxMinHelper.GetMax() },
                    { SleepCleanModelType.Motion, MaxMinHelper.GetMax() },
                    { SleepCleanModelType.Licht, MaxMinHelper.GetMax() }
                }

            };
        }


        public static Strukture GetMin(MobilePhonePlace mobilePhonePlace)
        {
            return new Strukture()
            {
                mobilePhonePlace = mobilePhonePlace,
                sleepApiDataTime = MaxMinHelper.GetMin(),
                data = new Dictionary<SleepCleanModelType, MaxMinHelper>()
                {
                    { SleepCleanModelType.Schlaf, MaxMinHelper.GetMin() },
                    { SleepCleanModelType.Motion, MaxMinHelper.GetMin() },
                    { SleepCleanModelType.Licht, MaxMinHelper.GetMin() }
                }
            };
        }

    }
}
