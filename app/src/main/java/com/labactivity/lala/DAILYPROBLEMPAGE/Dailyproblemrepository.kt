package com.labactivity.lala.DAILYPROBLEMPAGE

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.labactivity.lala.GAMIFICATION.XPManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for Daily Problem operations (Client-side only)
 * Admin operations are handled via CMS
 */
class DailyProblemRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val xpManager = XPManager()

    companion object {
        private const val TAG = "DailyProblemRepo"
        private const val COLLECTION_DAILY_PROBLEMS = "daily_problem"
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_PROGRESS = "daily_problem_progress"
    }

    /**
     * Fetch active daily problems (not expired)
     */
    fun getActiveDailyProblems(): Flow<Result<List<DailyProblem>>> = callbackFlow {
        val currentTime = Timestamp.now()

        val listener = firestore.collection(COLLECTION_DAILY_PROBLEMS)
            .whereGreaterThan("expiredAt", currentTime)
            .whereEqualTo("isActive", true)
            .orderBy("expiredAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching daily problems", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val problems = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(DailyProblem::class.java)?.copy(
                            problemId = doc.id
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing problem: ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                trySend(Result.success(problems))
            }

        awaitClose { listener.remove() }
    }

    /**
     * Fetch a specific daily problem by ID
     */
    suspend fun getDailyProblemById(problemId: String): Result<DailyProblem?> {
        return try {
            val snapshot = firestore.collection(COLLECTION_DAILY_PROBLEMS)
                .document(problemId)
                .get()
                .await()

            val problem = snapshot.toObject(DailyProblem::class.java)?.copy(
                problemId = snapshot.id
            )
            Result.success(problem)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching problem by ID: $problemId", e)
            Result.failure(e)
        }
    }

    /**
     * Get user's progress for a specific daily problem
     */
    suspend fun getUserProgress(problemId: String): Result<DailyProblemProgress?> {
        val userId = auth.currentUser?.uid ?: return Result.failure(
            IllegalStateException("User not authenticated")
        )

        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_PROGRESS)
                .document(problemId)
                .get()
                .await()

            val progress = snapshot.toObject(DailyProblemProgress::class.java)
            Result.success(progress)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user progress for problem: $problemId", e)
            Result.failure(e)
        }
    }

    /**
     * Submit user's solution for a daily problem
     */
    suspend fun submitProblemSolution(
        problemId: String,
        courseId: String,
        code: String,
        status: String,
        score: Int = 0,
        executionTime: Long = 0,
        testCasesPassed: Int = 0,
        totalTestCases: Int = 0
    ): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(
            IllegalStateException("User not authenticated")
        )

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "Submitting solution for problem: $problemId")
        Log.d(TAG, "User: $userId")
        Log.d(TAG, "Status: $status")
        Log.d(TAG, "Score: $score")
        Log.d(TAG, "Test cases: $testCasesPassed/$totalTestCases")

        val progress = DailyProblemProgress(
            problemId = problemId,
            courseId = courseId,
            code = code,
            status = status,
            submittedAt = Timestamp.now(),
            score = score,
            executionTime = executionTime,
            testCasesPassed = testCasesPassed,
            totalTestCases = totalTestCases
        )

        return try {
            Log.d(TAG, "Fetching progress for problem $problemId and user $userId")

            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_PROGRESS)
                .document(problemId)
                .set(progress)
                .await()

            Log.d(TAG, "Progress saved successfully")

            // Award XP if solution passed
            val passed = status == "completed" && score >= 70
            if (passed) {
                Log.d(TAG, "XPManager               com.labactivity.lala                 D  ═══════════════════════════════════════")
                Log.d(TAG, "XPManager               com.labactivity.lala                 D  Awarding Technical Assessment XP")
                Log.d(TAG, "XPManager               com.labactivity.lala                 D    Challenge: Daily Problem $problemId")
                Log.d(TAG, "XPManager               com.labactivity.lala                 D    Score: $score")

                xpManager.awardTechnicalAssessmentXP(userId, passed)

                Log.d(TAG, "✅ Awarded XP for completing daily problem: $problemId")
            }

            Log.d(TAG, "✅ Progress saved")
            Log.d(TAG, "═══════════════════════════════════════")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error submitting solution for problem: $problemId", e)
            Log.d(TAG, "═══════════════════════════════════════")
            Result.failure(e)
        }
    }

    /**
     * Update user's progress status
     */
    suspend fun updateProblemStatus(
        problemId: String,
        status: String
    ): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(
            IllegalStateException("User not authenticated")
        )

        return try {
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_PROGRESS)
                .document(problemId)
                .update("status", status)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating problem status: $problemId", e)
            Result.failure(e)
        }
    }

    /**
     * Get all user's daily problem progress
     */
    fun getAllUserProgress(): Flow<Result<List<DailyProblemProgress>>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(Result.failure(IllegalStateException("User not authenticated")))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_PROGRESS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching user progress", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val progressList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DailyProblemProgress::class.java)
                } ?: emptyList()

                trySend(Result.success(progressList))
            }

        awaitClose { listener.remove() }
    }
}