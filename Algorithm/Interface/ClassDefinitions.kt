
class SleepTime{

}


class SleepBehaviour{
    position:MobilePosition

    sleepTimeStart:LocalDateTime
    sleepTimeEnd:LocalDateTime

    generalSleep:SleepDefinition
    deepSleep:SleepDefinition
    remSleep:SleepDefinition

    soundAdjustment:SleepDefinition
    overTimeAdjustment:SleepOverTimeAdjustment
    sleepStartCondition:SleepStartCondition

}

    /**
    * Resets the complete algorithm values (sleep data values).
    */
interface SleepDefinition{
    light:Int
    motion:Int
    sleep:Int
}

class SleepValue:SleepDefinition{
    time:LocalDateTime
}

class SleepOverTimeAdjustment:SleepDefinition{
    breakpoint:Int
}

class SleepStartCondition:SleepDefinition{
    sleepDecreaseOverTime:Int
}

enum MobilePosition{
    inBed,
    onTable,
    notDetected
}