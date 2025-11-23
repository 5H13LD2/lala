📚 Overview

The Unified Compiler System is a scalable, plug-and-play architecture that allows your Android app to execute code for multiple programming languages. It uses the Strategy Pattern and a Factory Registry to make adding new languages effortless.

All technical assessments and coding challenges now use a single unified compiler, so you no longer need separate compilers per course. The system supports multiple languages, and assessments from Firebase are rendered client-side using the proper language compiler automatically.

Supported Languages (Built-in)

✅ Python (via Chaquopy)

✅ Java (via Janino)

✅ Kotlin (simple interpreter)

✅ PHP

✅ Ruby (future support)

Easy to Add

Any new language like JavaScript, Go, Swift, C++, Rust, etc.

┌───────────────────────────────────────────────┐
│             CLIENT (App/Activity)            │
│   - User enters code                          │
│   - Selects challenge / language             │
└─────────────────────┬─────────────────────────┘
                      │
                      ▼
┌───────────────────────────────────────────────┐
│                CompilerService                │
│ - executeCodeForCourse(courseId, code)        │
│ - getCompilerForLanguage(language)            │
└───────────────┬───────────────────────────────┘
                │
                │ Fetches challenge/course info from Firebase
                ▼
┌───────────────────────────────────────────────┐
│            Firebase Firestore                  │
│ Collection: technical_assessments             │
│ Fields include:                               │
│   - courseId ("java", "python", etc.)        │
│   - brokenCode                                 │
│   - correctOutput                              │
│   - hints, difficulty, category, title        │
│   - author, createdAt, updatedAt              │
└───────────────┬───────────────────────────────┘
                │
                ▼
┌───────────────────────────────────────────────┐
│               CompilerFactory                 │
│ Registry Map:                                 │
│   "python"  → PythonCompiler                  │
│   "java"    → JavaCompiler                    │
│   "kotlin"  → KotlinCompiler                  │
│   "php"     → PHPCompiler                     │
│   "ruby"    → RubyCompiler [Future]           │
└───────────────┬───────────────────────────────┘
                │
                ▼
┌───────────────────────────────────────────────┐
│               LanguageCompiler Interface      │
│  + compile(code, config): CompilerResult     │
│  + getLanguageId(): String                    │
│  + getFileExtension(): String                 │
└───────────────────────────────────────────────┘
                │
                ▼
┌───────────────────────────────────────────────┐
│             Execute Code & Return Result      │
│  CompilerResult:                               │
│    - success: Boolean                          │
│    - output: String                            │
│    - error: String?                            │
│    - executionTime: Long                        │
│    - testCasesPassed: Int                      │
│    - totalTestCases: Int                       │
└───────────────────────────────────────────────┘


UNIFIED COMPILER SYSTEM
📚 Overview

The Unified Compiler System is designed to execute any programming challenge stored in the technical_assessment collection of Firebase. It is a truly multi-language compiler, supporting Python, Java, Kotlin, PHP, Ruby, and future languages—all without requiring separate compiler files per challenge.

Key Goals

Execute all technical_assessment challenges automatically.

Support multiple programming languages using a single unified compiler.

No need for separate per-language compilers per course or challenge.

Easily extendable: add new languages in the compiler registry once.

Client-side rendering uses the correct compiler automatically.

🎯 Architecture Overview
Firebase Firestore: technical_assessment collection
  ├─ brokenCode
  ├─ compilerType (python, java, kotlin, php, etc.)
  ├─ title, hints, difficulty, courseId…
  ↓
UnifiedCompiler.runCode(language=compilerType, code=brokenCode)
  ↓
CompilerResult:
    - success: Boolean
    - output: String
    - error: String?
    - testCasesPassed: Int
    - totalTestCases: Int
  ↓
Client App (Activity/Fragment) renders result
Simplified Flow

Fetch challenge from Firebase.

Extract brokenCode and compilerType.

Pass brokenCode to UnifiedCompiler with the language.

UnifiedCompiler selects the right execution logic from the registry.

Returns CompilerResult with output, errors, and test case info.

Client app renders output to the user.

🔹 UnifiedCompiler Example (Kotlin)
object UnifiedCompiler {

