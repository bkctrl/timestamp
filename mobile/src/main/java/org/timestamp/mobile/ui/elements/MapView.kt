package org.timestamp.mobile.ui.elements

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.maps.android.compose.rememberCameraPositionState
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.mobile.R
import org.timestamp.mobile.TimestampActivity
import org.timestamp.mobile.models.EventViewModel
import org.timestamp.mobile.models.LocationViewModel
import org.timestamp.mobile.models.ThemeViewModel
import org.timestamp.mobile.ui.screens.ubuntuFontFamily
import org.timestamp.mobile.ui.theme.Colors
import java.sql.Time
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Bitmap.toCircularBitmap(): Bitmap {
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

fun Bitmap.toWhiteBitmap(): Bitmap {
    val mutableBitmap = copy(Bitmap.Config.ARGB_8888, true)
    // Create a canvas to draw on the mutable bitmap
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        colorFilter = PorterDuffColorFilter(android.graphics.Color.WHITE, PorterDuff.Mode.SRC_IN)
    }
    // Draw the original bitmap onto the canvas using the paint
    canvas.drawBitmap(this, 0f, 0f, paint)
    return mutableBitmap
}

fun Bitmap.createScaledBitmap(newWidth: Int, newHeight: Int): Bitmap {
    return Bitmap.createScaledBitmap(this, newWidth, newHeight, false)
}

