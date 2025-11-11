// SettingsActivity.kt
package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.LOGINPAGE.MainActivity
import com.labactivity.lala.databinding.ActivitySettingsBinding
import com.labactivity.lala.homepage.MainActivity4
import com.labactivity.lala.FIXBACKBUTTON.BaseActivity
import com.labactivity.lala.UTILS.DialogUtils

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
            DialogUtils.showInfoDialog(this, "Coming Soon", "Change Password feature coming soon")
        }

        binding.emailPreferencesLayout.setOnClickListener {
            // Navigate to email preferences screen
            DialogUtils.showInfoDialog(this, "Coming Soon", "Email Preferences feature coming soon")
        }

        binding.contactSupportLayout.setOnClickListener {
            // Navigate to contact support screen
            DialogUtils.showInfoDialog(this, "Coming Soon", "Contact Support feature coming soon")
        }

        binding.sendFeedbackLayout.setOnClickListener {
            // Navigate to send feedback screen
            DialogUtils.showInfoDialog(this, "Coming Soon", "Send Feedback feature coming soon")
        }

        binding.learningReminderLayout.setOnClickListener {
            // Navigate to learning reminder screen
            DialogUtils.showInfoDialog(this, "Coming Soon", "Learning Reminder Time feature coming soon")
        }




        binding.logoutButton.setOnClickListener {
            DialogUtils.showInfoDialog(this, "Logging Out", "Logging out...")
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