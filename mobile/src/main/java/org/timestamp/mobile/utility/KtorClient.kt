package org.timestamp.mobile.utility

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

/**
 * Inject the Authorization header into the ktorClient.
 * Required for backend authorization & access.
 */
object KtorClient {

    val backend = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    init {
        backend.plugin(HttpSend).intercept { req ->
            val result = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()
            val token = result?.token ?: throw Exception("No token found")

            req.headers.append("Authorization", "Bearer $token")
            execute(req)
        }
    }

    /**
     * Handler for a request, performs try catch and updates
     * states if required.
     */
    suspend fun <T> handler(
        tag: String = "Backend Request",
        onError: suspend (e: Exception?) -> Unit = {},
        action: suspend () -> T?
    ): T? {
        try {
            return action()
        } catch (e: Exception) {
            Log.e(tag, e.toString())
            onError(e)
        }
        return null
    }


    /**
     * Check if we get a successful response. If not, then return false and
     * log the response values.
     */
    fun HttpResponse.success(tag: String = "Timestamp Request"): Boolean {
        if (this.status.isSuccess()) return true

        Log.e(tag, "${this.status.value} - ${this.status.description}: $this")
        return false
    }
}
