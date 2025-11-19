package com.labactivity.lala.JAVACOMPILER.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.labactivity.lala.JAVACOMPILER.models.*
import com.labactivity.lala.GAMIFICATION.XPManager
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Firestore helper class for managing Java challenges
 * Provides coroutine-based methods for CRUD operations on Java challenges
 * Only shows challenges for enrolled courses
 */
class FirestoreJavaHelper {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val xpManager = XPManager()

    companion object {
        private const val TAG = "FirestoreJavaHelper"
        private const val COLLECTION_JAVA_CHALLENGES = "technical_assesment"
        private const val COLLECTION_USER_PROGRESS = "user_progress"
        private const val SUB_COLLECTION_JAVA_PROGRESS = "technical_assessment_progress"

        // Singleton instance
        @Volatile
        private var instance: FirestoreJavaHelper? = null

        fun getInstance(): FirestoreJavaHelper {
            return instance ?: synchronized(this) {
                instance ?: FirestoreJavaHelper().also { instance = it }
            }
        }
    }

    // ==================== Challenge Fetching Methods ====================

    /**
     * Fetches all active Java challenges from Firestore with unlock status
     * Only returns challenges for courses the user is enrolled in
     * @return List of JavaChallenge objects sorted by order
     */
    suspend fun getAllChallenges(): List<JavaChallenge> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: run {
                Log.w(TAG, "⚠️ User not authenticated")
                return@withContext emptyList()
            }

            // Get enrolled course IDs
            val enrolledCourseIds = getUserEnrolledCourseIds(userId)
            Log.d(TAG, "✅ User enrolled in courses: $enrolledCourseIds")

            if (enrolledCourseIds.isEmpty()) {
                Log.d(TAG, "⚠️ No enrolled courses found")
                return@withContext emptyList()
            }

            Log.d(TAG, "Fetching Java challenges for enrolled courses from Firestore")

            // Fetch challenges in batches (Firestore 'in' query limit is 10)
            val allChallenges = mutableListOf<JavaChallenge>()
            val batches = enrolledCourseIds.chunked(10)

            for (batch in batches) {
                val snapshot = firestore.collection(COLLECTION_JAVA_CHALLENGES)
                    .whereEqualTo("status", "available")
                    .whereIn("courseId", batch)
                    .orderBy("order", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val batchChallenges = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(JavaChallenge::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing challenge document ${doc.id}: ${e.message}")
                        null
                    }
                }
                allChallenges.addAll(batchChallenges)
            }

            Log.d(TAG, "Successfully fetched ${allChallenges.size} Java challenges for enrolled courses")

            // Apply unlock logic
            val challengesWithUnlockStatus = applyUnlockLogic(allChallenges)
            Log.d(TAG, "✅ Applied unlock logic to ${challengesWithUnlockStatus.size} Java challenges")

