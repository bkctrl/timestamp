package org.timestamp.mobile.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseUser
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.R
import org.timestamp.mobile.TimestampActivity
import org.timestamp.mobile.models.EventViewModel
import org.timestamp.mobile.ui.elements.AcceptEvent
import org.timestamp.mobile.ui.elements.CreateEvent
import org.timestamp.mobile.ui.elements.EventBox
import org.timestamp.mobile.ui.elements.MapView
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.time.OffsetDateTime
import java.time.ZoneId

@Composable
fun EventsScreen(
    viewModel: EventViewModel = viewModel(LocalContext.current as TimestampActivity),
    isMock: Boolean = false,
    currentUser: FirebaseUser?
) {

    val eventListState = viewModel.events.collectAsState()
    val eventList: List<EventDTO> = eventListState.value

    val pendingEventState = viewModel.pendingEvent.collectAsState()
    val pendingEvent: EventDTO? = pendingEventState.value

    if (pendingEvent != null) AcceptEvent(viewModel, pendingEvent)

    val createEvents = remember { mutableStateOf(false) }
    val hasEvents = remember { mutableStateOf(false) }
    val showMapView = remember { mutableStateOf(false) }
    if (createEvents.value) {
        CreateEvent(
            onDismissRequest = { createEvents.value = false },
            onConfirmation = { event ->
                viewModel.postEvent(event)
                createEvents.value = false
            },
            isMock = isMock,
            editEvent = null,
            currentUser = currentUser
        )
    }

    if (eventList.isNotEmpty()) hasEvents.value = true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Text(
                text = "Upcoming Events...",
                color = MaterialTheme.colors.secondary,
                fontFamily = ubuntuFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                modifier = Modifier
                    .offset(x = 20.dp, y = 70.dp)
            )
            if (!hasEvents.value) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Add an\nEvent!",
                        color = Color(0xFFE5E6EA),
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 80.sp,
                        textAlign = TextAlign.Start,
                        lineHeight = 80.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(92.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val now = OffsetDateTime.now(ZoneId.systemDefault())
                    val newEventList = eventList.sortedBy { it.arrival }
                    val next24Hours = now.plusHours(24)

                    val next24HourEvents = newEventList.filter { event ->
                        event.arrival.isBefore(next24Hours)
                    }
                    val otherEvents = newEventList.filter { event ->
                        event.arrival.isAfter(next24Hours)
                    }
                    item {
                        Text(
                            text = "Events Soon",
                            fontFamily = ubuntuFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.secondary,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(4.dp)
                        )
                        Divider(
                            color = MaterialTheme.colors.background,
                            thickness = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                        )
                    }
                    if (next24HourEvents.isNotEmpty()) {
                        next24HourEvents.forEach { item { EventBox(it, viewModel, currentUser, true) }}
                    } else {
                        item {
                            Text(
                                text = "No Events Soon!",
                                fontFamily = ubuntuFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = MaterialTheme.colors.background,
                                modifier = Modifier
                                    .padding(16.dp)
                            )
                        }
                    }
                    item {
                        Text(
                            text = "Events Later",
                            fontFamily = ubuntuFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.secondary,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(4.dp)
                        )
                        Divider(
                            color = MaterialTheme.colors.background,
                            thickness = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                        )
                    }
                    if (otherEvents.isNotEmpty()) {
                        otherEvents.forEach { item { EventBox(it, viewModel, currentUser, false) }}
                    } else {
                        item {
                            Text(
                                text = "No Events Later!",
                                fontFamily = ubuntuFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colors.background,
                                modifier = Modifier
                                    .padding(16.dp)
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }
        IconButton(onClick = {
            createEvents.value = true
        },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .offset(y=(-50).dp)
                .size(54.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.plus_button),
                contentDescription = "Add Event Button",
                modifier = Modifier.size(54.dp),
                tint = Color.Unspecified)
        }
    }
    if (showMapView.value) {
        MapView(
            currentUser = currentUser
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        IconButton(onClick = {
            showMapView.value = !showMapView.value
        },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .offset(y=24.dp)
                .size(48.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.earth_icon),
                contentDescription = "Earth Icon",
                modifier = Modifier.size(48.dp),
                tint = if (!showMapView.value) Colors.TeaRose else Colors.Bittersweet
            )
        }
    }
}

