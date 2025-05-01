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
     * Get all quiz questions for a specific module, limited to 10 questions
     * @param moduleId The ID of the module
     * @return List of Quiz objects for the module, or fallback questions if module not found
     */
    fun getQuestionsForModule(moduleId: String): List<Quiz> {
        Log.d("ModuleQuizRepository", "Getting questions for moduleId: $moduleId")
        
        // Try to get questions with exact module ID match
        val questions = quizzesByModule[moduleId]
        
        // If no questions found for this ID, use Python fallback questions
        val result = if (questions.isNullOrEmpty()) {
            Log.d("ModuleQuizRepository", "No questions found for moduleId: $moduleId, using fallback")
            quizzesByModule["python"] ?: emptyList()
        } else {
            Log.d("ModuleQuizRepository", "Found ${questions.size} questions for moduleId: $moduleId")
            questions
        }
        
        // Limit to 10 questions max
        return result.take(10)
    }
    
    /**
     * Get the total number of questions for a module
     * @param moduleId The ID of the module
     * @return Number of questions (max 10)
     */
    fun getQuestionCountForModule(moduleId: String): Int {
        val count = quizzesByModule[moduleId]?.size ?: 0
        return count.coerceAtMost(10)
    }
    
    /**
     * Get a list of all module IDs
     * @return List of module IDs
     */
    fun getAllModuleIds(): List<String> {
        return quizzesByModule.keys.toList()
    }
} 