package com.labactivity.lala.quiz

import android.content.ContentValues.TAG
import android.util.Log

/**
 * Repository that manages Python quiz questions
 */
class ModuleQuizRepository : QuizRepository {

    // Map of module IDs to lists of quiz questions
    private val quizzesByModule: Map<String, List<Quiz>> = mapOf(
        // Module 1: Fundamentals of Python
        "python_module_1" to listOf(
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
                question = "Which function is used to display output in Python?",
                options = listOf(
                    "print()",
                    "echo()",
                    "display()",
                    "show()"
                ),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.3",
                question = "How do you get input from a user in Python?",
                options = listOf(
                    "scanf()",
                    "get()",
                    "input()",
                    "cin"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.4",
                question = "Which of the following is a string data type?",
                options = listOf(
                    "10",
                    "'10'",
                    "10.0",
                    "True"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.5",
                question = "What symbol is used to add a comment in Python?",
                options = listOf(
                    "//",
                    "/*",
                    "#",
                    "--"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.6",
                question = "What is the output of: print(type(3.14))?",
                options = listOf(
                    "<class 'int'>",
                    "<class 'float'>",
                    "<class 'str'>",
                    "<class 'double'>"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "1.7",
                question = "What does this code output?\nname = 'Jerico'\nprint('Hello', name)",
                options = listOf(
                    "HelloJerico",
                    "Hello+Jerico",
                    "Hello Jerico",
                    "Jerico Hello"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.8",
                question = "Which data type is used for True/False values?",
                options = listOf(
                    "int",
                    "bool",
                    "str",
                    "char"
                ),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "1.9",
                question = "What keyword is used to define a constant in Python?",
                options = listOf(
                    "const",
                    "final",
                    "There is no keyword, just use all caps",
                    "static"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "1.10",
                question = "What does this code do?\n# This is a comment",
                options = listOf(
                    "Prints the comment",
                    "Raises an error",
                    "Nothing, it's ignored by Python",
                    "Executes the comment"
                ),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            )
        ),

        // Module 2: Control Flow in Python
        "module_2" to listOf(
            Quiz(
                id = "2.1",
                question = "What does the 'if' statement do?",
                options = listOf("Checks if a condition is true", "Loops over a range", "Prints values", "Defines a function"),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "2.2",
                question = "Which keyword breaks out of a loop?",
                options = listOf("continue", "exit", "break", "stop"),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "2.3",
                question = "What is the output?\nif x > 0:\n    print('Positive')\nelse:\n    print('Negative')",
                options = listOf("Positive", "Negative", "Error", "Nothing"),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "2.4",
                question = "What is the purpose of a 'while' loop?",
                options = listOf("Loops a fixed number of times", "Loops while a condition is true", "Loops until a condition is false", "Used for recursion"),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "2.5",
                question = "Which keyword skips the current iteration of a loop?",
                options = listOf("continue", "break", "exit", "skip"),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "2.6",
                question = "What is the result of the following?\nfor i in range(3):\n    print(i)",
                options = listOf("0 1 2", "1 2 3", "0 2 4", "0 1 2 3"),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "2.7",
                question = "Which of the following will create an infinite loop?",
                options = listOf("for i in range(5):", "while True:", "for i in range(10):", "while x < 10:"),
                correctOptionIndex = 1,
                difficulty = Difficulty.HARD
            ),
            Quiz(
                id = "2.8",
                question = "How do you check if a number is even inside a loop?",
                options = listOf("if x % 2 == 0:", "if x // 2 == 0:", "if x != 2:", "if x == 0:"),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "2.9",
                question = "Which statement will stop the loop early?",
                options = listOf("return", "continue", "break", "exit"),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "2.10",
                question = "What is the result of the following code?\nfor i in range(3):\n    if i == 1:\n        break\n    print(i)",
                options = listOf("0 1", "0", "0 2", "Error"),
                correctOptionIndex = 0,
                difficulty = Difficulty.HARD
            )
        ),

        "module_3" to listOf(
            Quiz(
                id = "3.1",
                question = "How do you define a function in Python?",
                options = listOf("def function_name():", "create function_name():", "function function_name():", "func function_name():"),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "3.2",
                question = "What is the output of the following code?\ndef greet(name):\n    print('Hello', name)\ngreet('John')",
                options = listOf("Hello", "Hello John", "greet John", "None"),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "3.3",
                question = "Which of the following is a valid recursion function?",
                options = listOf("def factorial(n):\n    return n * factorial(n-1)", "def factorial(n):\n    return n * n", "def factorial(n):\n    return n + factorial(n-1)", "def factorial(n):\n    return factorial(n-1)"),
                correctOptionIndex = 0,
                difficulty = Difficulty.HARD
            ),
            Quiz(
                id = "3.4",
                question = "What is the output of this function?\ndef add(a, b):\n    return a + b\nadd(2, 3)",
                options = listOf("23", "5", "Error", "None"),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "3.5",
                question = "What happens if the base case in a recursive function is missing?",
                options = listOf("The function stops running", "The function runs infinitely", "The function returns None", "The function throws an error"),
                correctOptionIndex = 1,
                difficulty = Difficulty.HARD
            ),
            Quiz(
                id = "3.6",
                question = "Which statement is used to return a value from a function?",
                options = listOf("yield", "return", "continue", "break"),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "3.7",
                question = "Which of the following is not valid in a function?",
                options = listOf("Calling the function before defining it", "Returning a value from the function", "Passing arguments to the function", "None of the above"),
                correctOptionIndex = 0,
                difficulty = Difficulty.HARD
            ),
            Quiz(
                id = "3.8",
                question = "How do you pass an argument to a function?",
                options = listOf("function(argument)", "function = argument", "function -> argument", "argument -> function"),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "3.9",
                question = "What does 'None' represent in Python?",
                options = listOf("A null value", "An undefined value", "A boolean value", "An error"),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "3.10",
                question = "What is the output of the following recursive function?\ndef factorial(n):\n    if n == 1:\n        return 1\n    return n * factorial(n-1)\nfactorial(3)",
                options = listOf("6", "3", "9", "None"),
                correctOptionIndex = 0,
                difficulty = Difficulty.HARD
            )
        ),

        "module_4" to listOf(
            Quiz(
                id = "4.1",
                question = "How do you create a list in Python?",
                options = listOf("[]", "()", "{}", "<>"),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "4.2",
                question = "Which of the following is a dictionary?",
                options = listOf("['a', 'b', 'c']", "{'a': 1, 'b': 2}", "(1, 2, 3)", "[1, 2, 3]"),
                correctOptionIndex = 1,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "4.3",
                question = "What is the output of this code?\ndata = [1, 2, 3]\ndata.append(4)",
                options = listOf("[1, 2, 3]", "[1, 2, 3, 4]", "Error", "None"),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "4.4",
                question = "Which of the following is a set in Python?",
                options = listOf("{1, 2, 3}", "[1, 2, 3]", "(1, 2, 3)", "{'a': 1, 'b': 2}"),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "4.5",
                question = "How do you access the first item of a list?",
                options = listOf("list[1]", "list(0)", "list[0]", "list.first()"),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "4.6",
                question = "Which of the following removes duplicates from a list?",
                options = listOf("set()", "remove()", "delete()", "pop()"),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "4.7",
                question = "How do you add an element to a set?",
                options = listOf("set.add(element)", "set.append(element)", "set.insert(element)", "set.append()"),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "4.8",
                question = "What is the output of the following?\nmy_list = [1, 2, 3]\nmy_list.remove(2)",
                options = listOf("[1, 3]", "[2, 3]", "[1, 2]", "Error"),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "4.9",
                question = "Which method is used to get the length of a list?",
                options = listOf("size()", "count()", "length()", "len()"),
                correctOptionIndex = 3,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "4.10",
                question = "What is the result of this code?\nmy_dict = {'a': 1, 'b': 2}\ndel my_dict['b']",
                options = listOf("{'a': 1, 'b': 2}", "{'a': 1}", "Error", "{'b': 2}"),
                correctOptionIndex = 1,
                difficulty = Difficulty.HARD
            )
        ),

        "module_5" to listOf(
            Quiz(
                id = "5.1",
                question = "How do you define a dictionary in Python?",
                options = listOf("{'key': 'value'}", "[key, value]", "('key', 'value')", "<key=value>"),
                correctOptionIndex = 0,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "5.2",
                question = "Which method is used to get all keys from a dictionary?",
                options = listOf("values()", "items()", "keys()", "getKeys()"),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "5.3",
                question = "What is the output?\nmydict = {'a': 1, 'b': 2}\nprint(mydict['a'])",
                options = listOf("a", "1", "2", "Error"),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "5.4",
                question = "What happens if you access a key that does not exist?",
                options = listOf("Returns None", "Returns 0", "Raises an error", "Creates the key"),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "5.5",
                question = "What does `set()` do in Python?",
                options = listOf("Creates a tuple", "Creates a list", "Creates a dictionary", "Creates a set"),
                correctOptionIndex = 3,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "5.6",
                question = "Which of the following removes duplicates?",
                options = listOf("list()", "dict()", "set()", "tuple()"),
                correctOptionIndex = 2,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "5.7",
                question = "What is the output of `len({'a': 1, 'b': 2})`?",
                options = listOf("1", "2", "3", "Error"),
                correctOptionIndex = 1,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "5.8",
                question = "How do you add a key-value pair to a dictionary?",
                options = listOf("dict.add()", "dict.append()", "dict[key] = value", "dict.push()"),
                correctOptionIndex = 2,
                difficulty = Difficulty.EASY
            ),
            Quiz(
                id = "5.9",
                question = "Which operation checks if a key exists?",
                options = listOf("'key' in dict", "dict.has('key')", "dict.find('key')", "dict.contains('key')"),
                correctOptionIndex = 0,
                difficulty = Difficulty.NORMAL
            ),
            Quiz(
                id = "5.10",
                question = "Which set operation gives common elements?",
                options = listOf("union()", "difference()", "intersection()", "combine()"),
                correctOptionIndex = 2,
                difficulty = Difficulty.HARD
            )
        )
    )

    override fun getQuestionsForModule(moduleId: String): List<Quiz> {
        Log.d("ModuleQuizRepository", "Getting questions for module ID: $moduleId")

        // Try to get questions for the specified module ID
        val questions = quizzesByModule[moduleId]

        // If exact match is found, return those questions
        if (questions != null) {
            Log.d("ModuleQuizRepository", "Found ${questions.size} questions for module ID: $moduleId")
            return questions.take(10) // Limit to 10 questions
        }

        // No exact match - determine the appropriate Python module
        val pythonModuleId = when {
            // Python core modules (e.g., python_module_1, python_module_2)
            moduleId.matches(Regex("^python_module_\\d+$")) -> {
                moduleId // Directly use the moduleId when prefixed with python_module_
            }
            // Default fallback for generic Python module
            else -> {
                Log.d("ModuleQuizRepository", "No specific Python module found, using fallback")
                "python_module_1" // Default to "python_module_1" as fallback
            }
        }

        // Get questions for the determined module type
        val moduleQuestions = quizzesByModule[pythonModuleId]
        Log.d("ModuleQuizRepository", "Using $pythonModuleId questions as fallback for $moduleId")

        return moduleQuestions?.take(10) ?: emptyList()
    }

    /**
     * Gets the total number of questions available for a module
     * @param moduleId The ID of the module
     * @return The number of questions available, limited to a maximum of 10
     */
    override fun getQuestionCountForModule(moduleId: String): Int {
        // Try to get questions for the specified module ID
        val questions = quizzesByModule[moduleId]

        // If exact match is found, return the count
        if (questions != null) {
            return minOf(questions.size, 10)
        }

        // If no questions found, return 0
        return 0
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
        Log.d(TAG, "Checking if module ID: $moduleId is a Python module")
        
        // Check for exact matches first
        if (quizzesByModule.containsKey(moduleId)) {
            Log.d(TAG, "Direct match found for Python module: $moduleId")
            return true
        }
        
        // Check for Python-specific patterns
        val isPythonModule = when {
            // Check for single digits 1-5 (Python modules)
            moduleId.matches(Regex("^[1-5]$")) -> true
            // Check for python_module_X format
            moduleId.matches(Regex("^python_module_[1-5]$")) -> true
            // Check for module_X format where X is 1-5 (Python modules)
            moduleId.matches(Regex("^module_[1-5]$")) -> true
            // Check for python_X format
            moduleId.matches(Regex("^python_[1-5]$")) -> true
            // Check for any ID containing "python" (case insensitive)
            moduleId.contains("python", ignoreCase = true) -> true
            else -> false
        }
        
        Log.d(TAG, "Module ID: $moduleId is Python module: $isPythonModule")
        return isPythonModule
    }
}