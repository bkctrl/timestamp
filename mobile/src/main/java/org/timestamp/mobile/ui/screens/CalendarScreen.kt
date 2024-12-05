package org.timestamp.mobile.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Typography
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseUser
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.R
import org.timestamp.mobile.TimestampActivity
import org.timestamp.mobile.models.EventViewModel
import org.timestamp.mobile.ui.elements.CreateEvent
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.elements.DynamicCalendar
import java.time.LocalDate

val ubuntuFontFamily = FontFamily(
    Font(R.font.ubuntu_regular),  // Regular
    Font(R.font.ubuntu_bold, FontWeight.Bold)  // Bold
)

@Composable
fun CalendarScreen(
    viewModel: EventViewModel = viewModel(LocalContext.current as TimestampActivity),
    currentUser: FirebaseUser?
) {
    val eventListState = viewModel.events.collectAsState()
    val eventList: MutableList<EventDTO> = eventListState.value.toMutableList()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null)}
    var refreshTrigger by remember { mutableStateOf(0) }

    val eventsOnSelectedDate by remember(selectedDate, eventList, refreshTrigger) {
        derivedStateOf {
            eventList.filter { it.arrival.toLocalDate() == selectedDate }
        }
    }

    var editingEvent by remember { mutableStateOf<EventDTO?>(null) }

    val calendarTypography = Typography(
        body1 = TextStyle(
            color = MaterialTheme.colors.secondary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
        ),
        h1 = TextStyle(
            color = MaterialTheme.colors.secondary,
            fontFamily = ubuntuFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
        ),
        h4 = TextStyle(
            color = Colors.Bittersweet,
            fontFamily = ubuntuFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
        ),
        h5 = TextStyle(
            color = MaterialTheme.colors.secondary,
            fontFamily = ubuntuFontFamily,
            fontSize = 24.sp,
        ),
        h6 = TextStyle(
            color = MaterialTheme.colors.secondary,
            fontFamily = ubuntuFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
    )

    MaterialTheme(typography = calendarTypography) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.size(70.dp))
                Text(text = "Calendar",
                    style = MaterialTheme.typography.h1,
                    modifier = Modifier
                        .offset(x = 20.dp)
                )
                Spacer(modifier = Modifier.size(15.dp))
                DynamicCalendar { date ->
                    selectedDate = date
                }

                if (selectedDate != null) {
                    Text(
                        text = "Events on ${selectedDate.toString()}",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.padding(top = 8.dp, start = 16.dp)
                    ) {
                        if (eventsOnSelectedDate.isNotEmpty()) {
                            eventsOnSelectedDate.forEach { event ->
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 4.dp
                                        ),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .width(375.dp),
                                        onClick = { editingEvent = event }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(IntrinsicSize.Min)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .width(10.dp)
                                                    .background(Colors.Bittersweet)
                                            )
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colors.primary)
                                                    .padding(16.dp)
                                            ) {
                                                Text(
                                                    text = event.name
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                Text("No events scheduled yet!")
                            }
                        }
                    }
                }
            }
            editingEvent?.let { event ->
                CreateEvent(
                    onDismissRequest = { editingEvent = null },
                    onConfirmation = { updatedEvent ->
                        Log.d("UPDATE EVENT", updatedEvent.id.toString())
                        viewModel.updateEvent(updatedEvent)
                        editingEvent = null
                        refreshTrigger++
                    },
                    isMock = false,
                    editEvent = event,
                    currentUser = currentUser
                )
            }
        }
    }
}