    private val registry = mutableMapOf<String, (String) -> String>(
        "python" to { code -> "Python output:\n$code" },
        "java" to { code -> "Java output:\n$code" },
        "kotlin" to { code -> "Kotlin output:\n$code" },
        "php" to { code -> "PHP output:\n$code" },
        "ruby" to { code -> "Ruby output:\n$code" }
    )

    fun runCode(language: String, code: String): CompilerResult {
        val key = language.trim().lowercase()
        val executor = registry[key]
            ?: return CompilerResult(success=false, output="", error="Unsupported language: $language")
        return CompilerResult(success=true, output=executor(code))
    }
}

data class CompilerResult(
    val success: Boolean,
    val output: String,
    val error: String? = null,
    val testCasesPassed: Int = 0,
    val totalTestCases: Int = 0
)
🔹 Usage with technical_assessment
val challenge = getChallengeFromFirebase("java_challenge001")

val result = UnifiedCompiler.runCode(
    language = challenge.compilerType,
    code = challenge.brokenCode
)

if(result.success) {
    println("Output: ${result.output}")
} else {
    println("Error: ${result.error}")
}

Works with any challenge in technical_assessment.

Automatically selects the correct compiler based on compilerType.

Supports multiple languages with no per-challenge compiler files.

Future languages: add to registry once; all challenges automatically supported.

✅ Summary of Features

One interface for all programming languages.

Multi-language support (Python, Java, Kotlin, PHP, Ruby…).

Direct execution of technical_assessment challenges.

Extensible registry for adding new languages.

Simplified architecture: no per-language files required per challenge.

Client-side rendering automatically shows output or errors.

This system ensures that all programming challenges are unified under a single, extensible compiler, making the execution and evaluation process consistent, maintainable, and scalable.

Firebase technical_assessment
  ├─ brokenCode
  ├─ compilerType (python, java, kotlin, php, ruby…)
  ├─ hints, difficulty, title…
  ↓
UnifiedCompiler.runCode(language=compilerType, code=brokenCode)
  ├─ Python → Chaquopy
  ├─ Java → Janino
  ├─ Kotlin → Kotlin Scripting
  ├─ PHP → PHP embedded runtime
  └─ Ruby → JRuby
  ↓
CompilerResult:
  - success, output, error, executionTime, testCasesPassed
  ↓
Client App renders result


ALL-IN-ONE UNIFIED COMPILER SYSTEM
📚 Overview

The All-in-One Unified Compiler System is designed to execute any programming challenge stored in the technical_assessment collection of Firebase. It supports multiple programming languages (Python, Java, Kotlin, PHP, Ruby, and more) through a single unified interface.

Key Goals

Execute all technical_assessment challenges automatically.

Support multiple programming languages using one unified compiler.

No need for separate compiler files per course or challenge.

Easy to extend: add new languages in the compiler registry once.

Automatic language execution based on compilerType field in Firebase.

🎯 Architecture Overview
Firebase technical_assessment
  ├─ brokenCode
  ├─ compilerType (python, java, kotlin, php, ruby…)
  ├─ hints, difficulty, title…
  ↓
UnifiedCompiler.runCode(language=compilerType, code=brokenCode)
  ├─ Python → Chaquopy
  ├─ Java → Janino
  ├─ Kotlin → Kotlin Scripting
  ├─ PHP → PHP embedded runtime
  └─ Ruby → JRuby
  ↓
CompilerResult:
  - success, output, error, executionTime, testCasesPassed
  ↓
Client App renders the output
Flow

Fetch challenge from Firebase.

Extract brokenCode and compilerType.

Pass brokenCode to UnifiedCompiler with the language.

UnifiedCompiler selects the correct execution logic from the registry.

Returns CompilerResult with output, errors, and test case info.

Client app renders the output.

🔹 UnifiedCompiler Kotlin Skeleton
object UnifiedCompiler {

    private val registry = mutableMapOf<String, suspend (String) -> CompilerResult>(
        "python" to { code -> PythonExecutor.execute(code) },
        "java" to { code -> JavaExecutor.execute(code) },
        "kotlin" to { code -> KotlinExecutor.execute(code) },
        "php" to { code -> PhpExecutor.execute(code) },
        "ruby" to { code -> RubyExecutor.execute(code) }
    )

    suspend fun runCode(language: String, code: String): CompilerResult {
        val key = language.trim().lowercase()
        val executor = registry[key]
            ?: return CompilerResult(success=false, output="", error="Unsupported language: $language")
        return executor(code)
    }
}

