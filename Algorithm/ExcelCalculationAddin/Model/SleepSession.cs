using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Model
{
    public class SleepSession
    {
        public DateTime dateTime;

        public List<SleepDataEntry> sleepDataEntrie;

        public Strukture structure;

        public SleepSession()
        {
        }

        public async Task<bool> CalcData()
        {
            dateTime = sleepDataEntrie.Where(x => x.time != null).FirstOrDefault().time;
            structure = new Strukture();

             await structure.CalcData(sleepDataEntrie);

            return true;
        }

    }

    public class MaxMinHelper
    {
        public int Max;
        public int Min;
        public int Median;
        public int Average;
        public int Diffrence;


    }



    public enum MobilePhonePlace
    {
        inBed,
        onTable,
        unidentified
    }

    public enum SleepTime
    {
        onDay,
        inNight
    }

    public enum Shutters
    {
        closed,
        open
    }

}
