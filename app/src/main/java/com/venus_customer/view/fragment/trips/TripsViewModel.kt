package com.venus_customer.view.fragment.trips

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TripsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is trips Fragment"
    }
    val text: LiveData<String> = _text
}