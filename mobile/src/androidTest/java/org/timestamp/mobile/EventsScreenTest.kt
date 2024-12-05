package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.timestamp.mobile.ui.screens.EventsScreen

@RunWith(AndroidJUnit4::class)
class EventsScreenTest {
    @JvmField @Rule
    val composeTestRule = createAndroidComposeRule<TimestampActivity>()

    @Test
    fun testElementRenders() {
        composeTestRule.activity.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Screen.Events.name) {
                composable(Screen.Events.name) {
                    EventsScreen(
                        isMock = true,
                        viewModel = TODO(),
                        currentUser = TODO()
                    )
                }
            }
        }
        composeTestRule.onNodeWithText("Add an\nEvent!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upcoming Events...").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Event Button")  // replace with content description if available
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Notification Bell")
            .assertIsDisplayed()
    }

    @Test
    fun createEventDialogOpens() {
        composeTestRule.activity.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Screen.Events.name) {
                composable(Screen.Events.name) {
                    EventsScreen(
                        isMock = true,
                        viewModel = TODO(),
                        currentUser = TODO()
                    )
                }
            }
        }
        composeTestRule.onNodeWithContentDescription("Add Event Button")  // replace with content description if available
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithText("Add Event")
            .assertIsDisplayed()
    }

    @Test
    fun dismissCreateEventDialog_whenDismissClicked() {
        composeTestRule.activity.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Screen.Events.name) {
                composable(Screen.Events.name) {
                    EventsScreen(
                        isMock = true,
                        viewModel = TODO(),
                        currentUser = TODO()
                    )
                }
            }
        }
        composeTestRule.onNodeWithContentDescription("Add Event Button")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Add Event").assertDoesNotExist()
    }

//    @Test
//    fun testEventListDisplaysEvents() {
//        eventList.add(EventDetailed(
//            name = "Mock Event",
//            description = "Mock Description",
//            address = "123 Mock Street, Mock City",
//            latitude = 37.7749,
//            longitude = -122.4194,
//            arrival = LocalDateTime.parse("2023-12-01T12:00:00"),
//        ))
//        composeTestRule.activity.setContent {
//            val navController = rememberNavController()
//            NavHost(navController = navController, startDestination = Screen.Events.name) {
//                composable(Screen.Events.name) {
//                    EventsScreen(isMock = true)
//                }
//            }
//        }
//        composeTestRule.onNodeWithText("Mock Event").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Mock Description").assertIsDisplayed()
//    }
}
