package com.labactivity.lala

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator

class JavaCourseFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var javaRvModules: RecyclerView
    private lateinit var javaProgressIndicator: LinearProgressIndicator
    private lateinit var javaModuleAdapter: JavaModuleAdapter

    private val completedLessonIds = mutableSetOf<String>()
    private lateinit var course: Courses

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.java_fragmentcourse, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize sharedPreferences
        sharedPreferences =
            requireContext().getSharedPreferences("java_course_preferences", Context.MODE_PRIVATE)

        // Initialize views
        javaRvModules = view.findViewById(R.id.javaRvModules)  // RecyclerView ID
        javaProgressIndicator = view.findViewById(R.id.javaProgressIndicator)  // Progress Indicator ID
        val javaToolbar: MaterialToolbar = view.findViewById(R.id.javaToolbar)  // Toolbar ID
        val javaTvCourseDescription: TextView = view.findViewById(R.id.javaTvCourseDescription)  // Course Description TextView ID

        // Load saved completed lessons
        loadCompletedLessons()

        // Initialize course data
        course = createDummyJavaCourse()

        // Set up course info
        javaToolbar.title = course.title
        javaTvCourseDescription.text = course.description

        // Set up RecyclerView
        javaRvModules.layoutManager = LinearLayoutManager(requireContext())
        javaModuleAdapter = JavaModuleAdapter(
            requireContext(),
            course.modules,
            completedLessonIds,
            onLessonCompleted = { lessonId ->
                completedLessonIds.add(lessonId)
                saveCompletedLessons()  // Save the updated completed lessons
                updateCoursesProgress() // Update the course progress UI
            },
            onLessonClick = { lesson ->
                // Handle lesson click (e.g., open lesson detail)
                // For now, just log the click
                android.util.Log.d("JavaCourseFragment", "Lesson clicked: ${lesson.title}")
            },
            onQuizClick = { module ->
                // Navigate to the quiz for this module
                android.util.Log.d("JavaCourseFragment", "Quiz button clicked for module: ${module.id}")
                val intent = android.content.Intent(requireContext(), com.labactivity.lala.quiz.DynamicQuizActivity::class.java).apply {
                    putExtra("module_id", module.id)
                    putExtra("module_title", module.title)
                }
                startActivity(intent)
            }
        )
        javaRvModules.adapter = javaModuleAdapter

        // Initialize course progress
        updateCoursesProgress()
    }

    private fun updateCoursesProgress() {
        val totalLessons = course.modules.sumOf { it.lessons.size }
        val completedCount = completedLessonIds.size
        val progressPercentage = if (totalLessons > 0) (completedCount * 100) / totalLessons else 0
        javaProgressIndicator.progress = progressPercentage
    }

    private fun loadCompletedLessons() {
        val completedLessons = sharedPreferences.getStringSet("completed_java_lessons", emptySet())
        completedLessonIds.addAll(completedLessons ?: emptySet())
    }

    private fun saveCompletedLessons() {
        sharedPreferences.edit().putStringSet("completed_java_lessons", completedLessonIds).apply()
    }
}

