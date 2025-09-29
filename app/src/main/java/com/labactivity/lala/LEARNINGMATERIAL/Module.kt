package com.labactivity.lala.LEARNINGMATERIAL

/**
 * Represents a learning module with lessons
 * Module ID format: "type_module_number" (e.g., "sql_module_1")
 */
data class Module(
    val id: String,
    val title: String,
    val description: String,
    val lessons: MutableList<Lesson> = mutableListOf(),
    var isExpanded: Boolean = false
) {
    // Get the course type (e.g., "sql" from "sql_module_1")
    val courseType: String
        get() = id.split("_").firstOrNull()?.trim() ?: ""

    // Get the module number (e.g., "1" from "sql_module_1")
    val moduleNumber: String
        get() = id.split("_").lastOrNull()?.trim() ?: ""

    // Get the quiz ID for this module (e.g., "sql_quiz")
    val quizId: String
        get() = "${courseType}_quiz"

    fun getProgressPercentage(completedLessonIds: Set<String>): Int {
        if (lessons.isEmpty()) return 0
        val completedCount = lessons.count { completedLessonIds.contains(it.id) }
        return (completedCount * 100) / lessons.size
    }

    fun isValidModuleId(): Boolean {
        return id.matches(Regex("^[a-z]+_module_([1-9]|10)$"))
    }
}
