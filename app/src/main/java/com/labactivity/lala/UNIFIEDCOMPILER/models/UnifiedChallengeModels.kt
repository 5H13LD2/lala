package com.labactivity.lala.UNIFIEDCOMPILER.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.Timestamp

/**
 * Unified Challenge Data Model
 * Works with any programming language (Python, Java, Kotlin, SQL, etc.)
 *
 * This model is compatible with your existing technical_assesment collection
 */
data class UnifiedChallenge(
    @DocumentId
    val id: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("description")
    val description: String = "",

    @PropertyName("difficulty")
    val difficulty: String = "", // "Easy", "Medium", "Hard"

    @PropertyName("courseId")
    val courseId: String = "",

    @PropertyName("brokenCode")
    val brokenCode: String = "", // Code to fix or starting point

    @PropertyName("correctOutput")
    val correctOutput: String = "", // Expected output

    @PropertyName("hint")
    val hint: String = "",

    @PropertyName("hints")
    val hints: List<String> = emptyList(), // Array of hints from Firebase

    @PropertyName("category")
    val category: String = "",

    @PropertyName("status")
    val status: String = "active",

    @PropertyName("author")
    val author: String = "",

    @PropertyName("tags")
    val tags: List<String> = emptyList(),

    @PropertyName("order")
    val order: Int = 0,

    @PropertyName("createdAt")
    val createdAt: Timestamp? = null,

    @PropertyName("updatedAt")
    val updatedAt: String = "",

    @PropertyName("compilerType")
    var compilerType: String = "", // Now read from Firebase: "python", "java", "kotlin", etc.

    // Runtime field (not in Firebase)
    var isUnlocked: Boolean = true
)

/**
 * Unified Challenge Progress
 * Tracks user's progress on challenges
 */
data class UnifiedChallengeProgress(
    @DocumentId
    val challengeId: String = "",

    @PropertyName("challengeTitle")
    val challengeTitle: String = "",

    @PropertyName("status")
    val status: String = "not_started", // "not_started", "in_progress", "completed"

    @PropertyName("attempts")
    val attempts: Int = 0,

    @PropertyName("bestScore")
    val bestScore: Int = 0,

    @PropertyName("lastAttemptDate")
    val lastAttemptDate: Timestamp? = null,

    @PropertyName("timeTaken")
    val timeTaken: Long = 0, // milliseconds

    @PropertyName("userCode")
    val userCode: String = "",

    @PropertyName("passed")
    val passed: Boolean = false,

    @PropertyName("compilerType")
    val compilerType: String = "",

    @PropertyName("updatedAt")
    val updatedAt: Timestamp? = null
)

/**
 * Challenge Execution Result
 * Contains the result of executing a challenge
 */
data class ChallengeExecutionResult(
    val compilerResult: CompilerResult,
    val score: Int,
    val passed: Boolean,
    val executionTime: Long,
    val testCasesPassed: Int = 0,
    val totalTestCases: Int = 0
)
