package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.toOffset
import org.timestamp.mobile.ui.elements.CreateEvent
import org.timestamp.mobile.ui.elements.FetchLocationWrapper
import java.time.LocalDateTime

class CreateEventTest {
    @JvmField
    @Rule
    val composeTestRule = createAndroidComposeRule<TimestampActivity>()

    private lateinit var mockFirebaseUser: FirebaseUser

    @Before
    fun setUp() {
        mockFirebaseUser = mockk(relaxed = true)
    }

    @Test
    fun setupTestContent() {
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = true,
                editEvent = null,
                currentUser = mockFirebaseUser,
            )
        }
    }

    @Test
    fun elementsAreDisplayed() {
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = true,
                editEvent = null,
                currentUser = mockFirebaseUser,
            )
        }
        composeTestRule.onNodeWithText("Add Event").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event Time").assertIsDisplayed()
    }

    @Test
    fun addButtonWorks() {
        var confirmedEvent: EventDTO? = null
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = { event -> confirmedEvent = event },
                isMock = true,
                editEvent = null,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Add").performClick()
        assert(confirmedEvent == null)
    }

    @Test
    fun dismissButtonWorks() {
        val dismissed = mutableStateOf(false)
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = { dismissed.value = true },
                onConfirmation = {},
                isMock = true,
                editEvent = null,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(dismissed.value)
    }

    @Test
    fun datePickerTest() {
        composeTestRule.activity.setContent {
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
    }

    @Test
    fun timePickerTest() {
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = true,
                editEvent = null,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.onNodeWithText("Event Time").performClick()
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }

    @Test
    fun validEventSubmission() {
        var confirmedEvent: EventDTO? = null
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = { event -> confirmedEvent = event },
                isMock = true,
                editEvent = null,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Event Name").performTextInput("Test Event")
        composeTestRule.onNodeWithText("Event Date").performClick()
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithText("Event Time").performClick()
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithText("Add").performClick()
        assert(confirmedEvent != null)
        assert(confirmedEvent?.name == "Test Event")
    }

    @Test
    fun editEventPopulatesFields() {
        val existingEvent = EventDTO(
            id = 123,
            name = "Existing Event",
            arrival = LocalDateTime.now().plusDays(1).toOffset(),
            latitude = 37.7749,
            longitude = -122.4194,
            description = "Test Location",
            address = "123 Test St."
        )
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = false,
                editEvent = existingEvent,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.onNodeWithText("Edit Event").assertIsDisplayed()
        composeTestRule.onNodeWithText("Existing Event").assertIsDisplayed()
    }

    @Test
    fun locationSelectionUpdatesMap() {
        var selectedLocation: LatLng? = null
        composeTestRule.activity.setContent {
            FetchLocationWrapper(LocalContext.current) { location ->
                selectedLocation = location
            }
        }
        composeTestRule.waitForIdle()
        assert(selectedLocation != null) {
            "Location selection failed; no location was set."
        }
    }

    @Test
    fun searchPredictionsAreDisplayed() {
        composeTestRule.activity.setContent {
            CreateEvent(
                onDismissRequest = {},
                onConfirmation = {},
                isMock = false,
                editEvent = null,
                currentUser = mockFirebaseUser
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("200 University Ave W, Waterloo, ON N2L 3G1, Canada", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText("200 University Ave W, Waterloo, ON N2L 3G1, Canada").performTextClearance()
        composeTestRule.onNodeWithText("Type to search...").assertExists()
        composeTestRule.onNodeWithText("Type to search...").performTextInput("Toronto")
    }
}