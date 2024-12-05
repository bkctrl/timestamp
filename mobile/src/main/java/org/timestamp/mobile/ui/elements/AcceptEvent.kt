package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import com.google.android.gms.maps.model.LatLng
import org.timestamp.lib.dto.EventDTO
import org.timestamp.mobile.models.EventViewModel
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.time.format.DateTimeFormatter

@Composable
fun AcceptEvent(
    eventViewModel: EventViewModel,
    event: EventDTO
) {

    val dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
    val context = LocalContext.current
    Dialog(
        onDismissRequest = {  }
    ) {
        Card (
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(6.dp, shape = RoundedCornerShape(32.dp))
                .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
            ) {
                Text(
                    modifier = Modifier
                        .padding(16.dp),
                    text = "Join Event",
                    color = MaterialTheme.colors.secondary,
                    fontFamily = ubuntuFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Divider(
                    color = MaterialTheme.colors.background,
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 16.dp)
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .shadow(
                            4.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0x33000000)
                        )
                        .border(1.dp, MaterialTheme.colors.background, shape = RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colors.primary, shape = RoundedCornerShape(16.dp))
                        .padding(vertical = 5.dp),
                    textAlign = TextAlign.Center,
                    text = event.name,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = event.arrival.toLocalDateTime().format(dateTimeFormatter),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                EventMap(
                    event.address,
                    event.name,
                    LatLng(event.latitude, event.longitude),
                    context,
                    false
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Sent from:\n${event.users.first().email}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(start = 10.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = eventViewModel::cancelPendingEvents,
                        colors = ButtonColors(
                            containerColor = MaterialTheme.colors.secondary,
                            contentColor = MaterialTheme.colors.primary,
                            disabledContainerColor = MaterialTheme.colors.secondary,
                            disabledContentColor = MaterialTheme.colors.primary
                        ),

                        ) {
                        Text(
                            text = "Cancel",
                            fontFamily = ubuntuFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colors.primary
                        )
                    }

                    Button(
                        onClick = eventViewModel::joinPendingEvent,
                        colors = ButtonColors(
                            containerColor = Colors.Bittersweet,
                            contentColor = MaterialTheme.colors.primary,
                            disabledContainerColor = MaterialTheme.colors.secondary,
                            disabledContentColor = MaterialTheme.colors.primary
                        ),
                    ) {
                        Text(
                            text = "Accept",
                            fontFamily = ubuntuFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}