data class CompilerResult(
    val success: Boolean,
    val output: String,
    val error: String? = null,
    val testCasesPassed: Int = 0,
    val totalTestCases: Int = 0,
    val executionTime: Long = 0
)

Registry handles all supported languages.

runCode() provides a single entry point.

Adding new languages: just register in the registry.

🔹 Usage with technical_assessment
val challenge = getChallengeFromFirebase("java_challenge001")

lifecycleScope.launch {
    val result = UnifiedCompiler.runCode(
        language = challenge.compilerType,
        code = challenge.brokenCode
    )

    if(result.success) {
        println(result.output)
    } else {
        println(result.error)
    }
}

Works with any challenge in technical_assessment.

Automatically selects the correct language executor.

No per-language compiler files required.

🔹 Android Libraries / Dependencies

To execute multiple languages on Android, the following libraries are recommended:

Language	Library / API	Gradle Dependency
Python	Chaquopy	id 'com.chaquo.python'
Java	Janino	org.codehaus.janino
Kotlin	Kotlin Scripting	org.jetbrains.kotlin
PHP	Embedded PHP (Porkchop / JNI)	Custom setup
Ruby	JRuby	org.jruby

All libraries are registered in the UnifiedCompiler registry.

App code interacts only with UnifiedCompiler.runCode().

Libraries handle the internal execution, while the interface remains unified.

✅ Summary of Features

Single unified interface for all languages.

Multi-language support (Python, Java, Kotlin, PHP, Ruby…).

Direct execution of technical_assessment challenges.

Extensible registry for adding new languages.

Simplified architecture: no per-language files needed per challenge.

Client-side rendering automatically shows output, errors, and test case results.

This system ensures that all programming challenges are executed in a unified, maintainable, and scalable way.


















# Unified Compiler System Documentation

## 📚 Overview

The **Unified Compiler System** is a scalable, plug-and-play architecture that allows you to run code for multiple programming languages in your Android app. It uses the **Strategy Pattern** and a **Factory Registry** to make adding new languages effortless.

### Supported Languages (Built-in)
- ✅ **Python** (via Chaquopy)
- ✅ **Java** (via Janino)
- ✅ **SQL** (via SQLite - query execution)
- ✅ **Kotlin** (simple interpreter)

### Easy to Add
- Ruby, JavaScript, Go, Swift, C++, Rust, PHP, etc.

---

## 🎯 Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    USER CODE REQUEST                     │
│              (from Activity/Fragment/ViewModel)          │
└─────────────────┬───────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────┐
│                   CompilerService                        │
│  - getCompilerForCourse(courseId)                       │
│  - executeCodeForCourse(courseId, code)                 │
└─────────────────┬───────────────────────────────────────┘
                  │
                  │ Fetches course info from Firebase
                  ▼
┌─────────────────────────────────────────────────────────┐
│                    Firebase Firestore                    │
│                                                          │
│  courses/{courseId}                                      │
│    - compilerType: "python"                             │
│    - courseName: "Python Basics"                        │
└─────────────────┬───────────────────────────────────────┘
                  │
                  │ Passes compilerType to Factory
                  ▼
┌─────────────────────────────────────────────────────────┐
│                   CompilerFactory                        │
│                                                          │
│  Registry Map:                                           │
│    "python"  → PythonCompiler()                         │
│    "java"    → JavaCompiler()                           │
│    "sql"     → SQLExecutor()                            │
│    "kotlin"  → KotlinCompiler()                         │
│    "ruby"    → RubyCompiler()     [Future]              │
└─────────────────┬───────────────────────────────────────┘
                  │
                  │ Returns appropriate compiler
                  ▼
┌─────────────────────────────────────────────────────────┐
│                   CourseCompiler                         │
│              (Interface implemented by all)              │
│                                                          │
│  + compile(code, config): CompilerResult                │
│  + getLanguageId(): String                              │
│  + validateSyntax(code): String?                        │
└─────────────────┬───────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────┐
│              Execute Code & Return Result                │
│                                                          │
│  CompilerResult:                                         │
│    - success: Boolean                                    │
│    - output: String                                      │
│    - error: String?                                      │
│    - executionTime: Long                                 │
│    - testCasesPassed: Int                               │
└──────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### 1. Initialize the System

