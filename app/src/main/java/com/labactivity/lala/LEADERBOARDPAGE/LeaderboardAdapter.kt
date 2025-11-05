package com.labactivity.lala.LEADERBOARDPAGE

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.R
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.labactivity.lala.GAMIFICATION.AchievementManager


class LeaderboardAdapter(private val users: List<User>) :
    RecyclerView.Adapter<LeaderboardAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameText: TextView = view.findViewById(R.id.usernameText)
        val scoreText: TextView = view.findViewById(R.id.scoreText)
        val rankText: TextView = view.findViewById(R.id.rankText)
        val levelText: TextView = view.findViewById(R.id.levelText)
        val achievementBadge: ImageView = view.findViewById(R.id.achievementBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val context = holder.itemView.context
        val achievementManager = AchievementManager(context)

        // Display username
        holder.usernameText.text = user.username

        // Display XP (fallback to score for backwards compatibility)
        val displayXP = if (user.totalXP > 0) user.totalXP else user.score
        holder.scoreText.text = displayXP.toString()

        // Display level
        val level = if (user.level > 0) user.level else (displayXP / 500)
        holder.levelText.text = "Level $level"

        // Display rank
        holder.rankText.text = (position + 1).toString()

        // Top 3 rank colors
        val color = when (position) {
            0 -> Color.parseColor("#FFD700") // Gold
            1 -> Color.parseColor("#C0C0C0") // Silver
            2 -> Color.parseColor("#CD7F32") // Bronze
            else -> Color.parseColor("#888888") // Gray
        }
        (holder.rankText.background as GradientDrawable).setColor(color)

        // Display achievement badge
        val badgeResId = achievementManager.getAchievementBadge(displayXP)
        if (badgeResId != null) {
            holder.achievementBadge.setImageResource(badgeResId)
            holder.achievementBadge.visibility = View.VISIBLE
        } else {
            holder.achievementBadge.visibility = View.GONE
        }
    }



    override fun getItemCount(): Int = users.size
}
