package com.labactivity.lala.quiz

import android.util.Log

/**
 * Repository that manages Java quiz questions
 */
class JavaModuleQuizRepository : QuizRepository {
    
    // Map of module IDs to lists of quiz questions
    private val quizzesByModule: Map<String, List<Quiz>> = mapOf(
        // Module 1: Java Fundamentals
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

        // Module 2: Java Control Flow
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
        
        // Module 3: Java OOP
        "java_module_3" to listOf(
            Quiz(
                id = "java_3.1",
                question = "What is inheritance in Java?",
                options = listOf(
                    "A way to create multiple instances of a class",
                    "A mechanism where a class inherits properties and behaviors from another class",
                    "A way to hide implementation details",
                    "A feature to override methods"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_3.2",
                question = "What is the difference between an interface and an abstract class in Java?",
                options = listOf(
                    "An interface can have method implementations, an abstract class cannot",
                    "An abstract class can have constructor, an interface cannot",
                    "An interface can be instantiated, an abstract class cannot",
                    "An abstract class can extend multiple classes, an interface cannot"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.HARD
            ),
            Quiz(
                id = "java_3.3",
                question = "Which keyword is used to prevent a class from being inherited in Java?",
                options = listOf(
                    "static",
                    "final",
                    "private",
                    "sealed"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_3.4",
                question = "What is method overloading in Java?",
                options = listOf(
                    "Defining multiple methods with the same name but different parameters",
                    "Redefining a method in a subclass with the same signature",
                    "A method that calls itself recursively",
                    "A method that can take variable number of arguments"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_3.5",
                question = "What is encapsulation in Java?",
                options = listOf(
                    "The ability to create multiple instances of a class",
                    "Hiding implementation details and exposing only functionality",
                    "Creating a subclass of a class",
                    "Creating multiple classes in a single file"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_3.6",
                question = "Which access modifier makes a class member accessible only within its own class?",
                options = listOf(
                    "public",
                    "protected",
                    "private",
                    "default"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "java_3.7",
                question = "What is polymorphism in Java?",
                options = listOf(
                    "The ability of a class to have multiple constructors",
                    "The ability to define multiple classes within a single file",
                    "The ability of an object to take on many forms",
                    "The ability to hide class members"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_3.8",
                question = "What is a constructor in Java?",
                options = listOf(
                    "A method used to destroy objects",
                    "A special method used to initialize objects",
                    "A method that returns the class type",
                    "A method that must be overridden in subclasses"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "java_3.9",
                question = "What happens if you don't define a constructor in a Java class?",
                options = listOf(
                    "Compilation error occurs",
                    "Java provides a default no-argument constructor",
                    "The class cannot be instantiated",
                    "You must use the factory pattern to create objects"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "java_3.10",
                question = "What is the 'super' keyword used for in Java?",
                options = listOf(
                    "To access superclass members",
                    "To declare a variable with higher priority",
                    "To create a new instance of the current class",
                    "To declare a class with enhanced features"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            )
        )
    )
    

    override fun getQuestionsForModule(moduleId: String): List<Quiz> {
        Log.d("JavaModuleQuizRepository", "Getting questions for module ID: $moduleId")
        
        // Try to get questions for the specified module ID
        val questions = quizzesByModule[moduleId]
        
        // If exact match is found, return those questions
        if (questions != null) {
            Log.d("JavaModuleQuizRepository", "Found ${questions.size} questions for module ID: $moduleId")
            return questions.take(10) // Limit to 10 questions
        }
        
        // No exact match - determine the appropriate Java module
        val javaModuleId = when {
            // If it contains "java_module_" followed by a number
            moduleId.matches(Regex("java_module_[1-7].*")) -> {
                val parsedId = moduleId.substringBefore(".")
                Log.d("JavaModuleQuizRepository", "Parsed Java module ID: $parsedId")
                if (quizzesByModule.containsKey(parsedId)) parsedId else "java_module_1"
            }
            // Default to first Java module
            else -> {
                Log.d("JavaModuleQuizRepository", "Using default Java module: java_module_1")
                "java_module_1"
            }
        }
        
        // Get questions for the determined module
        val moduleQuestions = quizzesByModule[javaModuleId]
        Log.d("JavaModuleQuizRepository", "Using $javaModuleId questions as fallback for $moduleId")
        
        return moduleQuestions?.take(10) ?: emptyList()
    }

    override fun getQuestionCountForModule(moduleId: String): Int {
        // Try to get questions for the specified module ID
        val questions = quizzesByModule[moduleId]
        
        // If exact match is found, return the count
        if (questions != null) {
            return minOf(questions.size, 10)
        }
        
        // No exact match - determine the appropriate Java module
        val javaModuleId = when {
            // If it contains "java_module_" followed by a number
            moduleId.matches(Regex("java_module_[1-7].*")) -> {
                val parsedId = moduleId.substringBefore(".")
                if (quizzesByModule.containsKey(parsedId)) parsedId else "java_module_1"
            }
            // Default to first Java module
            else -> "java_module_1"
        }
        
        // Get count for the determined module
        val moduleQuestions = quizzesByModule[javaModuleId]
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
        // Can handle if it's an exact match
        if (quizzesByModule.containsKey(moduleId)) {
            return true
        }
        
        // Can handle Java modules or anything with "java" in the module ID
        return moduleId.matches(Regex("java_module_[1-7]")) || // Exact match for java_module_1 through 7
               moduleId.contains("java", ignoreCase = true)
    }
} 