package com.marsapp_driver.util.permissionhelper


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.marsapp_driver.R
import com.marsapp_driver.SaloneDriver
import com.marsapp_driver.listeners.permissionhelper.IGetPermissionListener


const val PERMISION_GRANTED = 1
const val PERMISION_REVOKED = 2
const val PERMISION_RATIONAL = 3

class PermissionHelper {
    /**
     * A [IGetPermissionListener] object to sends call back to the Activity which implements it
     */
    private lateinit var mGetPermissionListener: IGetPermissionListener

    fun setListener(mGetPermissionListener: IGetPermissionListener) {
        this.mGetPermissionListener = mGetPermissionListener
    }

    /**
     * A [Activity] object to get the context of the Activity from where it is called
     */
    private var mActivity: AppCompatActivity? = null
    private var fragment: Fragment? = null

    /**
     * Method to check any permission. It will return true if permission granted
     * otherwise false
     *
     * @param requestCode is the code given for which permission is to be taken
     * @param context     of the activity
     * @param permissions that we want to take from the user
     * @return true if permission granted otherwise false
     */

    fun setActivity(activity: AppCompatActivity) {
        mActivity = activity
    }

    fun hasPermission(permissions: Array<String>): Boolean {
        var isAllGranted = true
        //check for all devices
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    mActivity!!,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                isAllGranted = false
            }
        }
        //check if all granted or not
        return if (!isAllGranted) {
            mGetPermissionListener.requestPermission()
            false
        } else true
    }

    /**
     * Method to check any permission. It will return true if permission granted
     * otherwise false
     *
     * @param mActivity
     * @param fragment    of the activity
     * @param permissions that we want to take from the user
     * @param requestCode is the code given for which permission is to be taken
     * @return true if permission granted otherwise false
     */
    /*fun hasPermissionInFragment(
        mActivity: AppCompatActivity?,
        fragment: Fragment,
        permissions: Array<String>,
        requestCode: Int
    ): Boolean {
        this.mActivity = mActivity
        this.fragment = fragment
        var isAllGranted = true
        //check for all devices
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    fragment.requireContext()
                    , permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                isAllGranted = false
            }
        }
        //check if all granted or not
        return if (!isAllGranted) {
            fragment.requestPermissions(permissions, requestCode)
            false
        } else true
    }*/

    /**
     * Method to check any permission. It will return true if permission granted
     * otherwise false
     *
     * @param context     of the activity
     * @param permissions that we want to take from the user
     * @return true if permission granted otherwise false
     */
    fun checkPermissionGrantOrNot(
        context: Context?,
        permissions: Array<String>
    ): Boolean {
        var isAllGranted = true
        //check for all devices
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                isAllGranted = false
                break
            }
        }
        //check if all granted or not
        return isAllGranted
    }

    /**
     * This method is used to set the result when
     * gets the callback of the permission
     *
     * @param requestCode  code at which particular permission was asked
     * @param permissions  name of the permission taken from the user
     * @param grantResults gives the result whether user grants or denies the permission
     */
    /*fun setPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray?
    ) {
        var isAllPermissionGranted = PERMISION_GRANTED
        for (i in permissions.indices) {


            if (grantResults!![i] == PackageManager.PERMISSION_GRANTED) {
                isAllPermissionGranted = PERMISION_GRANTED
            }

            else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    mActivity!!,
                    permissions[i]
                )
            ) {
                isAllPermissionGranted = PERMISION_REVOKED
                break
            } else {
                isAllPermissionGranted = PERMISION_RATIONAL
                break
            }
        }

        when (isAllPermissionGranted) {
            PERMISION_GRANTED -> {
                mGetPermissionListener.permissionGiven()
            }
            PERMISION_REVOKED -> {
                mGetPermissionListener.permissionCancel()
            }
            PERMISION_RATIONAL -> {
                showDialog(
                    BaseApplication.instance.resources?.getString(R.string.permission_from_device)!!,
                )
            }
        }
    }*/

    fun setPermissionResult(permissionMap: MutableMap<String, Boolean>) {
        var isAllPermissionGranted = PERMISION_GRANTED
        for (permissionResult in permissionMap) {

            if (permissionResult.value) {
                isAllPermissionGranted = PERMISION_GRANTED
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    mActivity!!,
                    permissionResult.key
                )
            ) {
                isAllPermissionGranted = PERMISION_REVOKED
                break
            } else {
                isAllPermissionGranted = PERMISION_RATIONAL
                break
            }
        }

        when (isAllPermissionGranted) {
            PERMISION_GRANTED -> {
                mGetPermissionListener.permissionGiven()
            }

            PERMISION_REVOKED -> {
                mGetPermissionListener.permissionCancel()
            }

            PERMISION_RATIONAL -> {
                showDialog(
                    SaloneDriver.appContext.resources?.getString(R.string.permission_from_device)!!,
                )
            }
        }
    }


    /*
     * method is used to show the setting go dialog
     * */
    private fun showDialog(message: String) {
        val builder = AlertDialog.Builder(mActivity)
        builder.setMessage(message)
            .setCancelable(false)
            .setNegativeButton(
                mActivity!!.resources.getString(R.string.cancel)
            ) { dialog, which ->
                dialog.dismiss()
                mGetPermissionListener.permissionCancel()
            }
            .setPositiveButton(
                mActivity!!.resources.getString(R.string.ok)
            ) { dialog, which ->
                if (fragment != null) {
                    fragment!!.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + SaloneDriver.appContext.packageName)
                        )
                    )
                } else {
                    mActivity!!.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + SaloneDriver.appContext.packageName)
                        )
                    )
                }
            }
        val alert = builder.create()
        alert.show()
    }


}