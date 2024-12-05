package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.firebase.auth.FirebaseUser
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.timestamp.mobile.ui.elements.CreateEvent
import org.timestamp.mobile.ui.elements.DatePickerDialog

class DatePickerTest {
    @JvmField @Rule
    val composeTestRule = createAndroidComposeRule<TimestampActivity>()

    private lateinit var mockFirebaseUser: FirebaseUser

    @Before
    fun setup() {
        mockFirebaseUser = mockk(relaxed = true)
    }

    @Test
    fun testDatePickerDialog_selectValidDateAndConfirm() {
        var selectedDate: String? = null
        composeTestRule.activity.setContent{
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = true,
                editEvent = null,
                currentUser = mockFirebaseUser
            )
        }

        composeTestRule.onNodeWithContentDescription("select date").performClick()

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()

        composeTestRule.onNodeWithText("OK").performClick()


        assert(selectedDate != null)
    }

    @Test
    fun testDatePickerDialog_cancelSelection() {
        var selectedDate: String? = null

        composeTestRule.activity.setContent{
            DatePickerDialog(
                onDateSelected = { dateMillis ->
                    selectedDate = dateMillis
                },
                onDismiss = {}
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(selectedDate == null)
    }

}