package com.labactivity.lala.LEARNINGMATERIAL

data class Quiz(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String
) 