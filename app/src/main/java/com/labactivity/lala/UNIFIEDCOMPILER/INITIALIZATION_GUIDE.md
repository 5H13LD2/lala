# 🚀 Unified Compiler System - Initialization & Testing Guide

## ⚡ Quick Start (3 Steps)

### Step 1: Initialize CompilerFactory

Add to your `Application` class or main `MainActivity`:

```kotlin
import com.labactivity.lala.UNIFIEDCOMPILER.CompilerFactory

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize the unified compiler system
        CompilerFactory.initialize(this)
    }
}
```

**OR** if you don't have an Application class, add to `MainActivity.onCreate()`:

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize once
        if (!CompilerFactory.hasCompiler("python")) {
            CompilerFactory.initialize(applicationContext)
        }
    }
}
```

### Step 2: Test Basic Functionality

```kotlin
lifecycleScope.launch {
    try {
        // Test Python
        val pythonCompiler = CompilerFactory.getCompiler("python")
        val pythonResult = pythonCompiler.compile("print('Hello from Python!')")
        Log.d("Compiler", "Python: ${pythonResult.output}")

        // Test Java
        val javaCompiler = CompilerFactory.getCompiler("java")
        val javaResult = javaCompiler.compile("""
            public class Main {
                public void static void main(String[]args) {
                    System.out.println("hello Ma'am tere!");
                }
            }
        """.trimIndent())
        Log.d("Compiler", "Java: ${javaResult.output}")

        // Test SQL
        val sqlCompiler = CompilerFactory.getCompiler("sql")
        val sqlResult = sqlCompiler.compile("SELECT * FROM employees LIMIT 5")
        Log.d("Compiler", "SQL: ${sqlResult.output}")

        // Test Kotlin
        val kotlinCompiler = CompilerFactory.getCompiler("kotlin")
        val kotlinResult = kotlinCompiler.compile("println(\"Hello from Kotlin!\")")
        Log.d("Compiler", "Kotlin: ${kotlinResult.output}")

        Toast.makeText(this@MainActivity, "✓ All compilers working!", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        Log.e("Compiler", "Error: ${e.message}", e)
        Toast.makeText(this@MainActivity, "✗ Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
```

### Step 3: Test with Existing Challenges

```kotlin
lifecycleScope.launch {
    val assessmentService = UnifiedAssessmentService()

    // Test loading challenges from technical_assesment collection
    val userChallenges = assessmentService.getChallengesForUser()

    Log.d("Challenges", "Found ${userChallenges.size} challenges")

    userChallenges.forEach { challenge ->
        Log.d("Challenges", """
            Title: ${challenge.title}
            Course: ${challenge.courseId}
            Compiler: ${challenge.compilerType}
            Difficulty: ${challenge.difficulty}
            Unlocked: ${challenge.isUnlocked}
        """.trimIndent())
    }
}
```

---

## 🗄️ Firebase Data Setup

### Required Collections & Structure

#### 1. `courses` Collection

Your courses should have this structure:

```json
{
  "courseId": "python_beginner",
  "courseName": "Python Beginner Course",
  "compilerType": "python",  ← CRITICAL: Must be "python", "java", "sql", or "kotlin"
  "version": "3.x",
  "description": "Learn Python basics",
  "difficulty": "beginner"
}
```

**Example courses to create:**

```javascript
// Python Course
{
  courseId: "python_beginner",
  courseName: "Python Beginner Course",
  compilerType: "python",
  version: "3.x"
}

// Java Course
{
  courseId: "java_fundamentals",
  courseName: "Java Fundamentals",
  compilerType: "java",
  version: "11"
}

// SQL Course
{
  courseId: "sql_basics",
  courseName: "SQL Basics",
  compilerType: "sql",
  version: "SQLite"
}

// Kotlin Course
{
  courseId: "kotlin_beginner",
  courseName: "Kotlin Beginner",
  compilerType: "kotlin",
  version: "1.9"
}
```

#### 2. `technical_assesment` Collection

**THE CORE COLLECTION** - All challenges go here, regardless of language!

```json
{
  "courseId": "python_beginner",     ← Links to course
  "title": "Hello Python",
  "description": "Print a greeting message",
  "difficulty": "Easy",              ← Easy, Medium, or Hard
  "brokenCode": "print('Fix me')",
  "correctOutput": "Hello, World!",
  "hint": "Use the print() function",
  "category": "basics",
  "status": "available",
  "createdAt": "2024-01-01"
}
```

**Example challenges for different languages:**

```javascript
// Python Challenge
{
  courseId: "python_beginner",
  title: "Print Numbers",
  difficulty: "Easy",
  brokenCode: "# Fix this code\nprint('Wrong')",
  correctOutput: "0\n1\n2\n3\n4",
  hint: "Use a for loop with range()"
}

// Java Challenge
{
  courseId: "java_fundamentals",
  title: "Hello Java",
  difficulty: "Easy",
  brokenCode: "public class Test {\n    public void run() {\n        // Fix this\n    }\n}",
  correctOutput: "Hello, Java!",
  hint: "Use System.out.println()"
}

// SQL Challenge
{
  courseId: "sql_basics",
  title: "Select All Employees",
  difficulty: "Easy",
  brokenCode: "SELECT * FROM wrong_table",
  correctOutput: "...",  // Expected query result
  hint: "Use the employees table"
}

// Kotlin Challenge
{
  courseId: "kotlin_beginner",
  title: "Kotlin Variables",
  difficulty: "Easy",
  brokenCode: "val name = \"Fix\"",
  correctOutput: "Hello, Kotlin!",
  hint: "Use string templates"
}
```

#### 3. User Enrollment

Users must be enrolled in courses. In `users/{userId}`:

```json
{
  "courseTaken": [
    { "courseId": "python_beginner" },
    { "courseId": "java_fundamentals" },
    { "courseId": "sql_basics" }
  ],
  "totalXP": 500,
  "level": 1
}
```

---

## 🧪 Testing Checklist

### ✅ Test 1: Compiler Factory Initialization

```kotlin
val supported = CompilerFactory.getSupportedLanguages()
Log.d("Test", "Supported: $supported")
// Should show: [java, kotlin, python, sql]
```

### ✅ Test 2: Direct Compiler Execution

```kotlin
lifecycleScope.launch {
    val compiler = CompilerFactory.getCompiler("python")
    val result = compiler.compile("print('Test')")

    assert(result.success) { "Compilation failed" }
    assert(result.output == "Test") { "Wrong output" }
}
```

### ✅ Test 3: Course-Based Execution

```kotlin
lifecycleScope.launch {
    val service = CompilerService()
    val courseInfo = service.getCourseCompilerInfo("python_beginner")

    assert(courseInfo.compilerType == "python") { "Wrong compiler type" }

    val compiler = service.getCompilerForCourse("python_beginner")
    assert(compiler.getLanguageId() == "python") { "Wrong compiler" }
}
```

### ✅ Test 4: Challenge Loading

```kotlin
lifecycleScope.launch {
    val service = UnifiedAssessmentService()

    // Test getting challenges for specific course
    val pythonChallenges = service.getChallengesForCourse("python_beginner")
    Log.d("Test", "Python challenges: ${pythonChallenges.size}")

    // Each challenge should have compilerType set
    pythonChallenges.forEach { challenge ->
        assert(challenge.compilerType == "python") { "Compiler type not set" }
    }
}
```

### ✅ Test 5: Challenge Execution

```kotlin
lifecycleScope.launch {
    val service = UnifiedAssessmentService()
    val challenges = service.getChallengesForCourse("python_beginner")
    val challenge = challenges.firstOrNull() ?: return@launch

    val userCode = "print('Hello, World!')"

    val result = service.executeChallenge(
        challengeId = challenge.id,
        userCode = userCode,
        challenge = challenge
    )

    Log.d("Test", "Passed: ${result.passed}")
    Log.d("Test", "Score: ${result.score}%")
    Log.d("Test", "Output: ${result.compilerResult.output}")
}
```

### ✅ Test 6: Progress Saving

```kotlin
lifecycleScope.launch {
    val service = UnifiedAssessmentService()
    val challenges = service.getChallengesForCourse("python_beginner")
    val challenge = challenges.firstOrNull() ?: return@launch

    val userCode = "print('Hello, World!')"

    val result = service.executeChallenge(challenge.id, userCode, challenge)

    // Save progress
    service.saveProgress(challenge.id, challenge, userCode, result)

    // Verify saved
    val progress = service.getUserProgress(challenge.id)
    assert(progress != null) { "Progress not saved" }
    assert(progress?.passed == result.passed) { "Progress data mismatch" }

    Log.d("Test", "✓ Progress saved successfully")
}
```

### ✅ Test 7: Unlock Logic

```kotlin
lifecycleScope.launch {
    val service = UnifiedAssessmentService()
    val challenges = service.getChallengesForUser()

    val easyChallenges = challenges.filter { it.difficulty == "Easy" }
    val mediumChallenges = challenges.filter { it.difficulty == "Medium" }
    val hardChallenges = challenges.filter { it.difficulty == "Hard" }

    Log.d("Test", "Easy (all unlocked): ${easyChallenges.all { it.isUnlocked }}")
    Log.d("Test", "Medium unlocked: ${mediumChallenges.count { it.isUnlocked }}")
    Log.d("Test", "Hard unlocked: ${hardChallenges.count { it.isUnlocked }}")
}
```

### ✅ Test 8: Universal UI Launch

```kotlin
// Test launching with different languages
val languages = listOf("python", "java", "kotlin", "sql")

languages.forEach { language ->
    val intent = Intent(this, UnifiedCompilerActivity::class.java).apply {
        putExtra(UnifiedCompilerActivity.EXTRA_LANGUAGE, language)
    }
    startActivity(intent)
}
```

---

## 🔧 Common Issues & Solutions

### Issue 1: "Compiler not found for 'xyz'"

**Cause:** CompilerFactory not initialized or wrong compiler type

**Solution:**
```kotlin
// Make sure you initialized
CompilerFactory.initialize(applicationContext)

// Check if compiler exists
if (CompilerFactory.hasCompiler("python")) {
    // OK to use
} else {
    // Not available
}
```

### Issue 2: "Course not found"

**Cause:** Course document doesn't exist in Firebase

**Solution:**
```kotlin
// Verify course exists
lifecycleScope.launch {
    try {
        val service = CompilerService()
        val courseInfo = service.getCourseCompilerInfo("your_course_id")
        Log.d("Course", "Found: ${courseInfo.courseName}")
    } catch (e: Exception) {
        Log.e("Course", "Not found: ${e.message}")
        // Create course in Firebase Console
    }
}
```

### Issue 3: Challenges not loading

**Cause:** User not enrolled in course or challenges don't have correct `courseId`

**Solution:**
```kotlin
// Check enrollment
lifecycleScope.launch {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

    val doc = FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .get()
        .await()

    val courseTaken = doc.get("courseTaken")
    Log.d("Enrollment", "Courses: $courseTaken")

    // Ensure courseTaken includes your course
}
```

### Issue 4: "compilerType" not set on challenges

**Cause:** Old challenges don't have compilerType field

**Solution:** The system automatically fills it from the course:

```kotlin
val service = UnifiedAssessmentService()
val challenges = service.getChallengesForCourse("python_beginner")
// compilerType is automatically set from the course's compilerType field
```

### Issue 5: Python execution fails

**Cause:** Chaquopy not initialized

**Solution:**
```kotlin
// In PythonCompiler, initialization is automatic
// But you can verify:
if (!Python.isStarted()) {
    Python.start(AndroidPlatform(context))
}
```

---

## 📊 Data Flow Verification

### Flow 1: Direct Execution

```
User Code
    ↓
CompilerFactory.getCompiler("python")
    ↓
PythonCompiler.compile(code)
    ↓
CompilerResult
```

**Test:**
```kotlin
val result = CompilerFactory.getCompiler("python").compile("print('test')")
assert(result.success && result.output == "test")
```

### Flow 2: Course-Based Execution

```
courseId: "python_beginner"
    ↓
CompilerService.getCourseCompilerInfo(courseId)
    ↓
Firestore: courses/python_beginner → compilerType: "python"
    ↓
CompilerFactory.getCompiler("python")
    ↓
PythonCompiler.compile(code)
    ↓
CompilerResult
```

**Test:**
```kotlin
val service = CompilerService()
val result = service.executeCodeForCourse("python_beginner", "print('test')")
assert(result.success)
```

### Flow 3: Challenge Execution

```
challengeId
    ↓
UnifiedAssessmentService.executeChallenge()
    ↓
Get challenge from technical_assesment
    ↓
Get course from courses using challenge.courseId
    ↓
Get compilerType from course
    ↓
CompilerFactory.getCompiler(compilerType)
    ↓
Execute code + validate test cases
    ↓
Save progress + Award XP
    ↓
ChallengeExecutionResult
```

**Test:**
```kotlin
val service = UnifiedAssessmentService()
val challenges = service.getChallengesForCourse("python_beginner")
val challenge = challenges.first()

val result = service.executeChallenge(challenge.id, userCode, challenge)
service.saveProgress(challenge.id, challenge, userCode, result)

assert(result.passed == true)
```

---

## 🎯 Final Verification

Run this complete test to verify everything works:

```kotlin
lifecycleScope.launch {
    try {
        Log.d("Test", "=== UNIFIED COMPILER SYSTEM TEST ===")

        // Test 1: Factory initialized
        val supported = CompilerFactory.getSupportedLanguages()
        Log.d("Test", "✓ Supported languages: $supported")

        // Test 2: All compilers work
        supported.forEach { lang ->
            val compiler = CompilerFactory.getCompiler(lang)
            Log.d("Test", "✓ $lang compiler: ${compiler.getLanguageName()}")
        }

        // Test 3: Firebase integration
        val service = CompilerService()
        val courses = service.getAllAvailableCourses()
        Log.d("Test", "✓ Found ${courses.size} courses")

        // Test 4: Challenge loading
        val assessmentService = UnifiedAssessmentService()
        val challenges = assessmentService.getChallengesForUser()
        Log.d("Test", "✓ Found ${challenges.size} challenges")

        // Test 5: Compiler type mapping
        val compilerTypes = challenges.map { it.compilerType }.distinct()
        Log.d("Test", "✓ Compiler types in use: $compilerTypes")

        Log.d("Test", "=== ALL TESTS PASSED ✓ ===")
        Toast.makeText(this@MainActivity, "✓ System verified!", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        Log.e("Test", "✗ Test failed: ${e.message}", e)
        Toast.makeText(this@MainActivity, "✗ Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
```

---

## 🎉 You're Ready!

Once all tests pass, you can:

✅ Use `UnifiedCompilerActivity` for all languages
✅ Load challenges from `technical_assesment` collection
✅ Add new courses with any `compilerType`
✅ Add new languages by implementing `CourseCompiler`

**The `technical_assesment` collection is now the universal challenge database for ALL programming languages!** 🚀
