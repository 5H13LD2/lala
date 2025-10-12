package com.labactivity.lala.PYTHONASSESMENT

data class Challenge(
    val id: String = "",                // Firestore document ID
    val title: String = "",
    val difficulty: String = "",
    val courseId: String = "",
    val brokenCode: String = "",
    val correctOutput: String = "",
    val hint: String = "",
    val category: String = "",
    val status: String = "available",
    val createdAt: String = "",
    val codePreview: String = if (brokenCode.isNotEmpty()) {
        brokenCode.lines().firstOrNull {
            it.trim().isNotEmpty() && !it.trim().startsWith("#")
        } ?: ""
    } else {
        ""
    }
)
