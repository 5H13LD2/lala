package com.labactivity.lala.PYTHONASSESMENT

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.labactivity.lala.GAMIFICATION.XPManager
import com.labactivity.lala.LEADERBOARDPAGE.Achievement

class TechnicalAssessmentService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val xpManager = XPManager()

    companion object {
        private const val TAG = "TechnicalAssessmentService"
        private const val COLLECTION_TECHNICAL_ASSESSMENT = "technical_assesment"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_USER_PROGRESS = "user_progress"
        private const val SUB_COLLECTION_TECHNICAL_ASSESSMENT_PROGRESS = "technical_assessment_progress"
        private const val FIELD_COURSE_TAKEN = "courseTaken"
        private const val FIELD_COURSE_ID = "courseId"
    }

    /** Fetch challenges based on user's enrolled courses **/
    suspend fun getChallengesForUser(): List<Challenge> {
        return try {
            val userId = auth.currentUser?.uid ?: run {
                Log.w(TAG, "⚠️ User not authenticated")
                return emptyList()
            }

            val enrolledCourseIds = getUserEnrolledCourseIds(userId)
            Log.d(TAG, "✅ User enrolled in courses: $enrolledCourseIds")

            if (enrolledCourseIds.isEmpty()) {
                Log.d(TAG, "⚠️ No enrolled courses found")
                return emptyList()
            }

            val allChallenges = fetchChallengesByCourseIds(enrolledCourseIds)
            Log.d(TAG, "✅ Found ${allChallenges.size} total challenges for enrolled courses")

            // Filter to only include Java and Python challenges
            val challenges = allChallenges.filter { challenge ->
                challenge.compilerType.lowercase() in listOf("java", "python")
            }
            Log.d(TAG, "✅ Filtered to ${challenges.size} Java/Python challenges (excluded ${allChallenges.size - challenges.size} other types)")

            // Calculate which challenges should be unlocked
            val challengesWithUnlockStatus = applyUnlockLogic(challenges)
            Log.d(TAG, "✅ Applied unlock logic to ${challengesWithUnlockStatus.size} challenges")

            challengesWithUnlockStatus
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching challenges for user", e)
            emptyList()
        }
    }

    /** Fetch user's enrolled course IDs **/
    private suspend fun getUserEnrolledCourseIds(userId: String): List<String> {
        return try {
            val document = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            val courseTaken = document.get(FIELD_COURSE_TAKEN) as? List<Map<String, Any>> ?: emptyList()

            val ids = courseTaken.mapNotNull { it[FIELD_COURSE_ID] as? String }
            Log.d(TAG, "📘 Found ${ids.size} enrolled courses")
            ids
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting enrolled course IDs", e)
            emptyList()
        }
    }

    /** Fetch challenges matching any of the provided course IDs **/
    private suspend fun fetchChallengesByCourseIds(courseIds: List<String>): List<Challenge> {
        return try {
            val allChallenges = mutableListOf<Challenge>()
            val batches = courseIds.chunked(10)

            for (batch in batches) {
                val snapshot = firestore.collection(COLLECTION_TECHNICAL_ASSESSMENT)
                    .whereIn(FIELD_COURSE_ID, batch)
                    .get()
                    .await()

                if (snapshot.isEmpty) {
                    Log.w(TAG, "⚠️ No challenges found for batch: $batch")
                    continue
                }

                snapshot.documents.forEach { doc ->
                    try {
                        val title = doc.getString("title")
                        Log.d(TAG, "🧩 Challenge found in Firestore: ${doc.id}")
                        Log.d(TAG, "📝 Title field: '$title'")

                        // Safely get createdAt timestamp
                        val createdAt = try {
                            doc.getTimestamp("createdAt")
                        } catch (e: Exception) {
                            Log.w(TAG, "⚠️ createdAt field is not a Timestamp for ${doc.id}, using null")
                            null
                        }

                        val challenge = Challenge(
                            id = doc.id,
                            title = title ?: "Untitled Challenge",
                            difficulty = doc.getString("difficulty") ?: "Unknown",
                            courseId = doc.getString("courseId") ?: "",
                            compilerType = doc.getString("compilerType") ?: "python", // Default to python if not specified
                            brokenCode = doc.getString("brokenCode") ?: "",
                            correctOutput = doc.getString("correctOutput") ?: "",
                            hint = doc.getString("hint") ?: "",
                            category = doc.getString("category") ?: "",
                            status = doc.getString("status") ?: "available",
                            createdAt = createdAt
                        )
                        Log.d(TAG, "✅ Created challenge: ${challenge.title}")
                        allChallenges.add(challenge)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error parsing challenge ${doc.id}: ${e.message}", e)
                        // Skip this challenge and continue with others
                    }
                }
            }

            allChallenges.sortedWith(
                compareBy<Challenge> { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
                    .thenBy { it.title }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching challenges by course IDs", e)
            emptyList()
        }
    }

    /**
     * Apply unlock logic to challenges based on difficulty progression
     * Rule: A challenge is unlocked if all easier challenges are completed
     * Difficulty order: Easy -> Medium -> Hard
     */
    private suspend fun applyUnlockLogic(challenges: List<Challenge>): List<Challenge> {
        return try {
            // Get all user progress
            val allProgress = getAllUserProgress()
            val completedChallengeIds = allProgress.filter { it.passed }.map { it.challengeId }.toSet()

            Log.d(TAG, "🔓 User has completed ${completedChallengeIds.size} challenges")

            // Group challenges by difficulty
            val easyChallenges = challenges.filter { it.difficulty.equals("Easy", ignoreCase = true) }
            val mediumChallenges = challenges.filter { it.difficulty.equals("Medium", ignoreCase = true) }
            val hardChallenges = challenges.filter { it.difficulty.equals("Hard", ignoreCase = true) }

            // Check if all challenges of a difficulty are completed
            val allEasyCompleted = easyChallenges.all { it.id in completedChallengeIds }
            val allMediumCompleted = mediumChallenges.all { it.id in completedChallengeIds }

            Log.d(TAG, "🔓 Easy: ${easyChallenges.size} total, all completed: $allEasyCompleted")
            Log.d(TAG, "🔓 Medium: ${mediumChallenges.size} total, all completed: $allMediumCompleted")
            Log.d(TAG, "🔓 Hard: ${hardChallenges.size} total")

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
                    Log.d(TAG, "🔒 Challenge locked: ${challenge.title} (${challenge.difficulty})")
                }

                challenge.copy(isUnlocked = isUnlocked)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error applying unlock logic", e)
            // On error, return all challenges as unlocked (fail-safe)
            challenges.map { it.copy(isUnlocked = true) }
        }
    }

    /** Update challenge status (e.g., when completed) **/
    suspend fun updateChallengeStatus(challengeId: String, status: String) {
        try {
            firestore.collection(COLLECTION_TECHNICAL_ASSESSMENT)
                .document(challengeId)
                .update("status", status)
                .await()

            Log.d(TAG, "✅ Updated challenge [$challengeId] status → $status")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating challenge status", e)
        }
    }

    /**
     * Save user progress for a technical assessment challenge
     * Stores in user_progress/{userId}/technical_assessment_progress/{challengeId}
     * @return Pair of (success: Boolean, unlockedAchievements: List<Achievement>)
     */
    suspend fun saveUserProgress(
        challengeId: String,
        challengeTitle: String,
        passed: Boolean,
        score: Int = 100,
        timeTaken: Long = 0,
        userCode: String = ""
    ): Pair<Boolean, List<Achievement>> {
        return try {
            val userId = auth.currentUser?.uid ?: run {
                Log.w(TAG, "⚠️ User not authenticated")
                return Pair(false, emptyList())
            }

            // Get existing progress or create new
            val existingProgress = getUserProgress(challengeId)

            val progressData = TechnicalAssessmentProgress(
                challengeId = challengeId,
                challengeTitle = challengeTitle,
                status = if (passed) "completed" else "in_progress",
                attempts = (existingProgress?.attempts ?: 0) + 1,
                bestScore = maxOf(score, existingProgress?.bestScore ?: 0),
                lastAttemptDate = com.google.firebase.Timestamp.now(),
                timeTaken = timeTaken,
                userCode = userCode,
                passed = passed,
                updatedAt = com.google.firebase.Timestamp.now()
            )

            firestore.collection(COLLECTION_USER_PROGRESS)
                .document(userId)
                .collection(SUB_COLLECTION_TECHNICAL_ASSESSMENT_PROGRESS)
                .document(challengeId)
                .set(progressData)
                .await()

            Log.d(TAG, "✅ Saved progress for challenge [$challengeId] - Passed: $passed")

            // Award XP if the challenge was passed
            var unlockedAchievements = emptyList<Achievement>()
            if (passed) {
                val xpResult = xpManager.awardTechnicalAssessmentXP(
                    challengeTitle = challengeTitle,
                    passed = true,
                    score = score
                )
                unlockedAchievements = xpResult.unlockedAchievements
            }

            Pair(true, unlockedAchievements)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving user progress", e)
            Pair(false, emptyList())
        }
    }

    /**
     * Get user progress for a specific challenge
     */
    suspend fun getUserProgress(challengeId: String): TechnicalAssessmentProgress? {
        return try {
            val userId = auth.currentUser?.uid ?: return null

            Log.d(TAG, "Fetching progress for challenge $challengeId and user $userId")

            val document = firestore.collection(COLLECTION_USER_PROGRESS)
                .document(userId)
                .collection(SUB_COLLECTION_TECHNICAL_ASSESSMENT_PROGRESS)
                .document(challengeId)
                .get()
                .await()

            if (!document.exists()) {
                Log.d(TAG, "Progress not found for challenge $challengeId")
                return null
            }

            val progress = try {
                document.toObject(TechnicalAssessmentProgress::class.java)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error parsing progress for $challengeId: ${e.message}")
                null
            }

            Log.d(TAG, "Progress found: ${progress != null}")
            progress

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting user progress", e)
            null
        }
    }

    /**
     * Get all user progress for technical assessments
     */
    suspend fun getAllUserProgress(): List<TechnicalAssessmentProgress> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()

            Log.d(TAG, "Fetching all progress for user $userId")

            val snapshot = firestore.collection(COLLECTION_USER_PROGRESS)
                .document(userId)
                .collection(SUB_COLLECTION_TECHNICAL_ASSESSMENT_PROGRESS)
                .get()
                .await()

            // Manually parse documents to handle mixed data types
            val progressList = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(TechnicalAssessmentProgress::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Skipping document ${doc.id} due to parsing error: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Found ${progressList.size} progress records")
            progressList

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting all user progress", e)
            emptyList()
        }
    }

    /** Re-fetch all challenges **/
    suspend fun refreshChallenges(): List<Challenge> = getChallengesForUser()
}
