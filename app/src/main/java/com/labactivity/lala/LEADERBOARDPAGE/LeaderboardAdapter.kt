package com.labactivity.lala.LEADERBOARDPAGE

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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
        val userBadgeText: TextView = view.findViewById(R.id.userBadgeText)
        val profileImageView: ImageView = view.findViewById(R.id.profileImageView)

        // Progress bar elements
        val xpProgressBar: View = view.findViewById(R.id.xpProgressBar)
        val currentLevelText: TextView = view.findViewById(R.id.currentLevelText)
        val nextLevelText: TextView = view.findViewById(R.id.nextLevelText)
        val xpProgressText: TextView = view.findViewById(R.id.xpProgressText)
        val milestoneText: TextView = view.findViewById(R.id.milestoneText)
        val milestoneMarkersContainer: android.widget.LinearLayout = view.findViewById(R.id.milestoneMarkersContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val context = holder.itemView.context

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

        // Display profile photo from Base64
        if (user.profilePhotoBase64.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(user.profilePhotoBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.profileImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // If there's an error decoding, show default image
                holder.profileImageView.setImageResource(R.drawable.default_profile)
            }
        } else {
            // No profile photo, show default
            holder.profileImageView.setImageResource(R.drawable.default_profile)
        }

        // Display user badge on profile and achievement badge icon
        val userBadge = user.currentBadge

        // Display achievement badge icon based on badge tier
        if (userBadge.isNotEmpty() && userBadge != Achievement.TIER_NONE) {
            val badgeDrawable = when (userBadge) {
                Achievement.TIER_BRONZE -> R.drawable.badge_bronze
                Achievement.TIER_SILVER -> R.drawable.badge_silver
                Achievement.TIER_GOLD -> R.drawable.badge_gold
                Achievement.TIER_PLATINUM -> R.drawable.badge_platinum
                Achievement.TIER_DIAMOND -> R.drawable.badge_diamond
                else -> null
            }
            if (badgeDrawable != null) {
                holder.achievementBadge.setImageResource(badgeDrawable)
                holder.achievementBadge.visibility = View.VISIBLE
            } else {
                holder.achievementBadge.visibility = View.GONE
            }

            // Display badge emoji next to username
            holder.userBadgeText.visibility = View.VISIBLE
            val badgeEmoji = when (userBadge) {
                Achievement.TIER_BRONZE -> "🥉"
                Achievement.TIER_SILVER -> "🥈"
                Achievement.TIER_GOLD -> "🥇"
                Achievement.TIER_PLATINUM -> "💎"
                Achievement.TIER_DIAMOND -> "💎"
                else -> ""
            }
            holder.userBadgeText.text = badgeEmoji
        } else {
            holder.achievementBadge.visibility = View.GONE
            holder.userBadgeText.visibility = View.GONE
        }

        // Calculate XP progress for current level (500 XP per level)
        val XP_PER_LEVEL = 500
        val currentLevel = level
        val nextLevel = level + 1
        val xpInCurrentLevel = displayXP % XP_PER_LEVEL
        val progressPercentage = (xpInCurrentLevel.toFloat() / XP_PER_LEVEL.toFloat() * 100).toInt()

        // Update progress bar width
        val progressBarWidth = (holder.xpProgressBar.parent as View).width
        val params = holder.xpProgressBar.layoutParams
        params.width = (progressBarWidth * progressPercentage / 100)
        holder.xpProgressBar.layoutParams = params

        // Update progress text
        holder.currentLevelText.text = "Level $currentLevel"
        holder.nextLevelText.text = "Level $nextLevel"
        holder.xpProgressText.text = "$xpInCurrentLevel/$XP_PER_LEVEL XP"

        // Check if user just reached a milestone (multiple of 500)
        val isMilestone = displayXP > 0 && displayXP % XP_PER_LEVEL == 0
        if (isMilestone) {
            holder.milestoneText.visibility = View.VISIBLE
            holder.milestoneText.text = "🏆 Level $currentLevel Achieved!"
        } else {
            holder.milestoneText.visibility = View.GONE
        }

        // Add milestone markers (every 500 XP)
        holder.milestoneMarkersContainer.removeAllViews()
        // For visual clarity, show markers at 25%, 50%, 75% positions in the progress bar
        val markerPositions = listOf(25, 50, 75)
        markerPositions.forEach { percentage ->
            val marker = ImageView(context)
            marker.setImageResource(R.drawable.milestone_marker)
            val markerParams = android.widget.LinearLayout.LayoutParams(
                16, // width in dp
                16  // height in dp
            )
            markerParams.marginStart = (progressBarWidth * percentage / 100) - 8 // Center the marker
            marker.layoutParams = markerParams
            holder.milestoneMarkersContainer.addView(marker)
        }
    }



    override fun getItemCount(): Int = users.size
}
