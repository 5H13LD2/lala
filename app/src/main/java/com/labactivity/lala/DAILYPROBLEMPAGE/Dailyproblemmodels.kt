package com.labactivity.lala.DAILYPROBLEMPAGE


import com.google.firebase.Timestamp

data class DailyProblem(
    val problemId: String = "",
    val courseId: String = "",
    val compilerType: String = "",
    val title: String = "",
    val description: String = "",
    val problemStatement: String = "",
    val difficulty: String = "",
    val points: Int = 0,
    val testCases: List<TestCase> = emptyList(),
    val hints: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val expiredAt: Timestamp? = null,
    val isActive: Boolean = true,
    val tags: List<String> = emptyList()
)

data class TestCase(
    val input: String = "",
    val expectedOutput: String = "",
    val isHidden: Boolean = false
)

data class DailyProblemProgress(
    val problemId: String = "",
    val courseId: String = "",
    val status: String = "", // pending, completed, failed
    val code: String = "",
    val submittedAt: Timestamp? = null,
    val score: Int = 0,
    val executionTime: Long = 0,
    val testCasesPassed: Int = 0,
    val totalTestCases: Int = 0
)

enum class ProblemStatus {
    PENDING,
    COMPLETED,
    FAILED
}

enum class CompilerType(val value: String) {
    PYTHON("python"),
    JAVA("java"),
    KOTLIN("kotlin"),
    JAVASCRIPT("javascript"),
    RUBY("ruby"),
    PHP("php"),
    SQL("sql");

    companion object {
        fun fromString(value: String): CompilerType? {
            return values().find { it.value.equals(value, ignoreCase = true) }
        }
    }
}

enum class Difficulty(val value: String) {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    companion object {
        fun fromString(value: String): Difficulty? {
            return values().find { it.value == value }
        }
    }
}
