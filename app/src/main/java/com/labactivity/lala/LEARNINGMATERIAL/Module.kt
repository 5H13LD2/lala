// Create a file named Module.kt
package com.labactivity.lala.LEARNINGMATERIAL

data class Module(
    val id: String,
    val title: String,
    val description: String,
    val lessons: List<Lesson>,
    var isExpanded: Boolean = false
) {
    fun getProgressPercentage(completedLessonIds: Set<String>): Int {
        if (lessons.isEmpty()) return 0
        val completedCount = lessons.count { completedLessonIds.contains(it.id) }
        return (completedCount * 100) / lessons.size
    }
}