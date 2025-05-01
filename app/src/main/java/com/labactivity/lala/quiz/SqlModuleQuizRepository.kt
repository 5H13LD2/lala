package com.labactivity.lala.quiz

import android.util.Log

/**
 * Repository that manages SQL quiz questions
 */
class SqlModuleQuizRepository : QuizRepository {
    
    // Map of module IDs to lists of quiz questions
    private val quizzesByModule: Map<String, List<Quiz>> = mapOf(
        // Module 1: SQL Basics
        "sql_module_1" to listOf(
            Quiz(
                id = "sql_1.1",
                question = "What does SQL stand for?",
                options = listOf(
                    "Structured Question Language",
                    "Structured Query Language",
                    "Standard Query Language",
                    "System Query Language"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "sql_1.2",
                question = "Which SQL statement is used to retrieve data from a database?",
                options = listOf(
                    "GET",
                    "OPEN",
                    "EXTRACT",
                    "SELECT"
                ),
                correctOptionIndex = 3,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "sql_1.3",
                question = "Which SQL clause is used to filter records?",
                options = listOf(
                    "WHERE",
                    "HAVING",
                    "FILTER",
                    "GROUP BY"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "sql_1.4",
                question = "What is a primary key?",
                options = listOf(
                    "The first column in a table",
                    "A column that uniquely identifies each row",
                    "A column that can contain NULL values",
                    "The main table in a database"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_1.5",
                question = "Which SQL statement is used to add new records to a table?",
                options = listOf(
                    "ADD",
                    "INSERT",
                    "UPDATE",
                    "CREATE"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "sql_1.6",
                question = "Which SQL statement is used to update existing records in a table?",
                options = listOf(
                    "SAVE",
                    "MODIFY",
                    "UPDATE",
                    "CHANGE"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "sql_1.7",
                question = "Which SQL statement is used to delete records from a table?",
                options = listOf(
                    "DELETE",
                    "REMOVE",
                    "DROP",
                    "CLEAR"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "sql_1.8",
                question = "Which SQL clause is used to sort the result set?",
                options = listOf(
                    "SORT BY",
                    "ORDER BY",
                    "GROUP BY",
                    "ARRANGE BY"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "sql_1.9",
                question = "What is a foreign key?",
                options = listOf(
                    "A key that uniquely identifies a record in a table",
                    "A key that links two tables together",
                    "A key used for encryption",
                    "A key used for indexing"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_1.10",
                question = "Which SQL function is used to return the highest value in a column?",
                options = listOf(
                    "TOP()",
                    "MAXIMUM()",
                    "HIGH()",
                    "MAX()"
                ),
                correctOptionIndex = 3,
                difficulty = Difficulty.NORMAL
            )
        ),
        
        // Module 2: SQL Joins and Relations
        "sql_module_2" to listOf(
            Quiz(
                id = "sql_2.1",
                question = "Which SQL join returns rows when there is at least one match in both tables?",
                options = listOf(
                    "INNER JOIN",
                    "LEFT JOIN",
                    "RIGHT JOIN",
                    "FULL JOIN"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_2.2",
                question = "What does a LEFT JOIN return?",
                options = listOf(
                    "All records from the right table and matching records from the left table",
                    "All records from the left table and matching records from the right table",
                    "Only matching records from both tables",
                    "All records from both tables"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_2.3",
                question = "Which SQL statement is used to create a relationship between tables?",
                options = listOf(
                    "RELATIONSHIP",
                    "FOREIGN KEY",
                    "LINK",
                    "CONNECT"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_2.4",
                question = "What type of join is needed if you want all records from both tables, even if there are no matches?",
                options = listOf(
                    "INNER JOIN",
                    "LEFT JOIN",
                    "RIGHT JOIN",
                    "FULL OUTER JOIN"
                ),
                correctOptionIndex = 3,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_2.5",
                question = "In a CROSS JOIN, how many rows will be returned if table A has 3 rows and table B has 4 rows?",
                options = listOf(
                    "3",
                    "4",
                    "7",
                    "12"
                ),
                correctOptionIndex = 3,
                difficulty = Difficulty.HARD
            ),
            Quiz(
                id = "sql_2.6",
                question = "Which join type will exclude rows that have matches in both tables?",
                options = listOf(
                    "INNER JOIN",
                    "OUTER JOIN",
                    "EXCLUSIVE JOIN",
                    "ANTI JOIN"
                ),
                correctOptionIndex = 3,
                difficulty = Difficulty.HARD
            ),
            Quiz(
                id = "sql_2.7",
                question = "What is the difference between a natural join and an inner join?",
                options = listOf(
                    "Natural join automatically joins tables on columns with the same name",
                    "Inner join is faster than natural join",
                    "Natural join only works on primary keys",
                    "Inner join automatically excludes null values"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_2.8",
                question = "Which join type is most commonly used in SQL queries?",
                options = listOf(
                    "INNER JOIN",
                    "LEFT JOIN",
                    "RIGHT JOIN",
                    "FULL JOIN"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "sql_2.9",
                question = "What does the ON clause in a JOIN statement specify?",
                options = listOf(
                    "The tables to be joined",
                    "The columns to be selected",
                    "The condition for joining tables",
                    "The order of the results"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_2.10",
                question = "Which join would you use to find all employees who are not assigned to any department?",
                options = listOf(
                    "INNER JOIN",
                    "LEFT JOIN with a NULL filter",
                    "RIGHT JOIN",
                    "FULL JOIN"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            )
        ),
        
        // Module 3: SQL Advanced Queries
        "sql_module_3" to listOf(
            Quiz(
                id = "sql_3.1",
                question = "What is a subquery in SQL?",
                options = listOf(
                    "A query that runs faster than a regular query",
                    "A query nested inside another query",
                    "A query that only returns subsets of data",
                    "A query that modifies data"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_3.2",
                question = "Which SQL clause is used to group rows that have the same values?",
                options = listOf(
                    "GROUP BY",
                    "ORDER BY",
                    "HAVING",
                    "WHERE"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_3.3",
                question = "What is the difference between WHERE and HAVING clauses?",
                options = listOf(
                    "WHERE filters before grouping, HAVING filters after grouping",
                    "WHERE works with SELECT, HAVING works with INSERT",
                    "WHERE is faster, HAVING is more accurate",
                    "WHERE works on rows, HAVING works on columns"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_3.4",
                question = "Which SQL function is used to count the number of rows in a result set?",
                options = listOf(
                    "SUM()",
                    "COUNT()",
                    "TOTAL()",
                    "NUMBER()"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "sql_3.5",
                question = "What is the purpose of the UNION operator in SQL?",
                options = listOf(
                    "To join tables horizontally",
                    "To combine the results of two or more SELECT statements",
                    "To find common rows between queries",
                    "To create table relationships"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_3.6",
                question = "Which SQL function returns the current date and time?",
                options = listOf(
                    "GETDATE()",
                    "CURRENT()",
                    "NOW()",
                    "TODAY()"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_3.7",
                question = "What is the purpose of the SQL CASE statement?",
                options = listOf(
                    "To execute stored procedures",
                    "To perform conditional logic",
                    "To create table indexes",
                    "To join tables"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "sql_3.8",
                question = "Which SQL constraint enforces a field to not accept NULL values?",
                options = listOf(
                    "UNIQUE",
                    "PRIMARY KEY",
                    "NOT NULL",
                    "CHECK"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "sql_3.9",
                question = "What does the SQL keyword DISTINCT do?",
                options = listOf(
                    "Sorts the result set",
                    "Filters duplicate values",
                    "Joins multiple queries",
                    "Groups related data"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "sql_3.10",
                question = "Which SQL statement is used to create an index on a table?",
                options = listOf(
                    "MAKE INDEX",
                    "CREATE INDEX",
                    "ADD INDEX",
                    "INDEX TABLE"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            )
        )
    )

    override fun getQuestionsForModule(moduleId: String): List<Quiz> {
        Log.d("SqlModuleQuizRepository", "Getting questions for module ID: $moduleId")
        
        // Try to get questions for the specified module ID
        val questions = quizzesByModule[moduleId]
        
        // If exact match is found, return those questions
        if (questions != null) {
            Log.d("SqlModuleQuizRepository", "Found ${questions.size} questions for module ID: $moduleId")
            return questions.take(10) // Limit to 10 questions
        }
        
        // No exact match - determine the appropriate SQL module
        val sqlModuleId = when {
            // If it's a recognized SQL module format
            moduleId.matches(Regex("sql_module_[1-7].*")) -> {
                val parsedId = moduleId.substringBefore(".")
                Log.d("SqlModuleQuizRepository", "Parsed SQL module ID: $parsedId")
                if (quizzesByModule.containsKey(parsedId)) parsedId else "sql_module_1"
            }
            // If it's the old format (module_1)
            moduleId.matches(Regex("module_[1-7]")) -> {
                val number = moduleId.substringAfter("_")
                Log.d("SqlModuleQuizRepository", "Converting old SQL module ID format")
                "sql_module_$number"
            }
            // Default to first SQL module
            else -> {
                Log.d("SqlModuleQuizRepository", "Using default SQL module: sql_module_1")
                "sql_module_1"
            }
        }
        
        // Get questions for the determined module
        val moduleQuestions = quizzesByModule[sqlModuleId]
        Log.d("SqlModuleQuizRepository", "Using $sqlModuleId questions as fallback for $moduleId")
        
        return moduleQuestions?.take(10) ?: emptyList()
    }

    override fun getQuestionCountForModule(moduleId: String): Int {
        // Try to get questions for the specified module ID
        val questions = quizzesByModule[moduleId]
        
        // If exact match is found, return the count
        if (questions != null) {
            return minOf(questions.size, 10)
        }
        
        // No exact match - determine the appropriate SQL module
        val sqlModuleId = when {
            // If it's a recognized SQL module format
            moduleId.matches(Regex("sql_module_[1-7].*")) -> {
                val parsedId = moduleId.substringBefore(".")
                if (quizzesByModule.containsKey(parsedId)) parsedId else "sql_module_1"
            }
            // If it's the old format (module_1)
            moduleId.matches(Regex("module_[1-7]")) -> {
                val number = moduleId.substringAfter("_")
                "sql_module_$number"
            }
            // Default to first SQL module
            else -> "sql_module_1"
        }
        
        // Get count for the determined module
        val moduleQuestions = quizzesByModule[sqlModuleId]
        return minOf(moduleQuestions?.size ?: 0, 10)
    }
    

    override fun getAllModuleIds(): List<String> {
        return quizzesByModule.keys.toList()
    }


    /**
     * Checks if this repository can handle questions for the given module ID
     * @param moduleId The ID of the module to check
     * @return true if this repository can handle the module, false otherwise
     */
    override fun canHandleModule(moduleId: String): Boolean {
        // Can handle if it's an exact match
        if (quizzesByModule.containsKey(moduleId)) {
            return true
        }
        
        // Explicitly exclude single-digit IDs which are Python modules
        if (moduleId.matches(Regex("^[1-7]$"))) {
            return false
        }
        
        // Can handle SQL modules or anything with "sql" in the module ID
        return moduleId.matches(Regex("sql_module_[1-7]")) || // Exact match for sql_module_1 through 7
               moduleId.matches(Regex("^module_[1-7]$")) ||   // Exact match for legacy module_1 through 7
               moduleId.contains("sql", ignoreCase = true)
    }
} 