//package com.labactivity.lala.UNIFIEDCOMPILER
//
//import android.content.Context
//import android.content.Intent
//import com.labactivity.lala.UNIFIEDCOMPILER.ui.UnifiedCompilerActivity
//
///**
// * ============================================================================
// * UNIFIED COMPILER TEST LAUNCHER
// * ============================================================================
// *
// * Use these helper functions to quickly test the unified compiler
// * from anywhere in your app.
// *
// * USAGE:
// * Add a test button in your activity:
// *
// * btnTestCompiler.setOnClickListener {
// *     TestLauncher.testPython(this)
// * }
// * ============================================================================
// */
//object TestLauncher {
//
//    /**
//     * Test Python Compiler
//     */
//    fun testPython(context: Context) {
//        val intent = Intent(context, UnifiedCompilerActivity::class.java).apply {
//            putExtra(UnifiedCompilerActivity.EXTRA_LANGUAGE, "python")
//            putExtra(UnifiedCompilerActivity.EXTRA_INITIAL_CODE, """
//# Python Test
//print("Hello from Python!")
//
//for i in range(5):
//    print(f"Number {i}")
//
//# Test functions
//def greet(name):
//    return f"Hello, {name}!"
//
//print(greet("World"))
//            """.trimIndent())
//        }
//        context.startActivity(intent)
//    }
//
//    /**
//     * Test Java Compiler
//     */
//    fun testJava(context: Context) {
//        val intent = Intent(context, UnifiedCompilerActivity::class.java).apply {
//            putExtra(UnifiedCompilerActivity.EXTRA_LANGUAGE, "java")
//            putExtra(UnifiedCompilerActivity.EXTRA_INITIAL_CODE, """
//
//            """.trimIndent())
//        }
//        context.startActivity(intent)
//    }
//
//    /**
//     * Test Kotlin Compiler
//     */
//    fun testKotlin(context: Context) {
//        val intent = Intent(context, UnifiedCompilerActivity::class.java).apply {
//            putExtra(UnifiedCompilerActivity.EXTRA_LANGUAGE, "kotlin")
//            putExtra(UnifiedCompilerActivity.EXTRA_INITIAL_CODE, """
//// Kotlin Test
//println("Hello from Kotlin!")
//
//// Test loop
//for (i in 0..4) {
//    println("Number ${'$'}i")
//}
//
//// Test variable
//val name = "Kotlin"
//println("Hello, ${'$'}name!")
//            """.trimIndent())
//        }
//        context.startActivity(intent)
//    }
//
//    /**
//     * Test SQL Executor
//     */
//    fun testSQL(context: Context) {
//        val intent = Intent(context, UnifiedCompilerActivity::class.java).apply {
//            putExtra(UnifiedCompilerActivity.EXTRA_LANGUAGE, "sql")
//            putExtra(UnifiedCompilerActivity.EXTRA_INITIAL_CODE, """
//-- SQL Test
//-- Query sample employees table
//SELECT * FROM employees LIMIT 5;
//            """.trimIndent())
//        }
//        context.startActivity(intent)
//    }
//
//    /**
//     * Test with Course ID (automatically detects compiler)
//     */
//    fun testWithCourse(context: Context, courseId: String) {
//        val intent = Intent(context, UnifiedCompilerActivity::class.java).apply {
//            putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, courseId)
//        }
//        context.startActivity(intent)
//    }
//
//    /**
//     * Launch compiler for a specific challenge
//     */
//    fun launchForChallenge(
//        context: Context,
//        courseId: String,
//        challengeCode: String,
//        challengeTitle: String? = null
//    ) {
//        val intent = Intent(context, UnifiedCompilerActivity::class.java).apply {
//            putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, courseId)
//            putExtra(UnifiedCompilerActivity.EXTRA_INITIAL_CODE, challengeCode)
//            challengeTitle?.let {
//                putExtra("EXTRA_TITLE", it)
//            }
//        }
//        context.startActivity(intent)
//    }
//
//    /**
//     * Test all compilers sequentially
//     */
//    fun testAllCompilers(context: Context) {
//        // You can call these one by one to test each compiler
//        testPython(context)
//        // testJava(context)
//        // testKotlin(context)
//        // testSQL(context)
//    }
//}
//
///**
// * ============================================================================
// * HOW TO TEST THE UNIFIED COMPILER
// * ============================================================================
// *
// * OPTION 1: Add Test Buttons to Your Activity
// * --------------------------------------------
// *
// * In your activity (e.g., MainActivity4.kt):
// *
// * ```kotlin
// * // Add test buttons in your layout
// * btnTestPython.setOnClickListener {
// *     TestLauncher.testPython(this)
// * }
// *
// * btnTestJava.setOnClickListener {
// *     TestLauncher.testJava(this)
// * }
// *
// * btnTestKotlin.setOnClickListener {
// *     TestLauncher.testKotlin(this)
// * }
// *
// * btnTestSQL.setOnClickListener {
// *     TestLauncher.testSQL(this)
// * }
// * ```
// *
// * OPTION 2: Replace Existing Challenge Activities
// * ------------------------------------------------
// *
// * OLD CODE (AllJavaChallengesActivity):
// * ```kotlin
// * val intent = Intent(this, JavaChallengeActivity::class.java)
// * intent.putExtra("challenge", challenge)
// * startActivity(intent)
// * ```
// *
// * NEW CODE (UnifiedCompilerActivity):
// * ```kotlin
// * TestLauncher.launchForChallenge(
// *     context = this,
// *     courseId = challenge.courseId,
// *     challengeCode = challenge.brokenCode,
// *     challengeTitle = challenge.title
// * )
// * ```
// *
// * OPTION 3: Test from Debug Menu
// * -------------------------------
// *
// * Add a debug menu in your app:
// *
// * ```kotlin
// * override fun onCreateOptionsMenu(menu: Menu): Boolean {
// *     if (BuildConfig.DEBUG) {
// *         menu.add("Test Python Compiler").setOnMenuItemClickListener {
// *             TestLauncher.testPython(this)
// *             true
// *         }
// *         menu.add("Test Java Compiler").setOnMenuItemClickListener {
// *             TestLauncher.testJava(this)
// *             true
// *         }
// *     }
// *     return super.onCreateOptionsMenu(menu)
// * }
// * ```
// *
// * OPTION 4: Direct Intent Launch
// * -------------------------------
// *
// * ```kotlin
// * val intent = Intent(this, UnifiedCompilerActivity::class.java).apply {
// *     putExtra(UnifiedCompilerActivity.EXTRA_LANGUAGE, "python")
// *     // Optional: Add initial code
// *     putExtra(UnifiedCompilerActivity.EXTRA_INITIAL_CODE, "print('test')")
// * }
// * startActivity(intent)
// * ```
// *
// * ============================================================================
// */
//
///**
// * Example: Add to MainActivity4 (Homepage) for testing
// */
///*
//class MainActivity4 : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main4)
//
//        // Initialize unified compiler
//        CompilerFactory.initialize(applicationContext)
//
//        // Add test buttons (you can add these to your layout temporarily)
//        // Or add a floating action button for testing
//
//        // Example: Test when clicking on a course card
//        pythonCourseCard.setOnClickListener {
//            TestLauncher.testPython(this)
//        }
//
//        javaCourseCard.setOnClickListener {
//            TestLauncher.testJava(this)
//        }
//
//        kotlinCourseCard.setOnClickListener {
//            TestLauncher.testKotlin(this)
//        }
//
//        sqlCourseCard.setOnClickListener {
//            TestLauncher.testSQL(this)
//        }
//    }
//}
//*/
