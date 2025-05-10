package com.labactivity.lala

data class Challenge(
    val title: String,
    val difficulty: String,
    val id: Int = 0,
    val brokenCode: String = "",
    val correctOutput: String = "",
    val hint: String = "",
    // Calculate codePreview from brokenCode if provided, otherwise empty string
    val codePreview: String = if (brokenCode.isNotEmpty()) {
        brokenCode.lines().firstOrNull {
            it.trim().isNotEmpty() && !it.trim().startsWith("#")
        } ?: ""
    } else {
        ""
    }
)

// Sample challenges list
val challenges = listOf(
    Challenge(
        id = 1,
        title = "Fix the Loop",
        difficulty = "Easy",
        brokenCode = """
            # Task: Print 'Hello, TechLauncher!' 5 times.
            # Bug: Loop is not correctly formatted.
            
            for i in range(5):
                print(f"Line {}: Hello, TechLauncher!")
        """.trimIndent(),
        correctOutput = """
            Line 0: Hello, TechLauncher!
            Line 1: Hello, TechLauncher!
            Line 2: Hello, TechLauncher!
            Line 3: Hello, TechLauncher!
            Line 4: Hello, TechLauncher!
        """.trimIndent(),
        hint = "Maybe the variable is missing inside the f-string."
    ),

    Challenge(
        id = 2,
        title = "Debug If-Else Statement",
        difficulty = "Easy",
        brokenCode = """
            # Task: Check if the number is positive, negative, or zero
            # Bug: The if-else structure has an issue
            
            num = 15
            
            if num > 0
                print("The number is positive")
            elif num < 0:
                print("The number is negative")
            else:
                print("The number is zero")
        """.trimIndent(),
        correctOutput = "The number is positive",
        hint = "Check the syntax of the if statement. Is it properly terminated?"
    ),

    Challenge(
        id = 3,
        title = "Fix Function Call",
        difficulty = "Medium",
        brokenCode = """
            # Task: Calculate the factorial of a number
            # Bug: Function call is incorrect
            
            def factorial(n):
                if n == 0 or n == 1:
                    return 1
                else:
                    return n * factorial(n-1)
            
            number = 5
            result = factorial[number]
            print(f"The factorial of {number} is {result}")
        """.trimIndent(),
        correctOutput = "The factorial of 5 is 120",
        hint = "How do you call a function in Python? [] is for different data structures."
    ),

    Challenge(
        id = 4,
        title = "List Comprehension",
        difficulty = "Medium",
        brokenCode = """
            # Task: Create a list of even numbers from 1 to 10
            # Bug: List comprehension syntax error
            
            even_numbers = [x for x in range(1, 11) if x % 2 = 0]
            print("Even numbers:", even_numbers)
        """.trimIndent(),
        correctOutput = "Even numbers: [2, 4, 6, 8, 10]",
        hint = "Check the comparison operator in the if condition."
    ),

    Challenge(
        id = 5,
        title = "Fix Dictionary Access",
        difficulty = "Hard",
        brokenCode = """
            # Task: Print the student's score
            # Bug: Dictionary access has a problem
            
            student_scores = {"John": 85, "Alice": 92, "Bob": 78}
            student_name = "Alice"
            
            print(f"{student_name}'s score is {student_scores(student_name)}")
        """.trimIndent(),
        correctOutput = "Alice's score is 92",
        hint = "How do you access values in a dictionary? () is for function calls."
    )
)