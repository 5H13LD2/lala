package com.labactivity.lala.quiz

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Manages quiz scores both locally (SharedPreferences) and remotely (Firestore)
 */
class QuizScoreManager(context: Context) {

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

    /**
     * Saves quiz score locally and syncs to Firestore if user is authenticated
     *
     * @param moduleId The module identifier (e.g., "java_module_1")
     * @param score Number of correct answers
     * @param total Total number of questions
     */
    fun saveQuizScore(moduleId: String, score: Int, total: Int) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "saveQuizScore: Saving score for module: $moduleId")
        Log.d(TAG, "  Score: $score/$total")
        Log.d(TAG, "  Percentage: ${(score * 100.0 / total).toInt()}%")

        // Save locally first
        saveScoreLocally(moduleId, score, total)

        // Sync to Firestore if user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "  User authenticated: ${currentUser.uid}")
            saveScoreToFirestore(currentUser.uid, moduleId, score, total)
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
     * Saves score to Firestore under user's document
     *
     * Structure:
     * /users/{userId}/quiz_scores/{moduleId}
     * {
     *   "score": 15,
     *   "total": 20,
     *   "percentage": 75.0,
     *   "passed": true,
     *   "timestamp": 1234567890,
     *   "attempts": 3
     * }
     */
    private fun saveScoreToFirestore(userId: String, moduleId: String, score: Int, total: Int) {
        Log.d(TAG, "  → Syncing to Firestore...")
        Log.d(TAG, "    Path: /users/$userId/quiz_scores/$moduleId")

        val percentage = (score * 100.0 / total)
        val passed = percentage >= 70.0

        val scoreData = hashMapOf(
            "score" to score,
            "total" to total,
            "percentage" to percentage,
            "passed" to passed,
            "timestamp" to System.currentTimeMillis(),
            "module_id" to moduleId
        )

        val docRef = firestore.collection("users")
            .document(userId)
            .collection("quiz_scores")
            .document(moduleId)

        // Get existing attempts count
        docRef.get()
            .addOnSuccessListener { document ->
                val attempts = if (document.exists()) {
                    (document.getLong("attempts") ?: 0L) + 1
                } else {
                    1L
                }

                scoreData["attempts"] = attempts

                Log.d(TAG, "    Attempt #$attempts")

                // Save to Firestore
                docRef.set(scoreData)
                    .addOnSuccessListener {
                        Log.d(TAG, "  ✓ Successfully synced to Firestore")
                        Log.d(TAG, "    Document: /users/$userId/quiz_scores/$moduleId")
                        Log.d(TAG, "    Data: $scoreData")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "  ✗ Failed to sync to Firestore", e)
                        Log.e(TAG, "    Error: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "  ✗ Failed to get attempts count", e)
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
     * Data class for quiz score information
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