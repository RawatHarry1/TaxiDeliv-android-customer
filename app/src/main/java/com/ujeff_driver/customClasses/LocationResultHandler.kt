package com.ujeff_driver.customClasses

import android.location.Location

interface LocationResultHandler {
    fun updatedLocation(location: Location)
}