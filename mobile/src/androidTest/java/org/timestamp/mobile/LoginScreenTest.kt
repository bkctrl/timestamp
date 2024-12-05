package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.timestamp.mobile.ui.screens.LoginScreen

class LoginScreenTest {
    @JvmField @Rule
    val composeTestRule = createAndroidComposeRule<TimestampActivity>()

    @Test
    fun elementsAreDisplayed() {
        composeTestRule.activity.setContent {
            LoginScreen {  }
        }
        composeTestRule.onNodeWithText("Sign in with Google").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome to\nTimestamp!").assertIsDisplayed()
        composeTestRule.onNodeWithText("On time in no time.").assertIsDisplayed()
    }
}