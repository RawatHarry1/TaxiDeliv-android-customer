package com.salonedriver.listeners.permissionhelper

/**
 * This interface is used to get the user permission callback to the mActivity or fragment who
 * implements it
 */
interface IGetPermissionListener {
    fun requestPermission()
    fun permissionGiven()
    fun permissionCancel()
}

