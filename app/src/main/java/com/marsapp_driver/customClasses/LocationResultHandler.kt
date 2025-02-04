package com.marsapp_driver.customClasses

import android.location.Location

interface LocationResultHandler {
    fun updatedLocation(location: Location)
}