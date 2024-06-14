package com.venus_customer.customClasses

import android.location.Location

interface LocationResultHandler {
    fun updatedLocation(location: Location)
}