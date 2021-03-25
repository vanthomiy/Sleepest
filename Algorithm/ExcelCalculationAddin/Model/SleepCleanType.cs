using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class SleepClean
    {
        public enum SleepCleanType
        {
            stoppedToLate = 3,
            stoppedToEarly = 2,
            startToEarly = 4,
            startToLate = 5,
            startToEarlyStoppedToEarly = 42,
            startToEarlyStoppedToLate = 43,
        }

        public delegate bool CheckCleanType(Strukture wach, Strukture sleep, Strukture diff);

        public static Dictionary<SleepCleanType,SleepCleanModel> sleepCleanModels;

        public static Dictionary<SleepCleanType, SleepParameter> sleepCleanParams;

        static SleepClean()
        {
            sleepCleanParams = new Dictionary<SleepCleanType, SleepParameter>();
            sleepCleanParams.Add(SleepCleanType.stoppedToEarly, SleepParameter.CreateBySleepCleanType(SleepCleanType.stoppedToEarly));
            sleepCleanParams.Add(SleepCleanType.stoppedToLate, SleepParameter.CreateBySleepCleanType(SleepCleanType.stoppedToLate));
            sleepCleanParams.Add(SleepCleanType.startToEarly, SleepParameter.CreateBySleepCleanType(SleepCleanType.startToEarly));
            sleepCleanParams.Add(SleepCleanType.startToLate, SleepParameter.CreateBySleepCleanType(SleepCleanType.startToLate));
            sleepCleanParams.Add(SleepCleanType.startToEarlyStoppedToEarly, SleepParameter.CreateBySleepCleanType(SleepCleanType.startToEarlyStoppedToEarly));
            sleepCleanParams.Add(SleepCleanType.startToEarlyStoppedToLate, SleepParameter.CreateBySleepCleanType(SleepCleanType.startToEarlyStoppedToLate));
        }

        //private static bool IsStopedToEarly(Strukture wach, Strukture sleep, Strukture diff)
        //{
        //    if (wach.sleepLikely.maxmintype[MaxMinHelperType.Factor] > 1.8f && sleep.sleepLikely.maxmintype[MaxMinHelperType.Average] > 82 && sleep.sleepLikely.Average < 85 && Math.Round(sleep.sleepLikely.Factor) == 0.94f && wach.motion.Factor > 0.9f)
        //    {
        //        return true;
        //    }

        //    return false;
        //}

        //private static bool IsStoppedToLate(Strukture wach, Strukture sleep, Strukture diff)
        //{
        //    if (wach.sleepLikely.Factor > 1.2f && wach.sleepLikely.Factor < 1.8f)
        //    {
        //        if (sleep.sleepLikely.Factor > 0.92f && sleep.sleepLikely.Factor < 0.98f)
        //        {
        //            if (diff.sleepLikely.Factor > 85f && diff.sleepLikely.Factor < 90f)
        //            {
        //                if (wach.motion.Factor > 0.84f && wach.motion.Factor < 0.95f)
        //                {
        //                    if (sleep.motion.Factor > 0.92f && sleep.motion.Factor < 2f)
        //                    {
        //                        if (diff.motion.Factor > 51f && diff.motion.Factor < 95f)
        //                        {
        //                            return true;
        //                        }
        //                    }
        //                }
        //            }
        //        }
        //    }

        //    return false;
        //}

        //private static bool IsStartToEarlyStoppedTpLate(Strukture wach, Strukture sleep, Strukture diff)
        //{
        //    if (wach.sleepLikely.Median < 6)
        //    {
        //        if (sleep.sleepLikely.Median > 88f)
        //        {
        //            if (sleep.sleepLikely.Average > 87f && sleep.sleepLikely.Average < 89.5f)
        //            {
        //                if (wach.motion.Factor > 0.82f && wach.motion.Factor < 0.95f)
        //                {
        //                    if (sleep.motion.Factor > 0.8f && sleep.motion.Factor < 2.1f)
        //                    {
        //                        return true;
        //                    }
        //                }
        //            }
        //        }
        //    }

        //    return false;
        //}

    }    
}
