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
            Quiz("java_1.1", "What is Java?", listOf("A programming language", "A coffee type", "An island in Indonesia", "All of the above"), 3, Difficulty.EASY),
            Quiz("java_1.2", "Which of these is NOT a Java feature?", listOf("Object-Oriented", "Interpreted", "Dynamic typing", "Platform Independent"), 2, Difficulty.NORMAL),
            Quiz("java_1.3", "What is the correct way to declare a variable in Java?", listOf("var x = 10;", "int x = 10;", "x = 10;", "Integer x = 10;"), 1, Difficulty.EASY),
            Quiz("java_1.4", "What is the Java Virtual Machine (JVM)?", listOf("A physical computer for running Java", "An abstract computing machine that enables Java platform independence", "A compiler that converts Java to machine code", "A tool that checks Java syntax"), 1, Difficulty.NORMAL),
            Quiz("java_1.5", "What is the main method signature in Java?", listOf("public void main()", "public static void main()", "public static void main(String[] args)", "public static void main(String args)"), 2, Difficulty.NORMAL),
            Quiz("java_1.6", "Which Java keyword is used to create a class?", listOf("struct", "class", "interface", "object"), 1, Difficulty.EASY),
            Quiz("java_1.7", "What is the correct way to create an object in Java?", listOf("MyClass obj = MyClass();", "MyClass obj = new MyClass;", "MyClass obj = new MyClass();", "new MyClass obj();"), 2, Difficulty.EASY),
            Quiz("java_1.8", "Which of the following is a primitive data type in Java?", listOf("String", "Array", "int", "Object"), 2, Difficulty.EASY),
            Quiz("java_1.9", "What is the size of int data type in Java?", listOf("8 bits", "16 bits", "32 bits", "64 bits"), 2, Difficulty.NORMAL),
            Quiz("java_1.10", "Which of the following declarations is incorrect?", listOf("int[] arr = new int[5];", "int arr[] = new int[5];", "int arr[] = new int[];", "int[] arr = {1, 2, 3, 4, 5};"), 2, Difficulty.NORMAL)
        ),

        // Module 2: Java Control Flow
        "java_module_2" to listOf(
            Quiz("java_2.1", "What is an if-else statement in Java?", listOf("A looping construct", "A conditional statement", "An error handling mechanism", "A data type"), 1, Difficulty.EASY),
            Quiz("java_2.2", "Which loop construct guarantees that the loop body will execute at least once?", listOf("for loop", "while loop", "do-while loop", "foreach loop"), 2, Difficulty.NORMAL),
            Quiz("java_2.3", "What is the correct syntax for a Java switch statement?", listOf("switch (expression) { case value: ... }", "switch expression { case value: ... }", "switch (expression) { case (value): ... }", "switch expression { case (value): ... }"), 0, Difficulty.NORMAL),
            Quiz("java_2.4", "Which statement is used to exit a loop in Java?", listOf("exit;", "goto;", "break;", "stop;"), 2, Difficulty.EASY),
            Quiz("java_2.5", "What is the output of this code: for(int i=0; i<5; i++) { System.out.print(i); }", listOf("0123", "01234", "1234", "12345"), 1, Difficulty.NORMAL),
            Quiz("java_2.6", "What will happen if you try to compile and run this code: while(true) { System.out.println(\"Hello\"); }", listOf("It will print 'Hello' once", "It will print 'Hello' infinite times", "Compilation error", "Runtime error"), 1, Difficulty.EASY),
            Quiz("java_2.7", "Which of the following is NOT a valid loop in Java?", listOf("for loop", "foreach loop", "while loop", "until loop"), 3, Difficulty.EASY),
            Quiz("java_2.8", "What is the enhanced for loop in Java?", listOf("A loop that only works with arrays", "A loop that automatically increments the counter", "A loop that iterates through elements of an array or collection", "A loop that can have multiple counters"), 2, Difficulty.NORMAL),
            Quiz("java_2.9", "What happens if break is not used in a case of a switch statement?", listOf("Compilation error", "Runtime error", "Falls through to the next case", "Switch exits automatically"), 2, Difficulty.NORMAL),
            Quiz("java_2.10", "Which statement is used for exception handling in Java?", listOf("if-else", "switch-case", "try-catch", "for-while"), 2, Difficulty.NORMAL)
        ),
        
        // Module 3: Java OOP
        "java_module_3" to listOf(
            Quiz("java_3.1", "What is inheritance in Java?", listOf("A way to create multiple instances of a class", "A mechanism where a class inherits properties and behaviors from another class", "A way to hide implementation details", "A feature to override methods"), 1, Difficulty.NORMAL),
            Quiz("java_3.2", "What is the difference between an interface and an abstract class in Java?", listOf("An interface can have method implementations, an abstract class cannot", "An abstract class can have constructor, an interface cannot", "An interface can be instantiated, an abstract class cannot", "An abstract class can extend multiple classes, an interface cannot"), 1, Difficulty.HARD),
            Quiz("java_3.3", "Which keyword is used to prevent a class from being inherited in Java?", listOf("static", "final", "private", "sealed"), 1, Difficulty.NORMAL),
            Quiz("java_3.4", "What is method overloading in Java?", listOf("Defining multiple methods with the same name but different parameters", "Redefining a method in a subclass with the same signature", "A method that calls itself recursively", "A method that can take variable number of arguments"), 0, Difficulty.NORMAL),
            Quiz("java_3.5", "What is encapsulation in Java?", listOf("The ability to create multiple instances of a class", "Hiding implementation details and exposing only functionality", "Creating a subclass of a class", "Creating multiple classes in a single file"), 1, Difficulty.NORMAL),
            Quiz("java_3.6", "Which access modifier makes a class member accessible only within its own class?", listOf("public", "protected", "private", "default"), 2, Difficulty.EASY),
            Quiz("java_3.7", "What is polymorphism in Java?", listOf("The ability of a class to have multiple constructors", "The ability to define multiple classes within a single file", "The ability of an object to take on many forms", "The ability to hide class members"), 2, Difficulty.NORMAL),
            Quiz("java_3.8", "What is a constructor in Java?", listOf("A method used to destroy objects", "A special method used to initialize objects", "A method that returns the class type", "A method that must be overridden in subclasses"), 1, Difficulty.EASY),
            Quiz("java_3.9", "What happens if you don't define a constructor in a Java class?", listOf("Compilation error occurs", "Java provides a default no-argument constructor", "The class cannot be instantiated", "You must use the factory pattern to create objects"), 1, Difficulty.NORMAL),
            Quiz("java_3.10", "What is the 'super' keyword used for in Java?", listOf("To access superclass members", "To declare a variable with higher priority", "To create a new instance of the current class", "To declare a class with enhanced features"), 0, Difficulty.NORMAL)
        ),
        "java_module_4" to listOf(
            Quiz(id = "java_4.1", question = "Which of the following correctly declares an array of integers in Java?", options = listOf("int numbers = [1, 2, 3];", "int[] numbers = {1, 2, 3};", "int numbers[] = (1, 2, 3);", "array int numbers = 1,2,3;"), correctOptionIndex = 1, difficulty = Difficulty.EASY),
            Quiz(id = "java_4.2", question = "What is the correct way to declare a 2D array in Java?", options = listOf("int matrix[][] = new int[3][3];", "int matrix[3][3];", "int[][] matrix = (3,3);", "2D int matrix = new int[3][3];"), correctOptionIndex = 0, difficulty = Difficulty.NORMAL),
            Quiz(id = "java_4.3", question = "Which method returns the length of a string in Java?", options = listOf("length()", "getSize()", "size()", "count()"), correctOptionIndex = 0, difficulty = Difficulty.EASY),
            Quiz(id = "java_4.4", question = "What does the method substring(0, 3) return if the string is 'Hello'?", options = listOf("Hel", "Hell", "Hello", "lo"), correctOptionIndex = 0, difficulty = Difficulty.NORMAL),
            Quiz(id = "java_4.5", question = "How do you access the third element of an array named 'nums'?", options = listOf("nums[3]", "nums(3)", "nums[2]", "nums{3}"), correctOptionIndex = 2, difficulty = Difficulty.EASY),
            Quiz(id = "java_4.6", question = "What happens if you access an index outside the bounds of an array?", options = listOf("Returns 0", "Returns null", "Compiles but fails silently", "Throws ArrayIndexOutOfBoundsException"), correctOptionIndex = 3, difficulty = Difficulty.NORMAL),
            Quiz(id = "java_4.7", question = "How do you convert a string to uppercase in Java?", options = listOf("toUpperCase()", "uppercase()", "strUpper()", "capitalize()"), correctOptionIndex = 0, difficulty = Difficulty.EASY),
            Quiz(id = "java_4.8", question = "Which method compares two strings for equality in Java?", options = listOf("equals()", "==", "compare()", "match()"), correctOptionIndex = 0, difficulty = Difficulty.NORMAL),
            Quiz(id = "java_4.9", question = "Which loop is best for iterating through all elements in an array?", options = listOf("do-while", "for-each", "while", "goto"), correctOptionIndex = 1, difficulty = Difficulty.EASY),
            Quiz(id = "java_4.10", question = "Which of these is a valid string declaration?", options = listOf("String str = 'hello';", "String str = hello;", "String str = \"hello\";", "str = new String(hello);"), correctOptionIndex = 2, difficulty = Difficulty.EASY)
        ),
        "java_module_5" to listOf(
            Quiz(id = "java_5.1", question = "What happens when an exception is not caught in a try-catch block?", options = listOf("Program continues normally", "Program crashes", "It is ignored", "Java automatically fixes it"), correctOptionIndex = 1, difficulty = Difficulty.EASY),
            Quiz(id = "java_5.2", question = "Which block is always executed after try-catch, regardless of an exception?", options = listOf("catch", "handle", "finally", "done"), correctOptionIndex = 2, difficulty = Difficulty.EASY),
            Quiz(id = "java_5.3", question = "Which keyword is used to throw an exception manually in Java?", options = listOf("throws", "throw", "catch", "raise"), correctOptionIndex = 1, difficulty = Difficulty.NORMAL),
            Quiz(id = "java_5.4", question = "How do you create a custom exception class?", options = listOf("class MyException implements Exception", "class MyException inherits Exception", "class MyException extends Exception", "exception MyException {}"), correctOptionIndex = 2, difficulty = Difficulty.NORMAL),
            Quiz(id = "java_5.5", question = "Which is a checked exception?", options = listOf("NullPointerException", "IOException", "ArithmeticException", "ArrayIndexOutOfBoundsException"), correctOptionIndex = 1, difficulty = Difficulty.NORMAL),
            Quiz(id = "java_5.6", question = "What is the superclass of all exceptions in Java?", options = listOf("Error", "Exception", "Throwable", "RuntimeException"), correctOptionIndex = 2, difficulty = Difficulty.NORMAL),
            Quiz(id = "java_5.7", question = "What type of exception is caught using `catch (ArithmeticException e)`?", options = listOf("Division by zero", "Null pointer", "File not found", "Syntax error"), correctOptionIndex = 0, difficulty = Difficulty.EASY),
            Quiz(id = "java_5.8", question = "Which method can be used to get the error message from an exception?", options = listOf("getMessage()", "errorMessage()", "toString()", "printMessage()"), correctOptionIndex = 0, difficulty = Difficulty.NORMAL),
            Quiz(id = "java_5.9", question = "Can multiple catch blocks be used with a single try block?", options = listOf("Yes", "No", "Only two", "Only if nested"), correctOptionIndex = 0, difficulty = Difficulty.EASY),
            Quiz(id = "java_5.10", question = "What is the purpose of a try-catch block?", options = listOf("To terminate the program", "To catch compilation errors", "To handle runtime exceptions", "To ignore exceptions"), correctOptionIndex = 2, difficulty = Difficulty.EASY)
        ),
        "java_module_6" to listOf(
            Quiz("java_6.1", "What is a class in Java?",
                listOf("A variable", "A method", "A blueprint for objects", "An instance of an object"), 2, Difficulty.EASY),
            Quiz("java_6.2", "What does inheritance allow in Java?",
                listOf("Storing multiple data types", "Creating child classes from parent classes", "Overriding exceptions", "None of the above"), 1, Difficulty.EASY),
            Quiz("java_6.3", "Polymorphism allows:",
                listOf("One class to inherit from multiple classes", "Methods to perform different tasks based on input", "Multiple methods with same name and same parameters", "Variables to change type"), 1, Difficulty.NORMAL),
            Quiz("java_6.4", "Which of the following is true about encapsulation?",
                listOf("It hides internal state using private access", "It shows implementation details to users", "It breaks class into pieces", "It’s the same as abstraction"), 0, Difficulty.NORMAL),
            Quiz("java_6.5", "What is abstraction?",
                listOf("Hiding data", "Hiding implementation details", "Hiding variables", "None"), 1, Difficulty.EASY),
            Quiz("java_6.6", "Which keyword is used for inheritance in Java?",
                listOf("inherit", "extends", "implements", "super"), 1, Difficulty.EASY),
            Quiz("java_6.7", "What is 'super' used for?",
                listOf("To access parent class methods/constructors", "To override methods", "To define classes", "To define static methods"), 0, Difficulty.NORMAL),
            Quiz("java_6.8", "Which is NOT a feature of OOP?",
                listOf("Encapsulation", "Inheritance", "Compilation", "Polymorphism"), 2, Difficulty.EASY),
            Quiz("java_6.9", "What does ‘this’ keyword refer to?",
                listOf("Current class", "Parent class", "Superclass", "Static class"), 0, Difficulty.EASY),
            Quiz("java_6.10", "What is the output of: new String(\"abc\") == \"abc\"?",
                listOf("true", "false", "Error", "Depends on JVM"), 1, Difficulty.NORMAL)
        ),
        "java_module_7" to listOf(
            Quiz("java_7.1", "Which class is best to simulate an ATM in Java?",
                listOf("Scanner", "ATM", "BankAccount", "System"), 1, Difficulty.EASY),
            Quiz("java_7.2", "In a Library System, which class might represent a user?",
                listOf("Book", "Member", "Author", "Database"), 1, Difficulty.EASY),
            Quiz("java_7.3", "Which data structure is suitable to store multiple student grades?",
                listOf("Array", "Class", "Method", "Interface"), 0, Difficulty.EASY),
            Quiz("java_7.4", "Which is best for storing and modifying a list of products in inventory?",
                listOf("Array", "ArrayList", "HashMap", "Set"), 1, Difficulty.NORMAL),
            Quiz("java_7.5", "What method should a BankAccount class include?",
                listOf("calculateArea()", "withdraw()", "playMusic()", "readFile()"), 1, Difficulty.EASY),
            Quiz("java_7.6", "What does a Student class most likely contain?",
                listOf("String brand;", "float balance;", "int grade;", "boolean isConnected;"), 2, Difficulty.EASY),
            Quiz("java_7.7", "In a Hospital System, what class could represent the medical record?",
                listOf("Doctor", "Patient", "MedicalRecord", "Hospital"), 2, Difficulty.NORMAL),
            Quiz("java_7.8", "Which is most suitable for a Product class?",
                listOf("displayBalance()", "calculateGPA()", "getPrice()", "getWheels()"), 2, Difficulty.EASY),
            Quiz("java_7.9", "What relationship is shown if a Library has many Books?",
                listOf("Inheritance", "Association", "Abstraction", "Encapsulation"), 1, Difficulty.NORMAL),
            Quiz("java_7.10", "Which principle helps you reuse code by extending existing classes?",
                listOf("Polymorphism", "Encapsulation", "Inheritance", "Composition"), 2, Difficulty.NORMAL)
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
        return moduleId.matches(Regex("java_module_[1-3]")) || // Exact match for java_module_1, 2, 3
               moduleId.contains("java", ignoreCase = true)
    }
} 