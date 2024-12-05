package org.timestamp.mobile.ui.screens

// Imports...
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import org.timestamp.mobile.R
import org.timestamp.mobile.TimestampActivity
import org.timestamp.mobile.models.EventViewModel
import org.timestamp.mobile.models.ThemeViewModel
import org.timestamp.mobile.ui.theme.Colors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: EventViewModel = viewModel(LocalContext.current as TimestampActivity),
    themeViewModel: ThemeViewModel = viewModel(LocalContext.current as TimestampActivity),
    currentUser: FirebaseUser?,
    onSignOutClick: () -> Unit
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    // Set up common style formatting
    val ubuntuFontFamily = FontFamily(
        Font(R.font.ubuntu_regular),  // Regular
        Font(R.font.ubuntu_bold, FontWeight.Bold)  // Bold
    )
    val textStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    )

    // Maintains state of whether we should be in light mode or dark mode
    var sliderPosition by remember { mutableStateOf(2) }
    val availablePositions = listOf(5, 10, 30, 60, 120, 300)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.size(70.dp))
            Text(text = "Settings",
                color = MaterialTheme.colors.secondary,
                fontFamily = ubuntuFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.size(30.dp))
            Divider(
                color = Colors.Platinum
            )
            Spacer(modifier = Modifier.size(30.dp))
            // Account Information Section
            Text(text = "Account Information",
                color = MaterialTheme.colors.secondary,
                fontFamily = ubuntuFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.size(20.dp))
            Divider(
                color = Colors.Platinum
            )
            Spacer(modifier = Modifier.size(20.dp))
            // Display user details here
            Row() {
                Column() {
                    Text(text = "Name",
                        color = MaterialTheme.colors.secondary,
                        fontFamily = ubuntuFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    currentUser?.let { user ->
                        user.displayName?.let { name ->
                            Text(
                                text = name,
                                color = MaterialTheme.colors.secondary,
                                fontFamily = ubuntuFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.size(25.dp))
                    }
                    // Allows user to sign out from the settings page
                    Button(
                        onClick = {
                            Log.d("SettingsScreen","signed out")
                            onSignOutClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colors.secondary
                        )) {
                        androidx.compose.material3.Text(
                            text = "Sign Out",
                            style = textStyle.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }
                Spacer(modifier = Modifier.width(100.dp))
                // User profile picture
                Column() {
                    currentUser?.let { user ->
                        user.photoUrl?.let {
                            AsyncImage(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(it)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "profile picture",
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Divider(
                color = Colors.Platinum
            )
            Spacer(modifier = Modifier.size(20.dp))
            // Account Preferences section
            Text(text = "Account Preferences",
                color = MaterialTheme.colors.secondary,
                fontFamily = ubuntuFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.size(20.dp))
            Divider(
                color = Colors.Platinum
            )
            Spacer(modifier = Modifier.size(20.dp))
            Row() {
                Text(text = if (isDarkTheme) "Dark Mode" else "Light Mode",
                    color = MaterialTheme.colors.secondary,
                    fontFamily = ubuntuFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.width(150.dp)
                )
                Spacer(modifier = Modifier.width(150.dp))
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = {
                        themeViewModel.toggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colors.secondary,
                        uncheckedThumbColor = MaterialTheme.colors.primary
                    ),
                    modifier = Modifier.scale(1.3f)
                )
            }
            Spacer(modifier = Modifier.size(20.dp))
            Divider(
                color = Colors.Platinum
            )
            Spacer(modifier = Modifier.size(20.dp))
            Row() {
                Text(
                    modifier = Modifier,
                    text = "Location Request Interval",
                    color = MaterialTheme.colors.secondary,
                    fontFamily = ubuntuFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
                Text(
                    modifier = Modifier
                        .offset(x = 40.dp),
                    text = availablePositions.get(sliderPosition).toString() + "sec",
                    color = MaterialTheme.colors.secondary,
                    fontFamily = ubuntuFontFamily,
                    fontSize = 20.sp,
                    maxLines = 1,
                )
            }
            Slider(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp),
                value = sliderPosition.toFloat(),
                onValueChange = { value ->
                    sliderPosition = value.roundToInt().coerceIn(0, availablePositions.lastIndex)
//                    val interval = availablePositions.get(sliderPosition).toLong() * 1000
                },
                colors = androidx.compose.material3.SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colors.primary,
                    inactiveTrackColor = Color.Gray,
                    thumbColor = Color.White,
                    disabledActiveTickColor = MaterialTheme.colors.secondary,
                    inactiveTickColor = MaterialTheme.colors.secondary,
                    activeTickColor = Color.Gray,
                ),
                steps = availablePositions.size - 2,
                valueRange = 0f..(availablePositions.size - 1).toFloat(),
            )
        }
    }
}