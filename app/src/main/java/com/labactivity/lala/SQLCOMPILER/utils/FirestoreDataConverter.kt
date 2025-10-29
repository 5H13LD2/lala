package com.labactivity.lala.SQLCOMPILER.utils

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.labactivity.lala.SQLCOMPILER.models.ExpectedResult
import com.labactivity.lala.SQLCOMPILER.models.SQLChallenge
import com.labactivity.lala.SQLCOMPILER.models.TableData
import org.json.JSONArray

/**
 * Utility class for converting Firestore documents to Kotlin objects
 * Handles edge cases where arrays might be stored as strings
 */
object FirestoreDataConverter {

    private const val TAG = "FirestoreDataConverter"

    /**
     * Converts a Firestore DocumentSnapshot to SQLChallenge
     * Handles the case where rows might be stored as strings instead of arrays
     */
    fun toSQLChallenge(document: DocumentSnapshot): SQLChallenge? {
        try {
            val id = document.id
            val title = document.getString("title") ?: return null
            val description = document.getString("description") ?: ""
            val difficulty = document.getString("difficulty") ?: "Easy"
            val topic = document.getString("topic") ?: ""
            val courseId = document.getString("courseId") ?: ""
            val expectedQuery = document.getString("expected_query") ?: ""
            val createdAt = document.getString("createdAt") ?: ""
            val updatedAt = document.getString("updatedAt") ?: ""
            val author = document.getString("author") ?: ""
            val status = document.getString("status") ?: "active"
            val order = document.getLong("order")?.toInt() ?: 0

            // Parse expected result
            val expectedResultMap = document.get("expected_result") as? Map<*, *>
            val expectedResult = if (expectedResultMap != null) {
                parseExpectedResult(expectedResultMap)
            } else {
                ExpectedResult()
            }

            // Parse sample table
            val sampleTableMap = document.get("sample_table") as? Map<*, *>
            val sampleTable = if (sampleTableMap != null) {
                parseTableData(sampleTableMap)
            } else {
                TableData()
            }

            // Parse additional tables (if any)
            val additionalTablesList = document.get("additionalTables") as? List<*>
            val additionalTables = additionalTablesList?.mapNotNull { tableMap ->
                if (tableMap is Map<*, *>) {
                    parseTableData(tableMap)
                } else {
                    null
                }
            } ?: emptyList()

            // Parse hints
            val hints = (document.get("hints") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

            // Parse tags
            val tags = (document.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

            return SQLChallenge(
                id = id,
                title = title,
                description = description,
                difficulty = difficulty,
                topic = topic,
                courseId = courseId,
                expectedQuery = expectedQuery,
                expectedResult = expectedResult,
                sampleTable = sampleTable,
                additionalTables = additionalTables,
                hints = hints,
                createdAt = createdAt,
                updatedAt = updatedAt,
                author = author,
                status = status,
                order = order,
                tags = tags
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error converting document to SQLChallenge: ${e.message}", e)
            return null
        }
    }

    /**
     * Parses ExpectedResult from a Firestore map
     * Handles rows stored as strings, arrays, or maps
     */
    private fun parseExpectedResult(map: Map<*, *>): ExpectedResult {
        val columns = (map["columns"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        val rowsData = map["rows"]

        val rows = when (rowsData) {
            is List<*> -> parseRowsList(rowsData, columns)
            else -> emptyList()
        }

        return ExpectedResult(columns = columns, rows = rows)
    }

    /**
     * Parses TableData from a Firestore map
     * Handles rows stored as strings, arrays, or maps
     */
    private fun parseTableData(map: Map<*, *>): TableData {
        val name = map["name"] as? String ?: ""
        val columns = (map["columns"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        val rowsData = map["rows"]

        val rows = when (rowsData) {
            is List<*> -> parseRowsList(rowsData, columns)
            else -> emptyList()
        }

        return TableData(name = name, columns = columns, rows = rows)
    }

    /**
     * Parses a list of rows, handling both string, array, and map formats
     * Firebase format examples:
     * - Array: [1, "Maria", 21]
     * - String: "[1, \"Maria\", 21]"
     * - Map: {id: 1, name: "Maria", age: 21}
     */
    private fun parseRowsList(rowsData: List<*>, columns: List<String> = emptyList()): List<List<Any>> {
        return rowsData.mapNotNull { rowData ->
            when (rowData) {
                is String -> parseRowFromString(rowData)
                is List<*> -> parseRowFromList(rowData)
                is Map<*, *> -> parseRowFromMap(rowData, columns)
                else -> null
            }
        }
    }

    /**
     * Parses a row that is stored as a Map in Firebase
     * Converts Map to ordered List based on column order
     * Example: {id: 1, name: "Maria", age: 21} with columns [id, name, age] -> [1, "Maria", 21]
     */
    private fun parseRowFromMap(rowMap: Map<*, *>, columns: List<String>): List<Any> {
        return if (columns.isNotEmpty()) {
            // Use column order to extract values
            columns.map { columnName ->
                val value = rowMap[columnName]
                when (value) {
                    is Number -> {
                        when {
                            value is Double && value % 1.0 == 0.0 -> value.toLong()
                            else -> value
                        }
                    }
                    is String -> value
                    is Boolean -> value
                    null -> ""
                    else -> value.toString()
                }
            }
        } else {
            // If no columns provided, just return map values in any order
            rowMap.values.mapNotNull { value ->
                when (value) {
                    is Number -> {
                        when {
                            value is Double && value % 1.0 == 0.0 -> value.toLong()
                            else -> value
                        }
                    }
                    is String -> value
                    is Boolean -> value
                    null -> ""
                    else -> value.toString()
                }
            }
        }
    }

    /**
     * Parses a row that is stored as a string in Firebase
     * Example: "[1, \"Maria\", 21]" or "[2, "Maria", 21]"
     */
    private fun parseRowFromString(rowString: String): List<Any>? {
        return try {
            // Clean up the string and parse as JSON array
            val cleanedString = rowString.trim()

            if (!cleanedString.startsWith("[") || !cleanedString.endsWith("]")) {
                Log.w(TAG, "Invalid row string format: $rowString")
                return null
            }

            val jsonArray = JSONArray(cleanedString)
            val row = mutableListOf<Any>()

            for (i in 0 until jsonArray.length()) {
                val value = when {
                    jsonArray.isNull(i) -> ""
                    else -> {
                        val obj = jsonArray.get(i)
                        when (obj) {
                            is Int -> obj.toLong()  // Convert Int to Long for consistency
                            is Long -> obj
                            is Double -> obj
                            is String -> obj
                            is Boolean -> obj
                            else -> obj.toString()
                        }
                    }
                }
                row.add(value)
            }

            row
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing row from string: $rowString", e)
            null
        }
    }

    /**
     * Parses a row that is stored as a proper array in Firebase
     * Example: [1, "Maria", 21]
     */
    private fun parseRowFromList(rowList: List<*>): List<Any> {
        return rowList.mapNotNull { cell ->
            when (cell) {
                is Number -> {
                    // Firestore returns numbers as Long or Double
                    when {
                        cell is Double && cell % 1.0 == 0.0 -> cell.toLong()
                        else -> cell
                    }
                }
                is String -> cell
                is Boolean -> cell
                null -> ""  // Convert null to empty string
                else -> cell.toString()
            }
        }
    }

    /**
     * Validates a SQLChallenge object
     */
    fun isValid(challenge: SQLChallenge): Boolean {
        return challenge.title.isNotBlank() &&
                challenge.description.isNotBlank() &&
                challenge.expectedResult.columns.isNotEmpty() &&
                challenge.sampleTable.name.isNotBlank() &&
                challenge.sampleTable.columns.isNotEmpty()
    }
}
