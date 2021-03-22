using ExcelCalculationAddin.Read;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class Parameters
    {


        public int GeneralSleep;
        public int GeneralLight;
        public int GeneralMotion;
        public int DeepSleep;
        public int DeepLight;
        public int DeepMotion;
        public int RemSleep;
        public int RemLight;
        public int RemMotion;
        public int SoundHelpBorder;
        public int SoundHelpMotion;
        public int SoundHelpDuration;
        public int DurationOverX;
        public int DurationBorder;
        public int SleepStartClearBorder;
        public int SleepStartClearMotion;
        public int SleepStartClearSleep;
        public int SleepStartDecrease;
        public int SleepStartBorder;


        public void AddValue(string value, ParamsPoints pp)
        {
            try
            {
                switch (pp)
                {
                    case ParamsPoints.GeneralSleep:
                        GeneralSleep = Convert.ToInt32(value);
                        break;
                    case ParamsPoints.GeneralLight:
                        GeneralLight = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.GeneralMotion:
                        GeneralMotion = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.DeepSleep:
                        DeepSleep = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.DeepLight:
                        DeepLight = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.DeepMotion:
                        DeepMotion = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.RemSleep:
                        RemSleep = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.RemLight:
                        RemLight = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.RemMotion:
                        RemMotion = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.SoundHelpBorder:
                        SoundHelpBorder = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.SoundHelpMotion:
                        SoundHelpMotion = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.SoundHelpDuration:
                        SoundHelpDuration = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.DurationOverX:
                        DurationOverX = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.DurationBorder:
                        DurationBorder = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.SleepStartClearBorder:
                        SleepStartClearBorder = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.SleepStartClearMotion:
                        SleepStartClearMotion = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.SleepStartClearSleep:
                        SleepStartClearSleep = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.SleepStartDecrease:
                        SleepStartDecrease = Convert.ToInt32(value);

                        break;
                    case ParamsPoints.SleepStartBorder:
                        SleepStartBorder = Convert.ToInt32(value);

                        break;
                    default:
                        break;
                }
            }
            catch (Exception)
            {
            }     
        }
    }
}
