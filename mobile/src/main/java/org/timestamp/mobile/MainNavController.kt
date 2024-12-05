package org.timestamp.mobile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import org.timestamp.mobile.models.EventViewModel
import org.timestamp.mobile.models.LocationViewModel
import org.timestamp.mobile.models.ThemeViewModel
import org.timestamp.mobile.ui.elements.BackgroundLocationDialog
import org.timestamp.mobile.ui.screens.CalendarScreen
import org.timestamp.mobile.ui.screens.EventsScreen
import org.timestamp.mobile.ui.screens.HomeScreen
import org.timestamp.mobile.ui.screens.LoginScreen
import org.timestamp.mobile.ui.screens.SettingsScreen
import org.timestamp.mobile.ui.theme.TimestampTheme
import org.timestamp.mobile.utility.PermissionProvider

enum class Screen {
    Login,
    Home,
    Events,
    Calendar,
    Settings
}

class MainNavController(
    private val context: Context,
    private val eventViewModel: EventViewModel,
    private val themeViewModel: ThemeViewModel,
    private val locationViewModel: LocationViewModel
) {
    private val auth = eventViewModel.auth
    private val credentialManager: CredentialManager = CredentialManager.create(context)
    private val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
        .Builder(context.getString(R.string.web_client_id))
        .build()
    private val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(signInWithGoogleOption)
        .build()

    /**
     * Initialize auth login with firebase, prompting a user flow if required
     */
    private suspend fun handleSignIn(navController : NavController) {
        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            val credential = result.credential

            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                Log.e("Sign In", "Wrong token type!")
                return
            }

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider
                .getCredential(googleIdTokenCredential.idToken, null)

            auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("Sign In", "Successfully logged in")
                    eventViewModel.pingBackend()
                    navController.navigate(Screen.Home.name)
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("Sign In", "Received an invalid google id token response", e)
        } catch (e: GetCredentialException) {
            Log.e("Sign In", e.toString())
        }
    }

    /**
     * Main composable for navigation features. Includes credential management.
     * Sets up Credential Manager.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun TimestampNavController() {
        val permissions = rememberMultiplePermissionsState(PermissionProvider.PERMISSIONS)
        val bgPermission = rememberPermissionState(PermissionProvider.BACKGROUND_LOCATION)
        val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()
        val startDestination = if (auth.currentUser == null) Screen.Login.name else Screen.Home.name

        LaunchedEffect(Unit) {
            if (!permissions.allPermissionsGranted) navController.navigate(startDestination)
            permissions.launchMultiplePermissionRequest()
        }

        fun signIn() { scope.launch { handleSignIn(navController) } }
        fun signOut() {
            auth.signOut()
            eventViewModel.stopGetEventsPolling()
            clearLocationService()
            scope.launch {
                credentialManager.clearCredentialState(
                    ClearCredentialStateRequest()
                )
                navController.popBackStack()
                navController.navigate(Screen.Login.name)
            }
        }

        TimestampTheme(darkTheme = isDarkTheme) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(
                    Screen.Login.name,
                ) {
                    LoginScreen(
                        onSignInClick = ::signIn
                    )
                }
                composable(Screen.Home.name) {
                    // False if all permissions granted
                    val permissionGranted = permissions.allPermissionsGranted
                    val showRationale = permissions.shouldShowRationale
                    val bgPermissionGranted = bgPermission.status.isGranted
                    val showBackgroundRationale = remember { mutableStateOf(!bgPermissionGranted) }

                    LaunchedEffect(permissionGranted) {
                        if (permissionGranted) startLocationService()
                        else clearLocationService()
                    }

                    HomeScreen(
                        onSignOutClick = ::signOut,
                        onContinueClick = {
                            if (permissionGranted) navController.navigate(Screen.Events.name)
                            else if (showRationale) permissions.launchMultiplePermissionRequest()
                            else {
                                // Need the user to update manually in settings
                                // after many denials
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            }
                        },
                        continueText = if (permissionGranted) "Continue" else "Settings",
                        warningText = if (permissionGranted) null else "Please enable all permissions"
                    )

                    if (permissionGranted && showBackgroundRationale.value) {
                        BackgroundLocationDialog(
                            onAllow = {
                                bgPermission.launchPermissionRequest()
                                showBackgroundRationale.value = false
                            },
                            onDeny = { showBackgroundRationale.value = false }
                        )
                    }
                }
                composable(Screen.Events.name) {
                    EventsScreen(currentUser = auth.currentUser)
                    NavBar(navController = navController, currentScreen = "Events")
                }
                composable(Screen.Calendar.name) {
                    CalendarScreen(currentUser = auth.currentUser)
                    NavBar(navController = navController, currentScreen = "Calendar")
                }
                composable(Screen.Settings.name) {
                    SettingsScreen(
                        currentUser = auth.currentUser,
                        onSignOutClick = ::signOut
                    )
                    NavBar(navController = navController, currentScreen = "Settings")
                }
            }
        }
    }

    /**
     * Composable for navigation bar at the bottom
     */
    @Composable
    fun NavBar(navController: NavController, currentScreen : String) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color(0xFFFF6F61))
                    .align(Alignment.BottomCenter)
                    .offset(y = (-2).dp)
            ) {
                Box (
                    modifier = Modifier
                        .height(3.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.secondary)
                        .align(Alignment.TopCenter)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box() {}
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Events.name)
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Events") Color.Black else Color(0xFF522D2A)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(0.95f)
                            .background(Color.LightGray)
                            .align(Alignment.Bottom)
                    )
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Calendar.name)
                        },
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 20.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Calendar") Color.Black else Color(0xFF522D2A)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(0.95f)
                            .background(Color.LightGray)
                            .align(Alignment.Bottom)
                    )
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Settings.name)
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (currentScreen == "Settings") Color.Black else Color(0xFF522D2A)
                        )
                    }
                    Box() {}
                }
            }
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(context, TimestampService::class.java)
        context.startService(serviceIntent)
    }

    private fun clearLocationService() {
        val serviceIntent = Intent(context, TimestampService::class.java)
        context.stopService(serviceIntent)
    }
}