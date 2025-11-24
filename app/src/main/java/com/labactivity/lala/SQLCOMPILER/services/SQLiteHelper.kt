package com.labactivity.lala.SQLCOMPILER.services

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.labactivity.lala.SQLCOMPILER.models.SQLChallenge

/**
 * SQLite helper for executing SQL queries in challenges
 */
class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "sql_challenge.db"
        private const val DATABASE_VERSION = 1
        private const val TAG = "SQLiteHelper"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Tables will be created dynamically based on challenge data
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop all tables and recreate
        db?.execSQL("DROP TABLE IF EXISTS employees")
        db?.execSQL("DROP TABLE IF EXISTS departments")
        db?.execSQL("DROP TABLE IF EXISTS products")
        db?.execSQL("DROP TABLE IF EXISTS customers")
        db?.execSQL("DROP TABLE IF EXISTS orders")
        onCreate(db)
    }

    /**
     * Create tables from challenge sample data
     */
    fun createTablesFromChallenge(challenge: SQLChallenge) {
        val db = writableDatabase

        try {
            val allTables = challenge.getAllTables()

            // Drop existing tables first
            allTables.forEach { table ->
                db.execSQL("DROP TABLE IF EXISTS ${table.name}")
            }

            // Create tables and insert data
            allTables.forEach { table ->
                // Infer column types from first row
                val columnTypes = inferColumnTypes(table)

                // Build CREATE TABLE statement with proper types
                val columns = table.columns.mapIndexed { index, columnName ->
                    val type = columnTypes.getOrElse(index) { "TEXT" }
                    "$columnName $type"
                }.joinToString(", ")

                val createTableSQL = "CREATE TABLE IF NOT EXISTS ${table.name} ($columns)"
                db.execSQL(createTableSQL)

                // Insert sample data
                table.rows.forEach { rowData ->
                    val columnNames = table.columns.joinToString(", ")
                    val placeholders = table.columns.joinToString(", ") { "?" }
                    val insertSQL = "INSERT INTO ${table.name} ($columnNames) VALUES ($placeholders)"

                    val values = rowData.map { value ->
                        when (value) {
                            is Number -> value
                            else -> value?.toString() ?: ""
                        }
                    }.toTypedArray()

                    db.execSQL(insertSQL, values)
                }

                Log.d(TAG, "✅ Created table ${table.name} with ${table.rows.size} rows")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating tables", e)
            throw e
        }
    }

    /**
     * Infer column types from table data
     */
    private fun inferColumnTypes(table: com.labactivity.lala.SQLCOMPILER.models.TableData): Map<Int, String> {
        if (table.rows.isEmpty()) return emptyMap()

        val columnTypes = mutableMapOf<Int, String>()
        val firstRow = table.rows.first()

        firstRow.forEachIndexed { index, value ->
            val sqlType = when (value) {
                is Int, is Long -> "INTEGER"
                is Double, is Float -> "REAL"
                is Boolean -> "INTEGER"
                else -> "TEXT"
            }
            columnTypes[index] = sqlType
        }

        return columnTypes
    }

    /**
     * Execute a SELECT query and return results as a list of maps
     */
    fun executeQuery(query: String): List<Map<String, Any?>> {
        val db = readableDatabase
        val results = mutableListOf<Map<String, Any?>>()

        try {
            val cursor: Cursor = db.rawQuery(query, null)

            if (cursor.moveToFirst()) {
                val columnNames = cursor.columnNames

                do {
                    val row = mutableMapOf<String, Any?>()
                    columnNames.forEachIndexed { index, columnName ->
                        val value = when (cursor.getType(index)) {
                            Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(index)
                            Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(index)
                            Cursor.FIELD_TYPE_STRING -> cursor.getString(index)
                            Cursor.FIELD_TYPE_BLOB -> cursor.getBlob(index)
                            else -> null
                        }
                        row[columnName] = value
                    }
                    results.add(row)
                } while (cursor.moveToNext())
            }

            cursor.close()
            Log.d(TAG, "✅ Query executed successfully: ${results.size} rows returned")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Query execution error", e)
            throw e
        }

        return results
    }

    /**
     * Execute a non-SELECT query (INSERT, UPDATE, DELETE)
     */
    fun executeUpdate(query: String): Int {
        val db = writableDatabase
        return try {
            db.execSQL(query)
            Log.d(TAG, "✅ Update executed successfully")
            1 // Return 1 to indicate success
        } catch (e: Exception) {
            Log.e(TAG, "❌ Update execution error", e)
            throw e
        }
    }

    /**
     * Clear all data from all tables
     */
    fun clearAllData() {
        val db = writableDatabase
        try {
            // Get all table names
            val cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name != 'android_metadata' AND name != 'sqlite_sequence'",
                null
            )

            val tables = mutableListOf<String>()
            if (cursor.moveToFirst()) {
                do {
                    tables.add(cursor.getString(0))
                } while (cursor.moveToNext())
            }
            cursor.close()

            // Drop all tables
            tables.forEach { tableName ->
                db.execSQL("DROP TABLE IF EXISTS $tableName")
                Log.d(TAG, "Dropped table: $tableName")
            }

            Log.d(TAG, "✅ All data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error clearing data", e)
        }
    }
}
