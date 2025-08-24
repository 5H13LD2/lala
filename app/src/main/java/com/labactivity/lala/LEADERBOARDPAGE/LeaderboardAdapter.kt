package com.labactivity.lala.LEADERBOARDPAGE

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.lala.R
import android.graphics.Color
import android.graphics.drawable.GradientDrawable


class LeaderboardAdapter(private val users: List<User>) :
    RecyclerView.Adapter<LeaderboardAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameText: TextView = view.findViewById(R.id.usernameText)
        val scoreText: TextView = view.findViewById(R.id.scoreText)
        val rankText: TextView = view.findViewById(R.id.rankText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.usernameText.text = user.username
        holder.scoreText.text = "${user.score} pts"
        holder.rankText.text = (position + 1).toString()

        // Top 3 colors
        val color = when (position) {
            0 -> Color.parseColor("#FFD700") // Gold
            1 -> Color.parseColor("#C0C0C0") // Silver
            2 -> Color.parseColor("#CD7F32") // Bronze
            else -> Color.parseColor("#888888") // Gray
        }
        (holder.rankText.background as GradientDrawable).setColor(color)
    }



    override fun getItemCount(): Int = users.size
}
