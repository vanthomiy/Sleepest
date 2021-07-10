package com.doitstudio.sleepest_master.sleepcalculation.ml


/**
 * Helper class to map the input on the input of a model.
 * The necessary json files are stored in the assets/ml/InputAssignmentsXXX
 * They are auto-created when creating a new ml model in the python code
 */
data class ModelInputAssignment(

        /**
         * The actual input shape that is expected by the model (should be [1 1])
         */
        val shape: String,
        /**
         * The name of the input shape: Its always serving_default_xxx where xxx is rather sleep, motion or brightness added by an index
         */
        val name: String,
        /**
         * The index of the actual input (Which input of the model)
         */
        val index: Int,
        /**
         * The data type of the input (Should always be int32)
         */
        val dtype: String

)

