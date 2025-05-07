package com.labactivity.lala.quiz

import android.content.ContentValues.TAG
import android.util.Log

/**
 * Repository that manages SQL quiz questions
 */
class SqlModuleQuizRepository : QuizRepository {
    
    // Map of module IDs to lists of quiz questions
    private val quizzesByModule: Map<String, List<Quiz>> = mapOf(
        // Module 1: SQL Basics
    "sql_module_1" to listOf(
        Quiz("sql_1.1", "What is SQL?", listOf("Structured Query Language", "Simple Query Language", "Systematic Query Language", "Standard Query List"), 0, Difficulty.EASY),
        Quiz("sql_1.2", "What does the SELECT statement do in SQL?", listOf("Inserts data into a table", "Deletes data from a table", "Retrieves data from a table", "Updates data in a table"), 2, Difficulty.EASY),
        Quiz("sql_1.3", "Which of these is the correct syntax to create a table?", listOf("CREATE TABLE users (id INT, name VARCHAR(255));", "TABLE CREATE users (id INT, name VARCHAR(255));", "CREATE users TABLE (id INT, name VARCHAR(255));", "CREATE TABLE users;"), 0, Difficulty.NORMAL),
        Quiz("sql_1.4", "What is the correct SQL statement to delete data from a table?", listOf("DELETE FROM table WHERE condition;", "REMOVE FROM table WHERE condition;", "DROP FROM table WHERE condition;", "DELETE table WHERE condition;"), 0, Difficulty.EASY),
        Quiz("sql_1.5", "Which command is used to create a new database in SQL?", listOf("CREATE DATABASE database_name;", "CREATE TABLE database_name;", "CREATE SCHEMA database_name;", "DATABASE CREATE database_name;"), 0, Difficulty.NORMAL),
        Quiz("sql_1.6", "What is the default sorting order of the ORDER BY clause?", listOf("Descending", "Ascending", "Alphabetical", "No sorting"), 1, Difficulty.EASY),
        Quiz("sql_1.7", "How do you add a comment in SQL?", listOf("-- comment", "/* comment */", "# comment", "ALL of the above"), 3, Difficulty.EASY),
        Quiz("sql_1.8", "What is the function of the WHERE clause?", listOf("To specify the database", "To filter records", "To define the table", "To insert data into the table"), 1, Difficulty.EASY),
        Quiz("sql_1.9", "Which of the following is a correct statement to retrieve all columns from a table?", listOf("SELECT * FROM table_name;", "SELECT table_name FROM *;", "SELECT * FROM *;", "SELECT table_name FROM table_name;"), 0, Difficulty.EASY),
        Quiz("sql_1.10", "Which clause is used to sort the result set?", listOf("ORDER BY", "GROUP BY", "HAVING", "LIMIT"), 0, Difficulty.NORMAL),
        ),

        // Module 2: Data Manipulation
    "sql_module_2" to listOf(
        Quiz("sql_2.1", "How do you insert data into a table?", listOf("INSERT INTO table VALUES (value1, value2);", "INSERT table VALUES (value1, value2);", "INSERT INTO table (column1, column2) VALUES (value1, value2);", "ADD INTO table (column1, column2) VALUES (value1, value2);"), 2, Difficulty.EASY),
        Quiz("sql_2.2", "Which SQL statement is used to modify existing data?", listOf("INSERT INTO", "UPDATE", "ALTER", "MODIFY"), 1, Difficulty.EASY),
        Quiz("sql_2.3", "What SQL command is used to remove data from a table?", listOf("REMOVE", "DELETE", "DROP", "CLEAR"), 1, Difficulty.EASY),
        Quiz("sql_2.4", "What is the correct SQL statement to change a column value?", listOf("UPDATE table_name SET column_name = new_value;", "UPDATE table_name SET column_name new_value;", "UPDATE table_name column_name = new_value;", "UPDATE table_name SET column_name TO new_value;"), 0, Difficulty.NORMAL),
        Quiz("sql_2.5", "Which SQL statement is used to add a new column to a table?", listOf("ALTER TABLE table_name ADD column_name data_type;", "ADD COLUMN table_name column_name data_type;", "ALTER TABLE ADD column_name data_type;", "TABLE ALTER table_name ADD column_name data_type;"), 0, Difficulty.NORMAL),
        Quiz("sql_2.6", "How do you change a column's datatype in SQL?", listOf("ALTER TABLE table_name CHANGE column_name new_datatype;", "ALTER TABLE table_name MODIFY column_name new_datatype;", "ALTER COLUMN table_name CHANGE datatype column_name;", "ALTER COLUMN table_name MODIFY datatype column_name;"), 1, Difficulty.NORMAL),
        Quiz("sql_2.7", "Which command removes all rows from a table without deleting the table itself?", listOf("DELETE", "DROP", "TRUNCATE", "REMOVE"), 2, Difficulty.EASY),
        Quiz("sql_2.8", "What will happen if you run a DELETE statement without a WHERE clause?", listOf("Nothing", "Deletes all rows in the table", "Deletes the entire database", "Deletes the table structure"), 1, Difficulty.NORMAL),
        Quiz("sql_2.9", "Which of the following is used to remove a table from the database?", listOf("REMOVE TABLE table_name;", "DELETE TABLE table_name;", "DROP TABLE table_name;", "DROP DATABASE table_name;"), 2, Difficulty.NORMAL),
        Quiz("sql_2.10", "What will the following query do? SELECT COUNT(*) FROM employees WHERE age > 30;", listOf("It counts all rows in the employees table", "It counts the rows where age is greater than 30", "It returns the age of employees older than 30", "It filters employees with age less than 30"), 1, Difficulty.NORMAL),
    ),

        // Module 3: SQL Joins
    "sql_module_3" to listOf(
        Quiz("sql_3.1", "Which SQL join returns only matching rows from both tables?", listOf("LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "OUTER JOIN"), 2, Difficulty.NORMAL),
        Quiz("sql_3.2", "What is the correct SQL syntax for a LEFT JOIN?", listOf("SELECT * FROM table1 LEFT JOIN table2 ON condition;", "SELECT * FROM table1 JOIN LEFT table2 ON condition;", "LEFT JOIN table1 ON condition;", "SELECT * FROM table1 LEFT JOIN table2;"), 0, Difficulty.NORMAL),
        Quiz("sql_3.3", "Which SQL join returns all rows from the right table and matched rows from the left table?", listOf("RIGHT JOIN", "LEFT JOIN", "INNER JOIN", "FULL OUTER JOIN"), 0, Difficulty.NORMAL),
        Quiz("sql_3.4", "Which of the following joins returns all rows from both tables?", listOf("INNER JOIN", "OUTER JOIN", "LEFT JOIN", "RIGHT JOIN"), 1, Difficulty.NORMAL),
        Quiz("sql_3.5", "What is the result of an INNER JOIN?", listOf("Returns all records from both tables", "Returns all records from the left table", "Returns only matching records", "Returns records with unmatched rows"), 2, Difficulty.NORMAL),
        Quiz("sql_3.6", "Which SQL join would you use if you want all rows from the left table and only matching rows from the right table?", listOf("LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "FULL OUTER JOIN"), 0, Difficulty.NORMAL),
        Quiz("sql_3.7", "Which SQL join type is useful when you want to return unmatched rows as well as matching rows?", listOf("LEFT JOIN", "RIGHT JOIN", "FULL OUTER JOIN", "INNER JOIN"), 2, Difficulty.NORMAL),
        Quiz("sql_3.8", "How can you join three tables in SQL?", listOf("JOIN table1, table2, table3", "JOIN table1 JOIN table2 JOIN table3", "SELECT * FROM table1 INNER JOIN table2 ON condition INNER JOIN table3 ON condition;", "Using multiple SELECT statements"), 2, Difficulty.NORMAL),
        Quiz("sql_3.9", "Which keyword is used to combine rows from two tables?", listOf("SELECT", "JOIN", "UNION", "MERGE"), 1, Difficulty.NORMAL),
        Quiz("sql_3.10", "What happens if no match is found in a LEFT JOIN?", listOf("Returns NULL values for columns from the right table", "Returns NULL values for columns from the left table", "Returns an error", "Returns empty rows"), 0, Difficulty.NORMAL),
    ),
        // Module 4: Advanced SQL Queries
    "sql_module_4" to listOf(
        Quiz("sql_4.1", "What is a subquery?", listOf("A query within a query", "A nested SELECT statement", "A query with multiple SELECTs", "A query that returns a single value"), 0, Difficulty.NORMAL),
        Quiz("sql_4.2", "Which of the following is an aggregate function in SQL?", listOf("SUM", "COUNT", "AVG", "All of the above"), 3, Difficulty.NORMAL),
        Quiz("sql_4.3", "How do you group rows in SQL?", listOf("Using GROUP BY", "Using ORDER BY", "Using HAVING", "Using SELECT GROUP"), 0, Difficulty.NORMAL),
        Quiz("sql_4.4", "What does the HAVING clause do?", listOf("Filters records before GROUP BY", "Filters records after GROUP BY", "Groups the records", "Specifies the condition for JOIN"), 1, Difficulty.NORMAL),
        Quiz("sql_4.5", "Which of the following is correct for calculating the average value in SQL?", listOf("SELECT AVG(column_name) FROM table;", "SELECT AVERAGE(column_name) FROM table;", "SELECT SUM(column_name) / COUNT(*) FROM table;", "SELECT MEAN(column_name) FROM table;"), 0, Difficulty.NORMAL),
        Quiz("sql_4.6", "How do you find the maximum value in a column?", listOf("SELECT MAX(column_name) FROM table;", "SELECT MAX(table.column_name) FROM table;", "SELECT TOP 1 column_name FROM table;", "SELECT column_name MAX FROM table;"), 0, Difficulty.NORMAL),
        Quiz("sql_4.7", "How do you calculate the total number of rows in a table?", listOf("SELECT COUNT(*) FROM table;", "SELECT TOTAL(*) FROM table;", "SELECT SUM(*) FROM table;", "SELECT NUM(*) FROM table;"), 0, Difficulty.EASY),
        Quiz("sql_4.8", "What is a correlated subquery?", listOf("A subquery that depends on the outer query", "A subquery that is independent of the outer query", "A subquery that returns multiple values", "A query with a WHERE clause"), 0, Difficulty.HARD),
        Quiz("sql_4.9", "Which SQL clause can be used with aggregate functions?", listOf("GROUP BY", "WHERE", "HAVING", "ORDER BY"), 2, Difficulty.NORMAL),
        Quiz("sql_4.10", "How do you combine the results of two SELECT statements?", listOf("Using UNION", "Using JOIN", "Using GROUP BY", "Using INTERSECT"), 0, Difficulty.NORMAL),
    ),
        // Module 5: SQL Constraints
    "sql_module_5" to listOf(
        Quiz("sql_5.1", "What is the purpose of the PRIMARY KEY constraint?", listOf("To uniquely identify each record in a table", "To ensure the column does not contain NULL values", "To create a relationship between two tables", "To automatically generate values in a column"), 0, Difficulty.NORMAL),
        Quiz("sql_5.2", "Which SQL constraint ensures that all values in a column are unique?", listOf("PRIMARY KEY", "UNIQUE", "FOREIGN KEY", "CHECK"), 1, Difficulty.NORMAL),
        Quiz("sql_5.3", "Which constraint is used to enforce a foreign key relationship?", listOf("UNIQUE", "FOREIGN KEY", "PRIMARY KEY", "CHECK"), 1, Difficulty.NORMAL),
        Quiz("sql_5.4", "What is the function of the NOT NULL constraint?", listOf("Ensures that a column cannot have NULL values", "Specifies that the column must be unique", "Specifies a default value for the column", "Enforces a foreign key relationship"), 0, Difficulty.EASY),
        Quiz("sql_5.5", "What does the CHECK constraint do?", listOf("Limits the value range of a column", "Ensures all values are unique", "Enforces a primary key", "Automatically generates values for a column"), 0, Difficulty.NORMAL),
        Quiz("sql_5.6", "Which of the following SQL constraints is used to define a default value for a column?", listOf("DEFAULT", "NOT NULL", "CHECK", "PRIMARY KEY"), 0, Difficulty.NORMAL),
        Quiz("sql_5.7", "Which of these constraints can be used to prevent the deletion of a record in the parent table?", listOf("ON DELETE NO ACTION", "ON DELETE CASCADE", "ON DELETE SET NULL", "ON DELETE RESTRICT"), 0, Difficulty.NORMAL),
        Quiz("sql_5.8", "How do you create a unique constraint for a column?", listOf("CREATE UNIQUE INDEX column_name;", "CREATE CONSTRAINT UNIQUE column_name;", "ALTER TABLE table_name ADD UNIQUE (column_name);", "ADD CONSTRAINT UNIQUE column_name TO table_name;"), 2, Difficulty.NORMAL),
        Quiz("sql_5.9", "Which SQL constraint allows only specific values for a column?", listOf("CHECK", "DEFAULT", "UNIQUE", "FOREIGN KEY"), 0, Difficulty.NORMAL),
        Quiz("sql_5.10", "Which SQL command is used to add a constraint to an existing table?", listOf("ADD CONSTRAINT", "ALTER TABLE", "CREATE CONSTRAINT", "UPDATE CONSTRAINT"), 1, Difficulty.NORMAL),

        )
    )

    override fun getQuestionsForModule(moduleId: String): List<Quiz> {
        Log.d(TAG, "Getting questions for module ID: $moduleId")

        // Try to get questions for the specified module ID
        val questions = quizzesByModule[moduleId]

        // If exact match is found, return those questions
        if (questions != null) {
            Log.d(TAG, "Found ${questions.size} questions for module ID: $moduleId")
            return questions.take(10) // Limit to 10 questions
        }

        // No exact match - determine the appropriate sql module
        val sqlModuleId = when {
            // If it's already in sql_module_X format
            moduleId.matches(Regex("^sql_module_[1-5]$")) -> {
                Log.d(TAG, "Using existing sql_module_X format: $moduleId")
                moduleId
            }
            // If it's in module_X format, convert to sql_module_X
            moduleId.matches(Regex("^module_[1-5]$")) -> {
                val moduleNum = moduleId.substringAfter("_")
                val newId = "sql_module_$moduleNum"
                Log.d(TAG, "Converting $moduleId to $newId")
                newId
            }
            // If it's in sql_X format, convert to sql_module_X
            moduleId.matches(Regex("^sql_[1-5]$")) -> {
                val moduleNum = moduleId.substringAfter("_")
                val newId = "sql_module_$moduleNum"
                Log.d(TAG, "Converting $moduleId to $newId")
                newId
            }
            // Default to first sql module
            else -> {
                Log.d(TAG, "Using default sql module: sql_module_1")
                "sql_module_1"
            }
        }

        // Get questions for the determined module
        val moduleQuestions = quizzesByModule[sqlModuleId]
        Log.d(TAG, "Using $sqlModuleId questions as fallback for $moduleId")

        return moduleQuestions?.take(10) ?: emptyList()
    }

    override fun getQuestionCountForModule(moduleId: String): Int {
        // Try to get questions for the specified module ID
        val questions = quizzesByModule[moduleId]

        // If exact match is found, return the count
        if (questions != null) {
            return minOf(questions.size, 10)
        }

        // No exact match - determine the appropriate sql module
        val sqlModuleId = when {
            // If it contains "sql_module_" followed by a number
            moduleId.matches(Regex("sql_module_[1-7].*")) -> {
                val parsedId = moduleId.substringBefore(".")
                if (quizzesByModule.containsKey(parsedId)) parsedId else "sql_module_1"
            }
            // Default to firs sql module
            else -> "sql_module_1"
        }

        // Get count for the determined module
        val moduleQuestions = quizzesByModule[sqlModuleId]
        return minOf(moduleQuestions?.size ?: 0, 10)
    }

    /**
     * Get a list of all module IDs
     * @return List of module IDs
     */
    override fun getAllModuleIds(): List<String> {
        return quizzesByModule.keys.toList()
    }

    /**
     * Checks if this repository can handle questions for the given module ID
     * @param moduleId The ID of the module to check
     * @return true if this repository can handle the module, false otherwise
     */
    override fun canHandleModule(moduleId: String): Boolean {
        Log.d(TAG, "Checking if module ID: $moduleId is an SQL module")
        
        // Check for exact matches first
        if (quizzesByModule.containsKey(moduleId)) {
            Log.d(TAG, "Direct match found for SQL module: $moduleId")
            return true
        }
        
        // Check for SQL-specific patterns
        val isSqlModule = when {
            // Check for sql_module_X format
            moduleId.matches(Regex("^sql_module_[1-5]$")) -> {
                Log.d(TAG, "Matched sql_module_X format")
                true
            }
            // Check for module_X format where X is 1-5 (SQL modules)
            moduleId.matches(Regex("^module_[1-5]$")) -> {
                Log.d(TAG, "Matched module_X format, converting to sql_module_X")
                true
            }
            // Check for sql_X format
            moduleId.matches(Regex("^sql_[1-5]$")) -> {
                Log.d(TAG, "Matched sql_X format")
                true
            }
            // Check for any ID containing "sql" (case insensitive)
            moduleId.contains("sql", ignoreCase = true) -> {
                Log.d(TAG, "Matched ID containing 'sql'")
                true
            }
            else -> {
                Log.d(TAG, "No SQL module pattern matched")
                false
            }
        }
        
        Log.d(TAG, "Module ID: $moduleId is SQL module: $isSqlModule")
        return isSqlModule
    }
} 