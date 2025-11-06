package com.labactivity.lala.GAMIFICATION

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.labactivity.lala.LEADERBOARDPAGE.Achievement
import com.labactivity.lala.R

/**
 * AchievementUnlockDialog - Shows a celebration dialog when an achievement is unlocked
 */
class AchievementUnlockDialog(
    private val context: Context,
    private val achievement: Achievement
) {

    private var dialog: Dialog? = null

    /**
     * Show the achievement unlock dialog
     */
    fun show() {
        // Create dialog
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(true)
        }

        // Inflate layout
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_achievement_unlock, null)
        dialog?.setContentView(view)

        // Set achievement details
        view.findViewById<TextView>(R.id.achievementTitleDialog).text = achievement.title
        view.findViewById<TextView>(R.id.achievementDescriptionDialog).text = achievement.description
        view.findViewById<TextView>(R.id.rewardMessageDialog).text = achievement.rewardMessage

        // Set badge earned text
        val badgeText = when (achievement.badgeTier) {
            Achievement.TIER_BRONZE -> "🥉 Bronze Badge Earned"
            Achievement.TIER_SILVER -> "🥈 Silver Badge Earned"
            Achievement.TIER_GOLD -> "🥇 Gold Badge Earned"
            Achievement.TIER_PLATINUM -> "💎 Platinum Badge Earned"
            Achievement.TIER_DIAMOND -> "💎 Diamond Badge Earned"
            else -> "Badge Earned"
        }
        view.findViewById<TextView>(R.id.badgeEarnedTextDialog).text = badgeText

        // Set badge color
        val badgeColor = Color.parseColor(Achievement.getBadgeColor(achievement.badgeTier))
        view.findViewById<TextView>(R.id.badgeEarnedTextDialog).setTextColor(badgeColor)

        // Close button
        view.findViewById<Button>(R.id.closeDialogBtn).setOnClickListener {
            dismiss()
        }

        // Show dialog
        dialog?.show()
    }

    /**
     * Dismiss the dialog
     */
    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }

    companion object {
        /**
         * Show achievement unlock dialog for a specific achievement
         */
        fun showAchievementUnlock(context: Context, achievement: Achievement) {
            AchievementUnlockDialog(context, achievement).show()
        }

        /**
         * Show achievement unlock dialog for multiple achievements
         * Shows them one by one
         */
        fun showMultipleAchievements(context: Context, achievements: List<Achievement>) {
            if (achievements.isEmpty()) return

            var currentIndex = 0

            fun showNext() {
                if (currentIndex < achievements.size) {
                    val dialog = AchievementUnlockDialog(context, achievements[currentIndex])
                    dialog.show()

                    // Auto-dismiss after 5 seconds and show next
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                        currentIndex++
                        showNext()
                    }, 5000)
                }
            }

            showNext()
        }
    }
}
