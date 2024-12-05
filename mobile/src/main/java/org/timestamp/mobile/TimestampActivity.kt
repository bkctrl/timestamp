package org.timestamp.mobile

/**
 * https://stackoverflow.com/questions/72563673/google-authentication-with-firebase-and-jetpack-compose
 */

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.google.android.libraries.places.api.Places
import org.timestamp.mobile.models.EventViewModel
import org.timestamp.mobile.models.LocationViewModel
import org.timestamp.mobile.models.ThemeViewModel
import org.timestamp.mobile.utility.GoogleAPI

class TimestampActivity : ComponentActivity() {

    private lateinit var googleAPI: GoogleAPI
    private lateinit var mainNavController: MainNavController
    private val eventViewModel: EventViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup variables
        googleAPI = GoogleAPI(this)
        mainNavController = MainNavController(this.applicationContext, eventViewModel, themeViewModel, locationViewModel)

        // Setup Places API
        val apiKey = packageManager
            .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData
            .getString("com.google.android.geo.API_KEY")
        Places.initialize(this, apiKey!!)

        // ATTENTION: This was auto-generated to handle app links.
        val appLinkIntent: Intent = intent
        val appLinkData: Uri? = appLinkIntent.data
        eventViewModel.setPendingEventLink(appLinkData)

        // Main content
        setContent {
            mainNavController.TimestampNavController()
        }
    }

    override fun onStart() {
        super.onStart()

        // Restrict the receiver to only receive broadcasts from the app
        val filter = IntentFilter(ACTION_LOCATION_UPDATE)
        registerReceiver(locationViewModel.receiver, filter, RECEIVER_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(locationViewModel.receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
