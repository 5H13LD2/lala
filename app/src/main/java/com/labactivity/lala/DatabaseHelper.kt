package com.labactivity.lala

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "sql_compiler.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create sample tables
        createSampleTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    private fun createSampleTables(db: SQLiteDatabase) {
        // Create users table
        val createUsersTable = """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                age INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createUsersTable)

        // Insert sample data
        val insertUsers = """
            INSERT INTO users (id, name, age) VALUES
            (1, 'Jerico', 20),
            (2, 'John', 22)
        """.trimIndent()

        db.execSQL("INSERT INTO users (id, name, age) VALUES (1, 'Jerico', 20)")
        db.execSQL("INSERT INTO users (id, name, age) VALUES (2, 'John', 22)")
    }

    fun getExtendedTestCases(): List<TestCase> {
        return listOf(
            TestCase(
                id = 1,
                title = "Select All Users",
                description = "Retrieve all user records from the users table",
                sampleTables = mapOf(
                    "users" to listOf(
                        mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                        mapOf("id" to 2, "name" to "John", "age" to 22),
                        mapOf("id" to 3, "name" to "Jane", "age" to 25)
                    )
                ),
                expectedQuery = "SELECT * FROM users",
                expectedOutput = listOf(
                    mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                    mapOf("id" to 2, "name" to "John", "age" to 22),
                    mapOf("id" to 3, "name" to "Jane", "age" to 25)
                ),
                difficulty = "easy",
                tags = listOf("basic", "select")
            ),

            TestCase(
                id = 2,
                title = "Filter Users by Age",
                description = "Select users who are older than 21 years",
                sampleTables = mapOf(
                    "users" to listOf(
                        mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                        mapOf("id" to 2, "name" to "John", "age" to 22),
                        mapOf("id" to 3, "name" to "Jane", "age" to 25)
                    )
                ),
                expectedQuery = "SELECT * FROM users WHERE age > 21",
                expectedOutput = listOf(
                    mapOf("id" to 2, "name" to "John", "age" to 22),
                    mapOf("id" to 3, "name" to "Jane", "age" to 25)
                ),
                difficulty = "easy",
                tags = listOf("where", "filter", "comparison")
            ),

            TestCase(
                id = 3,
                title = "Select Specific Columns",
                description = "Select only name and age columns from users",
                sampleTables = mapOf(
                    "users" to listOf(
                        mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                        mapOf("id" to 2, "name" to "John", "age" to 22),
                        mapOf("id" to 3, "name" to "Jane", "age" to 25)
                    )
                ),
                expectedQuery = "SELECT name, age FROM users",
                expectedOutput = listOf(
                    mapOf("name" to "Jerico", "age" to 20),
                    mapOf("name" to "John", "age" to 22),
                    mapOf("name" to "Jane", "age" to 25)
                ),
                difficulty = "easy",
                tags = listOf("select", "columns")
            ),

            TestCase(
                id = 4,
                title = "Order Users by Age",
                description = "Select all users ordered by age in descending order",
                sampleTables = mapOf(
                    "users" to listOf(
                        mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                        mapOf("id" to 2, "name" to "John", "age" to 22),
                        mapOf("id" to 3, "name" to "Jane", "age" to 25)
                    )
                ),
                expectedQuery = "SELECT * FROM users ORDER BY age DESC",
                expectedOutput = listOf(
                    mapOf("id" to 3, "name" to "Jane", "age" to 25),
                    mapOf("id" to 2, "name" to "John", "age" to 22),
                    mapOf("id" to 1, "name" to "Jerico", "age" to 20)
                ),
                difficulty = "medium",
                tags = listOf("order by", "sorting")
            ),

            TestCase(
                id = 5,
                title = "Count Users",
                description = "Count the total number of users",
                sampleTables = mapOf(
                    "users" to listOf(
                        mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                        mapOf("id" to 2, "name" to "John", "age" to 22),
                        mapOf("id" to 3, "name" to "Jane", "age" to 25)
                    )
                ),
                expectedQuery = "SELECT COUNT(*) as total_users FROM users",
                expectedOutput = listOf(
                    mapOf("total_users" to 3)
                ),
                difficulty = "medium",
                tags = listOf("aggregate", "count")
            )
        )
    }

    fun getTestCases(): List<TestCase> {
        return listOf(
            TestCase(
                id = 1,
                title = "Select All Users",
                description = "Retrieve all user records",
                sampleTables = mapOf(
                    "users" to listOf(
                        mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                        mapOf("id" to 2, "name" to "John", "age" to 22)
                    )
                ),
                expectedQuery = "SELECT * FROM users",
                expectedOutput = listOf(
                    mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                    mapOf("id" to 2, "name" to "John", "age" to 22)
                ),
                difficulty = "easy",
                tags = listOf("basic", "select")
            ),
            TestCase(
                id = 2,
                title = "Filter Users by Age",
                description = "Select users older than 21",
                sampleTables = mapOf(
                    "users" to listOf(
                        mapOf("id" to 1, "name" to "Jerico", "age" to 20),
                        mapOf("id" to 2, "name" to "John", "age" to 22)
                    )
                ),
                expectedQuery = "SELECT * FROM users WHERE age > 21",
                expectedOutput = listOf(
                    mapOf("id" to 2, "name" to "John", "age" to 22)
                ),
                difficulty = "easy",
                tags = listOf("where", "filter")
            )
        )


    }
}

