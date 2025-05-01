package com.labactivity.lala.quiz

import android.util.Log

/**
 * Repository that manages quiz questions for different modules
 */
class ModuleQuizRepository {
    
    // Map of module IDs to lists of quiz questions
    private val quizzesByModule: Map<String, List<Quiz>> = mapOf(
        // Module 1: Fundamentals of Python
        "1" to listOf(
            Quiz(
                id = "1.1",
                question = "What is the correct way to create a variable in Python?",
                options = listOf(
                    "var x = 5",
                    "x = 5",
                    "let x = 5",
                    "int x = 5"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.2",
                question = "Which of the following is a valid Python comment?",
                options = listOf(
                    "// This is a comment",
                    "/* This is a comment */",
                    "# This is a comment",
                    "<!-- This is a comment -->"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.3",
                question = "What is the output of print(type(3.14))?",
                options = listOf(
                    "<class 'int'>",
                    "<class 'float'>",
                    "<class 'double'>",
                    "<class 'decimal'>"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "1.4",
                question = "Which function is used to get the length of a list in Python?",
                options = listOf(
                    "size()",
                    "length()",
                    "count()",
                    "len()"
                ),
                correctOptionIndex = 3,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.5",
                question = "What will print(10 / 3) output in Python 3?",
                options = listOf(
                    "3",
                    "3.0",
                    "3.3333333333333335",
                    "3.33"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            )
        ),
        
        // Module 2: Control Flow in Python
        "2" to listOf(
            Quiz(
                id = "2.1",
                question = "Which of the following is a valid if statement in Python?",
                options = listOf(
                    "if (x > 5) { print('x is greater than 5') }",
                    "if x > 5: print('x is greater than 5')",
                    "if x > 5 then print('x is greater than 5')",
                    "if (x > 5): { print('x is greater than 5') }"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "2.2",
                question = "Which loop will execute at least once regardless of the condition?",
                options = listOf(
                    "for loop",
                    "while loop",
                    "do-while loop",
                    "None of the above"
                ),
                correctOptionIndex = 3, // Python doesn't have do-while loops
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "2.3",
                question = "What does the 'break' statement do in Python?",
                options = listOf(
                    "Skips the current iteration and continues with the next",
                    "Exits the current function",
                    "Exits the loop completely",
                    "Terminates the program"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "2.4",
                question = "What is the output of the following code?\nfor i in range(3): print(i)",
                options = listOf(
                    "1 2 3",
                    "0 1 2",
                    "0 1 2 3",
                    "1 2"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "2.5",
                question = "Which of the following is TRUE about Python's 'else' clause in loops?",
                options = listOf(
                    "It is executed when the loop condition becomes false",
                    "It is executed when a break statement is encountered",
                    "It is executed when the loop completes without a break",
                    "Python loops don't have else clauses"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.HARD
            )
        ),
        
        // Module 3: Data Structures in Python
        "3" to listOf(
            Quiz(
                id = "3.1",
                question = "Which of the following is a mutable data structure in Python?",
                options = listOf(
                    "Tuple",
                    "String",
                    "List",
                    "None of the above"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "3.2",
                question = "How do you create an empty dictionary in Python?",
                options = listOf(
                    "dict()",
                    "{}",
                    "[]",
                    "Both A and B"
                ),
                correctOptionIndex = 3,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "3.3",
                question = "What will be the output of: list('Python')?",
                options = listOf(
                    "['Python']",
                    "['P', 'y', 't', 'h', 'o', 'n']",
                    "Error",
                    "None of the above"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "3.4",
                question = "Which of the following is used to add an element to a set?",
                options = listOf(
                    "append()",
                    "insert()",
                    "add()",
                    "update()"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "3.5",
                question = "What is the output of: [1, 2, 3] + [4, 5, 6]?",
                options = listOf(
                    "[1, 2, 3, 4, 5, 6]",
                    "[5, 7, 9]",
                    "Error",
                    "[1, 2, 3, [4, 5, 6]]"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            )
        ),

        // Java Module IDs: map from java_module_1, java_module_2, etc.
        "java_module_1" to listOf(
            Quiz(
                id = "java_1.1",
                question = "What is Java?",
                options = listOf(
                    "A programming language",
                    "A coffee type",
                    "An island in Indonesia",
                    "All of the above"
                ),
                correctOptionIndex = 3,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "java_1.2",
                question = "Which of these is NOT a Java feature?",
                options = listOf(
                    "Object-Oriented",
                    "Interpreted",
                    "Dynamic typing",
                    "Platform Independent"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_1.3",
                question = "What is the correct way to declare a variable in Java?",
                options = listOf(
                    "var x = 10;",
                    "int x = 10;",
                    "x = 10;",
                    "Integer x = 10;"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "java_1.4",
                question = "What is the Java Virtual Machine (JVM)?",
                options = listOf(
                    "A physical computer for running Java",
                    "An abstract computing machine that enables Java platform independence",
                    "A compiler that converts Java to machine code",
                    "A tool that checks Java syntax"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_1.5",
                question = "What is the main method signature in Java?",
                options = listOf(
                    "public void main()",
                    "public static void main()",
                    "public static void main(String[] args)",
                    "public static void main(String args)"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            )
        ),

        "java_module_2" to listOf(
            Quiz(
                id = "java_2.1",
                question = "What is an if-else statement in Java?",
                options = listOf(
                    "A looping construct",
                    "A conditional statement",
                    "An error handling mechanism",
                    "A data type"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "java_2.2",
                question = "Which loop construct guarantees that the loop body will execute at least once?",
                options = listOf(
                    "for loop",
                    "while loop",
                    "do-while loop",
                    "foreach loop"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_2.3",
                question = "What is the correct syntax for a Java switch statement?",
                options = listOf(
                    "switch (expression) { case value: ... }",
                    "switch expression { case value: ... }",
                    "switch (expression) { case (value): ... }",
                    "switch expression { case (value): ... }"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_2.4",
                question = "Which statement is used to exit a loop in Java?",
                options = listOf(
                    "exit;",
                    "goto;",
                    "break;",
                    "stop;"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "java_2.5",
                question = "What is the output of this code: for(int i=0; i<5; i++) { System.out.print(i); }",
                options = listOf(
                    "0123",
                    "01234",
                    "1234",
                    "12345"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            )
        ),

        // SQL Module IDs
        "module_1" to listOf(
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
            )
        ),
        
        // Fallback module with Python questions (used if module ID doesn't match any specific module)
        "python" to listOf(
            Quiz(
                id = "p.1",
                question = "What is Python?",
                options = listOf(
                    "A type of snake",
                    "A high-level programming language",
                    "A database system",
                    "A web framework"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "p.2",
                question = "Which of these is NOT a Python data type?",
                options = listOf(
                    "List",
                    "Dictionary",
                    "Array",
                    "Tuple"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "p.3",
                question = "What will len([1, 2, 3]) return?",
                options = listOf(
                    "1",
                    "2",
                    "3",
                    "4"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "p.4",
                question = "How do you create a function in Python?",
                options = listOf(
                    "function myFunc():",
                    "def myFunc():",
                    "create myFunc():",
                    "func myFunc():"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "p.5",
                question = "What is the result of 3 + 2 * 2?",
                options = listOf(
                    "10",
                    "7",
                    "8",
                    "5"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            )
        )
    )
    
    /**
     * Gets quiz questions for a specific module
     * @param moduleId The ID of the module
     * @return List of Quiz objects for the specified module (limited to 10)
     */
    fun getQuestionsForModule(moduleId: String): List<Quiz> {
        Log.d("ModuleQuizRepository", "Getting questions for module ID: $moduleId")
        
        // Try to get questions for the specified module ID
        val questions = quizzesByModule[moduleId]
        
        // If no questions are found for this module ID, try using it as a prefix match
        val fallbackQuestions = if (questions == null) {
            // Look for modules that start with the given ID
            quizzesByModule.entries.find { (key, _) -> key.startsWith(moduleId) || moduleId.startsWith(key) }?.value
        } else null
        
        // If still no questions are found, return python questions as a fallback
        return when {
            questions != null -> {
                Log.d("ModuleQuizRepository", "Found ${questions.size} questions for module ID: $moduleId")
                questions.take(10) // Limit to 10 questions
            }
            fallbackQuestions != null -> {
                Log.d("ModuleQuizRepository", "Using fallback questions by prefix match for module ID: $moduleId")
                fallbackQuestions.take(10) // Limit to 10 questions
            }
            else -> {
                Log.d("ModuleQuizRepository", "No questions found for module ID: $moduleId, using Python fallback")
                quizzesByModule["python"]?.take(10) ?: emptyList()
            }
        }
    }
    
    /**
     * Gets the total number of questions available for a module
     * @param moduleId The ID of the module
     * @return The number of questions available, limited to a maximum of 10
     */
    fun getQuestionCountForModule(moduleId: String): Int {
        // Try to get questions for the specified module ID
        val questions = quizzesByModule[moduleId]
        
        // If no questions are found for this module ID, try using it as a prefix match
        val fallbackQuestions = if (questions == null) {
            // Look for modules that start with the given ID
            quizzesByModule.entries.find { (key, _) -> key.startsWith(moduleId) || moduleId.startsWith(key) }?.value
        } else null
        
        val count = when {
            questions != null -> questions.size
            fallbackQuestions != null -> fallbackQuestions.size
            else -> quizzesByModule["python"]?.size ?: 0
        }
        
        return minOf(count, 10) // Limit to 10 questions
    }
    
    /**
     * Get a list of all module IDs
     * @return List of module IDs
     */
    fun getAllModuleIds(): List<String> {
        return quizzesByModule.keys.toList()
    }
} 