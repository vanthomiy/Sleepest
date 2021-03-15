import java.time.LocalDateTime
using ClassDefinitions

/**
* Sleep calculation handler as singleton
* Call it to get calculated sleep data
*/
object SleepCalculationHandler {

    // Public values

    /**
    * Resets the complete algorithm values (sleep data values).
    */
    public fun resetValues(){}

    /**
    *Resets the complete algorithm parameters (parameters to default)
    */    
    public fun resetParameter(){}

    /**
    *Returns the actual Wake up time with the actual values 
    */   
    public fun getWakeUpTime(): LocalDateTime { return wakeUpTime}

    /**
    *Returns the actual Sleep Segments (Its a class containing (Sleep/No Sleep and 0,1,2,3,4 sleep states)
    */    
    public fun getSleepSegments(): SleepTime { return sleepSegments }

    /**
    *Returns the actual sleep behaviour (containing information about wheter the phone is in bed or on a table and the parameters etc..)
    */    
    public fun getSleepBehaviour() : SleepBehaviour { return sleepBehaviour}
    
    /**
    *Adds a single Google Sleep API value to the last
    */
    public fun insertSingleSleepValue(passedSleepValue : SleepValue) {
        sleepValues.Add(passedSleepValue)

        // later check the value if all values are in timebounds and need to be stored....
        // When time is longer ago then clear from that list.

        // get all sleep segments
        sleepSegments =  sleepAlgorithm.calculateAllSleepData()

        // get the new wakeup time
        wakeUpTime = sleepAlgorithm.calculateWakeUpTime()

    }

    /**
    *Inserts list of Google Sleep API values
    */    
    public fun insertListOfSleepValues(passedSleepValues : List<SleepValue>){
        sleepValues = passedSleepValues;

        // later check the value if all values are in timebounds and need to be stored....
        // When time is longer ago then clear from that list.
        // get all sleep segments
        sleepSegments =  sleepAlgorithm.calculateAllSleepData()

        // get the new wakeup time
        wakeUpTime = sleepAlgorithm.calculateWakeUpTime()
    }

    /**
    *Pass changed parameters from the UI (User Input) here
    *@vanthomiy possible its better to allow single params change instead of passing complete sleep behaviour class
    */    
    public fun updateParametersByUser(passedSleepBehaviour: SleepBehaviour){
        sleepBehaviour=passedSleepBehaviour

    }

    /**
    *Recalculate parameters with user behaviour
    */    
    public fun updateParametersBySleepBehaviour(){
        sleepBehaviour = sleepAlgorith.calculateSleepBehaviour()
    }

    // Private Values
    private sleepBehaviour:SleepBehaviour 
            get() = field
            set(value){
                if(value != sleepBehaviour){

                    // Create new sleep algorithm
                    sleepAlgorithm = CalcSleepBed()

                    value = sleepBehaviour
                }
            }
    private sleepAlgorithm:ISleepAlgorithm
    private wakeUpAlgorithm:CalcWakeUpTime
    private sleepValues:List<SleepValue> 
    private sleepSegments:List<SleepSegment> 
    private wakeUpTime:LocalDataTime
}




/**
 * Interface for diffrent algorithms
 * There are diffrent algorithms for diffrent places of the Mobile phone...
 */
interface ISleepAlgorithm
{
    /**
     * Calculates the sleep data and returns a list of sleep segments over the time
     */
    fun calculateAllSleepData():List<SleepSegment>

    public fun calculateWakeUpTime():LocalDataTime{
        // lalalal

        return LocalDataTime.now()
    }

    public fun calculateSleepBehaviour():SleepBehaviour{
        
    }
}

/**
 * Calc sleep when phone is in bed
 */
class CalcSleepBed:ISleepAlgorithm{
    public override fun calculateAllSleepData():List<SleepSegment>
    {

    }
}

/**
 * Calc sleep when phone is on table
 */
class CalcSleepTable:ISleepAlgorithm{
    
}

/**
 * Calc sleep when there is no information about the position
 */
class CalcSleepWithoutInformation:ISleepAlgorithm{
    
}