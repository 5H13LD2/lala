package com.labactivity.lala.LEADERBOARDPAGE

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.R

class AchievementAdapter(private val achievements: List<Achievement>) :
    RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    class AchievementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val achievementIcon: ImageView = view.findViewById(R.id.achievementIcon)
        val achievementTitle: TextView = view.findViewById(R.id.achievementTitle)
        val achievementDescription: TextView = view.findViewById(R.id.achievementDescription)
        val achievementReward: TextView = view.findViewById(R.id.achievementReward)
        val lockIcon: ImageView = view.findViewById(R.id.lockIcon)
        val xpRequiredText: TextView = view.findViewById(R.id.xpRequiredText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]

        holder.achievementTitle.text = achievement.title
        holder.achievementDescription.text = achievement.description
        holder.xpRequiredText.text = "${achievement.requiredXP} XP"

        if (achievement.isUnlocked) {
            // Achievement is unlocked
            holder.lockIcon.setImageResource(android.R.drawable.checkbox_on_background)
            holder.lockIcon.setColorFilter(android.graphics.Color.parseColor("#4CAF50"))
            holder.achievementIcon.alpha = 1.0f
            holder.achievementReward.visibility = View.VISIBLE
            holder.achievementReward.text = "✓ ${achievement.rewardMessage}"
            holder.xpRequiredText.visibility = View.GONE
        } else {
            // Achievement is locked
            holder.lockIcon.setImageResource(android.R.drawable.ic_lock_lock)
            holder.lockIcon.setColorFilter(android.graphics.Color.parseColor("#BDBDBD"))
            holder.achievementIcon.alpha = 0.3f
            holder.achievementReward.visibility = View.GONE
            holder.xpRequiredText.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = achievements.size
}
