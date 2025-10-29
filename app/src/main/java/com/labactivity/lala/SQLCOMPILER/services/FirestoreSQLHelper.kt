package com.labactivity.lala.SQLCOMPILER.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.labactivity.lala.SQLCOMPILER.models.*
import com.labactivity.lala.SQLCOMPILER.utils.FirestoreDataConverter
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Firestore helper class for managing SQL challenges
 * Provides coroutine-based methods for CRUD operations on SQL challenges
 */
class FirestoreSQLHelper {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "FirestoreSQLHelper"
        private const val COLLECTION_SQL_CHALLENGES = "technical_assesment"
        private const val COLLECTION_USERS = "users"
        private const val SUB_COLLECTION_SQL_PROGRESS = "sql_progress"

        // Singleton instance
        @Volatile
        private var instance: FirestoreSQLHelper? = null

        fun getInstance(): FirestoreSQLHelper {
            return instance ?: synchronized(this) {
                instance ?: FirestoreSQLHelper().also { instance = it }
            }
        }
    }

    // ==================== Challenge Fetching Methods ====================

    /**
     * Fetches all active SQL challenges from Firestore
     * @return List of SQLChallenge objects sorted by order
     */
    suspend fun getAllChallenges(): List<SQLChallenge> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching all SQL challenges from Firestore")

            val snapshot = firestore.collection(COLLECTION_SQL_CHALLENGES)
                .whereEqualTo("status", "active")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            val challenges = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(SQLChallenge::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing challenge document ${doc.id}: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Successfully fetched ${challenges.size} SQL challenges")
            challenges

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching SQL challenges: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetches challenges filtered by course ID
     * @param courseId The course ID to filter by
     * @return List of SQLChallenge objects for the specified course
     */
    suspend fun getChallengesByCourse(courseId: String): List<SQLChallenge> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching SQL challenges for course: $courseId")

                val snapshot = firestore.collection(COLLECTION_SQL_CHALLENGES)
                    .whereEqualTo("status", "active")
                    .whereEqualTo("courseId", courseId)
                    .orderBy("order", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val challenges = snapshot.toObjects(SQLChallenge::class.java)
                Log.d(TAG, "Found ${challenges.size} challenges for course $courseId")
                challenges

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching challenges by course: ${e.message}", e)
                emptyList()
            }
        }

    /**
     * Fetches challenges filtered by difficulty
     * @param difficulty The difficulty level ("Easy", "Medium", "Hard")
     * @return List of SQLChallenge objects with the specified difficulty
     */
    suspend fun getChallengesByDifficulty(difficulty: String): List<SQLChallenge> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching SQL challenges with difficulty: $difficulty")

                val snapshot = firestore.collection(COLLECTION_SQL_CHALLENGES)
                    .whereEqualTo("status", "active")
                    .whereEqualTo("difficulty", difficulty)
                    .orderBy("order", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val challenges = snapshot.toObjects(SQLChallenge::class.java)
                Log.d(TAG, "Found ${challenges.size} challenges with difficulty $difficulty")
                challenges

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching challenges by difficulty: ${e.message}", e)
                emptyList()
            }
        }

    /**
     * Fetches challenges filtered by topic
     * @param topic The SQL topic (e.g., "SELECT Basics", "JOINS")
     * @return List of SQLChallenge objects for the specified topic
     */
    suspend fun getChallengesByTopic(topic: String): List<SQLChallenge> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching SQL challenges for topic: $topic")

                val snapshot = firestore.collection(COLLECTION_SQL_CHALLENGES)
                    .whereEqualTo("status", "active")
                    .whereEqualTo("topic", topic)
                    .orderBy("order", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val challenges = snapshot.toObjects(SQLChallenge::class.java)
                Log.d(TAG, "Found ${challenges.size} challenges for topic $topic")
                challenges

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching challenges by topic: ${e.message}", e)
                emptyList()
            }
        }

    /**
     * Fetches a single challenge by its document ID
     * @param challengeId The Firestore document ID of the challenge
     * @return SQLChallenge object or null if not found
     */
    suspend fun getChallengeById(challengeId: String): SQLChallenge? =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching SQL challenge with ID: $challengeId")

                val document = firestore.collection(COLLECTION_SQL_CHALLENGES)
                    .document(challengeId)
                    .get()
                    .await()

                // Use FirestoreDataConverter to handle complex nested structures
                val challenge = FirestoreDataConverter.toSQLChallenge(document)

                if (challenge != null && FirestoreDataConverter.isValid(challenge)) {
                    Log.d(TAG, "Successfully fetched challenge: ${challenge.title}")
                    Log.d(TAG, "Sample table has ${challenge.sampleTable.rows.size} rows")
                } else {
                    Log.w(TAG, "Challenge with ID $challengeId not found or invalid")
                }
                challenge

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching challenge by ID: ${e.message}", e)
                null
            }
        }

    /**
     * Fetches challenges with advanced filtering
     * @param filter SQLChallengeFilter object with multiple filter criteria
     * @return List of SQLChallenge objects matching the filter
     */
    suspend fun getChallengesWithFilter(filter: SQLChallengeFilter): List<SQLChallenge> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching SQL challenges with filter: $filter")

                var query: Query = firestore.collection(COLLECTION_SQL_CHALLENGES)
                    .whereEqualTo("status", filter.status)

                // Apply courseId filter
                filter.courseId?.let {
                    query = query.whereEqualTo("courseId", it)
                }

                // Apply difficulty filter
                filter.difficulty?.let {
                    query = query.whereEqualTo("difficulty", it)
                }

                // Apply topic filter
                filter.topic?.let {
                    query = query.whereEqualTo("topic", it)
                }

                query = query.orderBy("order", Query.Direction.ASCENDING)

                val snapshot = query.get().await()
                var challenges = snapshot.toObjects(SQLChallenge::class.java)

                // Apply search query filter (client-side)
                filter.searchQuery?.let { searchTerm ->
                    if (searchTerm.isNotBlank()) {
                        challenges = challenges.filter { challenge ->
                            challenge.title.contains(searchTerm, ignoreCase = true) ||
                                    challenge.description.contains(searchTerm, ignoreCase = true) ||
                                    challenge.tags.any { tag ->
                                        tag.contains(searchTerm, ignoreCase = true)
                                    }
                        }
                    }
                }

                Log.d(TAG, "Found ${challenges.size} challenges matching filter")
                challenges

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching challenges with filter: ${e.message}", e)
                emptyList()
            }
        }

    // ==================== User Progress Methods ====================

    /**
     * Fetches the user's progress for a specific challenge
     * @param challengeId The challenge ID
     * @return SQLChallengeProgress object or null if not found
     */
    suspend fun getUserProgress(challengeId: String): SQLChallengeProgress? =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext null
                Log.d(TAG, "Fetching progress for challenge $challengeId and user $userId")

                val document = firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(SUB_COLLECTION_SQL_PROGRESS)
                    .document(challengeId)
                    .get()
                    .await()

                val progress = document.toObject(SQLChallengeProgress::class.java)
                Log.d(TAG, "Progress found: ${progress != null}")
                progress

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user progress: ${e.message}", e)
                null
            }
        }

    /**
     * Saves or updates the user's progress for a challenge
     * @param challengeId The challenge ID
     * @param progress The progress data to save
     */
    suspend fun saveUserProgress(
        challengeId: String,
        progress: SQLChallengeProgress
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext false
            Log.d(TAG, "Saving progress for challenge $challengeId and user $userId")

            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUB_COLLECTION_SQL_PROGRESS)
                .document(challengeId)
                .set(progress)
                .await()

            Log.d(TAG, "Progress saved successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Error saving user progress: ${e.message}", e)
            false
        }
    }

    /**
     * Updates user progress after a challenge attempt
     * @param challengeId The challenge ID
     * @param passed Whether the user passed the challenge
     * @param score The score achieved (0-100)
     * @param userQuery The SQL query the user submitted
     * @param timeTaken Time taken in seconds
     */
    suspend fun updateProgressAfterAttempt(
        challengeId: String,
        passed: Boolean,
        score: Int,
        userQuery: String,
        timeTaken: Long
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext false

            // Get existing progress or create new
            val existingProgress = getUserProgress(challengeId)

            val newProgress = SQLChallengeProgress(
                challengeId = challengeId,
                status = if (passed) "completed" else "in_progress",
                attempts = (existingProgress?.attempts ?: 0) + 1,
                bestScore = maxOf(score, existingProgress?.bestScore ?: 0),
                lastAttemptDate = getCurrentTimestamp(),
                timeTaken = timeTaken,
                userQuery = userQuery,
                passed = passed
            )

            saveUserProgress(challengeId, newProgress)

        } catch (e: Exception) {
            Log.e(TAG, "Error updating progress after attempt: ${e.message}", e)
            false
        }
    }

    /**
     * Fetches all user progress records
     * @return List of SQLChallengeProgress objects
     */
    suspend fun getAllUserProgress(): List<SQLChallengeProgress> =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext emptyList()
                Log.d(TAG, "Fetching all progress for user $userId")

                val snapshot = firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(SUB_COLLECTION_SQL_PROGRESS)
                    .get()
                    .await()

                val progressList = snapshot.toObjects(SQLChallengeProgress::class.java)
                Log.d(TAG, "Found ${progressList.size} progress records")
                progressList

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching all user progress: ${e.message}", e)
                emptyList()
            }
        }

    /**
     * Calculates statistics for the current user's SQL challenge progress
     * @return SQLChallengeStats object with aggregated statistics
     */
    suspend fun getUserStats(): SQLChallengeStats = withContext(Dispatchers.IO) {
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

            SQLChallengeStats(
                totalChallenges = allChallenges.size,
                completedChallenges = completedCount,
                totalAttempts = totalAttempts,
                averageScore = averageScore,
                totalTimeTaken = totalTime
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating user stats: ${e.message}", e)
            SQLChallengeStats()
        }
    }

    // ==================== Admin Methods ====================

    /**
     * Adds a new SQL challenge to Firestore (Admin only)
     * @param challenge The SQLChallenge object to add
     * @return The document ID of the created challenge, or null if failed
     */
    suspend fun addChallenge(challenge: SQLChallenge): String? = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext null
            Log.d(TAG, "Adding new SQL challenge: ${challenge.title}")

            // Check if user is admin (this requires custom claims to be set)
            val user = auth.currentUser
            val tokenResult = user?.getIdToken(false)?.await()
            val isAdmin = tokenResult?.claims?.get("admin") as? Boolean ?: false

            if (!isAdmin) {
                Log.w(TAG, "User $userId is not an admin. Cannot add challenge.")
                return@withContext null
            }

            // Add timestamp if not provided
            val timestamp = getCurrentTimestamp()
            val challengeWithTimestamp = challenge.copy(
                createdAt = if (challenge.createdAt.isEmpty()) timestamp else challenge.createdAt,
                updatedAt = timestamp
            )

            val docRef = firestore.collection(COLLECTION_SQL_CHALLENGES)
                .add(challengeWithTimestamp)
                .await()

            Log.d(TAG, "Challenge added successfully with ID: ${docRef.id}")
            docRef.id

        } catch (e: Exception) {
            Log.e(TAG, "Error adding challenge: ${e.message}", e)
            null
        }
    }

    /**
     * Updates an existing SQL challenge (Admin only)
     * @param challengeId The document ID of the challenge to update
     * @param challenge The updated SQLChallenge object
     * @return True if successful, false otherwise
     */
    suspend fun updateChallenge(challengeId: String, challenge: SQLChallenge): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext false
                Log.d(TAG, "Updating SQL challenge: $challengeId")

                // Check if user is admin
                val user = auth.currentUser
                val tokenResult = user?.getIdToken(false)?.await()
                val isAdmin = tokenResult?.claims?.get("admin") as? Boolean ?: false

                if (!isAdmin) {
                    Log.w(TAG, "User $userId is not an admin. Cannot update challenge.")
                    return@withContext false
                }

                // Update timestamp
                val challengeWithTimestamp = challenge.copy(
                    updatedAt = getCurrentTimestamp()
                )

                firestore.collection(COLLECTION_SQL_CHALLENGES)
                    .document(challengeId)
                    .set(challengeWithTimestamp)
                    .await()

                Log.d(TAG, "Challenge updated successfully")
                true

            } catch (e: Exception) {
                Log.e(TAG, "Error updating challenge: ${e.message}", e)
                false
            }
        }

    /**
     * Deletes a SQL challenge (Admin only)
     * Actually sets status to "archived" instead of deleting
     * @param challengeId The document ID of the challenge to delete
     * @return True if successful, false otherwise
     */
    suspend fun deleteChallenge(challengeId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext false
            Log.d(TAG, "Deleting (archiving) SQL challenge: $challengeId")

            // Check if user is admin
            val user = auth.currentUser
            val tokenResult = user?.getIdToken(false)?.await()
            val isAdmin = tokenResult?.claims?.get("admin") as? Boolean ?: false

            if (!isAdmin) {
                Log.w(TAG, "User $userId is not an admin. Cannot delete challenge.")
                return@withContext false
            }

            // Set status to archived instead of actually deleting
            firestore.collection(COLLECTION_SQL_CHALLENGES)
                .document(challengeId)
                .update(
                    mapOf(
                        "status" to "archived",
                        "updatedAt" to getCurrentTimestamp()
                    )
                )
                .await()

            Log.d(TAG, "Challenge archived successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting challenge: ${e.message}", e)
            false
        }
    }

    /**
     * Checks if the current user is an admin
     * @return True if user has admin custom claim, false otherwise
     */
    suspend fun isUserAdmin(): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext false
            val tokenResult = user.getIdToken(false).await()
            val isAdmin = tokenResult.claims["admin"] as? Boolean ?: false
            Log.d(TAG, "User admin status: $isAdmin")
            isAdmin
        } catch (e: Exception) {
            Log.e(TAG, "Error checking admin status: ${e.message}", e)
            false
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
