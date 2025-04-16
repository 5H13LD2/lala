// Create a file named Lesson.kt
package com.labactivity.lala

data class Lesson(
    val id: String,
    val moduleId: String,
    val number: String,
    val title: String,
    val explanation: String,
    val codeExample: String,
    val videoUrl: String,
    var isExpanded: Boolean = false
)