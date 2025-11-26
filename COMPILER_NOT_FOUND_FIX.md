# 🔧 Fixing "Compiler Not Found" Error

## Problem Summary

You're getting a **"Compiler not found. Requested: '', Supported: [java, kotlin, python]"** error when running code through the Unified Compiler system.

---

## Root Cause Analysis

The error occurs because `compilerType` is **empty** (`""`) when passed to `CompilerFactory.getCompiler()`.

### The Flow of the Bug:

```
1. TechnicalAssessmentAdapter passes challenge.compilerType to UnifiedCompilerActivity
   └── challenge.compilerType is "" (empty)

2. UnifiedCompilerActivity receives EXTRA_LANGUAGE as ""
   └── currentLanguage becomes ""

3. CompilerFactory.getCompiler("") is called
   └── "" doesn't match any compiler → Exception thrown
```

### Why is `compilerType` empty?

In your `UnifiedChallenge` model:

```kotlin
// Runtime fields (not in Firebase)
var compilerType: String = "", // "python", "java", "kotlin", etc. - auto-filled from course
```

This field is marked as "auto-filled from course" but **it's never actually being filled!**

---

## 🛠️ Solutions

### Solution 1: Quick Fix (Recommended for immediate use)

Modify `UnifiedCompilerActivity` to handle empty language gracefully.

**File:** `UnifiedCompilerActivity.kt`

Find the `loadInitialData()` function and update it:

```kotlin
private fun loadInitialData() {
    // Load course ID if provided
    courseId = intent.getStringExtra(EXTRA_COURSE_ID)

    // Load language - with fallback logic
    val intentLanguage = intent.getStringExtra(EXTRA_LANGUAGE)
    
    // Determine language from multiple sources
    currentLanguage = when {
        // 1. Use intent language if valid
        !intentLanguage.isNullOrBlank() && isValidLanguage(intentLanguage) -> intentLanguage.lowercase()
        
        // 2. Try to derive from courseId
        courseId != null -> deriveLanguageFromCourseId(courseId!!)
        
        // 3. Default to python
        else -> "python"
    }
    
    // Select the chip for the determined language
    selectLanguageChip(currentLanguage)

    // Load initial code if provided
    val initialCode = intent.getStringExtra(EXTRA_INITIAL_CODE)
    if (!initialCode.isNullOrEmpty()) {
        codeEditor.setText(initialCode)
    } else {
        loadSampleCode()
    }

    // Update UI for initial language
    updateEditorForLanguage()
}

/**
 * Check if a language string is valid
 */
private fun isValidLanguage(language: String): Boolean {
    val validLanguages = listOf("python", "python3", "py", "java", "kotlin", "kt")
    return language.lowercase().trim() in validLanguages
}

/**
 * Derive language from courseId naming convention
 */
private fun deriveLanguageFromCourseId(courseId: String): String {
    return when {
        courseId.contains("python", ignoreCase = true) -> "python"
        courseId.contains("java", ignoreCase = true) -> "java"
        courseId.contains("kotlin", ignoreCase = true) -> "kotlin"
        courseId.contains("py", ignoreCase = true) -> "python"
        courseId.contains("kt", ignoreCase = true) -> "kotlin"
        else -> "python" // Safe default
    }
}
```

Also update `executeCode()` to use `currentLanguage` directly:

```kotlin
private fun executeCode(code: String) {
    lifecycleScope.launch {
        try {
            showLoading(true, "Compiling...")
            errorCard.visibility = View.GONE
            testResultsCard.visibility = View.GONE

            // FIXED: Always use currentLanguage (which has fallback logic)
            val compiler = CompilerFactory.getCompiler(currentLanguage)
            val result = compiler.compile(code, CompilerConfig())

            showLoading(false)

            // Display results (keep existing code)
            if (result.success) {
                // ... existing success handling
            } else {
                // ... existing error handling
            }

        } catch (e: IllegalArgumentException) {
            // ... existing exception handling
        } catch (e: Exception) {
            // ... existing exception handling
        }
    }
}
```

---

### Solution 2: Fix at the Source (UnifiedAssessmentService)

Make sure `compilerType` is properly set when fetching challenges.

**File:** `UnifiedAssessmentService.kt`

Update `getChallengesForCourse()`:

