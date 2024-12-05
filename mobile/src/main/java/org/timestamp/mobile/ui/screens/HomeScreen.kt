package org.timestamp.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.timestamp.mobile.TimestampActivity
import org.timestamp.mobile.models.EventViewModel

@Composable
fun HomeScreen(
    viewModel: EventViewModel = viewModel(LocalContext.current as TimestampActivity),
    modifier: Modifier = Modifier,
    onSignOutClick: () -> Unit,
    onContinueClick: () -> Unit,

    // When we don't have all the permissions
    // We want to show a rationale dialog
    continueText: String = "Continue",
    warningText: String? = null
) {
    LaunchedEffect(Unit) {
        // Prefetch events to make it look quicker
        viewModel.startGetEventsPolling()
        viewModel.setPendingEvent()
    }

    val textStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    )

    Surface(
        color = Color(0xFFE5E6EA),
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            viewModel.auth.currentUser?.let { user ->
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
                user.displayName?.let { name ->
                    Text(
                        text = "Welcome, $name",
                        style = textStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = { onContinueClick() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2B2E)
                    )) {
                    Text(
                        text = continueText,
                        style = textStyle.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFFFFFFF)
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = { onSignOutClick() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2B2E)
                    )) {
                    Text(
                        text = "Sign Out",
                        style = textStyle.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFFFFFFF)
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
                warningText?.let {
                    Text(
                        text = it,
                        style = textStyle.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD32F2F)
                        ),
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }

}