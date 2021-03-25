using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static ExcelCalculationAddin.Model.SleepClean;

namespace ExcelCalculationAddin.Model
{
    public class SleepParameter
    {
        public TimeSpan awakeTime; // = new TimeSpan(00, 30, 00);
        public TimeSpan sleepTime; //= new TimeSpan(00, 50, 00);
        public TimeSpan wakeUpTime; // = new TimeSpan(01, 30, 00);

        public int sleepSleep;
        public int sleepAwake;
        public int motionSleep;
        public int motionAwake;
        public int sleep;
        public int diffSleep;
        public int diffSleepFuture;

        public int awake;
        public int diffAwake;


        public static SleepParameter GetDefault()
        {
            return new SleepParameter()
            {
                awakeTime = new TimeSpan(00, 30, 00),
                sleepTime = new TimeSpan(00, 50, 00),
                wakeUpTime = new TimeSpan(01, 30, 00),

                sleepSleep = 50,
                sleepAwake = 20,
                motionSleep = 4,
                motionAwake = 0,
                sleep = 75,
                diffSleep = 50,
                diffSleepFuture = 0,
                awake = 30,
                diffAwake = -5
            };
        }

        public static SleepParameter CreateBySleepCleanType(SleepCleanType sct)
        {
            var sp = SleepParameter.GetDefault();

            switch (sct)
            {
                case SleepCleanType.stoppedToLate:
                    sp.sleepAwake = 60;
                    sp.motionAwake = 3;
                    sp.awake = 40;
                    break;
                case SleepCleanType.stoppedToEarly:
                    sp.sleepSleep = 10;
                    break;
                case SleepCleanType.startToEarlyStoppedToEarly:
                    sp.sleepAwake = 60;
                    sp.motionAwake = 3;
                    sp.awake = 30;
                    sp.sleepSleep = 80;
                    break;
                default:
                    break;
            }


            return sp;

        }

    }
}
