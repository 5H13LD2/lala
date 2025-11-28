# Unified Compiler - Progress Saving Workflow

## Overview
This document explains how the Unified Compiler system saves user progress for technical assessment challenges across Python, Java, Kotlin, and SQL compilers.

---

## 📊 Architecture

### Key Components

1. **UnifiedCompilerActivity** - UI layer for code editor and execution
2. **UnifiedAssessmentService** - Business logic for challenge execution and progress tracking
3. **Firebase Firestore** - Backend storage for user progress
4. **XPManager** - Gamification and achievement system

---

## 🔄 Progress Saving Flow

### Step 1: Challenge Execution

```kotlin
// User clicks "Run" button
UnifiedCompilerActivity.executeCode(userCode)
  ↓
UnifiedAssessmentService.executeChallenge(
    challengeId,
    userCode,
    challenge
)
  ↓
CompilerFactory.getCompiler(language).compile(code)
```

### Step 2: Result Evaluation

```kotlin
// Calculate score based on test cases
val score = (testCasesPassed * 100) / totalTestCases
val passed = score >= 70  // 70% passing grade

// Create execution result
ChallengeExecutionResult(
    compilerResult = result,
    score = score,
    passed = passed,
    testCasesPassed = passed,
    totalTestCases = total
)
```

### Step 3: Save Progress (Only if Passed)

```kotlin
if (executionResult.passed) {
    val (success, achievements) = assessmentService.saveProgress(
        challengeId = challengeId,
        challenge = challenge,
        userCode = userCode,
        executionResult = executionResult
    )
}
```

#### Firestore Document Structure

Path: `user_progress/{userId}/technical_assessment_progress/{challengeId}`

```json
{
  "challengeTitle": "Fix the Loop",
  "status": "completed",          // or "in_progress"
  "attempts": 2,                   // Auto-incremented
  "bestScore": 100,
  "lastAttemptDate": Timestamp,
  "timeTaken": 1500,              // milliseconds
  "userCode": "print('Hello')",
  "passed": true,
  "updatedAt": Timestamp,
  "compilerType": "python"        // "python", "java", "kotlin", "sql"
}
```

**Note:** `challengeId` is NOT stored as a field - it's the document ID (via `@DocumentId` annotation).

### Step 4: Award XP

```kotlin
if (passed) {
    val xpResult = xpManager.awardTechnicalAssessmentXP(
        challengeTitle = challenge.title,
        passed = true,
        score = score
    )

    // Returns unlocked achievements
    unlockedAchievements = xpResult.unlockedAchievements
}
```

### Step 5: Show UI Dialog

#### Success Dialog (Score >= 70%)

```
🎉 Success!

✅ Correct! Well done!

Score: 100%

Your progress has been saved.

[Continue] button
```

- Shows achievement dialog if unlocked
- Exits activity after acknowledgment

#### Retry Dialog (Score < 70%)

```
Try Again

❌ Not quite right. Try again!

Score: 33%
Test Cases: 1/3 passed

Your Output:
orange

[Retry] [Exit]
```

- "Retry" keeps activity open
- "Exit" closes activity

---

## 🔍 Key Features

### 1. Automatic Progress Tracking

- **First Attempt**: Creates new progress document
- **Subsequent Attempts**: Updates existing document
- **Best Score**: Automatically tracked (never decreases)
- **Attempts Counter**: Auto-incremented using `FieldValue.increment(1)`

### 2. Multi-Language Support

All compilers save to the **same collection** but with different `compilerType`:

| Language | compilerType | Collection Path |
|----------|-------------|-----------------|
| Python   | `"python"`  | `technical_assessment_progress` |
| Java     | `"java"`    | `technical_assessment_progress` |
| Kotlin   | `"kotlin"`  | `technical_assessment_progress` |
| SQL      | `"sql"`     | `technical_assessment_progress` |

### 3. Data Type Consistency

All progress models use **Firebase Timestamp** for dates:

```kotlin
@PropertyName("lastAttemptDate")
val lastAttemptDate: Timestamp? = null
```

**Why Timestamp?**
- Prevents deserialization errors across different compilers
- Firestore native type (better performance)
- Consistent across all assessment types

