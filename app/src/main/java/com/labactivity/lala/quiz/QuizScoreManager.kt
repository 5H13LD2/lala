package com.labactivity.lala.quiz

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.labactivity.lala.GAMIFICATION.AchievementUnlockDialog
import com.labactivity.lala.GAMIFICATION.XPManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages quiz scores both locally (SharedPreferences) and remotely (Firestore)
 *
 * Firestore Structure (Versioned Attempts):
 * users/{userId}/quiz_scores/{quizId}
 *   - Summary fields: latestScore, totalAttempts, highestScore, etc.
 *   - attempts (subcollection)
 *     - {attemptId1}: Full attempt data
 *     - {attemptId2}: Full attempt data
 */
class QuizScoreManager(private val context: Context) {

    companion object {
        private const val TAG = "QuizScoreManager"
        private const val PREFS_NAME = "quiz_scores"
        private const val KEY_PREFIX_SCORE = "score_"
        private const val KEY_PREFIX_TOTAL = "total_"
        private const val KEY_PREFIX_TIMESTAMP = "timestamp_"
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val xpManager = XPManager()

    /**
     * Saves quiz score locally and syncs to Firestore if user is authenticated
     *
     * @param moduleId The module identifier (e.g., "java_module_1")
     * @param score Number of correct answers
     * @param total Total number of questions
     * @param courseName Optional course name for display
     * @param courseId Optional course ID for reference
     * @param difficulty Optional difficulty level
     * @param timeTaken Optional time taken to complete (in milliseconds)
     */
    fun saveQuizScore(
        moduleId: String,
        score: Int,
        total: Int,
        courseName: String? = null,
        courseId: String? = null,
        difficulty: String? = null,
        timeTaken: Long = 0L
    ) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "saveQuizScore: Saving score for module: $moduleId")
        Log.d(TAG, "  Score: $score/$total")
        Log.d(TAG, "  Percentage: ${(score * 100.0 / total).toInt()}%")
        Log.d(TAG, "  Course: $courseName")

        // Save locally first
        saveScoreLocally(moduleId, score, total)