In your `Application` class or `MainActivity.onCreate()`:

```kotlin
import com.labactivity.lala.UNIFIEDCOMPILER.CompilerFactory

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize the compiler factory
        CompilerFactory.initialize(this)
    }
}
```

### 2. Use in Your Activity/Fragment

**Method A: Direct Compiler Access**

```kotlin
import com.labactivity.lala.UNIFIEDCOMPILER.CompilerFactory
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import kotlinx.coroutines.launch

class CodeEditorActivity : AppCompatActivity() {

    fun executeCode() {
        lifecycleScope.launch {
            // Get compiler by type
            val compiler = CompilerFactory.getCompiler("python")

            // Execute code
            val result = compiler.compile("""
                print("Hello, World!")
                for i in range(5):
                    print(i)
            """.trimIndent())

            // Display result
            if (result.success) {
                outputTextView.text = result.output
            } else {
                errorTextView.text = result.error
            }
        }
    }
}
```

**Method B: Using CompilerService (Recommended)**

```kotlin
import com.labactivity.lala.UNIFIEDCOMPILER.services.CompilerService

class TechnicalAssessmentActivity : AppCompatActivity() {

    private val compilerService = CompilerService()

    fun runChallenge(courseId: String, userCode: String) {
        lifecycleScope.launch {
            try {
                // Automatically gets the right compiler for the course
                val result = compilerService.executeCodeForCourse(
                    courseId = courseId,
                    code = userCode
                )

                displayResult(result)
            } catch (e: Exception) {
                showError(e.message)
            }
        }
    }
}
```

**Method C: Extension Functions (Cleanest)**

```kotlin
import com.labactivity.lala.UNIFIEDCOMPILER.services.executeAs
import com.labactivity.lala.UNIFIEDCOMPILER.services.executeForCourse

lifecycleScope.launch {
    // Execute as specific language
    val result1 = """
        System.out.println("Hello from Java!");
    """.executeAs("java")

    // Execute for a specific course
    val result2 = """
        SELECT * FROM employees WHERE salary > 50000;
    """.executeForCourse("sql_beginner_course")

    println(result1.output)
    println(result2.output)
}
```

---

## 🔧 Configuration Options

### CompilerConfig

```kotlin
val config = CompilerConfig(
    timeout = 30000,              // Max execution time (ms)
    maxOutputLength = 10000,       // Max output characters
    enableStdin = true,            // Allow stdin input (Python)
    stdinInput = "John\n25\n",    // Stdin data
    testCases = listOf(           // Test cases for validation
        TestCase(
            input = "",
            expectedOutput = "Hello, World!",
            description = "Basic output test"
        )
    )
)

val result = compiler.compile(code, config)
```

### CompilerResult

```kotlin
data class CompilerResult(
    val success: Boolean,           // Execution success?
    val output: String,             // Program output
    val error: String?,             // Error message if failed
    val executionTime: Long,        // Execution time in ms
    val compiledSuccessfully: Boolean,  // Compilation success?
    val testCasesPassed: Int,       // Number of test cases passed
    val totalTestCases: Int,        // Total test cases
    val metadata: Map<String, Any>  // Additional data
)
```

---

## 📝 Firebase Integration

### Course Document Structure

In Firestore, create courses with this structure:

**Collection:** `courses`
**Document ID:** `python_beginner_course`

```json
{
  "courseId": "python_beginner_course",
  "courseName": "Python Beginner Course",
  "compilerType": "python",
  "version": "3.x",
  "supportedFeatures": ["basic", "functions", "loops"],
  "description": "Learn Python from scratch",
  "difficulty": "beginner"
}
```

**Important:** The `compilerType` field must match the language ID registered in `CompilerFactory`.

### Compiler Type Mapping

| compilerType | Language | Implementation |
|--------------|----------|----------------|
| `python`     | Python   | PythonCompiler (Chaquopy) |
| `java`       | Java     | JavaCompiler (Janino) |
| `sql`        | SQL      | SQLExecutor (SQLite) |
| `kotlin`     | Kotlin   | KotlinCompiler (Simple Interpreter) |

---

## ➕ Adding a New Language (Step-by-Step)

### Example: Adding Ruby Support

#### Step 1: Create RubyCompiler.kt