### 4. Error Handling

```kotlin
// Gracefully skip incompatible documents
snapshot.documents.mapNotNull { doc ->
    try {
        doc.toObject(UnifiedChallengeProgress::class.java)
    } catch (e: Exception) {
        Log.w(TAG, "⚠️ Skipping document ${doc.id}: ${e.message}")
        null  // Skip this document
    }
}
```

---

## 🎯 Passing Criteria

| Metric | Value |
|--------|-------|
| Passing Score | 70% |
| Test Cases | Must pass majority |
| XP Award | Only on first pass |
| Progress Save | Only when passed |

---

## 🐛 Common Issues & Solutions

### Issue 1: "challengeId conflicts with @DocumentId"

**Error:**
```
RuntimeException: 'challengeId' was found from document,
cannot apply @DocumentId on this property
```

**Solution:**
Remove `challengeId` from the data map - it's automatically set from document path.

```kotlin
// ❌ WRONG
val data = hashMapOf(
    "challengeId" to challengeId,  // Conflict!
    ...
)

// ✅ CORRECT
val data = hashMapOf(
    // challengeId omitted - it's the document ID
    "challengeTitle" to title,
    ...
)
```

### Issue 2: Timestamp vs String Type Mismatch

**Error:**
```
Could not deserialize object. Failed to convert
Timestamp to String (found in field 'lastAttemptDate')
```

**Solution:**
Use `Timestamp?` type in all progress models:

```kotlin
// ✅ CORRECT (All Models)
@PropertyName("lastAttemptDate")
val lastAttemptDate: Timestamp? = null

// ❌ WRONG (Old SQL Model)
val lastAttemptDate: String = ""
```

### Issue 3: Progress Vanishes Between Compilers

**Cause:** Different data types or parsing errors.

**Solution:** Add error handling when fetching progress:

```kotlin
val progressList = snapshot.documents.mapNotNull { doc ->
    try {
        doc.toObject(ProgressModel::class.java)
    } catch (e: Exception) {
        null  // Skip problematic documents
    }
}
```

---

## 📝 Example Usage

### From UnifiedCompilerActivity

```kotlin
// 1. User completes challenge
val executionResult = assessmentService.executeChallenge(...)

// 2. Check if passed
if (executionResult.passed) {
    // 3. Save progress
    val (success, achievements) = assessmentService.saveProgress(...)

    // 4. Show success dialog
    showSuccessDialog(executionResult.score, achievements)
} else {
    // 5. Show retry dialog
    showRetryDialog(executionResult.score, executionResult.compilerResult)
}
```

### From Activity Intent

```kotlin
val intent = Intent(this, UnifiedCompilerActivity::class.java).apply {
    putExtra(UnifiedCompilerActivity.EXTRA_LANGUAGE, "python")
    putExtra(UnifiedCompilerActivity.EXTRA_CHALLENGE_ID, "python_challenge_002")
    putExtra(UnifiedCompilerActivity.EXTRA_INITIAL_CODE, brokenCode)
}
startActivity(intent)
```

---

## 🔐 Security Considerations

1. **User Authentication**: Always check `auth.currentUser?.uid`
2. **Input Validation**: Sanitize user code before execution
3. **Rate Limiting**: Prevent spam attempts (TODO)
4. **Admin-Only Operations**: Challenge creation requires custom claims

---

## 🚀 Future Enhancements

- [ ] Add retry limit (max 3 attempts per challenge)
- [ ] Track time spent on each attempt
- [ ] Add code diff comparison
- [ ] Implement code review feedback
- [ ] Add peer code sharing
- [ ] Support custom test cases

---

## 📚 Related Files

| File | Purpose |
|------|---------|
| `UnifiedCompilerActivity.kt` | Main UI for code editor |
| `UnifiedAssessmentService.kt` | Progress tracking service |
| `UnifiedChallengeModels.kt` | Data models |
| `CompilerFactory.kt` | Multi-language compiler |
| `XPManager.kt` | Gamification system |

---

**Last Updated:** 2025-11-28
**Version:** 1.0
**Author:** Claude Code Assistant