@Composable
fun MapView(
    eventViewModel: EventViewModel = viewModel(LocalContext.current as TimestampActivity),
    locationViewModel: LocationViewModel = viewModel(LocalContext.current as TimestampActivity),
    currentUser: FirebaseUser?
) {
    val themeViewModel: ThemeViewModel = viewModel(LocalContext.current as TimestampActivity)

    var googleMapInstance: GoogleMap? by remember { mutableStateOf(null) }
    var userMarker: Marker? by remember { mutableStateOf(null) }

    val context = LocalContext.current
    val density = LocalDensity.current
    val eventListState = eventViewModel.events.collectAsState()
    val eventList: MutableList<EventDTO> = eventListState.value.toMutableList()

    val locationState by locationViewModel.location.collectAsState()
    val userLocation : LocationDTO? = locationState
    var eventCoordinates by remember { mutableStateOf(0 to 0) }

    val darkThemeState by themeViewModel.isDarkTheme.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = userLocation?.let { CameraPosition.fromLatLngZoom(LatLng(userLocation.latitude, userLocation.longitude), 12f) } ?:
        CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 12f)
    }

    val now = OffsetDateTime.now()

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("Event Select") }
    var dropTextFieldSize by remember { mutableStateOf(DpSize.Zero)}
    var isEventShowing by remember { mutableStateOf<EventDTO?>(null) }
    var pfpMarker by remember { mutableStateOf<BitmapDescriptor?>(null) }
    val pfp = currentUser?.photoUrl
    val loader = ImageLoader(context = LocalContext.current)
    val request = ImageRequest.Builder(LocalContext.current)
        .data(pfp)
        .size(Size.ORIGINAL)
        .target{ result ->
            val bitmap = result.toBitmap().toCircularBitmap()
            pfpMarker = BitmapDescriptorFactory.fromBitmap(bitmap)
        }
        .build()
    loader.enqueue(request)

    LaunchedEffect(cameraPositionState.position) {
        googleMapInstance?.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPositionState.position)
        )
    }

    LaunchedEffect(locationState) {
        locationState?.let { location ->
            userMarker = googleMapInstance?.addMarker(
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .icon(pfpMarker)
                    .title("My Location")
            )
        }
    }

    LaunchedEffect(pfpMarker) {
        if (pfpMarker != null && locationState != null) {
            // Remove the old marker if it exists
            userMarker?.remove()

            userMarker = googleMapInstance?.addMarker(
                MarkerOptions()
                    .position(LatLng(locationState!!.latitude, locationState!!.longitude))
                    .icon(pfpMarker)
                    .title("My Location")
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 64.dp)
            .background(MaterialTheme.colors.primary)
            .pointerInput(Unit) {
                detectTapGestures {  }
            }
    ) {
        AndroidView(
            modifier = Modifier
                .matchParentSize(),
            factory = { context ->
                MapView(context).apply {
                    onCreate(Bundle())
                    onResume()
                    getMapAsync(OnMapReadyCallback { googleMap ->
                        googleMapInstance = googleMap
                        googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                context, if (darkThemeState) R.raw.darkmap else R.raw.lightmap
                            )
                        )
                        googleMap.uiSettings.isZoomControlsEnabled = true
                        googleMap.uiSettings.isMyLocationButtonEnabled = true

                        for (event in eventList) {
                            var bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.event_location)
                            if (darkThemeState) bitmap = bitmap.toWhiteBitmap()
                            bitmap = bitmap.createScaledBitmap(100, 100)
                            val marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(LatLng(event.latitude, event.longitude))
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                    .anchor(0.5f, 1.25f)
                            )
                            marker?.tag = event.id
                        }

                        googleMap.setOnMarkerClickListener { marker ->
                            if (marker.id != userMarker?.id) {
                                val event = eventList.find { marker.tag == it.id }
                                isEventShowing = null
                                isEventShowing = event
                                val latLng =
                                    isEventShowing?.let { LatLng(it.latitude, it.longitude) }

                                if (latLng != null) {
                                    googleMap.projection.toScreenLocation(latLng).let { screenLocation ->
                                        // Handle screen coordinates (X, Y)
                                        val screenX = screenLocation.x
                                        val screenY = screenLocation.y
                                        eventCoordinates = (screenX to screenY)
                                    }
                                }
                            }
                            false
                        }

                        googleMap.setOnMapClickListener {
                            isEventShowing = null
                        }

                        googleMap.setOnCameraMoveListener {
                            val latLng =
                                isEventShowing?.let { LatLng(it.latitude, it.longitude) }  // Example coordinates (replace with your own)

                            if (latLng != null) {
                                googleMap.projection.toScreenLocation(latLng).let { screenLocation ->
                                    // Handle screen coordinates (X, Y)
                                    val screenX = screenLocation.x
                                    val screenY = screenLocation.y
                                    eventCoordinates = (
                                        screenX
                                     to
                                        screenY
                                    )
                                }
                            }
                        }

                        locationState?.let { location ->
                            googleMapInstance?.addMarker(
                                MarkerOptions()
                                    .position(LatLng(location.latitude, location.longitude))
                                    .icon(pfpMarker)
                                    .title("My Location")
                            )
                            googleMapInstance?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 10f)
                            )
                        }

                    })
                }
            },
            update = { mapView ->
                mapView.onResume()
            }
        )
        if (isEventShowing != null) {
            val isToday = isEventShowing!!.arrival.isBefore(now.plusHours(24))
            Box(modifier = Modifier
                .width(200.dp)
                .height(150.dp)
                .offset {
                    IntOffset(x = eventCoordinates.first,
                        y = eventCoordinates.second)
                }
                .offset(x = (-100).dp, y = (-170).dp)
                .shadow(5.dp, shape = RoundedCornerShape(32.dp))
                .background(color = MaterialTheme.colors.primary, shape = RoundedCornerShape(32.dp))
                .border(width = 3.dp, color = MaterialTheme.colors.secondary, shape = RoundedCornerShape(32.dp))
                .height(170.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = isEventShowing!!.name,
                        fontSize = 24.sp,
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colors.secondary
                    )
                    Text(
                        text = isEventShowing!!.address,
                        fontSize = 16.sp,
                        fontFamily = ubuntuFontFamily,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colors.secondary
                    )
                    Divider(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (isToday) {
                            val user = isEventShowing!!.users.find { it.id == currentUser?.uid }
                            Icon(
                                painter = painterResource(R.drawable.car_icon),
                                contentDescription = "distance",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            var distance: Double = 0.0
                            if (user != null) {
                                if (user.distance != null) {
                                    distance = user.distance!!
                                } else {
                                    distance = 0.0
                                }
                            }
                            var unitKm = false
                            if (distance >= 1000) {
                                unitKm = true
                                distance /= 1000
                            }
                            val userDistance: String = if (unitKm) {
                                String.format(locale = Locale.getDefault(), "%.1f", distance) + "km"
                            } else {
                                distance.toInt().toString() + "m"
                            }
                            Text(
                                text = userDistance,
                                fontFamily = ubuntuFontFamily,
                                color = MaterialTheme.colors.secondary
                            )
                            Icon(painter = painterResource(R.drawable.clock_icon),
                                contentDescription = "ETA",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            var time = 0
                            if (user != null) {
                                if (user.timeEst != null) {
                                    time = ((user.timeEst!! / 1000) / 60).toInt()
                                } else {
                                    time = 0
                                }
                            }
                            Text(
                                text = "$time" + "min",
                                color = MaterialTheme.colors.secondary
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.calendar),
                                contentDescription = "distance",
                                tint = Colors.PowderBlue,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            val dateFormatter = DateTimeFormatter.ofPattern("MMM. d, yyyy")
                            Text(
                                text = isEventShowing!!.arrival.format(dateFormatter),
                                color = MaterialTheme.colors.secondary
                            )
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(vertical = 64.dp)
                .offset(x = (-24).dp)
                .align(Alignment.TopCenter)
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = { selectedText = it },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp)
                    .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(32.dp))
                    .clickable {
                        isDropdownExpanded = !isDropdownExpanded
                    }
                    .onGloballyPositioned { coordinates ->
                        with(density) {
                            dropTextFieldSize = DpSize(
                                width = coordinates.size.width.toDp(),
                                height = coordinates.size.height.toDp()
                            )
                        }
                    },
                shape = RoundedCornerShape(32.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = MaterialTheme.colors.background,
                    textColor = MaterialTheme.colors.secondary
                ),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_drop_down),
                        contentDescription = "dropdown arrow",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                isDropdownExpanded = !isDropdownExpanded
                            }
                    )
                }
            )
            DropdownMenu(
                modifier = Modifier
                    .width(Dp(dropTextFieldSize.width.value)),
                expanded = isDropdownExpanded,
                onDismissRequest = {
                    isDropdownExpanded = false
                }
            ) {
                eventList.forEach { event ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .height(Dp(dropTextFieldSize.height.value - 12)),
                        onClick = {
                            selectedText = event.name
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                LatLng(event.latitude, event.longitude),
                                12f
                            )
                            isEventShowing = event
                            Log.d("CAMERA UPDATE", "${event.latitude}, ${event.longitude}")
                            isDropdownExpanded = false
                        },
                    ) {
                        Column(
                        ) {
                            Text(
                                text = event.name,
                                color = MaterialTheme.colors.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}