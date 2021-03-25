using ExcelCalculationAddin.Model;
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
                foreach (var session in user.sleepSession)
                {
                    try
                    {
                        await session.CalcSleepTimesRealTime(SleepParameter.GetDefault());
                    }
                    catch (Exception)
                    {

                        throw;
                    }

                    try
                    {
                        session.rw = "";
                        await session.CalcData();
                    }
                    catch (Exception)
                    {

                        throw;
                    }

                    // Check for sleep times adjustment types
                    try
                    {
                        foreach (var item in SleepClean.sleepCleanModels)
                        {
                            if (session.structureAwake == null ||  session.structureSleep == null || session.diffrence == null)
                            {
                                notpossible++;
                                continue;
                            }

                            found++;

                            if (item.Value.CheckIfIsTypeModel(session.structureAwake, session.structureSleep, session.diffrence))
                            {

                                a.Add(new Tuple<string, string, string>(user.sheetname, item.Key.ToString(), session.sleepDataEntrieSleep[0].FirstOrDefault().row.ToString()));
                                // a specific type was found
                                await session.CalcSleepTimesRealTime(SleepClean.sleepCleanParams[item.Key], 1);
                                foundCando++;
                                break;
                            }
                        }
                    }
                    catch (Exception)
                    {

                        throw;
                    }


                    // finde einen passenden schlaftyp später
                    //await session.CalcSleepData(ReadParameter.parameters);

                    // die Berechnung einspeichern
                    try
                    {
                        await session.WriteCalcData(user.sheetname);
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
