package com.labactivity.lala.FLASHCARD


data class FlashcardTopic(
    val id: Int,
    val title: String,
    val difficulty: String,
    val flashcards: List<Flashcard>
)

// Model class for individual flashcards
data class Flashcard(
    val question: String,
    val answer: String
)