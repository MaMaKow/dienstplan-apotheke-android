package de.mamakow.dienstplanapotheke.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import de.mamakow.dienstplanapotheke.R

object SystemUtils {

    /**
     * Get the application version name.
     */
    @JvmStatic
    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    /**
     * Creates and starts an intent to send a support email to the administrator.
     */
    @JvmStatic
    fun sendSupportEmail(context: Context) {
        val email = context.getString(R.string.support_email_address)
        val subject = context.getString(R.string.support_email_subject)
        val body =
            "\n\n--- System Info ---\nApp Version: ${getAppVersion(context)}\nAndroid Version: ${android.os.Build.VERSION.RELEASE}\nDevice: ${android.os.Build.MODEL}"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * Applies the theme based on the provided theme value.
     */
    @JvmStatic
    fun applyTheme(themeValue: String?) {
        when (themeValue) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
