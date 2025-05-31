package com.labactivity.lala.SQLCOMPILER

data class TestCase(
    val id: Int,
    val title: String,
    val description: String,
    val sampleTables: Map<String, List<Map<String, Any>>>,
    val expectedQuery: String,
    val expectedOutput: List<Map<String, Any>>,
    val difficulty: String,
    val tags: List<String>
)

// QueryResult.kt
data class QueryResult(
    val success: Boolean,
    val columns: List<String>,
    val rows: List<List<Any>>,
    val errorMessage: String? = null,
    val executionTime: Long = 0
)

// EvaluationResult.kt
data class EvaluationResult(
    val isCorrect: Boolean,
    val score: Int,
    val feedback: String,
    val expectedResult: QueryResult,
    val actualResult: QueryResult
)