package com.labactivity.lala

import android.database.sqlite.SQLiteDatabase
import android.database.Cursor
import com.labactivity.lala.QueryResult
import com.labactivity.lala.EvaluationResult

class QueryEvaluator(private val database: SQLiteDatabase) {

    fun executeQuery(query: String): QueryResult {
        return try {
            val startTime = System.currentTimeMillis()
            val cursor: Cursor = database.rawQuery(query, null)
            val endTime = System.currentTimeMillis()

            val columns = cursor.columnNames.toList()
            val rows = mutableListOf<List<Any>>()

            while (cursor.moveToNext()) {
                val row = mutableListOf<Any>()
                for (i in 0 until cursor.columnCount) {
                    when (cursor.getType(i)) {
                        Cursor.FIELD_TYPE_INTEGER -> row.add(cursor.getLong(i))
                        Cursor.FIELD_TYPE_FLOAT -> row.add(cursor.getDouble(i))
                        Cursor.FIELD_TYPE_STRING -> row.add(cursor.getString(i))
                        Cursor.FIELD_TYPE_NULL -> row.add("NULL")
                        else -> row.add(cursor.getString(i))
                    }
                }
                rows.add(row)
            }

            cursor.close()

            QueryResult(
                success = true,
                columns = columns,
                rows = rows,
                executionTime = endTime - startTime
            )

        } catch (e: Exception) {
            QueryResult(
                success = false,
                columns = emptyList(),
                rows = emptyList(),
                errorMessage = e.message
            )
        }
    }

    fun evaluateQuery(userQuery: String, expectedResult: QueryResult): EvaluationResult {
        val actualResult = executeQuery(userQuery)

        if (!actualResult.success) {
            return EvaluationResult(
                isCorrect = false,
                score = 0,
                feedback = "Query execution failed: ${actualResult.errorMessage}",
                expectedResult = expectedResult,
                actualResult = actualResult
            )
        }

        val isCorrect = compareResults(expectedResult, actualResult)
        val score = if (isCorrect) 100 else 0
        val feedback = generateFeedback(expectedResult, actualResult, isCorrect)

        return EvaluationResult(
            isCorrect = isCorrect,
            score = score,
            feedback = feedback,
            expectedResult = expectedResult,
            actualResult = actualResult
        )
    }

    private fun compareResults(expected: QueryResult, actual: QueryResult): Boolean {
        // Compare column count
        if (expected.columns.size != actual.columns.size) {
            return false
        }

        // Compare column names (case insensitive)
        val expectedCols = expected.columns.map { it.lowercase() }
        val actualCols = actual.columns.map { it.lowercase() }
        if (expectedCols != actualCols) {
            return false
        }

        // Compare row count
        if (expected.rows.size != actual.rows.size) {
            return false
        }

        // Compare data (order-independent for now)
        val expectedRows = expected.rows.map { it.toString() }.sorted()
        val actualRows = actual.rows.map { it.toString() }.sorted()

        return expectedRows == actualRows
    }

    private fun generateFeedback(expected: QueryResult, actual: QueryResult, isCorrect: Boolean): String {
        return when {
            isCorrect -> "✅ Correct Output! 🎉"
            expected.columns.size != actual.columns.size -> "❌ Column count mismatch. Expected ${expected.columns.size}, got ${actual.columns.size}"
            expected.rows.size != actual.rows.size -> "❌ Row count mismatch. Expected ${expected.rows.size}, got ${actual.rows.size}"
            else -> "❌ Data values don't match the expected output"
        }
    }
}