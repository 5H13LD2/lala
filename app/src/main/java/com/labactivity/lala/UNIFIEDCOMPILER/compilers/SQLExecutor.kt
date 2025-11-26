package com.labactivity.lala.UNIFIEDCOMPILER.compilers

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.labactivity.lala.UNIFIEDCOMPILER.CourseCompiler
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * SQL executor implementation using SQLite
 * This is a special case - it executes queries rather than compiling code
 */
class SQLExecutor(context: Context) : CourseCompiler {

    private val dbHelper = SQLDatabaseHelper(context)
    private val database: SQLiteDatabase = dbHelper.writableDatabase

    companion object {
        private const val TAG = "UnifiedSQLExecutor"
    }

    // Security: Blocked keywords to prevent destructive operations
    private val blockedKeywords = setOf(
        "DROP", "DELETE", "INSERT", "UPDATE", "CREATE", "ALTER",
        "EXEC", "EXECUTE", "TRUNCATE", "GRANT", "REVOKE"
    )

    override suspend fun compile(code: String, config: CompilerConfig): CompilerResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            // Validate query first
            val validationError = validateSyntax(code)
            if (validationError != null) {
                return@withContext CompilerResult(
                    success = false,
                    output = "",
                    error = validationError,
                    executionTime = System.currentTimeMillis() - startTime,
                    compiledSuccessfully = false
                )
            }

            withTimeout(config.timeout) {
                try {
                    Log.d(TAG, "Executing SQL query: $code")
                    val cursor: Cursor = database.rawQuery(code, null)
                    val executionTime = System.currentTimeMillis() - startTime

                    // Extract columns and rows
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
                                else -> row.add(cursor.getString(i) ?: "NULL")
                            }
                        }
                        rows.add(row)
                    }
                    cursor.close()

                    Log.d(TAG, "✅ Query executed successfully: ${rows.size} rows returned")

                    // Format output as table
                    val output = formatQueryResult(columns, rows)

                    // Test case validation if provided
                    var testCasesPassed = 0
                    if (config.testCases.isNotEmpty()) {
                        testCasesPassed = validateTestCases(code, config.testCases)
                    }

                    CompilerResult(
                        success = true,
                        output = output,
                        executionTime = executionTime,
                        compiledSuccessfully = true,
                        testCasesPassed = testCasesPassed,
                        totalTestCases = config.testCases.size,
                        metadata = mapOf(
                            "columns" to columns,
                            "rowCount" to rows.size
                        )
                    )
                } catch (e: Exception) {
                    CompilerResult(
                        success = false,
                        output = "",
                        error = "Query execution error: ${e.message}",
                        executionTime = System.currentTimeMillis() - startTime,
                        compiledSuccessfully = true
                    )
                }
            }
        } catch (e: Exception) {
            CompilerResult(
                success = false,
                output = "",
                error = "Timeout or error: ${e.message}",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        }
    }

    private fun validateTestCases(query: String, testCases: List<com.labactivity.lala.UNIFIEDCOMPILER.models.TestCase>): Int {
        var passed = 0
        testCases.forEach { testCase ->
            try {
                val cursor = database.rawQuery(query, null)
                val rows = mutableListOf<String>()

                while (cursor.moveToNext()) {
                    val row = mutableListOf<String>()
                    for (i in 0 until cursor.columnCount) {
                        row.add(cursor.getString(i) ?: "NULL")
                    }
                    rows.add(row.joinToString(","))
                }
                cursor.close()

                val actualOutput = rows.joinToString("\n")
                if (actualOutput.trim() == testCase.expectedOutput.trim()) {
                    passed++
                }
            } catch (e: Exception) {
                // Test case failed
            }
        }
        return passed
    }

    override fun getLanguageId(): String = "sql"

    override fun getLanguageName(): String = "SQL"

    override fun getFileExtension(): String = ".sql"

    override fun validateSyntax(code: String): String? {
        val query = code.trim().uppercase()

        // Check for blocked keywords
        blockedKeywords.forEach { keyword ->
            if (query.contains(keyword)) {
                return "Security Error: '$keyword' operations are not allowed. Only SELECT queries are permitted."
            }
        }

        // Check for SQL injection patterns
        if (query.contains("--") || query.contains("/*") || query.contains(";")) {
            return "Security Error: Comments and multiple statements are not allowed."
        }

        // Check max length
        if (code.length > 1000) {
            return "Error: Query exceeds maximum length of 1000 characters."
        }

        return null
    }

    /**
     * Format query result as a readable table
     */
    private fun formatQueryResult(columns: List<String>, rows: List<List<Any>>): String {
        if (rows.isEmpty()) {
            return "Query executed successfully. No rows returned.\nColumns: ${columns.joinToString(", ")}"
        }

        val result = StringBuilder()
        result.append("Results (${rows.size} rows):\n\n")

        // Header
        result.append(columns.joinToString(" | "))
        result.append("\n")
        result.append("-".repeat(columns.joinToString(" | ").length))
        result.append("\n")

        // Rows
        rows.forEach { row ->
            result.append(row.joinToString(" | "))
            result.append("\n")
        }

        return result.toString()
    }

    /**
     * Helper class to manage SQLite database for SQL execution
     */
    private class SQLDatabaseHelper(context: Context) : SQLiteOpenHelper(
        context,
        "unified_sql_compiler.db",
        null,
        1
    ) {
        override fun onCreate(db: SQLiteDatabase) {
            // Create sample tables for testing
            createSampleTables(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // Handle database upgrades if needed
        }

        private fun createSampleTables(db: SQLiteDatabase) {
            // Create a sample employees table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS employees (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    department TEXT,
                    salary REAL
                )
            """.trimIndent())

            // Create a sample products table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    price REAL,
                    category TEXT
                )
            """.trimIndent())
        }
    }

    /**
     * Add custom table data for challenges
     * This allows dynamic table creation from Firebase
     */
    fun addCustomTable(tableName: String, columns: List<String>, rows: List<List<Any>>) {
        try {
            Log.d(TAG, "Creating table: $tableName with ${columns.size} columns and ${rows.size} rows")

            // Create table
            val columnDefs = columns.joinToString(", ") { "$it TEXT" }
            database.execSQL("DROP TABLE IF EXISTS $tableName")
            database.execSQL("CREATE TABLE $tableName ($columnDefs)")

            // Insert rows
            rows.forEach { row ->
                val values = row.joinToString(", ") { "'$it'" }
                database.execSQL("INSERT INTO $tableName VALUES ($values)")
            }

            Log.d(TAG, "✅ Created table $tableName with ${rows.size} rows")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating table $tableName", e)
            throw e
        }
    }
}
