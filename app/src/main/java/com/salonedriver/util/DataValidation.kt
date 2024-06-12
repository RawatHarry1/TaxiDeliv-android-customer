package com.salonedriver.util

import com.salonedriver.R
import com.salonedriver.SaloneDriver


/**
 * This is common validation validation model used to check validation type throught the application
 *
 * @property message error message that is to show
 * @property type identify the type of erro if there are mutiple error meassage on the same screen
 */
data class DataValidation(
    val message: String = SaloneDriver.instance.applicationContext
        .getString(R.string.something_went_wrong),
    var type: Int = 0
)
