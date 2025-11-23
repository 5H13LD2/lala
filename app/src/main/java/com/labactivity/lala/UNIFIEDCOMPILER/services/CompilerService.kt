package com.labactivity.lala.UNIFIEDCOMPILER.services

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.labactivity.lala.UNIFIEDCOMPILER.CompilerFactory
import com.labactivity.lala.UNIFIEDCOMPILER.CourseCompiler
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerResult
import com.labactivity.lala.UNIFIEDCOMPILER.models.CourseCompilerInfo
import kotlinx.coroutines.tasks.await

/**
 * FIREBASE INTEGRATION SERVICE
 *
 * This service connects Firebase courses with the unified compiler system.
 * It fetches course compiler information and provides easy access to compilers.
 *
 * USAGE:
 * ```
 * val service = CompilerService()
 * val compiler = service.getCompilerForCourse("python_course_123")
 * val result = compiler.compile(code)
 * ```
 */
class CompilerService {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Get compiler for a specific course by courseId
     *
     * @param courseId The course ID from Firebase
     * @return CourseCompiler instance
     * @throws Exception if course not found or compiler not supported
     */
    suspend fun getCompilerForCourse(courseId: String): CourseCompiler {
        // Fetch course info from Firebase
        val courseInfo = getCourseCompilerInfo(courseId)

        // Get the appropriate compiler from factory
        return CompilerFactory.getCompiler(courseInfo.compilerType)
    }

    /**
     * Execute code for a specific course
     *
     * @param courseId The course ID from Firebase
     * @param code The code to execute
     * @param config Optional compiler configuration
     * @return CompilerResult
     */
    suspend fun executeCodeForCourse(
        courseId: String,
        code: String,
        config: CompilerConfig = CompilerConfig()
    ): CompilerResult {
        val compiler = getCompilerForCourse(courseId)
        return compiler.compile(code, config)
    }

    /**
     * Fetch course compiler information from Firebase
     *
     * Expected Firebase document structure:
     * ```
     * courses/{courseId}:
     *   - courseId: "python_beginner"
     *   - courseName: "Python Beginner Course"
     *   - compilerType: "python"
     *   - version: "3.x"
     *   - supportedFeatures: ["basic", "functions", "loops"]
     * ```
     */
    suspend fun getCourseCompilerInfo(courseId: String): CourseCompilerInfo {
        try {
            val document = firestore.collection("courses")
                .document(courseId)
                .get()
                .await()

            if (!document.exists()) {
                throw Exception("Course not found: $courseId")
            }

            return document.toObject(CourseCompilerInfo::class.java)
                ?: throw Exception("Failed to parse course data for: $courseId")

        } catch (e: Exception) {
            throw Exception("Error fetching course compiler info: ${e.message}")
        }
    }

    /**
     * Get all available courses with their compiler types
     *
     * @return List of CourseCompilerInfo
     */
    suspend fun getAllAvailableCourses(): List<CourseCompilerInfo> {
        return try {
            val snapshot = firestore.collection("courses")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(CourseCompilerInfo::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get courses by compiler type
     *
     * @param compilerType "python", "java", "kotlin", etc.
     * @return List of courses using that compiler
     */
    suspend fun getCoursesByCompilerType(compilerType: String): List<CourseCompilerInfo> {
        return try {
            val snapshot = firestore.collection("courses")
                .whereEqualTo("compilerType", compilerType)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(CourseCompilerInfo::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Check if a compiler type is supported
     *
     * @param compilerType The compiler type to check
     * @return True if supported
     */
    fun isCompilerSupported(compilerType: String): Boolean {
        return CompilerFactory.hasCompiler(compilerType)
    }

    /**
     * Get list of all supported compiler types
     *
     * @return List of supported language IDs
     */
    fun getSupportedCompilers(): List<String> {
        return CompilerFactory.getSupportedLanguages()
    }

    /**
     * Validate course compiler configuration
     * Checks if the course's compiler type is supported
     *
     * @param courseId Course ID to validate
     * @return Pair<Boolean, String?> - (isValid, errorMessage)
     */
    suspend fun validateCourseCompiler(courseId: String): Pair<Boolean, String?> {
        return try {
            val courseInfo = getCourseCompilerInfo(courseId)

            if (!isCompilerSupported(courseInfo.compilerType)) {
                return Pair(
                    false,
                    "Compiler '${courseInfo.compilerType}' is not supported. " +
                            "Supported compilers: ${getSupportedCompilers().joinToString(", ")}"
                )
            }

            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, "Validation error: ${e.message}")
        }
    }

    /**
     * Get compiler directly by compiler type (bypassing course lookup)
     * Useful when you already know the compiler type
     *
     * @param compilerType "python", "java", "kotlin", etc.
     * @return CourseCompiler instance
     */
    fun getCompilerByType(compilerType: String): CourseCompiler {
        return CompilerFactory.getCompiler(compilerType)
    }
}

/**
 * Extension functions for easier usage
 */

/**
 * Execute code with a specific compiler type
 */
suspend fun String.executeAs(compilerType: String, config: CompilerConfig = CompilerConfig()): CompilerResult {
    val compiler = CompilerFactory.getCompiler(compilerType)
    return compiler.compile(this, config)
}

/**
 * Execute code for a course
 */
suspend fun String.executeForCourse(courseId: String, config: CompilerConfig = CompilerConfig()): CompilerResult {
    val service = CompilerService()
    return service.executeCodeForCourse(courseId, this, config)
}
