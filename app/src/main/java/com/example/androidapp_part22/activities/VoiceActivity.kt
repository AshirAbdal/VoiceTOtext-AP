package com.example.androidapp_part22.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.androidapp_part22.R
import com.example.androidapp_part22.fragments.HistoryFragment
import com.example.androidapp_part22.fragments.SettingsFragment
import com.example.androidapp_part22.fragments.SpeechToTextFragment
import com.google.android.material.tabs.TabLayout


class VoiceActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var backButton: ImageButton
    lateinit var tabLayout: TabLayout  // Changed to public for fragment access
    private var speechToTextFragment: SpeechToTextFragment? = null
    private lateinit var prefs: SharedPreferences

    // Tab indices
    private val TAB_TEXT = 0
    private val TAB_SETTINGS = 1
    private val TAB_HISTORY = 2

    companion object {
        private const val API_ENDPOINT = "https://voicetotext.free.beeceptor.com"
        private const val API_HISTORY_PATH = "/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize preferences and register listener
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)

        // Apply theme
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice)

        // Initialize views
        initializeViews()

        // Setup back button
        setupBackButton()

        // Setup tab layout
        setupTabLayout()

        // Load speech-to-text fragment by default
        loadSpeechToTextFragment()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        tabLayout = findViewById(R.id.tabLayout)
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        // If we have fragments in backstack, just pop the backstack
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            tabLayout.getTabAt(TAB_TEXT)?.select()
            return
        }

        // Check if we're in the speech-to-text fragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.contentFrame)

        if (currentFragment is SpeechToTextFragment) {
            AlertDialog.Builder(this)
                .setTitle("Exit Voice to Text?")
                .setMessage("Do you want to exit the Voice to Text feature?")
                .setPositiveButton("Yes") { _, _ -> navigateToDashboard() }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        } else {
            navigateToDashboard()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    TAB_TEXT -> {
                        loadSpeechToTextFragment()
                    }
                    TAB_SETTINGS -> {
                        loadSettingsFragment()
                    }
                    TAB_HISTORY -> {
                        loadHistoryFragment()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Nothing needed here
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Re-run the same action
                onTabSelected(tab)
            }
        })
    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.contentFrame, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    private fun loadSpeechToTextFragment() {
        if (speechToTextFragment == null) {
            speechToTextFragment = SpeechToTextFragment()
        }

        speechToTextFragment?.let {
            loadFragment(it)
        }
    }

    private fun loadSettingsFragment() {
        val settingsFragment = SettingsFragment()
        loadFragment(settingsFragment, true)
    }

    private fun loadHistoryFragment() {
        val historyFragment = HistoryFragment.newInstance()
        loadFragment(historyFragment)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            tabLayout.getTabAt(TAB_TEXT)?.select()
        } else {
            super.onBackPressed() // Call the superclass method
        }
    }

    // Apply theme based on settings
    private fun applyTheme() {
        when (prefs.getString("theme", "System Default")) {
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    // Listen for setting changes
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "theme" -> applyTheme()
            "textSize", "fontStyle", "language" -> {
                // Refresh the current fragment if it's the SpeechToTextFragment
                speechToTextFragment?.refreshSettings()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }


}