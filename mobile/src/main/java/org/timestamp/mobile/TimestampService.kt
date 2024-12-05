package org.timestamp.mobile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.lib.dto.NotificationDTO
import org.timestamp.lib.dto.TravelMode
import org.timestamp.mobile.utility.ActivityRecognitionProvider
import org.timestamp.mobile.utility.KtorClient
import org.timestamp.mobile.utility.KtorClient.handler
import org.timestamp.mobile.utility.KtorClient.success
import org.timestamp.mobile.utility.LocationProvider
import org.timestamp.mobile.utility.PermissionProvider
import java.time.format.DateTimeFormatter

const val NOTIFICATION_ID = 1
const val EVENT_NOTIFICATION_ID = 2
const val CHANNEL_ID = "location"
const val CHANNEL_NAME = "Timestamp Service"
const val ACTION_LOCATION_UPDATE = "org.timestamp.mobile.LOCATION_UPDATE"
const val ACTION_DETECTED_ACTIVITY = "org.timestamp.mobile.DETECTED_ACTIVITY"
const val INTENT_EXTRA_LOCATION = "location"
const val FIVE_MINUTES = 300000L
const val THIRTY_SECONDS = 30000L

/**
 * A foreground service used to send the backend updates on the users'
 * current location
 */
class TimestampService: Service() {

    private lateinit var pmp: PermissionProvider
    private lateinit var lp: LocationProvider
    private lateinit var arp: ActivityRecognitionProvider
    private lateinit var ktorClient: HttpClient
    private lateinit var largeIcon: Bitmap
    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    private var notification: NotificationDTO? = null
    private var pollingNotification = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        lp = LocationProvider(this)
        pmp = PermissionProvider(this)
        arp = ActivityRecognitionProvider(this)
        ktorClient = KtorClient.backend

        val decodedIcon = BitmapFactory.decodeResource(this.resources, R.drawable.cs346logoteef)
        largeIcon = Bitmap.createScaledBitmap(decodedIcon, 128, 128, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingNotification = false
        arp.cleanup()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!pmp.fineLocationPermission || !pmp.activityRecognitionPermission) {
            Log.d("LocationService", "Permissions not granted")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            startForeground(NOTIFICATION_ID, createBasicNotification())
            getTimestampNotification(FIVE_MINUTES)
            arp.startActivityRecognition { travelMode ->
                lp.travelMode = travelMode
            }
            lp.startLocationUpdates(THIRTY_SECONDS) {
                broadcastLocation(it)
                updateLocation(it)
            }
        } catch (e: SecurityException) {
            Log.d("LocationService", "Permissions not granted")
            arp.stopActivityRecognition()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        } catch (e: Exception) {
            Log.e("LocationService", "Error starting location updates", e)
            arp.stopActivityRecognition()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        serviceChannel.setSound(null, null)
        serviceChannel.setShowBadge(true)
        notificationManager.createNotificationChannel(serviceChannel)
    }

    private fun replaceNotification(title: String, text: String) {
        val notification = createBasicNotification(title, text)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun replaceNotification(notification: NotificationDTO) {
        notificationManager.notify(EVENT_NOTIFICATION_ID, createEventNotification(notification))
    }

    /**
     * Create a notification to show the user that the service is running
     * and tracking their location.
     */
    private fun createBasicNotification(
        title: String = "Timestamp",
        text: String = "Timestamp is tracking your location...",

    ): Notification {
        // Open timestamp when the notification is clicked
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, TimestampActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.cs346logoteefsmaller)
            .setLargeIcon(largeIcon)

        return notificationBuilder.build()
    }

    /**
     * Create a custom notification to show the next upcoming event
     * and the estimated time to get there.
     */
    private fun createEventNotification(
        notification: NotificationDTO
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, TimestampActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dateTimeFormatter = DateTimeFormatter.ofPattern("H:mm a")
        val layout = RemoteViews(this.packageName, R.layout.notification)
        val layoutCollapsed = RemoteViews(this.packageName, R.layout.notification_collapsed)
        val eventName = notification.event.name
        val eventTime = notification.event.arrival.format(dateTimeFormatter)
        layoutCollapsed.setTextViewText(R.id.notification_title, eventName)
        layoutCollapsed.setTextViewText(R.id.notification_event_time, eventTime)
        layout.setTextViewText(R.id.notification_title, eventName)
        layout.setTextViewText(R.id.notification_event_time, eventTime)

        for (routeInfo in notification.routeInfos) {
            val timeEst = routeInfo.timeEst
            val time = if (timeEst == null) "Not Calculated" else formatDuration(timeEst)
            val viewId = when (routeInfo.travelMode) {
                TravelMode.Car -> R.id.notification_car_time
                TravelMode.Bike -> R.id.notification_bike_time
                else -> R.id.notification_walk_time
            }

            layout.setTextViewText(viewId, "${routeInfo.travelMode!!.name}: $time")
        }

        val notificationBuilder = NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setCustomContentView(layoutCollapsed)
            .setCustomBigContentView(layout)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.cs346logoteefsmaller)
            .setLargeIcon(largeIcon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        return notificationBuilder.build()
    }

    /**
     * Broadcast the current location to Broadcast Receivers listening
     * for location updates. For instance, the AppViewModel listens for
     * location updates to use for UI
     */
    private fun broadcastLocation(locationDTO: LocationDTO) {
        val intent = Intent(ACTION_LOCATION_UPDATE)
        val content = Json.encodeToString(locationDTO)
        intent.putExtra(INTENT_EXTRA_LOCATION, content)
        sendBroadcast(intent)
    }

    /**
     * Update the backend with the current location.
     */
    private fun updateLocation(location: LocationDTO) = CoroutineScope(Dispatchers.IO).launch {
        val base = getString(R.string.backend_url)
        val tag = "Update Location"
        handler(tag) {
            val endpoint = "$base/users/me/location"
            val res = ktorClient.patch(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(location)
            }

            if (!res.success(tag)) return@handler
            Log.d(tag, "Updated location with $location")
        }
    }

    private fun getTimestampNotification(
        interval: Long = 30000L
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (pollingNotification) return@launch

        pollingNotification = true

        val base = getString(R.string.backend_url)
        val tag = "Get Notification"

        while(pollingNotification) {
            handler(tag) {
                val endpoint = "$base/users/me/notifications"
                val res = ktorClient.get(endpoint)
                if (!res.success(tag)) return@handler
                val tmp = res.body<NotificationDTO>()

                // If the notification hasn't changed, don't update the notification
                if (tmp == notification) return@handler

                notification = tmp
                replaceNotification(notification!!)
                Log.d(tag, notification.toString())
            }

            delay(interval)
        }
    }

    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days day${if (days > 1) "s" else ""}"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}"
            else -> "$seconds second${if (seconds > 1) "s" else ""}"
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}