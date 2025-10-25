package com.labactivity.lala.LEARNINGMATERIAL

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Manages module progress synchronization between local storage and Firebase
 */
class ModuleProgressManager(private val context: Context) {

    private val TAG = "ModuleProgressManager"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val prefs = context.getSharedPreferences("course_prefs", Context.MODE_PRIVATE)

    /**
     * Save completed lesson to both local storage and Firebase
     */
    fun saveCompletedLesson(courseId: String, moduleId: String, lessonId: String, callback: ((Boolean) -> Unit)? = null) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "No authenticated user, saving only to local storage")
            saveToLocalStorage(courseId, lessonId)
            callback?.invoke(false)
            return
        }

        Log.d(TAG, "Saving completed lesson: $lessonId for module: $moduleId")

        // Save to local storage first
        saveToLocalStorage(courseId, lessonId)

        // Save to Firebase
        val userProgressRef = firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .collection("modules")
            .document(moduleId)

        userProgressRef.update(
            "completed_lessons", FieldValue.arrayUnion(lessonId),
            "last_updated", FieldValue.serverTimestamp()
        ).addOnSuccessListener {
            Log.d(TAG, "Successfully saved lesson $lessonId to Firebase")
            updateModuleProgress(courseId, moduleId)
            callback?.invoke(true)
        }.addOnFailureListener { e ->
            // If document doesn't exist, create it
            if (e.message?.contains("NOT_FOUND") == true) {
                createModuleProgressDocument(userId, courseId, moduleId, lessonId, callback)
            } else {
                Log.e(TAG, "Error saving lesson to Firebase", e)
                callback?.invoke(false)
            }
        }
    }

    /**
     * Remove completed lesson from both local storage and Firebase
     */
    fun removeCompletedLesson(courseId: String, moduleId: String, lessonId: String, callback: ((Boolean) -> Unit)? = null) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "No authenticated user, removing only from local storage")
            removeFromLocalStorage(courseId, lessonId)
            callback?.invoke(false)
            return
        }

        Log.d(TAG, "Removing completed lesson: $lessonId from module: $moduleId")

        // Remove from local storage first
        removeFromLocalStorage(courseId, lessonId)

        // Remove from Firebase
        val userProgressRef = firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .collection("modules")
            .document(moduleId)

        userProgressRef.update(
            "completed_lessons", FieldValue.arrayRemove(lessonId),
            "last_updated", FieldValue.serverTimestamp()
        ).addOnSuccessListener {
            Log.d(TAG, "Successfully removed lesson $lessonId from Firebase")
            updateModuleProgress(courseId, moduleId)
            callback?.invoke(true)
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error removing lesson from Firebase", e)
            callback?.invoke(false)
        }
    }

    /**
     * Load completed lessons from Firebase and sync with local storage
     */
    fun loadCompletedLessons(courseId: String, callback: (Set<String>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "No authenticated user, loading from local storage only")
            callback(loadFromLocalStorage(courseId))
            return
        }

        Log.d(TAG, "Loading completed lessons for course: $courseId from Firebase")

        firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .collection("modules")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val allCompletedLessons = mutableSetOf<String>()

                querySnapshot.documents.forEach { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val completedLessons = doc.get("completed_lessons") as? List<String> ?: emptyList()
                    allCompletedLessons.addAll(completedLessons)
                }

                Log.d(TAG, "Loaded ${allCompletedLessons.size} completed lessons from Firebase")

                // Sync with local storage
                syncToLocalStorage(courseId, allCompletedLessons)
                callback(allCompletedLessons)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading lessons from Firebase, falling back to local storage", e)
                callback(loadFromLocalStorage(courseId))
            }
    }

    /**
     * Update module progress percentage in Firebase
     */
    private fun updateModuleProgress(courseId: String, moduleId: String) {
        val userId = auth.currentUser?.uid ?: return

        val moduleRef = firestore.collection("courses")
            .document(courseId)
            .collection("modules")
            .document(moduleId)

        val userProgressRef = firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .collection("modules")
            .document(moduleId)

        // Get total lessons count
        moduleRef.collection("lessons").get()
            .addOnSuccessListener { lessonsSnapshot ->
                val totalLessons = lessonsSnapshot.size()

                // Get completed lessons count
                userProgressRef.get().addOnSuccessListener { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val completedLessons = doc.get("completed_lessons") as? List<String> ?: emptyList()
                    val completedCount = completedLessons.size

                    val progress = if (totalLessons > 0) {
                        (completedCount * 100) / totalLessons
                    } else {
                        0
                    }

                    Log.d(TAG, "Module $moduleId progress: $progress% ($completedCount/$totalLessons)")

                    // Update progress in Firebase
                    userProgressRef.update(
                        "progress", progress,
                        "total_lessons", totalLessons,
                        "completed_count", completedCount
                    ).addOnSuccessListener {
                        Log.d(TAG, "Updated module progress in Firebase")
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error updating module progress", e)
                    }
                }
            }
    }

    /**
     * Create initial module progress document in Firebase
     */
    private fun createModuleProgressDocument(
        userId: String,
        courseId: String,
        moduleId: String,
        lessonId: String,
        callback: ((Boolean) -> Unit)?
    ) {
        val progressData = hashMapOf(
            "module_id" to moduleId,
            "course_id" to courseId,
            "completed_lessons" to listOf(lessonId),
            "progress" to 0,
            "total_lessons" to 0,
            "completed_count" to 1,
            "last_updated" to FieldValue.serverTimestamp()
        )

        firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .collection("modules")
            .document(moduleId)
            .set(progressData)
            .addOnSuccessListener {
                Log.d(TAG, "Created new module progress document")
                updateModuleProgress(courseId, moduleId)
                callback?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error creating module progress document", e)
                callback?.invoke(false)
            }
    }

    // Local storage helper methods
    private fun saveToLocalStorage(courseId: String, lessonId: String) {
        val completedLessons = loadFromLocalStorage(courseId).toMutableSet()
        completedLessons.add(lessonId)
        prefs.edit().putStringSet(courseId, completedLessons).apply()
        Log.d(TAG, "Saved to local storage: $lessonId")
    }

    private fun removeFromLocalStorage(courseId: String, lessonId: String) {
        val completedLessons = loadFromLocalStorage(courseId).toMutableSet()
        completedLessons.remove(lessonId)
        prefs.edit().putStringSet(courseId, completedLessons).apply()
        Log.d(TAG, "Removed from local storage: $lessonId")
    }

    private fun loadFromLocalStorage(courseId: String): Set<String> {
        return prefs.getStringSet(courseId, emptySet()) ?: emptySet()
    }

    private fun syncToLocalStorage(courseId: String, lessons: Set<String>) {
        prefs.edit().putStringSet(courseId, lessons).apply()
        Log.d(TAG, "Synced ${lessons.size} lessons to local storage")
    }
}
