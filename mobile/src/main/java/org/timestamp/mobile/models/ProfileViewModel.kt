package org.timestamp.mobile.models

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.net.Uri
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.mobile.INTENT_EXTRA_LOCATION
import org.timestamp.mobile.R
import org.timestamp.mobile.utility.KtorClient

class ProfileViewModel (
    private val application: Application
) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()

    private val _pfpMarker: MutableStateFlow<BitmapDescriptor?> = MutableStateFlow(null)
    val pfpMarker = _pfpMarker.asStateFlow()

    private var currentPhotoUrl: Uri? = null

    init {
        updatePfpRequest() // Initialize new pfp marker
    }

    fun updatePfpRequest() {
        val photoUrl = auth.currentUser?.photoUrl
        if (photoUrl != currentPhotoUrl) return

        currentPhotoUrl = photoUrl

        val request = ImageRequest.Builder(application.applicationContext)
            .data(photoUrl)
            .size(Size.ORIGINAL)
            .target{ result ->
                val bitmap = result.toBitmap().toCircularBitmap()
                _pfpMarker.value = BitmapDescriptorFactory.fromBitmap(bitmap)
            }
            .build()

        val loader = ImageLoader(context = application.applicationContext)
        loader.enqueue(request)
    }

    private fun Bitmap.toCircularBitmap(): Bitmap {
        // First, ensure we have a software bitmap
        val softwareBitmap = if (this.isMutable) {
            this
        } else {
            this.copy(Bitmap.Config.ARGB_8888, true)
        }

        val size = minOf(softwareBitmap.width, softwareBitmap.height)
        val output = Bitmap.createBitmap(size, size + 20, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(softwareBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val centerX = size / 2f
        val centerRadius = size / 2f

        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        canvas.drawCircle(centerX, centerX, centerRadius - 5, borderPaint)
        canvas.drawCircle(centerX, centerX, centerRadius - 7, paint)

        val trianglePaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.BLACK
        }

        val path = Path().apply {
            moveTo(centerX - 15f, centerX + centerRadius - 2) // Left point of the triangle
            lineTo(centerX + 15f, centerX + centerRadius - 2) // Right point of the triangle
            lineTo(centerX, centerX + centerRadius + 25f) // Bottom point of the triangle
            close()
        }

        canvas.drawPath(path, trianglePaint)

        return output
    }
}