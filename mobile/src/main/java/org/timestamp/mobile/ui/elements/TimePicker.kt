package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vsnappy1.timepicker.TimePicker
import com.vsnappy1.timepicker.data.model.TimePickerTime
import kotlinx.datetime.LocalTime
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.min

@Composable
fun TimePickerDialog(
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
    initialTime: String = ""
) {
    val currentTime = java.time.LocalTime.now()
    val selectedHour = remember { mutableStateOf(currentTime.hour) }
    val selectedMinute = remember { mutableStateOf(currentTime.minute) }
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val parsedTime = if (initialTime.isNotEmpty()) {
        LocalTime.parse(initialTime)
    } else {
        currentTime
    }

    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.secondary) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .shadow(6.dp, shape = RoundedCornerShape(32.dp))
                    .background(
                        color = MaterialTheme.colors.primary,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(color = MaterialTheme.colors.primary)
                        .fillMaxWidth()
                ) {
                    TimePicker(
                        onTimeSelected = { h, m ->
                            selectedHour.value = h
                            selectedMinute.value = m
                        },
                        modifier = Modifier
                            .padding(8.dp),
                        time = TimePickerTime(
                            hour = selectedHour.value,
                            minute = selectedMinute.value
                        ),
                    )
                    Button(
                        onClick = {
                            onConfirm(selectedHour.value, selectedMinute.value)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Colors.Bittersweet, // Background color
                            contentColor = MaterialTheme.colors.primary  // Text color
                        ),
                    ) {
                        androidx.compose.material3.Text(
                            text = "OK",
                            fontFamily = ubuntuFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colors.secondary
                        )
                    }
                }
            }
        }
    }
}
