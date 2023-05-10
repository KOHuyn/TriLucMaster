package com.mobileplus.dummytriluc.ui.utils.extensions

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


/**
 * Created by KO Huyn on 1/15/2021.
 */

fun AppCompatActivity.checkSelfPermissionCompat(permission: String):Boolean =
    ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

fun AppCompatActivity.requestPermissionsCompat(permissionsArray: Array<String>,
                                               requestCode: Int) {
    ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
}