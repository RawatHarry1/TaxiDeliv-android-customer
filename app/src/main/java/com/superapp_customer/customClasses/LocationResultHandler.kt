package com.superapp_customer.customClasses

import android.location.Location

interface LocationResultHandler {
    fun updatedLocation(location: Location)
}