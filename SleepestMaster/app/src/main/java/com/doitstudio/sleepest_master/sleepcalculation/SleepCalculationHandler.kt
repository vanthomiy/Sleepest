package com.doitstudio.sleepest_master.sleepcalculation

import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.MainViewModel

object SleepCalculationHandler {

    private val mainViewModel: MainViewModel by lazy {
        MainViewModel((application as MainApplication).repository)
    }

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
     *Resets the complete algorithm parameters (parameters to default)
     */
    public fun calculateSleepData() {

    }


}