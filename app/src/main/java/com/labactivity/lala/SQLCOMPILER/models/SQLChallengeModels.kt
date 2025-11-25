package com.labactivity.lala.SQLCOMPILER.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Main data class representing a SQL challenge from Firestore
 * Maps to: sql_challenges/{challengeId}
 */
data class SQLChallenge(
    @DocumentId
    val id: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("description")
    val description: String = "",

    @PropertyName("difficulty")
    val difficulty: String = "",  // "Easy", "Medium", "Hard"

    @PropertyName("topic")
    val topic: String = "",

    @PropertyName("courseId")
    val courseId: String = "",

    @PropertyName("expected_query")
    val expectedQuery: String = "",

    @PropertyName("expected_result")
    val expectedResult: ExpectedResult = ExpectedResult(),

    @PropertyName("sample_table")
    val sampleTable: TableData = TableData(),

    @PropertyName("additional_tables")
    val additionalTables: List<TableData> = emptyList(),

    @PropertyName("hints")
    val hints: List<String> = emptyList(),

    @PropertyName("createdAt")
    val createdAt: String = "",

    @PropertyName("updatedAt")
    val updatedAt: String = "",

    @PropertyName("author")
    val author: String = "",

    @PropertyName("status")
    val status: String = "active",  // "active", "draft", "archived"

    @PropertyName("order")
    val order: Int = 0,

    @PropertyName("tags")
    val tags: List<String> = emptyList(),

    @PropertyName("testCases")
    val testCases: List<TestCase> = emptyList(),

    // Whether challenge is unlocked for the current user (computed at runtime, not stored in Firestore)
    val isUnlocked: Boolean = true
) {
    /**
     * Returns a preview of the description (first 100 characters)
     */
    val descriptionPreview: String
        get() = if (description.length > 100) {
            description.substring(0, 100) + "..."
        } else {
            description
        }

    /**
     * Returns the difficulty color for UI display
     */
    val difficultyColor: String
        get() = when (difficulty.lowercase()) {
            "easy" -> "#4CAF50"
            "medium" -> "#FF9800"
            "hard" -> "#F44336"
            else -> "#9E9E9E"
        }

    /**
     * Returns all tables (primary + additional) for database setup
     */
    fun getAllTables(): List<TableData> {
        return listOf(sampleTable) + additionalTables
    }

    /**
     * Returns a comma-separated string of tags
     */
    val tagsString: String
        get() = tags.joinToString(", ")
}

/**
 * Data class representing the expected SQL query result
 * Used for comparison with user's query output
 */
data class ExpectedResult(
    @PropertyName("columns")
    val columns: List<String> = emptyList(),

    @PropertyName("rows")
    val rows: List<List<Any>> = emptyList()
) {
    /**
     * Returns the number of expected rows
     */
    val rowCount: Int
        get() = rows.size

    /**
     * Returns the number of columns
     */
    val columnCount: Int
        get() = columns.size
}

/**
 * Data class representing a table structure with data
 * Used for setting up the database for each challenge
 */
