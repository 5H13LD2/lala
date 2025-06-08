package com.labactivity.lala

data class LeaderboardItem(
    val username: String = "",
    val score: Int = 0,
    val rank: Int = 0
) {
    companion object {
        fun fromFirestore(username: String, score: Int, rank: Int): LeaderboardItem {
            return LeaderboardItem(username, score, rank)
        }
    }
}