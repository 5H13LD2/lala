package com.labactivity.lala.PYTHONASSESMENT

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TechnicalAssessmentService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "TechnicalAssessmentService"
        private const val COLLECTION_TECHNICAL_ASSESSMENT = "technical_assesment"
        private const val COLLECTION_USERS = "users"
        private const val FIELD_COURSE_TAKEN = "courseTaken"
        private const val FIELD_COURSE_ID = "courseId"
    }

    /** Fetch challenges based on user’s enrolled courses **/
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

            val challenges = fetchChallengesByCourseIds(enrolledCourseIds)
            Log.d(TAG, "✅ Found ${challenges.size} challenges for enrolled courses")

            challenges
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
                    val title = doc.getString("title")
                    Log.d(TAG, "🧩 Challenge found in Firestore: ${doc.id}")
                    Log.d(TAG, "📝 Title field: '$title'")
                    Log.d(TAG, "📝 All fields: ${doc.data}")

                    val challenge = Challenge(
                        id = doc.id,
                        title = title ?: "Untitled Challenge",
                        difficulty = doc.getString("difficulty") ?: "Unknown",
                        courseId = doc.getString("courseId") ?: "",
                        brokenCode = doc.getString("brokenCode") ?: "",
                        correctOutput = doc.getString("correctOutput") ?: "",
                        hint = doc.getString("hint") ?: "",
                        category = doc.getString("category") ?: "",
                        status = doc.getString("status") ?: "available",
                        createdAt = doc.getTimestamp("createdAt")?.toDate()?.toString() ?: ""
                    )
                    Log.d(TAG, "✅ Created challenge: ${challenge.title}")
                    allChallenges.add(challenge)
                }
            }

            allChallenges.sortedWith(compareBy<Challenge> { it.createdAt.ifEmpty { "9999" } }.thenBy { it.title })
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching challenges by course IDs", e)
            emptyList()
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

    /** Re-fetch all challenges **/
    suspend fun refreshChallenges(): List<Challenge> = getChallengesForUser()
}
