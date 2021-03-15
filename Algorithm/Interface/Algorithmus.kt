import java.time.LocalDateTime
using ClassDefinitions

// Example Interface for the algorithm and the Use-Case
// This is the first thing that is definied... no code inside (or fake return values) but nothing much to do here
// Just defining the accessable Values and describe them 
object SleepCalculationHandler {

    /**
    * Resets the complete algorithm values (sleep data values).
    */
    fun resetValues(){}

    // Resets the complete algorithm parameters (parameters to default)
    fun resetParameter(){}

    // Returns the actual Wake up time with the actual values 
    fun getWakeUpTime(): LocalDateTime { return LocalDateTime.now()}

    // Returns the actual Sleep Time (Its a class containing (Sleep/No Sleep and 0,1,2,3,4 sleep states)
    fun getSleepTime(): SleepTime { return SleepTime()}

    // Returns the actual sleep behaviour (containing information about wheter the phone is in bed or on a table and the parameters etc..)
    fun getSleepBehaviour() : SleepBehaviour { return SleepBehaviour()}
    
    // A event that is fired when a new wake up time is calculated
    event onAwakeTimeUpdate

    // A event that is fired when a new sleep time is calculated
    event onSleepTimeUpdate

    // A event that is fired when a new sleep behaviour is calculated
    event onSleepBehaviourUpdate

    // Insert a single Google Sleep API value
    fun insertSingleSleepValue(sleepValue : SleepValue) {}

    // Inserts list of Google Sleep API values
    fun insertListOfSleepValues(sleepValues : List<SleepValue>){}

    // Pass changed parameters from the UI (User Input) here
    fun updateParametersByUser(sleepCalculationParams: SleepCalculationParams){}

    // Recalculate parameters with user behaviour
    fun updateParametersBySleepBehaviour(sleepCalculationParams: SleepCalculationParams){}
}

