package com.labactivity.lala.homepage

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getEnrolledCourses(): List<String> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            when (val enrolledData = document.get("enrolledCourses")) {
                is String -> listOf(enrolledData)
                is List<*> -> enrolledData.filterIsInstance<String>()
                else -> emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
} 