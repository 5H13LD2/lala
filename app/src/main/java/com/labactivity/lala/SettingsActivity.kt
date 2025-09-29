// SettingsActivity.kt
package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.LOGINPAGE.MainActivity
import com.labactivity.lala.databinding.ActivitySettingsBinding
import com.labactivity.lala.homepage.MainActivity4

import com.labactivity.lala.FIXBACKBUTTON.BaseActivity

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        setupSwitches()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity4::class.java)
            startActivity(intent)
        }
    }

    private fun setupClickListeners() {
        binding.changePasswordLayout.setOnClickListener {
            // Navigate to change password screen
            Toast.makeText(this, "Change Password clicked", Toast.LENGTH_SHORT).show()
        }

        binding.emailPreferencesLayout.setOnClickListener {
            // Navigate to email preferences screen
            Toast.makeText(this, "Email Preferences clicked", Toast.LENGTH_SHORT).show()
        }

        binding.contactSupportLayout.setOnClickListener {
            // Navigate to contact support screen
            Toast.makeText(this, "Contact Support clicked", Toast.LENGTH_SHORT).show()
        }

        binding.sendFeedbackLayout.setOnClickListener {
            // Navigate to send feedback screen
            Toast.makeText(this, "Send Feedback clicked", Toast.LENGTH_SHORT).show()
        }

        binding.learningReminderLayout.setOnClickListener {
            // Navigate to learning reminder screen
            Toast.makeText(this, "Learning Reminder Time clicked", Toast.LENGTH_SHORT).show()
        }




        binding.logoutButton.setOnClickListener {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // Add your logout logic here
        }
    }

    private fun setupSwitches() {
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save notification preference
            saveNotificationPreference(isChecked)
        }

        binding.hapticFeedbackSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save haptic feedback preference
            saveHapticFeedbackPreference(isChecked)
        }
    }

    private fun saveNotificationPreference(disabled: Boolean) {
        // Save to SharedPreferences
        getSharedPreferences("app_preferences", MODE_PRIVATE).edit()
            .putBoolean("notifications_enabled",disabled)
            .apply()
    }

    private fun saveHapticFeedbackPreference(enabled: Boolean) {
        // Save to SharedPreferences
        getSharedPreferences("app_preferences", MODE_PRIVATE).edit()
            .putBoolean("haptic_feedback_enabled", enabled)
            .apply()
    }
}