```kotlin
/**
 * Get all challenges for a specific course
 * Automatically sets compilerType based on courseId
 */
suspend fun getChallengesForCourse(courseId: String): List<UnifiedChallenge> {
    return try {
        // Determine compiler type for this course
        val compilerType = deriveCompilerType(courseId)
        
        val snapshot = firestore.collection(COLLECTION_TECHNICAL_ASSESSMENT)
            .whereEqualTo("courseId", courseId)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            doc.toObject(UnifiedChallenge::class.java)?.copy(
                id = doc.id,
                compilerType = compilerType  // ← SET THE COMPILER TYPE HERE
            )
        }
    } catch (e: Exception) {
        android.util.Log.e("UnifiedAssessment", "Error fetching challenges", e)
        emptyList()
    }
}

/**
 * Get all challenges for enrolled courses
 * With unlock logic based on difficulty
 */
suspend fun getChallengesForUser(): List<UnifiedChallenge> {
    val userId = auth.currentUser?.uid ?: return emptyList()

    try {
        val enrolledCourseIds = getUserEnrolledCourseIds(userId)
        if (enrolledCourseIds.isEmpty()) return emptyList()

        val allChallenges = mutableListOf<UnifiedChallenge>()

        enrolledCourseIds.chunked(10).forEach { batch ->
            val snapshot = firestore.collection(COLLECTION_TECHNICAL_ASSESSMENT)
                .whereIn("courseId", batch)
                .get()
                .await()

            val challenges = snapshot.documents.mapNotNull { doc ->
                val challenge = doc.toObject(UnifiedChallenge::class.java)?.copy(id = doc.id)
                
                // SET COMPILER TYPE BASED ON COURSE ID
                challenge?.copy(
                    compilerType = deriveCompilerType(challenge.courseId)
                )
            }

            allChallenges.addAll(challenges)
        }

        return applyUnlockLogic(allChallenges, userId)

    } catch (e: Exception) {
        android.util.Log.e("UnifiedAssessment", "Error fetching user challenges", e)
        return emptyList()
    }
}

/**
 * Derive compiler type from courseId
 * Maps course naming conventions to compiler types
 */
private fun deriveCompilerType(courseId: String): String {
    return when {
        courseId.contains("python", ignoreCase = true) -> "python"
        courseId.contains("java", ignoreCase = true) -> "java"
        courseId.contains("kotlin", ignoreCase = true) -> "kotlin"
        courseId.contains("sql", ignoreCase = true) -> "sql"
        courseId.contains("py_", ignoreCase = true) -> "python"
        courseId.contains("kt_", ignoreCase = true) -> "kotlin"
        else -> "python" // Safe default
    }
}
```

---

### Solution 3: Add compilerType to Firebase (Best Long-term Solution)

Add a `compilerType` field directly to your Firebase documents.

#### Option A: Add to `courses` collection

Go to Firebase Console → Firestore → `courses` collection and add:

| Document ID | compilerType |
|-------------|--------------|
| python_course_123 | `python` |
| java_basics | `java` |
| kotlin_intro | `kotlin` |

#### Option B: Add to `technical_assesment` collection

Add `compilerType` to each challenge document:

```
technical_assesment/{challengeId}:
  - title: "Fix the Loop"
  - courseId: "python_beginner"
  - compilerType: "python"    ← ADD THIS FIELD
  - brokenCode: "..."
  - correctOutput: "..."
```

Then update your `UnifiedChallenge` model to read it from Firebase:

```kotlin
data class UnifiedChallenge(
    @DocumentId
    val id: String = "",

    @PropertyName("title")
    val title: String = "",

    // ... other fields ...

    // CHANGE THIS: Now read from Firebase instead of runtime
    @PropertyName("compilerType")
    var compilerType: String = "python",  // Default to python

    var isUnlocked: Boolean = true
)
```

---

### Solution 4: Fix CompilerFactory to Handle Empty Input

Add defensive handling in `CompilerFactory`.

**File:** `CompilerFactory.kt`

