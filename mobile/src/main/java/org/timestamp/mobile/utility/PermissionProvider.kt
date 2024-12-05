package org.timestamp.mobile.utility

import android.content.Context
import android.content.pm.PackageManager

class PermissionProvider(private val context: Context) {
    val fineLocationPermission: Boolean
        get() = checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val coarseLocationPermission: Boolean
        get() = checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    val backgroundLocationPermission: Boolean
        get() = checkPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    val activityRecognitionPermission: Boolean
        get() = checkPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
    val postNotificationPermission: Boolean
        get() = checkPermission(android.Manifest.permission.POST_NOTIFICATIONS)

    private fun checkPermission(permission: String): Boolean {
        val check = context.checkSelfPermission(permission)
        return check == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        val PERMISSIONS = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACTIVITY_RECOGNITION,
            android.Manifest.permission.POST_NOTIFICATIONS
        )

        const val BACKGROUND_LOCATION = android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    }
}