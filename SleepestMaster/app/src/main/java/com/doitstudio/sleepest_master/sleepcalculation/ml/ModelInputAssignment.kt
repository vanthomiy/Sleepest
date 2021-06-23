package com.doitstudio.sleepest_master.sleepcalculation.ml


/**
 * Helper class to map the input on the input of a model. The necessary json files are stored in the Assets
 */
data class ModelInputAssignment (

        val shape : String,
        val name : String,
        val index : Int,
        val dtype : String

)

