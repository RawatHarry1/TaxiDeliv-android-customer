package com.venus_customer.util

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.venus_customer.VenusApp
import com.venus_customer.model.api.convertGsonString
import com.venus_customer.model.api.convertStringIntoClass
import com.venus_customer.view.activity.walk_though.WalkThrough

object SharedPreferencesManager {

    private var mInstance: SharedPreferences? = null

    fun getInstance(): SharedPreferences {
        if (mInstance == null)
            mInstance =
                PreferenceManager.getDefaultSharedPreferences(VenusApp.instance.applicationContext)
        return mInstance!!
    }

    fun getInt(key: String): Int {
        return getInstance().getInt(key, 0)
    }

    fun put(key: String, value: Int) {
        val editor = getInstance().edit()
        editor.putInt(key, value)
        // Commit the edits!
        editor.apply()
    }

    fun put(key: String, value: Long) {
        val editor = getInstance().edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String): Long {
        return getInstance().getLong(key, 0)
    }

    fun put(key: String, value: Float) {
        val editor = getInstance().edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun getFloat(key: String): Float? {
        return getInstance().getFloat(key, 0f)
    }

    fun put(key: String, value: Boolean) {
        val editor = getInstance().edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean {
        return getInstance().getBoolean(key, false)
    }

    fun put(key: String, value: String) {
        val editor = getInstance().edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String {
        return getInstance().getString(key, "")!!
    }

    fun clearAllPreferences() {
        val editor = getInstance().edit()
        editor.clear()
        editor.apply()
    }


    fun <T> putModel(key: String, value: T){
        val editor = getInstance().edit()
        editor.putString(key, value.convertGsonString())
        editor.apply()
    }


    inline fun <reified T> getModel(key: String): T? {
        val data = getInstance().getString(key, "")
        return if (data?.isNotEmpty() == true) data.convertStringIntoClass() else null
    }

    fun clearKeyData(key: String) {
        val editor = getInstance().edit()
        editor.putString(key, null)
        editor.apply()
    }


    fun clearAllData() {
        val editor = getInstance().edit()
        editor.clear()
        editor.apply()
    }




    object Keys {
        const val CLIENT_CONFIG = "clientConfig"
        const val USER_DATA = "userData"
        const val WALKTHROUGH = "walkThrough"
        const val TRIP_ID = "tripId"
        const val SESSION_ID = "session_id"
    }
}