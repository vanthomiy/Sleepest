using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class Strukture
    {

        public MobilePhonePlace mobilePhonePlace;
        public SleepTime sleepTime;
        public Shutters shutters;

        public MaxMinHelper sleepApiDataTime;
        public MaxMinHelper sleepLikely;
        public MaxMinHelper motion;
        public MaxMinHelper light;


        public Task<bool> CalcData(List<SleepDataEntry> sleepDataEntrie)
        {

            sleepApiDataTime = new MaxMinHelper();
            sleepLikely = new MaxMinHelper();
            motion = new MaxMinHelper();
            light = new MaxMinHelper();

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


            sleepApiDataTime.Average = (int)sleepTime.Average();
            sleepLikely.Average = (int)sleepDataEntrie.Average(x => x.sleep);
            motion.Average = (int)sleepDataEntrie.Average(x => x.motion);
            light.Average = (int)sleepDataEntrie.Average(x => x.light);

            sleepApiDataTime.Max = (int)sleepTime.Max();
            sleepLikely.Max = (int)sleepDataEntrie.Max(x => x.sleep);
            motion.Max = (int)sleepDataEntrie.Max(x => x.motion);
            light.Max = (int)sleepDataEntrie.Max(x => x.light);

            sleepApiDataTime.Min = (int)sleepTime.Min();
            sleepLikely.Min = (int)sleepDataEntrie.Min(x => x.sleep);
            motion.Min = (int)sleepDataEntrie.Min(x => x.motion);
            light.Min = (int)sleepDataEntrie.Min(x => x.light);

            sleepApiDataTime.Median = sleepTime.OrderByDescending(x => x).ToList()[(int)sleepTime.Count / 2];
            sleepLikely.Median = sleepDataEntrie.OrderByDescending(x => x.sleep).ToList()[(int)sleepDataEntrie.Count / 2].sleep;
            motion.Median = sleepDataEntrie.OrderByDescending(x => x.motion).ToList()[(int)sleepDataEntrie.Count / 2].motion;
            light.Median = sleepDataEntrie.OrderByDescending(x => x.light).ToList()[(int)sleepDataEntrie.Count / 2].light;

            sleepApiDataTime.Diffrence = sleepApiDataTime.Max - sleepApiDataTime.Min;
            sleepLikely.Diffrence = sleepLikely.Max - sleepLikely.Min;
            motion.Diffrence = motion.Max - motion.Min;
            light.Diffrence = light.Max - light.Min;


            return Task.FromResult(true);
        }

    }
}
