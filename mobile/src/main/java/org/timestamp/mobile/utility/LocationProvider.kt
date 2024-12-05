package org.timestamp.mobile.utility

import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.lib.dto.TravelMode

class LocationProvider(context: Context) {
    private val locationClient = LocationServices.getFusedLocationProviderClient(context)
    private val permission = PermissionProvider(context)
    private var locationCallback: LocationCallback? = null
    var travelMode: TravelMode = TravelMode.Car
    var locationDTO = LocationDTO(0.0, 0.0, travelMode)
        private set

    var isRunning = false
        private set

    /**
     * Start location updates, and run the callback every [intervalMillis]
     */
    fun startLocationUpdates(intervalMillis: Long = 30000L, callback: (LocationDTO) -> Unit) {
        if (!permission.fineLocationPermission || isRunning) return

        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .build()

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val lastLocation = result.lastLocation ?: return
                locationDTO = LocationDTO(
                    lastLocation.latitude,
                    lastLocation.longitude,
                    travelMode
                )
                callback(locationDTO)
            }
        }

        try {
            isRunning = true
            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch(e: SecurityException) {
            isRunning = false
            throw e
        }
    }
}