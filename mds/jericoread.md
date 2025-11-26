# 🎓 How to Add a Course to Use the Unified Compiler

## 📋 Required Firebase Fields

When adding a new course to your Firebase `courses` collection, include these fields:

### **Minimum Required Fields for Compiler Integration**

```javascript
{
  // ✅ REQUIRED - Course Identification
  "courseId": "python_beginner",           // Unique ID for the course
  "courseName": "Python Beginner Course",  // Display name

  // ✅ REQUIRED - Compiler Configuration
  "compilerType": "python",                // CRITICAL: Tells system which compiler to use

  // ✅ REQUIRED - Course Details
  "description": "Learn Python basics",
  "category": "Programming",
  "difficulty": "Beginner",                // "Beginner", "Intermediate", "Advanced"

  // ✅ OPTIONAL - Additional Fields
  "thumbnail": "https://...",
  "duration": "4 weeks",
  "lessons": 20,
  "xpReward": 100
}
```

---

## 🔑 The Most Important Field: `compilerType`

### **Supported Values:**

| compilerType | Language | Description |
|--------------|----------|-------------|
| `"python"` | Python 3.x | Uses Chaquopy runtime |
| `"java"` | Java | Uses Janino in-memory compiler |
| `"kotlin"` | Kotlin | Uses Kotlin interpreter |
| `"sql"` | SQL | Uses SQLite database |

### **Examples:**

```javascript
// Python Course
{
  "courseId": "python_fundamentals",
  "courseName": "Python Fundamentals",
  "compilerType": "python"  // ← System will use PythonCompiler
}

// Java Course
{
  "courseId": "java_basics",
  "courseName": "Java Basics",
  "compilerType": "java"  // ← System will use JavaCompiler
}

// SQL Course
{
  "courseId": "sql_queries",
  "courseName": "SQL Queries Masterclass",
  "compilerType": "sql"  // ← System will use SQLExecutor
}

// Kotlin Course
{
  "courseId": "kotlin_android",
  "courseName": "Kotlin for Android",
  "compilerType": "kotlin"  // ← System will use KotlinCompiler
}
```

---

## 📚 How to Add Technical Assessments/Challenges

### **Step 1: Add Course to `courses` Collection**

```javascript
// Firebase Console → Firestore → courses → Add Document
{
  "courseId": "python_beginner",
  "courseName": "Python Beginner Course",
  "compilerType": "python",  // ← This is KEY!
  "description": "Learn Python basics",
  "category": "Programming",
  "difficulty": "Beginner"
}
```

### **Step 2: Add Challenges to `technical_assesment` Collection**

```javascript
// Firebase Console → Firestore → technical_assesment → Add Document
{
  "courseId": "python_beginner",           // ← Must match course's courseId
  "title": "Fix the Print Statement",
  "description": "Fix the code to print 'Hello, World!'",
  "difficulty": "Easy",                    // "Easy", "Medium", "Hard"
  "brokenCode": "print('Fix me')",         // Starting code
  "correctOutput": "Hello, World!",        // Expected output
  "hint": "Use proper string syntax",
  "category": "Syntax",
  "status": "available",
  "createdAt": "2024-01-15"
}
```

### **Challenge Unlock Logic:**

The system automatically unlocks challenges based on difficulty:

1. ✅ **Easy** challenges: Unlocked immediately
2. 🔒 **Medium** challenges: Unlocked after completing all Easy challenges
3. 🔒 **Hard** challenges: Unlocked after completing all Medium challenges

---

## 🎯 Full Example: Adding a New Kotlin Course

### **Step-by-Step Guide:**

#### **1. Add to `courses` Collection**

```javascript
// Document ID: kotlin_basics (or auto-generated)
{
  "courseId": "kotlin_basics",
  "courseName": "Kotlin Basics",
  "compilerType": "kotlin",  // ← IMPORTANT!
  "description": "Learn Kotlin programming from scratch",
  "category": "Programming",
  "difficulty": "Beginner",
  "thumbnail": "https://example.com/kotlin-icon.png",
  "duration": "3 weeks",
  "lessons": 15,
  "xpReward": 150
}
```

#### **2. Add Easy Challenge to `technical_assesment`**

```javascript
// Document ID: auto-generated
{
  "courseId": "kotlin_basics",  // ← Links to course
  "title": "Hello Kotlin",
  "description": "Write a function that prints 'Hello, Kotlin!'",
  "difficulty": "Easy",
  "brokenCode": "fun main() {\n    // Write your code here\n}",
  "correctOutput": "Hello, Kotlin!",
  "hint": "Use println() function",
  "category": "Basics",
  "status": "available",
  "createdAt": "2024-01-15"
}
```

#### **3. Add Medium Challenge**

```javascript
{
  "courseId": "kotlin_basics",
  "title": "Calculate Sum",
  "description": "Write a function that calculates sum of two numbers",
  "difficulty": "Medium",
  "brokenCode": "fun sum(a: Int, b: Int): Int {\n    // Implement this\n    return 0\n}",
  "correctOutput": "15",  // For sum(7, 8)
  "hint": "Use the + operator",
  "category": "Functions",
  "status": "available",
  "createdAt": "2024-01-15"
}
```

#### **4. Add Hard Challenge**

```javascript
{
  "courseId": "kotlin_basics",
  "title": "FizzBuzz",
  "description": "Implement the classic FizzBuzz algorithm",
  "difficulty": "Hard",
  "brokenCode": "fun fizzBuzz(n: Int) {\n    // Implement FizzBuzz\n}",
  "correctOutput": "1\n2\nFizz\n4\nBuzz\nFizz\n...",
  "hint": "Check divisibility by 3 and 5",
  "category": "Logic",
  "status": "available",
  "createdAt": "2024-01-15"
}
```

