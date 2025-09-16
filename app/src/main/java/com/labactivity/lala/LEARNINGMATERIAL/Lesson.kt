package com.labactivity.lala.LEARNINGMATERIAL

data class Lesson(
    val id: String,         // Use 'id' everywhere for consistency
    val number: String,
    val title: String,
    val description: String,
    val codeExample: String,
    val explanation: String,
    val videoUrl: String,
    var isExpanded: Boolean = false
)
