package com.salonedriver.customClasses

import android.location.Location

interface LocationResultHandler {
    fun updatedLocation(location: Location)
}