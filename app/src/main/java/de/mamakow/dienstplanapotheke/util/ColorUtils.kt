package de.mamakow.dienstplanapotheke.util

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils as AndroidColorUtils

object ColorUtils {

    /**
     * Desaturates and darkens a color for better appearance in Dark Mode.
     */
    @JvmStatic
    @ColorInt
    fun adjustColorForDarkMode(@ColorInt color: Int): Int {
        val hsl = FloatArray(3)
        AndroidColorUtils.colorToHSL(color, hsl)

        // Lower saturation (max 40%)
        hsl[1] = hsl[1].coerceAtMost(0.4f)

        // Lower lightness for background (between 15% and 25% for dark surface feel)
        hsl[2] = hsl[2].coerceIn(0.15f, 0.25f)

        return AndroidColorUtils.HSLToColor(hsl)
    }

    /**
     * Returns a contrasting text color (White or Black) based on the background color.
     */
    @JvmStatic
    @ColorInt
    fun getContrastColor(@ColorInt backgroundColor: Int): Int {
        val luminance = AndroidColorUtils.calculateLuminance(backgroundColor)
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }

    /**
     * Checks if the current color is light.
     */
    @JvmStatic
    fun isLightColor(@ColorInt color: Int): Boolean {
        return AndroidColorUtils.calculateLuminance(color) > 0.5
    }
}
