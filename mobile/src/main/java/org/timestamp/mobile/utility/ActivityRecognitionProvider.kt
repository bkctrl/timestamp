package org.timestamp.mobile.utility

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.common.internal.safeparcel.SafeParcelableSerializer
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import org.timestamp.lib.dto.TravelMode
import org.timestamp.mobile.ACTION_DETECTED_ACTIVITY

class ActivityRecognitionProvider(
    private val context: Context
) {
    private val pmp = PermissionProvider(context)
    private val request = ActivityTransitionRequest(getTransitions())
    private val activityRecognitionClient = ActivityRecognition.getClient(context)
    private val intent = Intent(context, ActivityBroadcastReceiver::class.java).apply {
        action = ACTION_DETECTED_ACTIVITY
    }
    private val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_MUTABLE
    )

    private var isRunning = false
    var travelMode: TravelMode? = null
        private set

    init {
        ActivityBroadcastReceiver.updateTravelMode = {
            travelMode = it
        }

        val filter = IntentFilter(ACTION_DETECTED_ACTIVITY)
        context.registerReceiver(ActivityBroadcastReceiver, filter, Context.RECEIVER_EXPORTED)
    }

    fun cleanup() {
        context.unregisterReceiver(ActivityBroadcastReceiver)
        stopActivityRecognition()
    }

    @SuppressLint("MissingPermission")
    fun startActivityRecognition(
        callback: (TravelMode) -> Unit
    ) {
        if (isRunning || !pmp.activityRecognitionPermission) return

        // Set the callback
        ActivityBroadcastReceiver.callback = callback

        val task = activityRecognitionClient
            .requestActivityTransitionUpdates(request, pendingIntent)

        task.addOnSuccessListener {
            isRunning = true
            testTransition()
        }
        task.addOnFailureListener { isRunning = false }
    }

    @SuppressLint("MissingPermission")
    fun stopActivityRecognition() {
        if (!isRunning || !pmp.activityRecognitionPermission) return
        activityRecognitionClient.removeActivityUpdates(pendingIntent)
    }

    private fun testTransition(detectedActivity: Int = DetectedActivity.IN_VEHICLE) {
        val intent = Intent(ACTION_DETECTED_ACTIVITY)
        val events: ArrayList<ActivityTransitionEvent> = arrayListOf(
            ActivityTransitionEvent(
                detectedActivity,
                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
                SystemClock.elapsedRealtimeNanos()
            )
        )

        val result = ActivityTransitionResult(events)
        SafeParcelableSerializer.serializeToIntentExtra(
            result,
            intent,
            "com.google.android.location.internal.EXTRA_ACTIVITY_TRANSITION_RESULT"
        )

        context.sendBroadcast(intent)
    }

    private fun getTransitions(): MutableList<ActivityTransition> {

        val transitions: MutableList<ActivityTransition> = mutableListOf()

        fun addTransition(activityType: Int) {
            transitions +=
                ActivityTransition.Builder()
                    .setActivityType(activityType)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build()
        }

        addTransition(DetectedActivity.STILL)
        addTransition(DetectedActivity.WALKING)
        addTransition(DetectedActivity.ON_BICYCLE)
        addTransition(DetectedActivity.IN_VEHICLE)

        return transitions
    }

    private object ActivityBroadcastReceiver: BroadcastReceiver() {

        lateinit var updateTravelMode: (TravelMode) -> Unit
        lateinit var callback: (TravelMode) -> Unit

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_DETECTED_ACTIVITY) return
            if (!ActivityTransitionResult.hasResult(intent)) return

            val result = ActivityTransitionResult.extractResult(intent) ?: return
            val detectedActivity = result.transitionEvents.singleOrNull() ?: return

            val travelMode = toTravelMode(detectedActivity.activityType)
            updateTravelMode(travelMode)
            callback(travelMode)

            Log.d("ActivityRecognition", "Detected activity: $travelMode")
        }

        private fun toTravelMode(activityType: Int): TravelMode {
            return when (activityType) {
                DetectedActivity.ON_BICYCLE -> TravelMode.Bike
                DetectedActivity.IN_VEHICLE -> TravelMode.Car
                else -> TravelMode.Foot
            }
        }
    }
}