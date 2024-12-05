package org.timestamp.mobile.ui.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.day.DefaultDay
import io.github.boguszpawlowski.composecalendar.rememberCalendarState
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import java.time.LocalDate
import org.timestamp.mobile.ui.theme.Colors

@Composable
fun DynamicCalendar(
    onDateSelected: (LocalDate) -> Unit
) {
    SelectableCalendar(
        calendarState = rememberSelectableCalendarState(),
        showAdjacentMonths = false,
        modifier = Modifier
            .size(500.dp)
            .padding(16.dp)
            .testTag("calendar"),
        dayContent = { dayState ->
            DefaultDay(
                selectionColor = Colors.Bittersweet,
                state = dayState,
                onClick = { selectedDate ->
                    onDateSelected(selectedDate)
                    dayState.selectionState.onDateSelected(selectedDate)
                }
            )
        }
    )
}
