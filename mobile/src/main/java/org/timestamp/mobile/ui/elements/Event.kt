package org.timestamp.mobile.ui.elements

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.R
import org.timestamp.mobile.models.EventViewModel
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventMap(locationName: String, eventName: String, eventLocation: LatLng, context: Context, isClickable: Boolean) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(eventLocation, 15f)
    }
    val markerState = rememberMarkerState(position = eventLocation)
    var uiSettings = MapUiSettings()
    if (!isClickable) {
        uiSettings = uiSettings.copy(zoomControlsEnabled = false)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings
        ) {
            Marker(
                state = markerState,
                title = locationName,
                snippet = eventName
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    if (isClickable) {
                        openGoogleMaps(context, eventLocation)
                    }
                }
        )
    }
}

fun openGoogleMaps(context: Context, location: LatLng) {
    val uri = Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    context.startActivity(intent)
}

@Composable
fun EventBox(
    data: EventDTO,
    viewModel: EventViewModel = viewModel(),
    currentUser: FirebaseUser?,
    today: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isUsersOpen by remember { mutableStateOf(false) }
    var isEditingEvent by remember { mutableStateOf(false) }
    var loadingDistance by remember { mutableStateOf(false) }
    var loadingTime by remember { mutableStateOf(false) }
    val now = OffsetDateTime.now(ZoneId.systemDefault())
    val next24Hours = now.plusHours(24)
    val isToday = data.arrival.isBefore(next24Hours)
    val context = LocalContext.current
    val user = data.users.find { it.id == currentUser?.uid }

    if (isUsersOpen) {
        if (currentUser != null) {
            ViewUsers(
                event = data,
                onDismissRequest = {
                    isUsersOpen = false
                },
                currentUser = currentUser,
                viewModel = viewModel,
                isToday = isToday
            )
        }
    }

    if (isEditingEvent) {
        CreateEvent(
            onDismissRequest = {
                isEditingEvent = false
            },
            onConfirmation = { event ->
                Log.d("UPDATE EVENT", event.id.toString())
                viewModel.updateEvent(event)
                isEditingEvent = false
            },
            isMock = false,
            editEvent = data,
            currentUser = currentUser
        )
    }

    // Define the box content
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .shadow(
                shape = RoundedCornerShape(12.dp),
                elevation = 4.dp,
            )
            .border(width = 2.dp, MaterialTheme.colors.secondary, shape = RoundedCornerShape(12.dp))
            .background(MaterialTheme.colors.primary)
            .clickable { isExpanded = !isExpanded }  // Toggle expand/collapse on click
            .padding(horizontal = 16.dp)
            .animateContentSize()  // Smooth transition for height change
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = data.name,
                color = MaterialTheme.colors.secondary,
                fontSize = 24.sp,
                fontFamily = ubuntuFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .widthIn(max = 280.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = data.users.size.toString(),
                color = MaterialTheme.colors.secondary,
                fontFamily = ubuntuFontFamily,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(vertical = 2.dp)
            )
            val multUsers = data.users.size > 1
            IconButton(
                onClick = {
                    isUsersOpen = true
                },
                modifier = Modifier
                    .size(24.dp)
                    .offset(y = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (multUsers) R.drawable.users_icon else R.drawable.user_icon),
                    contentDescription = "user icon",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
            IconButton(
                onClick = {
                    isDropdownExpanded = !isDropdownExpanded
                },
                modifier = Modifier
                    .size(24.dp)
                    .offset(y = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.dots_icon),
                    contentDescription = "dots icon",
                    tint = MaterialTheme.colors.secondary,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = data.description,
            color = MaterialTheme.colors.secondary,
            fontSize = 16.sp,
            fontFamily = ubuntuFontFamily,
            style = TextStyle(lineHeight = 14.sp),
            modifier = Modifier
                .fillMaxWidth()
        )
        Text(
            text = data.address,
            fontSize = 12.sp,
            fontFamily = ubuntuFontFamily,
            color = Color.LightGray,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()

        ) {
            if (isToday) {
                if (currentUser != null) {
                    Image(
                        painter = rememberAsyncImagePainter(currentUser.photoUrl),
                        contentDescription = "current user icon",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                }
                Text(
                    text = "Status:",
                    color = MaterialTheme.colors.secondary,
                    fontFamily = ubuntuFontFamily,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                )
                var time = 0
                if (user != null) {
                    time = if (user.timeEst != null) {
                        ((user.timeEst!! / 1000) / 60).toInt()
                    } else {
                        0
                    }
                }
                if (user?.arrived == true) {
                    Text(
                        text = "Arrived",
                        color = Color.Green,
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                    )
                } else if (now.plusMinutes(time.toLong()).isBefore(data.arrival)) {
                    Text(
                        text = "On time",
                        color = Color.Green,
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                    )
                } else {
                    val timeLate = now.plusMinutes(time.toLong())
                    val differenceInMinutes = Duration.between(data.arrival, timeLate).toMinutes()
                    Text(
                        text = "Late $differenceInMinutes min",
                        color = Color.Red,
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                val formatter = DateTimeFormatter.ofPattern("h:mm a")
                val formattedTime: String = data.arrival.format(formatter)
                Text(
                    text = formattedTime,
                    color = MaterialTheme.colors.secondary,
                    fontFamily = ubuntuFontFamily,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .offset(x = (-4).dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.clock_icon),
                    contentDescription = "clock icon",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(18.dp)
                )
            } else {
                val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
                val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
                val formattedDate: String = data.arrival.format(dateFormatter)
                val formattedTime: String = data.arrival.format(timeFormatter)
                Icon(
                    painter = painterResource(id = R.drawable.event_calendar),
                    contentDescription = "calendar icon",
                    tint = Colors.PowderBlue,
                    modifier = Modifier
                        .size(18.dp)
                )
                Text(
                    text = formattedDate,
                    color = MaterialTheme.colors.secondary,
                    fontFamily = ubuntuFontFamily,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .offset(x = 4.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formattedTime,
                    color = MaterialTheme.colors.secondary,
                    fontFamily = ubuntuFontFamily,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .offset(x = (-4).dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.clock_icon),
                    contentDescription = "clock icon",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = {
                isDropdownExpanded = false
            },
            modifier = Modifier
                .align(Alignment.End)
                .background(color = MaterialTheme.colors.primary),
            offset = DpOffset(x = 220.dp, y = (-130).dp)
        ) {
            if (currentUser?.uid == data.creator) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Edit",
                            color = MaterialTheme.colors.secondary,
                            fontFamily = ubuntuFontFamily,
                            fontSize = 16.sp
                        )
                    },
                    onClick = {
                        isEditingEvent = true
                        isDropdownExpanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = {
                    Text(
                        text = "View Users",
                        color = MaterialTheme.colors.secondary,
                        fontFamily = ubuntuFontFamily,
                        fontSize = 16.sp
                    )
                },
                onClick = {
                    isUsersOpen = true
                    isDropdownExpanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Remove",
                        fontFamily = ubuntuFontFamily,
                        fontSize = 16.sp,
                        color = Color.Red,
                    )
                },
                onClick = {
                    viewModel.deleteEvent(data.id!!)
                    isDropdownExpanded = false
                }
            )
        }

        // Conditionally show extra content when expanded
        if (isExpanded) {
            EventMap(
                locationName = data.description,
                eventName = data.name,
                eventLocation = LatLng(data.latitude, data.longitude),
                context = context,
                isClickable = true
            )
            if (today) {
                var distance: Double = 0.0
                if (user != null) {
                    if (user.distance != null) {
                        distance = user.distance!!
                        loadingDistance = false
                    } else {
                        distance = 0.0
                        loadingDistance = true
                    }
                }
                var time = 0
                if (user != null) {
                    if (user.timeEst != null) {
                        time = ((user.timeEst!! / 1000) / 60).toInt()
                        loadingDistance = false
                    } else {
                        time = 0
                        loadingTime = true
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.location_icon),
                        contentDescription = "location icon",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    if (loadingDistance) {
                        CircularProgressIndicator(
                            color = Colors.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(16.dp)
                        )
                    } else {
                        Text(
                            text = userDistance,
                            color = MaterialTheme.colors.secondary,
                            fontFamily = ubuntuFontFamily,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = painterResource(id = R.drawable.car_icon),
                        contentDescription = "transportation icon",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    if (loadingTime) {
                        CircularProgressIndicator(
                            color = Colors.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(16.dp)
                        )
                    } else {
                        Text(
                            text = time.toString() + "min",
                            color = MaterialTheme.colors.secondary,
                            fontFamily = ubuntuFontFamily,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Divider(
            color = Color.LightGray,
            thickness = 1.5.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )

        Icon(
            painter = painterResource(id = if (isExpanded) R.drawable.arrow_drop_up else R.drawable.arrow_drop_down),
            contentDescription = if (isExpanded) "arrow drop up icon" else "arrow drop down icon",
            tint = MaterialTheme.colors.secondary,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}
