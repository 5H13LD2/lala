package com.labactivity.lala


import android.view.LayoutInflater


import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter : ListAdapter<LeaderboardItem, LeaderboardAdapter.LeaderboardViewHolder>(LeaderboardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvScore: TextView = itemView.findViewById(R.id.tvScore)

        fun bind(item: LeaderboardItem) {
            tvRank.text = item.rank.toString()
            tvUsername.text = item.username
            tvScore.text = item.score.toString()

            // Set different colors for top 3 ranks
            when (item.rank) {
                1 -> {
                    tvRank.setBackgroundResource(R.drawable.rank_gold_background)
                    tvRank.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                2 -> {
                    tvRank.setBackgroundResource(R.drawable.rank_silver_background)
                    tvRank.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                3 -> {
                    tvRank.setBackgroundResource(R.drawable.rank_bronze_background)
                    tvRank.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                else -> {
                    tvRank.setBackgroundResource(R.drawable.rank_circle_background)
                    tvRank.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
            }
        }
    }

    class LeaderboardDiffCallback : DiffUtil.ItemCallback<LeaderboardItem>() {
        override fun areItemsTheSame(oldItem: LeaderboardItem, newItem: LeaderboardItem): Boolean {
            return oldItem.username == newItem.username
        }

        override fun areContentsTheSame(oldItem: LeaderboardItem, newItem: LeaderboardItem): Boolean {
            return oldItem == newItem
        }
    }
}