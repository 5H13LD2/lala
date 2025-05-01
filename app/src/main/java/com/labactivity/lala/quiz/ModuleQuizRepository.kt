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
            ),
            Quiz(
                id = "1.6",
                question = "Which operator is used for exponentiation in Python?",
                options = listOf(
                    "^",
                    "**",
                    "++",
                    "*^"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.7",
                question = "What will print(\"Hello\" + \"World\") output?",
                options = listOf(
                    "Hello World",
                    "HelloWorld",
                    "Hello + World",
                    "Error"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.8",
                question = "What is the correct way to create a multi-line string in Python?",
                options = listOf(
                    "Using single quotes (\'...\')",
                    "Using double quotes (\"...\")",
                    "Using triple quotes (\'\'\'...\'\'\')",
                    "Using backslashes (\\n)"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "1.9",
                question = "How do you convert a string to an integer in Python?",
                options = listOf(
                    "int(str)",
                    "toString(int)",
                    "str.toInt()",
                    "Integer.parse(str)"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.10",
                question = "Which of the following is True in Python?",
                options = listOf(
                    "None == 0",
                    "1 == True",
                    "0 == True",
                    "False == None"
                ),
                correctOptionIndex = 1,
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
            ),
            Quiz(
                id = "2.6",
                question = "What does the 'continue' statement do in Python?",
                options = listOf(
                    "Skips the current iteration and continues with the next",
                    "Continues to the next function",
                    "Exits the loop completely",
                    "Pauses the loop execution"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "2.7",
                question = "Which statement is used to handle exceptions in Python?",
                options = listOf(
                    "catch",
                    "try",
                    "except",
                    "handle"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "2.8",
                question = "What is the output of the following code?\nif True: print('A'); else: print('B')",
                options = listOf(
                    "A",
                    "B",
                    "AB",
                    "Error"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "2.9",
                question = "Which of the following is NOT a valid loop in Python?",
                options = listOf(
                    "while loop",
                    "for loop",
                    "do-while loop",
                    "foreach loop"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "2.10",
                question = "What will the following code output?\nx = 5; y = 10; print('Yes' if x > y else 'No')",
                options = listOf(
                    "Yes",
                    "No",
                    "Error",
                    "None"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
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
            ),
            Quiz(
                id = "3.6",
                question = "What does the pop() method do for a dictionary in Python?",
                options = listOf(
                    "Removes the last element",
                    "Removes an element with a specific key and returns its value",
                    "Removes all elements",
                    "Sorts the dictionary"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "3.7",
                question = "Which data structure in Python uses keys and values?",
                options = listOf(
                    "List",
                    "Tuple",
                    "Set",
                    "Dictionary"
                ),
                correctOptionIndex = 3,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "3.8",
                question = "What will be the output of: set([1, 2, 3, 2, 1])?",
                options = listOf(
                    "{1, 2, 3, 2, 1}",
                    "{1, 2, 3}",
                    "Error",
                    "[1, 2, 3]"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "3.9",
                question = "Which method would you use to sort a list in Python?",
                options = listOf(
                    "list.sort()",
                    "sorted(list)",
                    "Both A and B",
                    "list.order()"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "3.10",
                question = "What is a frozenset in Python?",
                options = listOf(
                    "A set that can't be modified after creation",
                    "A set that contains only immutable elements",
                    "A set with numerical values only",
                    "A set that's automatically sorted"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.HARD
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
            ),
            Quiz(
                id = "java_1.6",
                question = "Which Java keyword is used to create a class?",
                options = listOf(
                    "struct",
                    "class",
                    "interface",
                    "object"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "java_1.7",
                question = "What is the correct way to create an object in Java?",
                options = listOf(
                    "MyClass obj = MyClass();",
                    "MyClass obj = new MyClass;",
                    "MyClass obj = new MyClass();",
                    "new MyClass obj();"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "java_1.8",
                question = "Which of the following is a primitive data type in Java?",
                options = listOf(
                    "String",
                    "Array",
                    "int",
                    "Object"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "java_1.9",
                question = "What is the size of int data type in Java?",
                options = listOf(
                    "8 bits",
                    "16 bits",
                    "32 bits",
                    "64 bits"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_1.10",
                question = "Which of the following declarations is incorrect?",
                options = listOf(
                    "int[] arr = new int[5];",
                    "int arr[] = new int[5];",
                    "int arr[] = new int[];",
                    "int[] arr = {1, 2, 3, 4, 5};"
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
            ),
            Quiz(
                id = "java_2.6",
                question = "What will happen if you try to compile and run this code: while(true) { System.out.println(\"Hello\"); }",
                options = listOf(
                    "It will print 'Hello' once",
                    "It will print 'Hello' infinite times",
                    "Compilation error",
                    "Runtime error"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "java_2.7",
                question = "Which of the following is NOT a valid loop in Java?",
                options = listOf(
                    "for loop",
                    "foreach loop",
                    "while loop",
                    "until loop"
                ),
                correctOptionIndex = 3,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "java_2.8",
                question = "What is the enhanced for loop in Java?",
                options = listOf(
                    "A loop that only works with arrays",
                    "A loop that automatically increments the counter",
                    "A loop that iterates through elements of an array or collection",
                    "A loop that can have multiple counters"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_2.9",
                question = "What happens if break is not used in a case of a switch statement?",
                options = listOf(
                    "Compilation error",
                    "Runtime error",
                    "Falls through to the next case",
                    "Switch exits automatically"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_2.10",
                question = "Which statement is used for exception handling in Java?",
                options = listOf(
                    "if-else",
                    "switch-case",
                    "try-catch",
                    "for-while"
                ),
                correctOptionIndex = 2,
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
            ),
            Quiz(
                id = "p.6",
                question = "Which of the following is used to define a block of code in Python?",
                options = listOf(
                    "Curly braces {}",
                    "Parentheses ()",
                    "Indentation",
                    "Square brackets []"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "p.7",
                question = "What will be the output of 'Hello' + 'World'?",
                options = listOf(
                    "Hello World",
                    "HelloWorld",
                    "Hello + World",
                    "Error"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "p.8",
                question = "Which Python data type is mutable?",
                options = listOf(
                    "String",
                    "Tuple",
                    "List",
                    "int"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "p.9",
                question = "What will be the output of 5 // 2?",
                options = listOf(
                    "2.5",
                    "2",
                    "3",
                    "2.0"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "p.10",
                question = "What is the Python package manager called?",
                options = listOf(
                    "npm",
                    "pip",
                    "pkg",
                    "pypm"
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
        
        // If exact match is found, return those questions
        if (questions != null) {
            Log.d("ModuleQuizRepository", "Found ${questions.size} questions for module ID: $moduleId")
            return questions.take(10) // Limit to 10 questions
        }
        
        // No exact match - determine the module type based on ID prefix
        val moduleType = when {
            // Python core modules (numbered 1, 2, 3)
            moduleId.matches(Regex("^[1-3].*$")) -> {
                val pythonModuleId = moduleId.substringBefore(".")
                Log.d("ModuleQuizRepository", "Identified as Python module $pythonModuleId")
                if (quizzesByModule.containsKey(pythonModuleId)) pythonModuleId else "python"
            }
            
            // Java modules
            moduleId.contains("java", ignoreCase = true) -> {
                Log.d("ModuleQuizRepository", "Identified as Java module")
                "java_module_1"  // Default to first Java module
            }
            
            // SQL modules
            moduleId.contains("sql", ignoreCase = true) -> {
                Log.d("ModuleQuizRepository", "Identified as SQL module")
                "module_1"  // Default to first SQL module
            }
            
            // Default fallback
            else -> {
                Log.d("ModuleQuizRepository", "Module type unknown, using Python fallback")
                "python"
            }
        }
        
        // Get questions for the determined module type
        val moduleQuestions = quizzesByModule[moduleType]
        Log.d("ModuleQuizRepository", "Using $moduleType questions as fallback for $moduleId")
        
        return moduleQuestions?.take(10) ?: emptyList()
    }
    
    /**
     * Gets the total number of questions available for a module
     * @param moduleId The ID of the module
     * @return The number of questions available, limited to a maximum of 10
     */
    fun getQuestionCountForModule(moduleId: String): Int {
        // Try to get questions for the specified module ID
        val questions = quizzesByModule[moduleId]
        
        // If exact match is found, return the count
        if (questions != null) {
            return minOf(questions.size, 10)
        }
        
        // No exact match - determine the module type based on ID prefix
        val moduleType = when {
            // Python core modules (numbered 1, 2, 3)
            moduleId.matches(Regex("^[1-3].*$")) -> {
                val pythonModuleId = moduleId.substringBefore(".")
                if (quizzesByModule.containsKey(pythonModuleId)) pythonModuleId else "python"
            }
            
            // Java modules
            moduleId.contains("java", ignoreCase = true) -> "java_module_1"
            
            // SQL modules
            moduleId.contains("sql", ignoreCase = true) -> "module_1"
            
            // Default fallback
            else -> "python"
        }
        
        // Get count for the determined module type
        val moduleQuestions = quizzesByModule[moduleType]
        return minOf(moduleQuestions?.size ?: 0, 10)
    }
    
    /**
     * Get a list of all module IDs
     * @return List of module IDs
     */
    fun getAllModuleIds(): List<String> {
        return quizzesByModule.keys.toList()
    }
} 