```kotlin
package com.labactivity.lala.UNIFIEDCOMPILER.compilers

import com.labactivity.lala.UNIFIEDCOMPILER.CourseCompiler
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RubyCompiler : CourseCompiler {

    override suspend fun compile(code: String, config: CompilerConfig): CompilerResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            // TODO: Implement Ruby execution
            // Option 1: Use JRuby library
            // Option 2: Execute external Ruby interpreter

            CompilerResult(
                success = true,
                output = "Ruby execution result here",
                executionTime = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            CompilerResult(
                success = false,
                output = "",
                error = e.message,
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        }
    }

    override fun getLanguageId(): String = "ruby"
    override fun getLanguageName(): String = "Ruby"
    override fun getFileExtension(): String = ".rb"
}
```

#### Step 2: Register in CompilerFactory.kt

Open `CompilerFactory.kt` and add to `registerDefaultCompilers()`:

```kotlin
private fun registerDefaultCompilers() {
    registry["python"] = PythonCompiler(applicationContext)
    registry["java"] = JavaCompiler()
    registry["sql"] = SQLExecutor(applicationContext)
    registry["kotlin"] = KotlinCompiler()

    // Add your new compiler here
    registry["ruby"] = RubyCompiler()  // ✅ ADD THIS LINE
}
```

#### Step 3: Add Course in Firebase

In Firebase Console, add a new document:

**Collection:** `courses`
**Document ID:** `ruby_beginner_course`

```json
{
  "courseId": "ruby_beginner_course",
  "courseName": "Ruby Beginner Course",
  "compilerType": "ruby",
  "version": "2.7",
  "supportedFeatures": ["basic", "oop"]
}
```

#### Step 4: Use It!

```kotlin
lifecycleScope.launch {
    val service = CompilerService()
    val result = service.executeCodeForCourse("ruby_beginner_course", """
        puts "Hello from Ruby!"
        5.times { |i| puts i }
    """)

    println(result.output)
}
```

**That's it!** No need to modify any other code. The system automatically picks up the new compiler.

---

## 🔍 Advanced Features

### 1. Test Case Validation

```kotlin
val testCases = listOf(
    TestCase(
        input = "",
        expectedOutput = "Hello, World!",
        description = "Test basic output"
    ),
    TestCase(
        input = "5",
        expectedOutput = "25",
        description = "Test square calculation"
    )
)

val config = CompilerConfig(testCases = testCases)
val result = compiler.compile(code, config)

println("Passed: ${result.testCasesPassed}/${result.totalTestCases}")
```

### 2. Syntax Validation Before Execution

```kotlin
val compiler = CompilerFactory.getCompiler("python")
val error = compiler.validateSyntax(code)

if (error != null) {
    showError("Syntax Error: $error")
} else {
    // Safe to execute
    val result = compiler.compile(code)
}
```

### 3. Check Supported Compilers

```kotlin
val service = CompilerService()

// Get all supported languages
val languages = service.getSupportedCompilers()
println("Supported: ${languages.joinToString(", ")}")

// Check if specific compiler exists
if (service.isCompilerSupported("rust")) {
    println("Rust is supported!")
} else {
    println("Rust not available yet")
}
```

### 4. Get Courses by Compiler Type

```kotlin
val service = CompilerService()

// Get all Python courses
val pythonCourses = service.getCoursesByCompilerType("python")
pythonCourses.forEach { course ->
    println("${course.courseName} (${course.compilerType})")
}
```

---

## 🏗️ Use Cases

### 1. In-App Code Compiler

```kotlin
class CompilerActivity : AppCompatActivity() {
    private val compilerService = CompilerService()

    fun runCode(language: String, code: String) {
        lifecycleScope.launch {
            val compiler = CompilerFactory.getCompiler(language)
            val result = compiler.compile(code)
            displayResult(result)
        }
    }
}
```

### 2. Technical Assessments

```kotlin
class AssessmentViewModel : ViewModel() {

    suspend fun evaluateChallenge(courseId: String, userCode: String): Int {
        val service = CompilerService()

        val testCases = getTestCasesFromFirebase(challengeId)
        val config = CompilerConfig(testCases = testCases)

        val result = service.executeCodeForCourse(courseId, userCode, config)

        return (result.testCasesPassed * 100) / result.totalTestCases
    }
}
```

### 3. Coding Challenges

