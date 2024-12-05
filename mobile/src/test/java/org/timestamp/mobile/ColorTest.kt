package org.timestamp.mobile

import androidx.compose.ui.graphics.Color
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.timestamp.mobile.ui.theme.Colors

class ColorTest {
    @Test
    fun colorsAreCorrect() {
        assertEquals(Colors.Bittersweet, Color(0xFFFF6F61))
        assertEquals(Colors.TeaRose, Color(0xFFF7CAC9))
        assertEquals(Colors.Platinum, Color(0xFFE5E6EA))
        assertEquals(Colors.PowderBlue, Color(0xFF9CC5E1))
        assertEquals(Colors.BittersweetDark, Color(0xFFCC1A09))
    }
}