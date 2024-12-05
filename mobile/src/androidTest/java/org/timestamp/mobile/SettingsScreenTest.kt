package org.timestamp.mobile

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseUser
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.timestamp.mobile.ui.screens.SettingsScreen

// Unit tests for the Settings screen
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @JvmField @Rule
    val composeTestRule = createAndroidComposeRule<TimestampActivity>()

    private lateinit var mockFirebaseUser: FirebaseUser

    @Test
    fun testSettingsScreenRenders() {
        composeTestRule.activity.setContent {
            SettingsScreen(currentUser = null, onSignOutClick = {})
        }

        // Check if visual elements render with no issues
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Account Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Out").assertIsDisplayed()
        composeTestRule.onNodeWithText("Account Preferences").assertIsDisplayed()
    }
}