package com.vyba_dri.customClasses

import android.location.Location

interface LocationResultHandler {
    fun updatedLocation(location: Location)
}