package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vsnappy1.datepicker.data.DefaultDatePickerConfig
import com.vsnappy1.datepicker.data.model.DatePickerDate
import com.vsnappy1.datepicker.data.model.SelectionLimiter
import com.vsnappy1.datepicker.ui.model.DatePickerConfiguration
import org.timestamp.mobile.ui.theme.Colors
import org.timestamp.mobile.ui.theme.ubuntuFontFamily
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    initialDate: String = ""
) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    val parsedDate = if (initialDate.isNotEmpty()) {
        LocalDate.parse(initialDate, formatter)
    } else {
        today
    }

    var selectedDate by remember { mutableStateOf(parsedDate) }
    val formattedSelectedDate by remember { derivedStateOf { selectedDate.format(formatter) } }
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = Modifier
                .shadow(6.dp, shape = RoundedCornerShape(32.dp))
                .background(color = MaterialTheme.colors.primary, shape = RoundedCornerShape(32.dp))
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.primary)
                    .fillMaxWidth()
            ) {
                com.vsnappy1.datepicker.DatePicker(
                    onDateSelected = { year, month, day ->
                        selectedDate = LocalDate.of(year, month + 1, day)
                    },
                    date = DatePickerDate(
                        year = parsedDate.year,
                        month = parsedDate.monthValue - 1,
                        day = parsedDate.dayOfMonth
                    ),
                    selectionLimiter = SelectionLimiter(
                        fromDate = DatePickerDate(
                            year = today.year,
                            month = today.monthValue - 1,
                            day = today.dayOfMonth
                        )
                    ),
                    modifier = Modifier
                        .background(MaterialTheme.colors.primary),
                    configuration = DatePickerConfiguration.Builder()
                        .height(height = 250.dp)
                        .dateTextStyle(DefaultDatePickerConfig.dateTextStyle.copy(color = MaterialTheme.colors.secondary))
                        .selectedDateBackgroundColor(color = Colors.Bittersweet)
                        .sundayTextColor(color = Colors.Bittersweet)
                        .headerTextStyle(TextStyle(
                            fontFamily = ubuntuFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colors.secondary
                        ))
                        .build()
                )
                Button(
                    onClick = {
                        onDateSelected(formattedSelectedDate)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.Bittersweet, // Background color
                        contentColor = MaterialTheme.colors.primary  // Text color
                    ),
                ) {
                    Text(
                        text = "OK",
                        color = MaterialTheme.colors.secondary,
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}
