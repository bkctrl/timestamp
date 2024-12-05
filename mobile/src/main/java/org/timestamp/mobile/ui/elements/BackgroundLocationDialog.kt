package org.timestamp.mobile.ui.elements

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BackgroundLocationDialog(
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    /**
     * This function is used to show a dialog to the user
     * when the app needs to access the user's location in the background.
     *
     * Must have "Location Always"
     */
    AlertDialog(
        onDismissRequest = onDeny,
        icon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = "") },
        title = {
            Text(text = "Update Location Settings")
        },
        text = {
            Text(
                text = "This app requires access to your location in the background - " +
                        "\"Allow All The Time\"",
            )
        },
        confirmButton = {
            Button(onClick = onAllow) {
                Text(text = "Update Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDeny) {
                Text(text = "No Thanks")
            }
        }
    )
}