---

## 🔄 How the System Works

### **Automatic Compiler Detection:**

```kotlin
// When user clicks "Practice" on a course:
1. System reads courseId: "python_beginner"
2. Fetches course document from Firebase
3. Reads compilerType: "python"
4. CompilerFactory.getCompiler("python")
5. Returns PythonCompiler instance
6. User can now run Python code!
```

### **Challenge Flow:**

```
User enrolls in "Python Beginner" course
    ↓
System loads challenges from technical_assesment
    WHERE courseId = "python_beginner"
    ↓
Applies unlock logic (Easy first)
    ↓
User selects a challenge
    ↓
System detects compilerType: "python"
    ↓
Opens UnifiedCompilerActivity with Python compiler
    ↓
User writes code → System executes via PythonCompiler
    ↓
Validates output → Saves progress → Awards XP
```

---

## ✅ Checklist for Adding a New Course

- [ ] Add course document to `courses` collection
- [ ] Set `compilerType` field correctly (`"python"`, `"java"`, `"kotlin"`, or `"sql"`)
- [ ] Add at least 1 Easy challenge to `technical_assesment`
- [ ] Ensure challenge's `courseId` matches course's `courseId`
- [ ] Set challenge `difficulty`: "Easy", "Medium", or "Hard"
- [ ] Test by enrolling in the course and clicking "Practice"

---

## 🚨 Common Mistakes to Avoid

### ❌ **Wrong:**
```javascript
{
  "courseId": "python_course",
  "compilerType": "Python"  // ❌ Capital letter won't work
}
```

### ✅ **Correct:**
```javascript
{
  "courseId": "python_course",
  "compilerType": "python"  // ✅ Lowercase
}
```

---

### ❌ **Wrong:**
```javascript
{
  "courseId": "my_python_course",
  "compilerType": "py"  // ❌ Wrong abbreviation
}
```

### ✅ **Correct:**
```javascript
{
  "courseId": "my_python_course",
  "compilerType": "python"  // ✅ Full name
}
```

---

### ❌ **Wrong:**
```javascript
// Challenge with different courseId
{
  "courseId": "python_basics",  // ❌ Doesn't match course
  "title": "Fix Code"
}

// Course
{
  "courseId": "python_beginner"  // ← Mismatch!
}
```

### ✅ **Correct:**
```javascript
// Challenge
{
  "courseId": "python_beginner",  // ✅ Matches course
  "title": "Fix Code"
}

// Course
{
  "courseId": "python_beginner"  // ✅ Same ID
}
```

---

## 🎨 Course Categories Examples

You can organize courses by category:

```javascript
// Programming Courses
{ "category": "Programming", "compilerType": "python" }
{ "category": "Programming", "compilerType": "java" }
{ "category": "Programming", "compilerType": "kotlin" }

// Database Courses
{ "category": "Database", "compilerType": "sql" }

// Web Development (Future)
{ "category": "Web Development", "compilerType": "javascript" }  // When you add JS compiler
```

---

## 📊 User Progress Tracking

When users complete challenges, the system automatically saves progress:

```javascript
// Firestore Path: user_progress/{userId}/technical_assessment_progress/{challengeId}
{
  "challengeId": "abc123",
  "challengeTitle": "Fix the Print Statement",
  "status": "completed",          // "not_started", "in_progress", "completed"
  "passed": true,
  "attempts": 3,
  "bestScore": 100,
  "lastAttemptDate": Timestamp,
  "timeTaken": 12345,             // milliseconds
  "userCode": "print('Hello, World!')",
  "compilerType": "python",       // Tracked automatically
  "updatedAt": Timestamp
}
```

---

## 🚀 Quick Start Template

Copy this template to add a new course:

```javascript
// === courses collection ===
{
  "courseId": "YOUR_COURSE_ID",
  "courseName": "Your Course Name",
  "compilerType": "python",  // ← Change to: python, java, kotlin, or sql
  "description": "Course description",
  "category": "Programming",
  "difficulty": "Beginner",
  "xpReward": 100
}

// === technical_assesment collection ===
{
  "courseId": "YOUR_COURSE_ID",  // ← Must match above
  "title": "Challenge Title",
  "description": "Challenge description",
  "difficulty": "Easy",
  "brokenCode": "// Starting code",
  "correctOutput": "Expected output",
  "hint": "Helpful hint",
  "category": "Basics",
  "status": "available",
  "createdAt": "2024-01-15"
}
```

---

## 📞 Need Help?

If you encounter issues:

1. ✅ Check that `compilerType` is lowercase: `"python"`, not `"Python"`
2. ✅ Verify `courseId` matches between `courses` and `technical_assesment`
3. ✅ Ensure `difficulty` is exactly: `"Easy"`, `"Medium"`, or `"Hard"`
4. ✅ Test with `TestLauncher.testJava(context)` or similar functions
5. ✅ Check Android Studio Logcat for error messages

---

## 🎉 You're Ready!

The unified compiler system will automatically:
- ✅ Detect the compiler type from your course
- ✅ Load the correct compiler (Python, Java, Kotlin, or SQL)
- ✅ Execute user code
- ✅ Validate outputs
- ✅ Track progress
- ✅ Award XP

Just add your course data to Firebase and the system handles the rest! 🚀