        // Sync to Firestore if user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "  User authenticated: ${currentUser.uid}")
            saveQuizAttemptToFirestore(
                userId = currentUser.uid,
                quizId = moduleId,
                score = score,
                total = total,
                courseName = courseName ?: "",
                courseId = courseId ?: "",
                difficulty = difficulty ?: "NORMAL",
                timeTaken = timeTaken
            )
        } else {
            Log.d(TAG, "  ⚠ User not authenticated - skipping Firestore sync")
        }

        Log.d(TAG, "═══════════════════════════════════════")
    }

    /**
     * Saves score to local SharedPreferences
     */
    private fun saveScoreLocally(moduleId: String, score: Int, total: Int) {
        val timestamp = System.currentTimeMillis()

        sharedPrefs.edit().apply {
            putInt("${KEY_PREFIX_SCORE}$moduleId", score)
            putInt("${KEY_PREFIX_TOTAL}$moduleId", total)
            putLong("${KEY_PREFIX_TIMESTAMP}$moduleId", timestamp)
            apply()
        }

        Log.d(TAG, "  ✓ Score saved locally")
        Log.d(TAG, "    Key: score_$moduleId = $score")
        Log.d(TAG, "    Key: total_$moduleId = $total")
        Log.d(TAG, "    Key: timestamp_$moduleId = $timestamp")
    }

    /**
     * Saves a new quiz attempt to Firestore with versioned attempt tracking
     *
     * New Structure (Non-destructive):
     * /users/{userId}/quiz_scores/{quizId}
     *   - Summary document with latest and aggregate stats
     *   /attempts (subcollection)
     *     /{attemptId1} - Full attempt data
     *     /{attemptId2} - Full attempt data
     */
    private fun saveQuizAttemptToFirestore(
        userId: String,
        quizId: String,
        score: Int,
        total: Int,
        courseName: String,
        courseId: String,
        difficulty: String,
        timeTaken: Long
    ) {
        Log.d(TAG, "  → Saving versioned attempt to Firestore...")
        Log.d(TAG, "    Path: /users/$userId/quiz_scores/$quizId/attempts")

        val percentage = (score * 100.0 / total)
        val passed = percentage >= 70.0
        val timestamp = System.currentTimeMillis()

        val quizScoreDocRef = firestore.collection("users")
            .document(userId)
            .collection("quiz_scores")
            .document(quizId)

        // Step 1: Get existing summary to determine attempt number
        quizScoreDocRef.get()
            .addOnSuccessListener { summaryDoc ->
                val attemptNumber = if (summaryDoc.exists()) {
                    (summaryDoc.getLong("totalAttempts") ?: 0L).toInt() + 1
                } else {
                    1
                }

                Log.d(TAG, "    Attempt #$attemptNumber")

                // Step 2: Create new attempt document
                val attemptData = QuizAttempt(
                    attemptId = "",  // Will be auto-generated
                    quizId = quizId,
                    courseId = courseId,
                    courseName = courseName,
                    score = score,
                    totalQuestions = total,
                    percentage = percentage,
                    passed = passed,
                    difficulty = difficulty,
                    timestamp = timestamp,
                    timeTaken = timeTaken,
                    attemptNumber = attemptNumber
                )

                // Step 3: Save attempt to subcollection
                quizScoreDocRef.collection("attempts")
                    .add(attemptData.toMap())
                    .addOnSuccessListener { attemptDocRef ->
                        Log.d(TAG, "  ✓ Attempt saved: ${attemptDocRef.id}")

                        // Step 4: Update summary document
                        updateQuizScoreSummary(
                            userId = userId,
                            quizId = quizId,
                            courseId = courseId,
                            courseName = courseName,
                            latestAttempt = attemptData,
                            isFirstAttempt = (attemptNumber == 1)
                        )

                        // Step 5: Award XP for completing the quiz
                        CoroutineScope(Dispatchers.IO).launch {
                            val xpResult = xpManager.awardQuizXP(
                                score = score,
                                totalQuestions = total,
                                difficulty = difficulty
                            )

                            // Show achievement dialog if any achievements were unlocked
                            if (xpResult.success && xpResult.unlockedAchievements.isNotEmpty()) {
                                withContext(Dispatchers.Main) {
                                    AchievementUnlockDialog.showMultipleAchievements(
                                        context,
                                        xpResult.unlockedAchievements
                                    )
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "  ✗ Failed to save attempt", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "  ✗ Failed to get attempt count", e)
            }
    }

    /**
     * Updates the quiz score summary document with latest and aggregate stats
     */
    private fun updateQuizScoreSummary(
        userId: String,
        quizId: String,
        courseId: String,
        courseName: String,
        latestAttempt: QuizAttempt,
        isFirstAttempt: Boolean
    ) {
        val quizScoreDocRef = firestore.collection("users")
            .document(userId)
            .collection("quiz_scores")
            .document(quizId)

        // Fetch all attempts to calculate aggregate statistics
        quizScoreDocRef.collection("attempts")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { attemptsSnapshot ->
                val allAttempts = attemptsSnapshot.documents.mapNotNull { doc ->
                    try {
                        QuizAttempt.fromMap(doc.data ?: emptyMap(), doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing attempt ${doc.id}", e)
                        null
                    }
                }

                if (allAttempts.isEmpty()) {
                    Log.w(TAG, "No attempts found for summary calculation")
                    return@addOnSuccessListener
                }

                // Calculate statistics
                val totalAttempts = allAttempts.size
                val highestScoreAttempt = allAttempts.maxByOrNull { it.percentage }
                val averageScore = allAttempts.map { it.score }.average()
                val averagePercentage = allAttempts.map { it.percentage }.average()
                val firstAttemptTimestamp = allAttempts.first().timestamp
                val lastAttemptTimestamp = allAttempts.last().timestamp

                // Create summary document
                val summary = QuizScoreSummary(
                    quizId = quizId,
                    courseId = courseId,
                    courseName = courseName,
                    totalAttempts = totalAttempts,
                    latestScore = latestAttempt.score,
                    latestTotal = latestAttempt.totalQuestions,
                    latestPercentage = latestAttempt.percentage,
                    latestPassed = latestAttempt.passed,
                    latestDifficulty = latestAttempt.difficulty,
                    latestTimestamp = latestAttempt.timestamp,
                    highestScore = highestScoreAttempt?.score ?: 0,
                    highestPercentage = highestScoreAttempt?.percentage ?: 0.0,
                    averageScore = averageScore,
                    averagePercentage = averagePercentage,
                    firstAttemptTimestamp = firstAttemptTimestamp,
                    lastAttemptTimestamp = lastAttemptTimestamp
                )

                // Save summary
                quizScoreDocRef.set(summary.toMap())
                    .addOnSuccessListener {
                        Log.d(TAG, "  ✓ Summary updated successfully")
                        Log.d(TAG, "    Total Attempts: $totalAttempts")
                        Log.d(TAG, "    Highest Score: ${highestScoreAttempt?.percentage}%")
                        Log.d(TAG, "    Average Score: ${"%.1f".format(averagePercentage)}%")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "  ✗ Failed to update summary", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "  ✗ Failed to fetch attempts for summary", e)
            }
    }

    /**
     * Gets the saved score for a module from local storage
     *
     * @param moduleId The module identifier
     * @return Pair of (score, total) or null if not found
     */
    fun getQuizScore(moduleId: String): Pair<Int, Int>? {
        val score = sharedPrefs.getInt("${KEY_PREFIX_SCORE}$moduleId", -1)
        val total = sharedPrefs.getInt("${KEY_PREFIX_TOTAL}$moduleId", -1)

        return if (score >= 0 && total > 0) {
            Log.d(TAG, "getQuizScore: Found score for $moduleId: $score/$total")
            Pair(score, total)
        } else {
            Log.d(TAG, "getQuizScore: No score found for $moduleId")
            null
        }
    }

    /**
     * Checks if user has passed the quiz for a module (score >= 70%)
     */
    fun hasPassedQuiz(moduleId: String): Boolean {
        val scoreData = getQuizScore(moduleId)
        return if (scoreData != null) {
            val (score, total) = scoreData
            val percentage = (score * 100.0 / total)
            percentage >= 70.0
        } else {
            false
        }
    }

    /**
     * Gets all quiz scores from Firestore for the current user
     *
     * @param onComplete Callback with map of moduleId to score data
     */
    fun getAllQuizScoresFromFirestore(onComplete: (Map<String, QuizScoreData>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "getAllQuizScoresFromFirestore: User not authenticated")
            onComplete(emptyMap())
            return
        }

        Log.d(TAG, "getAllQuizScoresFromFirestore: Fetching all scores for user ${currentUser.uid}")

        firestore.collection("users")
            .document(currentUser.uid)
            .collection("quiz_scores")
            .get()
            .addOnSuccessListener { documents ->
                val scoresMap = mutableMapOf<String, QuizScoreData>()

                documents.forEach { doc ->
                    try {
                        val scoreData = QuizScoreData(
                            moduleId = doc.getString("module_id") ?: doc.id,
                            score = doc.getLong("score")?.toInt() ?: 0,
                            total = doc.getLong("total")?.toInt() ?: 0,
                            percentage = doc.getDouble("percentage") ?: 0.0,
                            passed = doc.getBoolean("passed") ?: false,
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            attempts = doc.getLong("attempts")?.toInt() ?: 1
                        )
                        scoresMap[scoreData.moduleId] = scoreData

                        Log.d(TAG, "  Found score: ${scoreData.moduleId} = ${scoreData.score}/${scoreData.total}")
                    } catch (e: Exception) {
                        Log.e(TAG, "  Error parsing score document ${doc.id}", e)
                    }
                }

                Log.d(TAG, "  ✓ Fetched ${scoresMap.size} scores from Firestore")
                onComplete(scoresMap)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "  ✗ Failed to fetch scores from Firestore", e)
                onComplete(emptyMap())
            }
    }

    /**
     * Clears a specific quiz score (for retaking)
     */
    fun clearQuizScore(moduleId: String) {
        Log.d(TAG, "clearQuizScore: Clearing score for $moduleId")

        sharedPrefs.edit().apply {
            remove("${KEY_PREFIX_SCORE}$moduleId")
            remove("${KEY_PREFIX_TOTAL}$moduleId")
            remove("${KEY_PREFIX_TIMESTAMP}$moduleId")
            apply()
        }

        Log.d(TAG, "  ✓ Local score cleared")
    }

    /**
     * Gets all quiz attempts for a specific quiz from Firestore
     *
     * @param quizId The quiz/module identifier
     * @param onComplete Callback with list of attempts
     */
    fun getQuizAttemptsFromFirestore(
        quizId: String,
        onComplete: (List<QuizAttempt>) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "getQuizAttemptsFromFirestore: User not authenticated")
            onComplete(emptyList())
            return
        }

        Log.d(TAG, "getQuizAttemptsFromFirestore: Fetching attempts for quiz $quizId")

        firestore.collection("users")
            .document(currentUser.uid)
            .collection("quiz_scores")
            .document(quizId)
            .collection("attempts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val attempts = documents.mapNotNull { doc ->
                    try {
                        QuizAttempt.fromMap(doc.data, doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing attempt ${doc.id}", e)
                        null
                    }
                }
                Log.d(TAG, "  ✓ Fetched ${attempts.size} attempts for quiz $quizId")
                onComplete(attempts)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "  ✗ Failed to fetch attempts for quiz $quizId", e)
                onComplete(emptyList())
            }
    }

    /**
     * Gets all quiz score summaries for the current user
     *
     * @param onComplete Callback with list of quiz summaries
     */
    fun getAllQuizSummariesFromFirestore(
        onComplete: (List<QuizScoreSummary>) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "getAllQuizSummariesFromFirestore: User not authenticated")
            onComplete(emptyList())
            return
        }

        Log.d(TAG, "getAllQuizSummariesFromFirestore: Fetching all summaries")

        firestore.collection("users")
            .document(currentUser.uid)
            .collection("quiz_scores")
            .orderBy("latestTimestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val summaries = documents.mapNotNull { doc ->
                    try {
                        QuizScoreSummary.fromMap(doc.data)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing summary ${doc.id}", e)
                        null
                    }
                }
                Log.d(TAG, "  ✓ Fetched ${summaries.size} quiz summaries")
                onComplete(summaries)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "  ✗ Failed to fetch quiz summaries", e)
                onComplete(emptyList())
            }
    }

    /**
     * Gets all quiz attempts across all quizzes for the current user
     *
     * @param limit Maximum number of recent attempts to fetch
     * @param onComplete Callback with list of attempts
     */
    fun getAllRecentAttemptsFromFirestore(
        limit: Int = 20,
        onComplete: (List<QuizAttempt>) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "getAllRecentAttemptsFromFirestore: User not authenticated")
            onComplete(emptyList())
            return
        }

        Log.d(TAG, "getAllRecentAttemptsFromFirestore: Fetching recent attempts (limit: $limit)")

        firestore.collection("users")
            .document(currentUser.uid)
            .collection("quiz_scores")
            .get()
            .addOnSuccessListener { quizDocs ->
                val allAttempts = mutableListOf<QuizAttempt>()
                var completedQueries = 0
                val totalQuizzes = quizDocs.size()

                if (totalQuizzes == 0) {
                    onComplete(emptyList())
                    return@addOnSuccessListener
                }

                quizDocs.forEach { quizDoc ->
                    quizDoc.reference.collection("attempts")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { attemptDocs ->
                            attemptDocs.forEach { attemptDoc ->
                                try {
                                    val attempt = QuizAttempt.fromMap(attemptDoc.data, attemptDoc.id)
                                    allAttempts.add(attempt)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing attempt ${attemptDoc.id}", e)
                                }
                            }

                            completedQueries++
                            if (completedQueries == totalQuizzes) {
                                // Sort all attempts by timestamp and limit
                                val sortedAttempts = allAttempts
                                    .sortedByDescending { it.timestamp }
                                    .take(limit)
                                Log.d(TAG, "  ✓ Fetched ${sortedAttempts.size} total recent attempts")
                                onComplete(sortedAttempts)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to fetch attempts for quiz ${quizDoc.id}", e)
                            completedQueries++
                            if (completedQueries == totalQuizzes) {
                                val sortedAttempts = allAttempts
                                    .sortedByDescending { it.timestamp }
                                    .take(limit)
                                onComplete(sortedAttempts)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "  ✗ Failed to fetch quiz documents", e)
                onComplete(emptyList())
            }
    }

    /**
     * Gets quiz performance analytics for a specific quiz
     *
     * @param quizId The quiz/module identifier
     * @param onComplete Callback with analytics data
     */
    fun getQuizAnalytics(
        quizId: String,
        onComplete: (QuizAnalytics?) -> Unit
    ) {
        getQuizAttemptsFromFirestore(quizId) { attempts ->
            if (attempts.isEmpty()) {
                onComplete(null)
                return@getQuizAttemptsFromFirestore
            }

            val analytics = QuizAnalytics(
                quizId = quizId,
                totalAttempts = attempts.size,
                highestScore = attempts.maxByOrNull { it.percentage }?.percentage ?: 0.0,
                lowestScore = attempts.minByOrNull { it.percentage }?.percentage ?: 0.0,
                averageScore = attempts.map { it.percentage }.average(),
                improvementRate = calculateImprovementRate(attempts),
                passRate = attempts.count { it.passed }.toDouble() / attempts.size * 100,
                averageTimeTaken = attempts.map { it.timeTaken }.average().toLong(),
                attempts = attempts
            )

            onComplete(analytics)
        }
    }

    /**
     * Calculates improvement rate between first and last attempt
     */
    private fun calculateImprovementRate(attempts: List<QuizAttempt>): Double {
        if (attempts.size < 2) return 0.0

        val sorted = attempts.sortedBy { it.timestamp }
        val first = sorted.first().percentage
        val last = sorted.last().percentage

        return last - first
    }

    /**
     * Data class for quiz score information (LEGACY - kept for backward compatibility)
     */
    data class QuizScoreData(
        val moduleId: String,
        val score: Int,
        val total: Int,
        val percentage: Double,
        val passed: Boolean,
        val timestamp: Long,
        val attempts: Int
    )
}

/**
 * Analytics data for quiz performance tracking
 */
data class QuizAnalytics(
    val quizId: String,
    val totalAttempts: Int,
    val highestScore: Double,
    val lowestScore: Double,
    val averageScore: Double,
    val improvementRate: Double,
    val passRate: Double,
    val averageTimeTaken: Long,
    val attempts: List<QuizAttempt>
)