data class TableData(
    @PropertyName("name")
    val name: String = "",

    @PropertyName("columns")
    val columns: List<String> = emptyList(),

    @PropertyName("rows")
    val rows: List<List<Any>> = emptyList()
) {
    /**
     * Infers column types from the first row of data
     * Returns a map of column name to SQL type
     */
    fun inferColumnTypes(): Map<String, String> {
        if (rows.isEmpty() || columns.isEmpty()) {
            return emptyMap()
        }

        val columnTypes = mutableMapOf<String, String>()
        val firstRow = rows.first()

        columns.forEachIndexed { index, columnName ->
            if (index < firstRow.size) {
                val value = firstRow[index]
                val sqlType = when (value) {
                    is Int, is Long -> "INTEGER"
                    is Double, is Float -> "REAL"
                    is String -> "TEXT"
                    is Boolean -> "INTEGER"  // Store as 0 or 1
                    else -> "TEXT"
                }
                columnTypes[columnName] = sqlType
            }
        }

        return columnTypes
    }

    /**
     * Generates CREATE TABLE SQL statement
     */
    fun generateCreateTableSQL(): String {
        if (name.isEmpty() || columns.isEmpty()) {
            return ""
        }

        val columnTypes = inferColumnTypes()
        val columnDefinitions = columns.joinToString(", ") { column ->
            val type = columnTypes[column] ?: "TEXT"
            "$column $type"
        }

        return "CREATE TABLE IF NOT EXISTS $name ($columnDefinitions);"
    }

    /**
     * Generates INSERT statements for all rows
     */
    fun generateInsertSQL(): List<String> {
        if (name.isEmpty() || columns.isEmpty() || rows.isEmpty()) {
            return emptyList()
        }

        return rows.map { row ->
            val values = row.joinToString(", ") { value ->
                when (value) {
                    is String -> "'${value.replace("'", "''")}'"  // Escape single quotes
                    is Boolean -> if (value) "1" else "0"
                    null -> "NULL"
                    else -> value.toString()
                }
            }
            "INSERT INTO $name (${columns.joinToString(", ")}) VALUES ($values);"
        }
    }

    /**
     * Returns a formatted preview of the table for display
     */
    fun getTablePreview(maxRows: Int = 5): String {
        val sb = StringBuilder()
        sb.append("Table: $name\n")
        sb.append("Columns: ${columns.joinToString(", ")}\n")
        sb.append("Rows: ${rows.size}\n")

        if (rows.isNotEmpty()) {
            sb.append("\nSample Data:\n")
            val previewRows = rows.take(maxRows)
            previewRows.forEach { row ->
                sb.append(row.joinToString(" | "))
                sb.append("\n")
            }
            if (rows.size > maxRows) {
                sb.append("... (${rows.size - maxRows} more rows)\n")
            }
        }

        return sb.toString()
    }
}

/**
 * Data class for test case metadata
 * Can be used for additional validation beyond basic query comparison
 */
data class TestCase(
    @PropertyName("id")
    val id: Int = 0,

    @PropertyName("description")
    val description: String = "",

    @PropertyName("expectedRowCount")
    val expectedRowCount: Int = 0,

    @PropertyName("expectedColumnCount")
    val expectedColumnCount: Int = 0
)

/**
 * Data class representing user's progress on a SQL challenge
 * Maps to: users/{userId}/sql_progress/{challengeId}
 */
data class SQLChallengeProgress(
    @DocumentId
    val challengeId: String = "",

    @PropertyName("status")
    val status: String = "not_started",  // "not_started", "in_progress", "completed"

    @PropertyName("attempts")
    val attempts: Int = 0,

    @PropertyName("bestScore")
    val bestScore: Int = 0,

    @PropertyName("lastAttemptDate")
    val lastAttemptDate: String = "",

    @PropertyName("timeTaken")
    val timeTaken: Long = 0,  // in seconds

    @PropertyName("userQuery")
    val userQuery: String = "",

    @PropertyName("passed")
    val passed: Boolean = false
) {
    /**
     * Returns a user-friendly status text
     */
    val statusText: String
        get() = when (status) {
            "not_started" -> "Not Started"
            "in_progress" -> "In Progress"
            "completed" -> "Completed"
            else -> "Unknown"
        }

    /**
     * Returns a formatted time taken string
     */
    val formattedTimeTaken: String
        get() {
            if (timeTaken == 0L) return "N/A"
            val minutes = timeTaken / 60
            val seconds = timeTaken % 60
            return if (minutes > 0) {
                "${minutes}m ${seconds}s"
            } else {
                "${seconds}s"
            }
        }
}

/**
 * Data class for challenge filter options
 */
data class SQLChallengeFilter(
    val courseId: String? = null,
    val difficulty: String? = null,
    val topic: String? = null,
    val status: String = "active",
    val searchQuery: String? = null
)

/**
 * Data class for SQL challenge statistics
 */
data class SQLChallengeStats(
    val totalChallenges: Int = 0,
    val completedChallenges: Int = 0,
    val totalAttempts: Int = 0,
    val averageScore: Double = 0.0,
    val totalTimeTaken: Long = 0  // in seconds
) {
    val completionPercentage: Int
        get() = if (totalChallenges > 0) {
            ((completedChallenges.toDouble() / totalChallenges) * 100).toInt()
        } else {
            0
        }

    val formattedAverageScore: String
        get() = String.format("%.1f%%", averageScore)
}