```kotlin
fun getCompiler(compilerType: String): CourseCompiler {
    val normalizedType = compilerType.trim().lowercase()
    
    // Handle empty or blank input
    if (normalizedType.isBlank()) {
        android.util.Log.w("CompilerFactory", "Empty compilerType received, defaulting to Python")
        return registry["python"] 
            ?: throw IllegalArgumentException("Default compiler (python) not initialized")
    }
    
    // Check for aliases
    val registryKey = when (normalizedType) {
        "python3", "py" -> "python"
        "kt" -> "kotlin"
        else -> normalizedType
    }
    
    return registry[registryKey]
        ?: throw IllegalArgumentException(
            "Compiler not found. Requested: '$compilerType' (resolved: '$registryKey'), " +
            "Supported: ${getSupportedLanguages()}"
        )
}
```

---

## 📋 Fix CourseCompilerInfo Model

Also update your `CourseCompilerInfo` to match Firebase structure:

**File:** `models/CourseCompilerInfo.kt` (or wherever your models are)

```kotlin
package com.labactivity.lala.UNIFIEDCOMPILER.models

import com.google.firebase.Timestamp

/**
 * Course compiler configuration from Firebase
 * Must match ALL fields in Firebase 'courses' collection
 */
data class CourseCompilerInfo(
    // Compiler-related fields
    var courseId: String = "",
    var courseName: String = "",
    var compilerType: String = "python",  // Default value
    var version: String = "",
    var supportedFeatures: List<String> = emptyList(),
    
    // Course metadata fields (match your Firebase structure)
    var difficulty: String = "",
    var moduleCount: Int = 0,
    var name: String = "",
    var description: String = "",
    var category: String = "",
    var title: String = "",
    var hadCompiler: Boolean = false,
    var updatedAt: Timestamp? = null
)
```

---

## ✅ Verification Checklist

After applying fixes, verify:

- [ ] `CompilerFactory.initialize(context)` is called in Application/MainActivity
- [ ] `CourseCompilerInfo` has all fields matching Firebase `courses` collection
- [ ] `UnifiedChallenge.compilerType` is being set (either from Firebase or derived)
- [ ] `UnifiedCompilerActivity` has fallback logic for empty language
- [ ] Firebase documents have `compilerType` field (if using Solution 3)

---

## 🧪 Debug Logging

Add this temporary logging to trace the issue:

```kotlin
// In TechnicalAssessmentAdapter.openCompiler()
private fun openCompiler(challenge: Challenge) {
    android.util.Log.d("DEBUG", "Opening compiler for challenge: ${challenge.id}")
    android.util.Log.d("DEBUG", "challenge.compilerType: '${challenge.compilerType}'")
    android.util.Log.d("DEBUG", "challenge.courseId: '${challenge.courseId}'")
    
    val intent = Intent(context, UnifiedCompilerActivity::class.java).apply {
        putExtra(UnifiedCompilerActivity.EXTRA_LANGUAGE, challenge.compilerType)
        // ... rest of extras
    }
    context.startActivity(intent)
}

// In UnifiedCompilerActivity.loadInitialData()
private fun loadInitialData() {
    val intentLanguage = intent.getStringExtra(EXTRA_LANGUAGE)
    android.util.Log.d("DEBUG", "EXTRA_LANGUAGE from intent: '$intentLanguage'")
    android.util.Log.d("DEBUG", "EXTRA_COURSE_ID from intent: '${intent.getStringExtra(EXTRA_COURSE_ID)}'")
    
    // ... rest of function
}
```

---

## 📁 Files to Modify Summary

| File | Change |
|------|--------|
| `UnifiedCompilerActivity.kt` | Add fallback logic in `loadInitialData()` and `executeCode()` |
| `UnifiedAssessmentService.kt` | Set `compilerType` when fetching challenges |
| `CompilerFactory.kt` | Handle empty input gracefully |
| `CourseCompilerInfo.kt` | Add missing Firebase fields |
| Firebase `courses` collection | (Optional) Add `compilerType` field |
| Firebase `technical_assesment` collection | (Optional) Add `compilerType` field |

---

## 🚀 Recommended Implementation Order

1. **Immediate Fix**: Apply Solution 1 (UnifiedCompilerActivity fallback)
2. **Source Fix**: Apply Solution 2 (UnifiedAssessmentService)
3. **Defensive Fix**: Apply Solution 4 (CompilerFactory)
4. **Model Fix**: Update CourseCompilerInfo
5. **Long-term**: Consider Solution 3 (Firebase structure)

This ensures your app works immediately while you implement a more robust solution.
