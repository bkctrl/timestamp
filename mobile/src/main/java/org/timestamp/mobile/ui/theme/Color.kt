package org.timestamp.mobile.ui.theme

import androidx.compose.ui.graphics.Color

object Colors {
    val Bittersweet = Color(0xFFFF6F61)
    val TeaRose = Color(0xFFF7CAC9)
    val Platinum = Color(0xFFE5E6EA)
    val PowderBlue = Color(0xFF9CC5E1)
    val BittersweetDark = Color(0xFFCC1A09)
    var Black = Color(0xFF2A2B2E)
    var White = Color(0xFFFFFFFF)

    fun setThemeColors(darkModeOn : Boolean) {
        if (darkModeOn) {
            Black = Color(0xFFFFFFFF)
            White = Color(0xFF2A2B2E)
        } else {
            Black = Color(0xFF2A2B2E)
            White = Color(0xFFFFFFFF)
        }
    }
}