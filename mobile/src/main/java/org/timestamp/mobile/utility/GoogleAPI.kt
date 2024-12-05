package org.timestamp.mobile.utility

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.timestamp.mobile.TimestampActivity
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * This class is used to access google api endpoints
 * e.g. Calendar, Gmail
 */
class GoogleAPI(
    private val activity: TimestampActivity,
    private val googleScopes: List<Scope> = listOf(
        Scope("https://www.googleapis.com/auth/calendar")
    )
) {
    // Used to pause state of google API authorization if we rely on user input
    private var googleContinuation: (Continuation<AuthorizationResult>)? = null

    // Acquire a new access token, and run any function necessary after
    private val reqAuthIntentLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        Log.i("GoogleAPI Authorization", "Result Launcher")
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val authorizationResult = Identity
                .getAuthorizationClient(activity)
                .getAuthorizationResultFromIntent(data)

            Log.i("GoogleAPI Authorization", authorizationResult.accessToken!!)
            googleContinuation?.resume(authorizationResult)
        } else {
            googleContinuation?.resumeWithException(Exception("Authorization canceled"))
        }

        googleContinuation = null // Safety precaution
    }

    /**
     * Function to get the access token from google for google calendar etc.
     */
    suspend fun getGoogleAccessToken() = authorizeGoogleAPI(googleScopes)

    /**
     * Authorize Google API scopes, and sets up a user-flow for permissions if required.
     * Returns an AuthorizationResult which contains the access token.
     */
    private suspend fun authorizeGoogleAPI(
        scopes: List<Scope>
    ): AuthorizationResult? {
        try {
            // Attempt to obtain authorization
            val authorizationReq = AuthorizationRequest.Builder().setRequestedScopes(scopes).build()
            var result = Identity.getAuthorizationClient(activity).authorize(authorizationReq).await()
            val pendingIntent = result.pendingIntent // Verify intent

            if (result.hasResolution() && pendingIntent != null) { // Need user-flow permission
                try {
                    val intent = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                    result = waitForAuthIntent(intent)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("GoogleAPI Authorization",
                        "Couldn't start Authorization UI: ${e.localizedMessage}"
                    )
                }
            } else {
                Log.i("GoogleAPI Authorization", "Already Granted")
            }

            return result
        } catch (e: Exception) {
            Log.e("GoogleAPI Authorization", "Failed", e)
        }

        return null
    }

    /**
     * Return an AuthorizationResult after waiting for the reqAuthIntentLauncher to finish.
     * That is, the user flow to admit permissions.
     */
    private suspend fun waitForAuthIntent(
        intentSenderRequest: IntentSenderRequest
    ): AuthorizationResult = suspendCancellableCoroutine { continuation ->
        // Wait for the user flow to complete and then exec
        googleContinuation = continuation
        try {
            reqAuthIntentLauncher.launch(intentSenderRequest) // pause here
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }

        // Cleanup in case
        continuation.invokeOnCancellation {
            googleContinuation = null
        }
    }

    /**
     * Test function to check access tokens
     */
    private suspend fun fetchCalendarEvents(accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.googleapis.com/calendar/v3/calendars/primary/events")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            val responseCode = connection.responseCode
            return@withContext if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { block ->
                    val text = block.readText()
                    Log.i("Calendar Text", text)
                    text
                }
            } else {
                Log.e("GoogleCalendar", "Error fetching events: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("GoogleCalendar", "Exception: ${e.message}")
            null
        }
    }
}