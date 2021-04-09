using ExcelCalculationAddin.Model;
using ExcelCalculationAddin.Model.SleepStateDetect;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExcelCalculationAddin.Live
{
    public class CalcSleepLive
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
                foreach (var session in user.sleepSessionWhile)
                {
                    session.times = 0;
                    var param = SleepTimeParameter.GetDefault();
                    param = SleepTimeParameter.AddFactorToParameter(param, SleepType.sleepTimeParameter[session.sleepUserType], false);

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
                            session.sleepUserType = SleepType.UserFactorPattern.standard;
                            param = SleepTimeParameter.AddFactorToParameter(param, SleepType.sleepTimeParameter[session.sleepUserType], false);

                            await session.CalcSleepTimesRealTime(param, 0);

                            if (!session.foundSleep)
                            {
                                session.nf2 = "nf";
                            }

                        }
                        else if (session.times > 1)
                        {
                            session.nf1 = "tm: " + session.times;
                            session.sleepUserType = SleepType.UserFactorPattern.heavy;
                            session.times = 0;
                            param = SleepTimeParameter.AddFactorToParameter(param, SleepType.sleepTimeParameter[session.sleepUserType], false);

                            await session.CalcSleepTimesRealTime(param, 0);

                            if (!session.foundSleep)
                            {
                                session.nf2 = "th haha";
                            }
                            else
                            {
                                session.nf2 = "tm: " + session.times;

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
                        List<SleepTimeParameter> ssp = new List<SleepTimeParameter>();
                        session.rw2 = "Type ";
                        // Check if model is available
                        foreach (var item in SleepTimeClean.sleepTimeModelsWhile)
                        {
                            if (session.structureAwake == null || session.structureSleep == null || session.diffrence == null)
                            {
                                notpossible++;
                                continue;
                            }

                            found++;

                            if (item.Value.CheckIfIsTypeModel(param, session.structureAwake, session.structureSleep, session.diffrence))
                            {
                                session.rw2 += item.Key;

                                //a.Add(new Tuple<string, string, string>(user.sheetname, item.Key.ToString(), session.sleepDataEntrieSleep[0].FirstOrDefault().row.ToString()));
                                // a specific type was found

                                bool isNormal = (int)item.Key == 3 || (int)item.Key == 5;
                               // var aa = SleepTimeClean.sleepTimeParamsWhile[item.Value.sleepTimeModel];
                               // var bb = SleepType.sleepTimeParameter[session.sleepUserType];
                                ssp.Add(SleepTimeParameter.AddFactorToParameter(SleepTimeClean.sleepTimeParamsWhile[item.Key], SleepType.sleepTimeParameter[session.sleepUserType], isNormal));
                            }
                        }

                        param = SleepTimeParameter.Combine(ssp);

                        if (session.rw2 == "Type ")
                        {
                            session.rw2 = "nt";
                        }

                        /*
                        session.rw2 = "nf";
                        foreach (var item in SleepClean.sleepCleanModelsWhile)
                        {
                            if (session.structureAwake == null || session.structureSleep == null || session.diffrence == null)
                            {
                                notpossible++;
                                continue;
                            }

                            found++;

                            if (item.Value.CheckIfIsTypeModel(param, session.structureAwake, session.structureSleep, session.diffrence))
                            {
                                session.rw2 = "Type " + (int)item.Key + ":";
                                a.Add(new Tuple<string, string, string>(user.sheetname, item.Key.ToString(), session.sleepDataEntrieSleep[0].FirstOrDefault().row.ToString()));
                                // a specific type was found

                                param = SleepParameter.AddFactorToParameter(SleepClean.sleepCleanParamsWhile[item.Key], SleepType.sleepTypeParamsWhile[session.sleepUserType]);

                                await session.CalcSleepTimesRealTime(param, 1);
                                foundCando++;
                                break;
                            }
                        }

                        */
                    }
                    catch (Exception ex)
                    {

                        throw;
                    }


                    var paramState = SleepStateParameter.GetDefault();
                    //paramState = SleepParameter.AddFactorToParameter(paramState, SleepType.sleepTypeParamsAfter[session.sleepUserType]);

                    // Define diffrent sleep states in the sleep time 
                    try
                    {
                        await session.CalcSleepStatesWhileSleep(paramState, 0);


                        List<SleepStateParameter> ssp = new List<SleepStateParameter>();
                        session.f1 = "Type ";
                        // Check if model is available
                        foreach (var item in SleepStateClean.sleepStateModels)
                        {
                            if (session.structureAwake == null || session.structureSleep == null || session.diffrence == null)
                            {
                                notpossible++;
                                continue;
                            }

                            found++;

                            if (item.Value.CheckIfIsTypeModel(paramState, session.structureAwake, session.structureSleep, session.diffrence))
                            {
                                session.f1 += item.Key;

                                //a.Add(new Tuple<string, string, string>(user.sheetname, item.Key.ToString(), session.sleepDataEntrieSleep[0].FirstOrDefault().row.ToString()));
                                // a specific type was found

                                bool isNormal = (item.Key.StartsWith("3") || item.Key.StartsWith("5") || item.Key.StartsWith("7"));

                                ssp.Add(SleepStateParameter.AddFactorToParameter(SleepStateClean.sleepStateParams[item.Value.sleepStateModel], SleepType.sleepStateParameter[item.Value.sleepStateType], isNormal));
                            }
                        }

                        paramState = SleepStateParameter.Combine(ssp);

                        if (session.f1 == "Type ")
                        {
                            session.f1 = "nt";
                        }

                        await session.CalcSleepStatesWhileSleep(paramState, 1);

                    }
                    catch (Exception ex)
                    {

                    }


                    // die Berechnung einspeichern
                    try
                    {
                        await session.WriteCalcData(true, user.sheetname);
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
