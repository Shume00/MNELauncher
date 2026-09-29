package com.shume.mnelauncher
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {

    // Define SharedPreferences file name
    private val PREFS_NAME = "LauncherPreferences"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Hide the system status bar and navigation bar
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val btnRadio = findViewById<LinearLayout>(R.id.btnRadio)
        val btnMedia = findViewById<LinearLayout>(R.id.btnMedia)
        val btnPhone = findViewById<LinearLayout>(R.id.btnPhone)
        val btnEco = findViewById<LinearLayout>(R.id.btnEco)
        val btnNav = findViewById<LinearLayout>(R.id.btnNav)
        val btnSettings = findViewById<LinearLayout>(R.id.btnSettings)
        val btnRadioText = findViewById<TextView>(R.id.btnRadioText)
        val btnMediaText = findViewById<TextView>(R.id.btnMediaText)
        val btnPhoneText = findViewById<TextView>(R.id.btnPhoneText)
        val btnEcoText = findViewById<TextView>(R.id.btnEcoText)
        val btnNavText = findViewById<TextView>(R.id.btnNavText)
        val btnSettingsText = findViewById<TextView>(R.id.btnSettingsText)
        val btnThemeToggle = findViewById<ImageView>(R.id.btnThemeToggle)
        val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootLayout)
        val textClock = findViewById<TextClock>(R.id.textClock)
        val btnAndroidAuto = findViewById<View>(R.id.bottomBar)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var isDarkMode = prefs.getBoolean("IS_DARK_MODE", true)
        applyTheme(isDarkMode, rootLayout, btnThemeToggle, btnRadioText, btnMediaText, btnPhoneText, btnEcoText, btnNavText, btnSettingsText, textClock)

        btnThemeToggle.setOnClickListener {
            isDarkMode = !isDarkMode

            // Save the new state
            prefs.edit { putBoolean("IS_DARK_MODE", isDarkMode) }

            // Apply the new colors
            applyTheme(isDarkMode, rootLayout, btnThemeToggle, btnRadioText, btnMediaText, btnPhoneText, btnEcoText, btnNavText, btnSettingsText, textClock)
        }

        // --- RADIO BUTTON SETUP ---
        btnRadio.setOnClickListener {
            // Load saved app or fall back to a default package if none is selected
            val targetApp = getSavedApp("BTN_RADIO", "com.example.defaultradio")
            launchApp(targetApp)
        }

        btnRadio.setOnLongClickListener {
            showAppPicker("BTN_RADIO")
            true // true indicates the long click was consumed
        }

        // --- MEDIA BUTTON SETUP ---
        btnMedia.setOnClickListener {
            val targetApp = getSavedApp("BTN_MEDIA", "com.spotify.music")
            launchApp(targetApp)
        }

        btnMedia.setOnLongClickListener {
            showAppPicker("BTN_MEDIA")
            true
        }

        btnPhone.setOnClickListener {
            val targetApp = getSavedApp("BTN_PHONE", "com.example.defaultphone")
            launchApp(targetApp)
        }

        btnPhone.setOnLongClickListener {
            showAppPicker("BTN_PHONE")
            true
        }

        btnEco.setOnClickListener {
            val targetApp = getSavedApp("BTN_ECO", "com.example.defaulteco")
            launchApp(targetApp)
        }

        btnEco.setOnLongClickListener {
            showAppPicker("BTN_ECO")
            true
        }

        btnNav.setOnClickListener {
            val targetApp = getSavedApp("BTN_NAV", "com.google.android.apps.maps")
            launchApp(targetApp)
        }

        btnNav.setOnLongClickListener {
            showAppPicker("BTN_NAV")
            true
        }

        btnSettings.setOnClickListener {
            val targetApp = getSavedApp("BTN_SETTINGS", "com.android.settings")
            launchApp(targetApp)
        }

        btnSettings.setOnLongClickListener {
            showAppPicker("BTN_SETTINGS")
            true
        }

    }

    private fun applyTheme(isDark: Boolean, rootLayout: androidx.constraintlayout.widget.ConstraintLayout, btnThemeToggle: android.widget.ImageView, btnRadioText: android.widget.TextView, btnMediaText: android.widget.TextView, btnPhoneText: android.widget.TextView, btnEcoText: android.widget.TextView, btnNavText: android.widget.TextView, btnSettingsText: android.widget.TextView, textClock: android.widget.TextClock) {
        if (isDark) {
            btnThemeToggle.setImageResource(R.drawable.ic_moon)
            btnThemeToggle.setColorFilter("#E0E0E0".toColorInt())
            rootLayout.setBackgroundColor("#000000".toColorInt())
            btnRadioText.setTextColor("#E0E0E0".toColorInt())
            btnMediaText.setTextColor("#E0E0E0".toColorInt())
            btnPhoneText.setTextColor("#E0E0E0".toColorInt())
            btnEcoText.setTextColor("#E0E0E0".toColorInt())
            btnNavText.setTextColor("#E0E0E0".toColorInt())
            btnSettingsText.setTextColor("#E0E0E0".toColorInt())
            textClock.setTextColor("#E0E0E0".toColorInt())
        } else {
            btnThemeToggle.setImageResource(R.drawable.ic_sun)
            btnThemeToggle.setColorFilter("#000000".toColorInt())
            rootLayout.setBackgroundColor("#E0E0E0".toColorInt())
            btnRadioText.setTextColor("#000000".toColorInt())
            btnMediaText.setTextColor("#000000".toColorInt())
            btnPhoneText.setTextColor("#000000".toColorInt())
            btnEcoText.setTextColor("#000000".toColorInt())
            btnNavText.setTextColor("#000000".toColorInt())
            btnSettingsText.setTextColor("#000000".toColorInt())
            textClock.setTextColor("#000000".toColorInt())
        }
    }

    /**
     * Fetches a list of all apps installed on the device that can be launched.
     */
    private fun getInstalledApps(): List<AppLaunchInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)

        return resolveInfos.map {
            AppLaunchInfo(
                name = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName
            )
        }.sortedBy { it.name } // Alphabetical order
    }

    /**
     * Shows a dialog allowing the user to pick an app, then saves the choice.
     */
    private fun showAppPicker(buttonKey: String) {
        val apps = getInstalledApps()
        val appNames = apps.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Application")
            .setItems(appNames) { _, which ->
                val selectedApp = apps[which]
                saveApp(buttonKey, selectedApp.packageName)
                Toast.makeText(this, "Assigned to ${selectedApp.name}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Saves the chosen package name to SharedPreferences.
     */
    private fun saveApp(buttonKey: String, packageName: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(buttonKey, packageName).apply()
    }

    /**
     * Retrieves the saved package name from SharedPreferences.
     */
    private fun getSavedApp(buttonKey: String, defaultPackage: String): String {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(buttonKey, defaultPackage) ?: defaultPackage
    }

    /**
     * Launches the app by its package name.
     */
    private fun launchApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "App not installed or selected", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back button from exiting the launcher
    }
}

// Data class to hold app name and package name together
data class AppLaunchInfo(val name: String, val packageName: String)