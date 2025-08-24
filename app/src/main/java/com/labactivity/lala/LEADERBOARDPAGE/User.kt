package com.labactivity.lala.LEADERBOARDPAGE

// Data class for User with no-argument constructor for Firestore
data class User(
    val username: String = "",
    val score: Int = 0,
    val userId: String = "",
    val email: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    // Required empty constructor for Firestore
    constructor() : this("", 0, "", "", 0)
} 