```kotlin
class ChallengeActivity : AppCompatActivity() {

    fun submitSolution(challenge: Challenge, userCode: String) {
        lifecycleScope.launch {
            val compiler = CompilerFactory.getCompiler(challenge.compilerType)

            val config = CompilerConfig(
                testCases = challenge.testCases,
                timeout = 10000
            )

            val result = compiler.compile(userCode, config)

            if (result.testCasesPassed == result.totalTestCases) {
                showSuccess("All test cases passed! +${challenge.xp} XP")
            } else {
                showPartial("${result.testCasesPassed}/${result.totalTestCases} passed")
            }
        }
    }
}
```

---

## 🛡️ Security Considerations

### SQL Executor

The `SQLExecutor` blocks destructive operations:

```kotlin
Blocked Keywords:
- DROP, DELETE, INSERT, UPDATE
- CREATE, ALTER, TRUNCATE
- EXEC, EXECUTE, GRANT, REVOKE

Also blocks:
- Comments (-- and /* */)
- Multiple statements (;)
- Queries longer than 1000 characters
```

### Timeout Protection

All compilers have configurable timeouts to prevent infinite loops:

```kotlin
val config = CompilerConfig(timeout = 5000) // 5 seconds max
val result = compiler.compile(infiniteLoopCode, config)
// Will timeout and return error
```

---

## 📊 File Structure

```
UNIFIEDCOMPILER/
├── CourseCompiler.kt              # Core interface
├── CompilerFactory.kt             # Registry & factory
├── models/
│   └── CompilerModels.kt         # Data classes
├── compilers/
│   ├── PythonCompiler.kt         # Python implementation
│   ├── JavaCompiler.kt           # Java implementation
│   ├── SQLExecutor.kt            # SQL implementation
│   ├── KotlinCompiler.kt         # Kotlin implementation
│   └── FUTURE_EXAMPLES.kt        # Templates for new languages
├── services/
│   └── CompilerService.kt        # Firebase integration
└── README.md                      # This file
```

---

## 🎓 Best Practices

### 1. Always Initialize in Application Class

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CompilerFactory.initialize(this)
    }
}
```

### 2. Use CompilerService for Course-Based Execution

```kotlin
// ✅ Good - Automatic compiler selection
compilerService.executeCodeForCourse(courseId, code)

// ❌ Avoid - Manual type handling
val type = getCourseType(courseId)
val compiler = CompilerFactory.getCompiler(type)
```

### 3. Handle Errors Gracefully

```kotlin
try {
    val result = compilerService.executeCodeForCourse(courseId, code)
    if (result.success) {
        showOutput(result.output)
    } else {
        showError(result.error ?: "Unknown error")
    }
} catch (e: Exception) {
    showError("Compiler not found: ${e.message}")
}
```

### 4. Validate Before Adding Course

```kotlin
val (isValid, error) = compilerService.validateCourseCompiler(courseId)
if (!isValid) {
    println("Course validation failed: $error")
}
```

---

## 🐛 Troubleshooting

### "Compiler not found for 'xyz'"

**Cause:** The compiler type is not registered.

**Solution:**
1. Check if the compiler is implemented
2. Verify it's registered in `CompilerFactory.registerDefaultCompilers()`
3. Ensure `compilerType` in Firebase matches `getLanguageId()`

### "CompilerFactory not initialized"

**Cause:** You didn't call `CompilerFactory.initialize(context)`

**Solution:** Add initialization in `Application.onCreate()` or `MainActivity.onCreate()`

### SQL queries fail with "Security Error"

**Cause:** You're using blocked keywords (DROP, DELETE, etc.)

**Solution:** Only SELECT queries are allowed. For other operations, use a different approach.

---

## 📞 Support

For issues or questions:
- Check `FUTURE_EXAMPLES.kt` for implementation templates
- Review existing compilers (PythonCompiler, JavaCompiler) for reference
- Ensure Firebase course documents have correct `compilerType` field

---

## 🎉 Summary

This unified compiler system gives you:

✅ **One interface** for all languages
✅ **Plug-and-play** architecture - add languages without touching existing code
✅ **Firebase integration** - course-based compiler selection
✅ **Test case validation** - automatic grading
✅ **Security** - timeout protection and SQL safety
✅ **Scalability** - easily add Ruby, JavaScript, Go, Swift, etc.

**No more separate Java, Python, SQL compilers!** Everything is unified and extensible.