fun createDummyJavaCourse(): Courses {
    val modules = listOf(
        Module(
            id = "java_module_1",
            title = "Module 1: Java Basics",
            description = "Introduction to Java programming language",
            lessons = listOf(
                Lesson("java_lesson_1_1", "java_module_1", "1.1", "Java Introduction", "Learn what Java is and why it's used.", "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, Java!\");\n    }\n}", "https://youtu.be/GoXwIVyNvX0"),
                Lesson("java_lesson_1_2", "java_module_1", "1.2", "Java Setup", "Set up Java and your development environment.", "Install JDK and set up IntelliJ IDEA or VSCode.", "https://youtu.be/BGTx91t8q50"),
                Lesson("java_lesson_1_3", "java_module_1", "1.3", "Variables and Data Types", "Explore different data types and how to use variables.", "int age = 20;\ndouble price = 19.99;", "https://youtu.be/hlGoQC332VM"),
                Lesson("java_lesson_1_4", "java_module_1", "1.4", "Input and Output", "Read user input and display output.", "Scanner sc = new Scanner(System.in);\nSystem.out.println(\"Enter name: \");", "https://youtu.be/WPvGqX-TXP0")
            ),
            isExpanded = false
        ),
        Module(
            id = "java_module_2",
            title = "Module 2: if-else Statements",
            description = "Learn how to control the flow of your Java programs",
            lessons = listOf(
                Lesson("java_lesson_2_1", "java_module_2", "2.1", "If-Else Statements", "Use conditional logic to make decisions.", "if (x > 10) {\n    System.out.println(\"x is greater than 10\");\n}", "https://youtu.be/zo-xwK10HM0"),
                Lesson("java_lesson_2_2", "java_module_2", "2.2", "Switch Statements", "Simplify complex conditional logic.", "switch (day) {\n    case 1: System.out.println(\"Monday\"); break;\n}", "https://youtu.be/G43R_-IEzjw"),
                Lesson("java_lesson_2_3", "java_module_2", "2.3", "Loops in Java", "Repeat actions using for, while, and do-while loops.", "for (int i = 0; i < 5; i++) {\n    System.out.println(i);\n}", "https://youtu.be/yJp4SCFEyaY"),
                Lesson("java_lesson_2_4", "java_module_2", "2.4", "Nested Loops", "Use loops within loops for complex structures.", "for (int i = 0; i < 3; i++) {\n    for (int j = 0; j < 3; j++) {\n        System.out.println(i + \",\" + j);\n    }\n}", "https://youtu.be/CaVQXbMVv8A")
            ),
            isExpanded = false
        ),
        Module(
            id = "java_module_3",
            title = "Module 3: Methods and Functions",
            description = "Create reusable blocks of code with methods",
            lessons = listOf(
                Lesson("java_lesson_3_1", "java_module_3", "3.1", "Defining Methods", "Learn how to define and call methods.", "public static void greet() {\n    System.out.println(\"Hello!\");\n}", "https://youtu.be/DSGyEsJ17cI"),
                Lesson("java_lesson_3_2", "java_module_3", "3.2", "Method Parameters", "Pass data into your methods.", "public static int add(int a, int b) {\n    return a + b;\n}", "https://youtu.be/2V9EfvOHcTU"),
                Lesson("java_lesson_3_3", "java_module_3", "3.3", "Method Overloading", "Use multiple methods with the same name but different parameters.", "int add(int a, int b)\nint add(int a, int b, int c)", "https://youtu.be/8D3F2zRm4DU"),
                Lesson("java_lesson_3_4", "java_module_3", "3.4", "Return Values", "Return values from your methods.", "return \"Hello\";", "https://youtu.be/bCnXEKtGzHA")
            ),
            isExpanded = false
        ),
        Module(
            id = "java_module_4",
            title = "Module 4: Arrays and Strings",
            description = "Store and manipulate collections of data",
            lessons = listOf(
                Lesson("java_lesson_4_1", "java_module_4", "4.1", "Arrays", "Work with fixed-size data collections.", "int[] numbers = {1, 2, 3, 4};", "https://youtu.be/6d0XjI1zS7Y"),
                Lesson("java_lesson_4_2", "java_module_4", "4.2", "Multidimensional Arrays", "Create and access 2D arrays.", "int[][] matrix = new int[3][3];", "https://youtu.be/_TjZ0rcqf8w"),
                Lesson("java_lesson_4_3", "java_module_4", "4.3", "String Basics", "Manipulate strings in Java.", "String s = \"Hello\";\ns.length();", "https://youtu.be/o1wFv3lMbWA"),
                Lesson("java_lesson_4_4", "java_module_4", "4.4", "String Methods", "Use string functions like substring and indexOf.", "s.substring(0, 2);", "https://youtu.be/M5dNFdvQ7GQ")
            ),
            isExpanded = false
        ),
        Module(
            id = "java_module_5",
            title = "Module 5: Exception Handling",
            description = "Handle runtime errors gracefully",
            lessons = listOf(
                Lesson("java_lesson_5_1", "java_module_5", "5.1", "Try-Catch Block", "Catch and handle exceptions using try-catch.", "try {\n    int result = 10 / 0;\n} catch (ArithmeticException e) {\n    System.out.println(\"Error: \" + e.getMessage());\n}", "https://youtu.be/BWnPJ45UR9M"),
                Lesson("java_lesson_5_2", "java_module_5", "5.2", "Finally Block", "Use finally block for cleanup.", "finally {\n    System.out.println(\"Cleanup code\");\n}", "https://youtu.be/yKXb_FZT6j0"),
                Lesson("java_lesson_5_3", "java_module_5", "5.3", "Throw Keyword", "Throw custom exceptions.", "throw new IllegalArgumentException(\"Invalid input\");", "https://youtu.be/Le4vALv0cy4"),
                Lesson("java_lesson_5_4", "java_module_5", "5.4", "Custom Exceptions", "Define your own exception classes.", "class MyException extends Exception {}", "https://youtu.be/jcF8Vd9ftXk")
            ),
            isExpanded = false
        ),
        Module(
            id = "java_module_6",
            title = "Module 6: Object-Oriented Programming",
            description = "Master the core concepts of OOP in Java",
            lessons = listOf(
                Lesson("java_lesson_6_1", "java_module_6", "6.1", "Classes and Objects", "Define and use classes and objects in Java.", "class Car {\n    String brand;\n    void drive() {\n        System.out.println(\"Driving\");\n    }\n}", "https://youtu.be/H4uT3JZZV_I"),
                Lesson("java_lesson_6_2", "java_module_6", "6.2", "Inheritance", "Use inheritance to extend class behavior.", "class Dog extends Animal {\n    void bark() {\n        System.out.println(\"Woof!\");\n    }\n}", "https://youtu.be/dgYyRyt4hDY"),
                Lesson("java_lesson_6_3", "java_module_6", "6.3", "Polymorphism", "Understand method overriding and dynamic binding.", "Animal a = new Dog();\na.makeSound();", "https://youtu.be/xgLKYaL9aVU"),
                Lesson("java_lesson_6_4", "java_module_6", "6.4", "Encapsulation and Abstraction", "Use access modifiers and abstract classes.", "private int age;\npublic void setAge(int a) {\n    this.age = a;\n}", "https://youtu.be/QVj4LHPtWzw")
            ),
            isExpanded = false
        ),
        Module(
            id = "java_module_7",
            title = "Module 7: Mini Projects",
            description = "Apply everything you've learned in real-world projects",
            lessons = listOf(
                Lesson("java_lesson_7_1", "java_module_7", "7.1", "ATM Simulation", "Simulate an ATM with options like withdraw, deposit, and check balance.", "// ATM logic using Scanner and control flow", "https://youtu.be/uolTUtioIrc"),
                Lesson("java_lesson_7_2", "java_module_7", "7.2", "Library Management System", "Build a basic system for borrowing and returning books.", "// Use classes like Book, Member, and Library", "https://youtu.be/EVxl7FvA5kI"),
                Lesson("java_lesson_7_3", "java_module_7", "7.3", "Student Grade Calculator", "Calculate student grades using arrays and loops.", "// Input student scores and compute average", "https://youtu.be/l0HjN5zDmsk"),
                Lesson("java_lesson_7_4", "java_module_7", "7.4", "Simple Inventory Tracker", "Create a program to track items and stock count.", "// ArrayList with product objects", "https://youtu.be/C0fGjTbx_5I")
            ),
            isExpanded = false
        )
    )

    return Courses(
        id = "java_course_001",
        title = "Java Programming Mastery",
        description = "Learn Java from basics to advanced with practical examples and projects.",
        modules = modules
    )
}