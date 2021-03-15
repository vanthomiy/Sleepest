
/**
 * The sleep time of the user
 * @vanthomiy later implement sleep times for diffrent days ??
 */
class SleepTime{
    /**
     * Sleep Time start
     */
    timeStart:LocalDateTime

    /**
     * Sleep Time end
     */
    timeEnd:LocalDateTime
}

class SleepSegment{
    state:SleepState
    duration:Int
    startTime:LocalDataTime
    endTime:LocalDataTime
}

/**
 * Defines the actual sleep behaviour
 * it contains information and parameter about the sleep
 */
class SleepBehaviour{

    /**
     * The actual position of the mobile phone
     */
    position:MobilePosition

    /**
     * Defines whether user sleeps in complete darkness nor [eg. when shutters closed]
     */
    sleepInDark:Boolean

    /**
     * Defines the sleep times of the user
     */
    sleepTime:SleepTime

    /**
     * Definition: What is sleep
     */
    generalSleep:SleepDefinition
    /**
     * Definition: What is deep sleep
     */
    deepSleep:SleepDefinition
    /**
     * Definition: What is rem sleep
     */
    remSleep:SleepDefinition

    /**
     * Sound Adjustment parameters
     */
    soundAdjustment:SleepDefinition
    /**
     * Time Adjustment parameters
     */
    overTimeAdjustment:SleepOverTimeAdjustment
    /**
     * Detect the Sleep start parameters
     */
    sleepStartCondition:SleepStartCondition

}

/**
 * General Sleep value defintion
 * Containing light, motion and sleep state
 */
open class SleepDefinition{
    light:Int
    motion:Int
    sleep:Int
}

/**
 * A Sleep Value definition
 * Inherits from SleepDefinition and extends with localdatetime
 */
class SleepValue:SleepDefinition(){
    time:LocalDateTime
}

/**
 * A Sleep Time Adjustment definition
 * Inherits from SleepDefinition and extends with a breakpoint value 
 */
class SleepOverTimeAdjustment:SleepDefinition(){
    breakpoint:Int
}

/**
* A Sleep Start Condition definition
 * Inherits from SleepDefinition and extends with localdatetime
 */
class SleepStartCondition:SleepDefinition(){
    sleepDecreaseOverTime:Int
}

/**
 * Possible positions for the mobile phone while sleeping
*/
enum MobilePosition{
    inBed,
    onTable,
    notDetected
}

/**
 * Defines the possible states of a person
 */
enum SleepState{
    awake,
    lightSleep,
    deepSleep,
    remSleep
}
enum WeekDays{
    Mo,
    Tu,
    We,
    Th,
    Fr,
    Sa,
    Su
}