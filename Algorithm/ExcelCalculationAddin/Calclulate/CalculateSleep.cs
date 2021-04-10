using ExcelCalculationAddin.Model;
using ExcelCalculationAddin.Model.SleepStateDetect;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Calclulate
{
    public class CalculateSleep
    {


        // Jede session wird unabhängig ausgewertet und definiert
        public static async Task<bool> CalcAllSleepData()
        {
            int found = 0;
            int foundCando = 0;
            int notpossible = 0;

            SleepSession.actualRow = 3;


            List<Tuple<string, string, string>> a = new List<Tuple<string, string, string>>();


            foreach (var user in ReadParameter.values)
            {
                foreach (var session in user.sleepSessionAfter)
                {
                    session.times = 0;
                    var param = SleepTimeParameter.GetDefault();
                    param = SleepTimeParameter.AddFactorToParameter(param, SleepType.sleepTypeParamsAfter[session.sleepUserType], false);

                    try
                    {                     
                        await session.CalcSleepTimesRealTime(param);
                    }
                    catch (Exception)
                    {

                        throw;
                    }

                    try
                    {
                        session.rw11 = "";
                        session.rw12 = "";
                        session.rw2 = "";

                        // if no session was found
                        if (!session.foundSleep)
                        {
                            session.nf1 = "nf";
                            session.sleepUserType = SleepType.UserFactorPattern.LIGHT;
                            param = SleepTimeParameter.AddFactorToParameter(param, SleepType.sleepTypeParamsAfter[session.sleepUserType], false);

                            await session.CalcSleepTimesRealTime(param, 0);

                            if (!session.foundSleep)
                            {
                                session.nf2 = "nf";
                            }

                        }
                        else if (session.times > 1)
                        {
                            session.nf1 = "tm: "+ session.times;
                            session.sleepUserType = SleepType.UserFactorPattern.HEAVY;
                            session.times = 0;
                            param = SleepTimeParameter.AddFactorToParameter(param, SleepType.sleepTypeParamsAfter[session.sleepUserType], false);

                            await session.CalcSleepTimesRealTime(param, 0);

                            if (!session.foundSleep)
                            {
                                session.nf2 = "th haha";
                            }
                            else
                            {
                                session.nf2 = "tm: "+ session.times;

                            }
                        }
                        


                        await session.CalcData();
                    }
                    catch (Exception)
                    {

                        throw;
                    }

                    // Check for sleep times adjustment types
                    try
                    {
                        foreach (var item in SleepTimeClean.sleepCleanModelsAfter)
                        {
                            if (session.structureAwake == null ||  session.structureSleep == null || session.diffrence == null)
                            {
                                notpossible++;
                                continue;
                            }

                            found++;

                            if (item.Value.CheckIfIsTypeModel(param, session.structureAwake, session.structureSleep, session.diffrence))
                            {

                                a.Add(new Tuple<string, string, string>(user.sheetname, item.Key.ToString(), session.sleepDataEntrieSleep[0].FirstOrDefault().row.ToString()));
                                // a specific type was found

                                param = SleepTimeParameter.AddFactorToParameter(SleepTimeClean.sleepCleanParamsAfter[item.Key], SleepType.sleepTypeParamsAfter[session.sleepUserType], false);

                                await session.CalcSleepTimesRealTime(param, 1);
                                foundCando++;
                                break;
                            }
                        }
                    }
                    catch (Exception)
                    {

                        throw;
                    }


                    var paramState = SleepStateParameter.GetDefault();
                    //paramState = SleepParameter.AddFactorToParameter(paramState, SleepType.sleepTypeParamsAfter[session.sleepUserType]);

                    // Define diffrent sleep states in the sleep time 
                    try
                    {
                        await session.CalcSleepStatesWhileSleep(paramState,0);
                    }
                    catch (Exception ex)
                    {

                    }



                    // die Berechnung einspeichern
                    try
                    {
                        await session.WriteCalcData(false, user.sheetname);
                    }
                    catch (Exception ex)
                    {

                        throw;
                    }
                }
            }

            return true;
        }
    }
}
