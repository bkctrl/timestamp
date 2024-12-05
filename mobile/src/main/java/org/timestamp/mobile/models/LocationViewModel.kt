package org.timestamp.mobile.models

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.mobile.INTENT_EXTRA_LOCATION
import org.timestamp.mobile.R
import org.timestamp.mobile.utility.KtorClient

class LocationViewModel (
    application: Application
) : AndroidViewModel(application) {

    private val base = application.getString(R.string.backend_url) // Base Url of backend
    private val ktorClient = KtorClient.backend

    private var _location: MutableStateFlow<LocationDTO?> = MutableStateFlow(null)
    val location: StateFlow<LocationDTO?> = _location.asStateFlow()

    // This is the receiver that will listen for location updates
    val receiver: BroadcastReceiver = LocationReceiver

    init {
        // Inject the location update function into the receiver
        LocationReceiver.onLocationUpdate = {
            _location.value = it
        }
    }

    private object LocationReceiver: BroadcastReceiver() {
        // We will inject the location update function into here
        lateinit var onLocationUpdate: ((LocationDTO) -> Unit)

        override fun onReceive(context: Context?, intent: Intent?) {
            val content = intent?.getStringExtra(INTENT_EXTRA_LOCATION) ?: return
            try {
                val locationDTO = Json.decodeFromString<LocationDTO>(content)
                onLocationUpdate(locationDTO)

                Log.d("LocationReceiver", "Received location update: $locationDTO")
            } catch (e: Exception) {
                Log.e("LocationReceiver", "Failed to parse location update: $content")
            }
        }
    }
}