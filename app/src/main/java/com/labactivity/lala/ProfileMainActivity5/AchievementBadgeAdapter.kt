package com.labactivity.lala.ProfileMainActivity5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.R

class AchievementBadgeAdapter(
    private val achievements: List<AchievementBadgeItem>
) : RecyclerView.Adapter<AchievementBadgeAdapter.AchievementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement_badge, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount() = achievements.size

    inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val badgeImage: ImageView = itemView.findViewById(R.id.badgeImage)
        private val badgeName: TextView = itemView.findViewById(R.id.badgeName)
        private val lockOverlay: View = itemView.findViewById(R.id.lockOverlay)

        fun bind(achievement: AchievementBadgeItem) {
            // Set badge image
            badgeImage.setImageResource(achievement.badgeDrawable)

            // Set badge name
            badgeName.text = achievement.name

            // Show/hide lock overlay based on unlock status
            if (achievement.isUnlocked) {
                lockOverlay.visibility = View.GONE
                badgeImage.alpha = 1.0f
                badgeName.alpha = 1.0f
            } else {
                lockOverlay.visibility = View.VISIBLE
                badgeImage.alpha = 0.3f
                badgeName.alpha = 0.5f
            }

            // Set click listener to show achievement details
            itemView.setOnClickListener {
                val context = itemView.context
                val message = if (achievement.isUnlocked) {
                    "${achievement.name} - ${achievement.description}\n✓ Unlocked!"
                } else {
                    "${achievement.name}\nRequires ${achievement.requiredXP} XP\n🔒 Locked"
                }

                android.widget.Toast.makeText(
                    context,
                    message,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
