package com.labactivity.lala.LEADERBOARDPAGE

// Data class for User with no-argument constructor for Firestore
data class User(
    val username: String = "",
    val score: Int = 0,  // Legacy field - kept for backwards compatibility
    val totalXP: Int = 0,  // New XP field
    val level: Int = 0,    // New level field
    val achievementTier: String = "",  // Achievement tier (BRONZE, SILVER, GOLD, etc.) - legacy
    val currentBadge: String = "",  // Current badge tier (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND)
    val achievementsUnlocked: List<String> = emptyList(),  // List of unlocked achievement IDs
    val userId: String = "",
    val email: String = "",
    val profilePhotoBase64: String = "",  // Profile photo in Base64 format
    val timestamp: Long = System.currentTimeMillis()
) {
    // Required empty constructor for Firestore
    constructor() : this("", 0, 0, 0, "", "", emptyList(), "", "", "", 0)
} 