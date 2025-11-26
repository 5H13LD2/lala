package com.labactivity.lala.PYTHONASSESMENT

import com.google.firebase.Timestamp

data class Challenge(
    val id: String = "",                // Firestore document ID
    val title: String = "",
    val difficulty: String = "",
    val courseId: String = "",
    val compilerType: String = "",      // Language compiler: "python", "java", "kotlin", "ruby", "javascript", "php"
    val brokenCode: String = "",
    val correctOutput: String = "",
    val hint: String = "",              // Single hint string
    val hints: List<String> = emptyList(), // Array of hints
    val description: String = "",       // Challenge description
    val category: String = "",
    val status: String = "active",
    val author: String = "",
    val tags: List<String> = emptyList(),
    val order: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: String = "",
    val isUnlocked: Boolean = true,     // Whether challenge is unlocked for user
    val codePreview: String = if (brokenCode.isNotEmpty()) {
        brokenCode.lines().firstOrNull {
            it.trim().isNotEmpty() && !it.trim().startsWith("#")
        } ?: ""
    } else {
        ""
    }
)
