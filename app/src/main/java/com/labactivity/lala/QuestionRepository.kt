package com.labactivity.lala

import android.util.Log

/**
 * Repository for general quiz questions.
 * 
 * @deprecated This repository is deprecated and will be removed in future versions.
 * For module-specific questions, use QuizRepositoryFactory instead.
 */
@Deprecated("Use QuizRepositoryFactory for all quiz questions")
class QuestionRepository {
    init {
        Log.w("QuestionRepository", "WARNING: QuestionRepository is deprecated. Use QuizRepositoryFactory instead for all module quizzes.")
    }
    
    fun getQuestions(): List<Question> {
        Log.w("QuestionRepository", "getQuestions() called - this method is deprecated, use QuizRepositoryFactory.getRepositoryForModule(moduleId).getQuestionsForModule() instead")
        return listOf(
            // BEGINNER
            Question(
                1,
                "Which of the following is used to output data in Python?",
                listOf("print()", "input()", "output()", "echo()"),
                0,
                Difficulty.BEGINNER
            ),
            Question(
                2,
                "Which of the following is a valid variable name in Python?",
                listOf("1variable", "_variable", "variable!", "var@ble"),
                1,
                Difficulty.BEGINNER
            ),
            Question(
                3,
                "What is the correct way to define a list in Python?",
                listOf("list = ()", "list = {}", "list = []", "list = <>"),
                2,
                Difficulty.BEGINNER
            ),
            Question(
                4,
                "Which function is used to find the length of a list in Python?",
                listOf("length()", "count()", "len()", "size()"),
                2,
                Difficulty.BEGINNER
            ),
            Question(
                5,
                "What is the result of this code: `x = 5; y = 3; print(x + y)`?",
                listOf("8", "15", "53", "Error"),
                0,
                Difficulty.BEGINNER
            ),

            // FUNDAMENTAL
            Question(
                6,
                "Which of the following is NOT a built-in data type in Python?",
                listOf("List", "Dictionary", "Array", "Tuple"),
                2,
                Difficulty.FUNDAMENTAL
            ),
            Question(
                7,
                "What will be the output of this code: `print(2 ** 3)`?",
                listOf("6", "8", "5", "Error"),
                1,
                Difficulty.FUNDAMENTAL
            ),
            Question(
                8,
                "How do you start a single-line comment in Python?",
                listOf("//", "#", "/*", "<!--"),
                1,
                Difficulty.FUNDAMENTAL
            ),
            Question(
                9,
                "Which keyword is used to define a function in Python?",
                listOf("func", "define", "def", "function"),
                2,
                Difficulty.FUNDAMENTAL
            ),
            Question(
                10,
                "Which function is used to get input from the user in Python?",
                listOf("scanf()", "read()", "input()", "get()"),
                2,
                Difficulty.FUNDAMENTAL
            ),

            // INTERMEDIATE
            Question(
                11,
                "What does the `len()` function do in Python?",
                listOf("Returns the number of elements", "Converts to string", "Sorts a list", "Adds elements"),
                0,
                Difficulty.INTERMEDIATE
            ),
            Question(
                12,
                "How do you handle exceptions in Python?",
                listOf("try-catch", "handle-except", "try-except", "catch-throw"),
                2,
                Difficulty.INTERMEDIATE
            ),
            Question(
                13,
                "Which method is used to remove an item from a list by value?",
                listOf("delete()", "remove()", "discard()", "pop()"),
                1,
                Difficulty.INTERMEDIATE
            ),
            Question(
                14,
                "Which of these is used to define a class in Python?",
                listOf("function", "method", "class", "def"),
                2,
                Difficulty.INTERMEDIATE
            ),
            Question(
                15,
                "What is the output of: `print(bool(0))`?",
                listOf("True", "False", "None", "0"),
                1,
                Difficulty.INTERMEDIATE
            ),

            // ADVANCED INTERMEDIATE
            Question(
                16,
                "What is the purpose of `self` in Python classes?",
                listOf("Defines static methods", "Refers to the current instance", "Creates global variable", "Initializes a class"),
                1,
                Difficulty.ADVANCED_INTERMEDIATE
            ),
            Question(
                17,
                "Which of the following is true about Python dictionaries?",
                listOf("They are ordered", "They allow duplicate keys", "They store key-value pairs", "They are immutable"),
                2,
                Difficulty.ADVANCED_INTERMEDIATE
            ),
            Question(
                18,
                "Which module in Python is used for regular expressions?",
                listOf("regex", "regexp", "re", "match"),
                2,
                Difficulty.ADVANCED_INTERMEDIATE
            ),
            Question(
                19,
                "What does the `map()` function do?",
                listOf("Applies a function to each item in an iterable", "Filters a list", "Sorts a list", "Combines two lists"),
                0,
                Difficulty.ADVANCED_INTERMEDIATE
            ),
            Question(
                20,
                "What is a lambda function?",
                listOf("A loop", "A class", "An anonymous function", "A dictionary"),
                2,
                Difficulty.ADVANCED_INTERMEDIATE
            )
        )
    }
}