            challengesWithUnlockStatus

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Java challenges: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetch user's enrolled course IDs
     * @param userId The user ID
     * @return List of enrolled course IDs
     */
    private suspend fun getUserEnrolledCourseIds(userId: String): List<String> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val courseTaken = document.get("courseTaken") as? List<Map<String, Any>> ?: emptyList()
            val ids = courseTaken.mapNotNull { it["courseId"] as? String }
            Log.d(TAG, "📘 Found ${ids.size} enrolled courses")
            ids
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting enrolled course IDs", e)
            emptyList()
        }
    }

    /**
     * Apply unlock logic to Java challenges based on difficulty progression
     * Rule: A challenge is unlocked if all easier challenges are completed
     * Difficulty order: Easy -> Medium -> Hard
     * @param challenges List of challenges to process
     * @return List of challenges with isUnlocked field set correctly
     */
    private suspend fun applyUnlockLogic(challenges: List<JavaChallenge>): List<JavaChallenge> {
        return try {
            // Get all user progress
            val allProgress = getAllUserProgress()
            val completedChallengeIds = allProgress.filter { it.passed }.map { it.challengeId }.toSet()

            Log.d(TAG, "🔓 User has completed ${completedChallengeIds.size} Java challenges")

            // Group challenges by difficulty
            val easyChallenges = challenges.filter { it.difficulty.equals("Easy", ignoreCase = true) }
            val mediumChallenges = challenges.filter { it.difficulty.equals("Medium", ignoreCase = true) }
            val hardChallenges = challenges.filter { it.difficulty.equals("Hard", ignoreCase = true) }

            // Check if all challenges of a difficulty are completed
            val allEasyCompleted = easyChallenges.all { it.id in completedChallengeIds }
            val allMediumCompleted = mediumChallenges.all { it.id in completedChallengeIds }

            Log.d(TAG, "🔓 Java Easy: ${easyChallenges.size} total, all completed: $allEasyCompleted")
            Log.d(TAG, "🔓 Java Medium: ${mediumChallenges.size} total, all completed: $allMediumCompleted")
            Log.d(TAG, "🔓 Java Hard: ${hardChallenges.size} total")

            // Apply unlock logic
            challenges.map { challenge ->
                val isUnlocked = when (challenge.difficulty.lowercase()) {
                    "easy" -> {
                        // All Easy challenges are always unlocked
                        true
                    }
                    "medium" -> {
                        // Medium challenges unlock when all Easy are completed
                        allEasyCompleted
                    }
                    "hard" -> {
                        // Hard challenges unlock when all Easy AND Medium are completed
                        allEasyCompleted && allMediumCompleted
                    }
                    else -> {
                        // Unknown difficulty - unlock by default
                        true
                    }
                }

                if (!isUnlocked) {
                    Log.d(TAG, "🔒 Java Challenge locked: ${challenge.title} (${challenge.difficulty})")
                }

                challenge.copy(isUnlocked = isUnlocked)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error applying unlock logic to Java challenges", e)
            // On error, return all challenges as unlocked (fail-safe)
            challenges.map { it.copy(isUnlocked = true) }
        }
    }

    /**
     * Fetches challenges filtered by difficulty
     * @param difficulty The difficulty level ("Easy", "Medium", "Hard")
     * @return List of JavaChallenge objects with the specified difficulty
     */
    suspend fun getChallengesByDifficulty(difficulty: String): List<JavaChallenge> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching Java challenges with difficulty: $difficulty")

                val snapshot = firestore.collection(COLLECTION_JAVA_CHALLENGES)
                    .whereEqualTo("status", "available")
                    .whereEqualTo("difficulty", difficulty)
                    .orderBy("order", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val challenges = snapshot.toObjects(JavaChallenge::class.java)
                Log.d(TAG, "Found ${challenges.size} challenges with difficulty $difficulty")
                challenges

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching challenges by difficulty: ${e.message}", e)
                emptyList()
            }
        }

    /**
     * Fetches a single challenge by its document ID
     * @param challengeId The Firestore document ID of the challenge
     * @return JavaChallenge object or null if not found
     */
    suspend fun getChallengeById(challengeId: String): JavaChallenge? =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching Java challenge with ID: $challengeId")

                val document = firestore.collection(COLLECTION_JAVA_CHALLENGES)
                    .document(challengeId)
                    .get()
                    .await()

                val challenge = document.toObject(JavaChallenge::class.java)

                if (challenge != null) {
                    Log.d(TAG, "Successfully fetched challenge: ${challenge.title}")
                } else {
                    Log.w(TAG, "Challenge with ID $challengeId not found")
                }
                challenge

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching challenge by ID: ${e.message}", e)
                null
            }
        }

    // ==================== User Progress Methods ====================

    /**
     * Fetches the user's progress for a specific challenge
     * @param challengeId The challenge ID
     * @return JavaChallengeProgress object or null if not found
     */
    suspend fun getUserProgress(challengeId: String): JavaChallengeProgress? =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext null
                Log.d(TAG, "Fetching progress for challenge $challengeId and user $userId")

                val document = firestore.collection(COLLECTION_USER_PROGRESS)
                    .document(userId)
                    .collection(SUB_COLLECTION_JAVA_PROGRESS)
                    .document(challengeId)
                    .get()
                    .await()

                val progress = document.toObject(JavaChallengeProgress::class.java)
                Log.d(TAG, "Progress found: ${progress != null}")
                progress

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user progress: ${e.message}", e)
                null
            }
        }

    /**
     * Updates user progress after a challenge attempt
     * @param challengeId The challenge ID
     * @param passed Whether the user passed the challenge
     * @param score The score achieved (0-100)
     * @param userCode The Java code the user submitted
     * @param timeTaken Time taken in seconds
     */
    suspend fun updateProgressAfterAttempt(
        challengeId: String,
        passed: Boolean,
        score: Int,
        userCode: String,
        timeTaken: Long
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext false

            // Get existing progress or create new
            val existingProgress = getUserProgress(challengeId)

            val newProgress = JavaChallengeProgress(
                challengeId = challengeId,
                status = if (passed) "completed" else "in_progress",
                attempts = (existingProgress?.attempts ?: 0) + 1,
                bestScore = maxOf(score, existingProgress?.bestScore ?: 0),
                lastAttemptDate = getCurrentTimestamp(),
                timeTaken = timeTaken,
                userCode = userCode,
                passed = passed
            )

            firestore.collection(COLLECTION_USER_PROGRESS)
                .document(userId)
                .collection(SUB_COLLECTION_JAVA_PROGRESS)
                .document(challengeId)
                .set(newProgress)
                .await()

            Log.d(TAG, "Progress saved successfully")

            // Award XP if the challenge was passed
            if (passed) {
                // Get challenge details for title
                val challenge = getChallengeById(challengeId)
                xpManager.awardTechnicalAssessmentXP(
                    challengeTitle = challenge?.title ?: "Java Challenge",
                    passed = true,
                    score = score
                )
                Log.d(TAG, "✅ Awarded XP for completing Java challenge: $challengeId")
            }

            true

        } catch (e: Exception) {
            Log.e(TAG, "Error updating progress after attempt: ${e.message}", e)
            false
        }
    }

    /**
     * Fetches all user progress records
     * @return List of JavaChallengeProgress objects
     */
    suspend fun getAllUserProgress(): List<JavaChallengeProgress> =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext emptyList()
                Log.d(TAG, "Fetching all progress for user $userId")

                val snapshot = firestore.collection(COLLECTION_USER_PROGRESS)
                    .document(userId)
                    .collection(SUB_COLLECTION_JAVA_PROGRESS)
                    .get()
                    .await()

                val progressList = snapshot.toObjects(JavaChallengeProgress::class.java)
                Log.d(TAG, "Found ${progressList.size} progress records")
                progressList

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching all user progress: ${e.message}", e)
                emptyList()
            }
        }

    /**
     * Calculates statistics for the current user's Java challenge progress
     * @return JavaChallengeStats object with aggregated statistics
     */
    suspend fun getUserStats(): JavaChallengeStats = withContext(Dispatchers.IO) {
        try {
            val allProgress = getAllUserProgress()
            val allChallenges = getAllChallenges()

            val completedCount = allProgress.count { it.status == "completed" }
            val totalAttempts = allProgress.sumOf { it.attempts }
            val averageScore = if (allProgress.isNotEmpty()) {
                allProgress.map { it.bestScore }.average()
            } else {
                0.0
            }
            val totalTime = allProgress.sumOf { it.timeTaken }

            JavaChallengeStats(
                totalChallenges = allChallenges.size,
                completedChallenges = completedCount,
                totalAttempts = totalAttempts,
                averageScore = averageScore,
                totalTimeTaken = totalTime
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating user stats: ${e.message}", e)
            JavaChallengeStats()
        }
    }

    // ==================== Utility Methods ====================

    /**
     * Gets the current timestamp in ISO 8601 format
     * @return Current timestamp as string
     */
    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }

    /**
     * Clears the singleton instance (useful for testing)
     */
    fun clearInstance() {
        instance = null
    }
}
