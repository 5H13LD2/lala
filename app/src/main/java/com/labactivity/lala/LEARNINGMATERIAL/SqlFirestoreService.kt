package com.labactivity.lala.LEARNINGMATERIAL

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SqlFirestoreService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getModules(): List<Module> {
        return try {
            val modulesSnapshot = firestore.collection("courses")
                .document("sql")
                .collection("modules")
                .get()
                .await()

            modulesSnapshot.documents.mapNotNull { moduleDoc ->
                val moduleId = moduleDoc.id
                val moduleData = moduleDoc.data ?: return@mapNotNull null

                // Get lessons for this module
                val lessonsSnapshot = moduleDoc.reference.collection("lessons")
                    .get()
                    .await()

                val lessons = lessonsSnapshot.documents.mapNotNull { lessonDoc ->
                    val lessonData = lessonDoc.data ?: return@mapNotNull null
                    Lesson(
                        id = lessonDoc.id,
                        moduleId = moduleId,  // Add the moduleId here
                        number = lessonData["number"] as? String ?: "",
                        title = lessonData["title"] as? String ?: "",
                        explanation = lessonData["explanation"] as? String ?: "",
                        codeExample = lessonData["codeExample"] as? String ?: "",
                        videoUrl = lessonData["videoUrl"] as? String ?: "",
                        isExpanded = false
                    )
                }

                Module(
                    id = moduleId,  // Pass the moduleId here
                    title = moduleData["title"] as? String ?: "",
                    description = moduleData["description"] as? String ?: "",
                    lessons = lessons,
                    isExpanded = false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getQuizzes(moduleId: String): List<Quiz> {
        return try {
            val quizzesSnapshot = firestore.collection("courses")
                .document("sql")
                .collection("modules")
                .document(moduleId)
                .collection("quizzes")
                .get()
                .await()

            quizzesSnapshot.documents.mapNotNull { quizDoc ->
                val quizData = quizDoc.data ?: return@mapNotNull null
                Quiz(
                    id = quizDoc.id,
                    question = quizData["question"] as? String ?: "",
                    options = (quizData["options"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    correctAnswer = quizData["correctAnswer"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun submitQuizAttempt(moduleId: String, score: Int, totalQuestions: Int) {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users")
                .document(userId)
                .collection("quizAttempts")
                .document("sql_${moduleId}")
                .set(mapOf(
                    "score" to score,
                    "totalQuestions" to totalQuestions,
                    "timestamp" to System.currentTimeMillis()
                ))
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getCompletedLessons(): Set<String> {
        val userId = auth.currentUser?.uid ?: return emptySet()
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            (userDoc.get("completedSqlLessons") as? List<*>)
                ?.filterIsInstance<String>()
                ?.toSet()
                ?: emptySet()
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }

    suspend fun updateCompletedLesson(lessonId: String, completed: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        try {
            val userDoc = firestore.collection("users").document(userId)
            val completedLessons = getCompletedLessons().toMutableSet()

            if (completed) {
                completedLessons.add(lessonId)
            } else {
                completedLessons.remove(lessonId)
            }

            userDoc.update("completedSqlLessons", completedLessons.toList()).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 