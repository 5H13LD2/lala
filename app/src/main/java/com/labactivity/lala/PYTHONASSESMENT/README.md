# Python Assessment Module - Technical Documentation

## Overview
The Python Assessment module provides a comprehensive system for managing, executing, and tracking technical coding challenges for Python, Java, and Kotlin. It integrates with the Unified Compiler system to provide consistent challenge execution across multiple programming languages.

---

## 📁 Module Structure

```
PYTHONASSESMENT/
├── README.md                           (This file)
├── Challenge.kt                        Data model for challenges
├── TechnicalAssessmentProgress.kt      Data model for user progress
├── TechnicalAssessmentService.kt       Business logic & Firebase operations
├── TechnicalAssesmentAdapter.kt        RecyclerView adapter for challenge list
├── AllAssessmentsActivity.kt           Main activity displaying all challenges
├── TechnicalInterviewAdapter.kt        Adapter for interview challenges
└── AllInterviewsActivity.kt            Activity for interview challenges
```

---

## 🏗️ Architecture

### **MVC Pattern Implementation**

```
┌─────────────────────────────────────────────────────────┐
│                        VIEW LAYER                        │
├─────────────────────────────────────────────────────────┤
│  AllAssessmentsActivity.kt                              │
│  - Displays challenge list in grid layout               │
│  - Handles filtering (difficulty, status)               │
│  - Manages loading states & animations                  │
│  - Refreshes progress on resume                         │
├─────────────────────────────────────────────────────────┤
│  TechnicalAssesmentAdapter.kt                           │
│  - RecyclerView adapter for challenge cards             │
│  - Displays progress indicators (✓ Completed, ⟳ Progress)│
│  - Handles click events & locked states                 │
│  - Manages skeleton loading animations                  │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                     CONTROLLER LAYER                     │
├─────────────────────────────────────────────────────────┤
│  TechnicalAssessmentService.kt                          │
│  - Fetches challenges from Firestore                    │
│  - Manages user progress (CRUD operations)              │
│  - Implements unlock logic (Easy → Medium → Hard)       │
│  - Awards XP and tracks achievements                    │
│  - Handles progress synchronization                     │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                       MODEL LAYER                        │
├─────────────────────────────────────────────────────────┤
│  Challenge.kt                                            │
│  - Challenge metadata (title, difficulty, code preview) │
│  - Compiler type (python, java, kotlin)                 │
│  - Unlock status (runtime computed)                     │
├─────────────────────────────────────────────────────────┤
│  TechnicalAssessmentProgress.kt                         │
│  - User progress tracking                               │
│  - Status (not_started, in_progress, completed)         │
│  - Best score, attempts, timestamps                     │
│  - Formatted helper properties                          │
└─────────────────────────────────────────────────────────┘
```

---

## 🎮 Controller: TechnicalAssessmentService

### **Purpose**
The service acts as the **main controller** for all Python/Java/Kotlin assessment operations. It handles data fetching, progress management, and business logic.

### **Key Responsibilities**

#### **1. Challenge Management**
```kotlin
suspend fun getChallengesForUser(): List<Challenge>
```
- Fetches challenges based on user's enrolled courses
- Applies unlock logic (progressive difficulty)
- Filters by enrollment status

#### **2. Progress Tracking**
```kotlin
suspend fun updateProgressAfterAttempt(
    challengeId: String,
    passed: Boolean,
    score: Int,
    userCode: String,
    timeTaken: Long,
    challengeTitle: String
): Pair<Boolean, List<Achievement>>
```
- Saves user progress to Firestore
- Updates best score and attempt count
- Awards XP on first pass
- Returns unlocked achievements

#### **3. Progress Retrieval**
```kotlin
suspend fun getUserProgress(challengeId: String): TechnicalAssessmentProgress?
suspend fun getAllUserProgress(): List<TechnicalAssessmentProgress>
```
- Fetches individual or all progress records
- Handles parsing errors gracefully
- Skips incompatible documents (e.g., SQL progress)

#### **4. Unlock Logic**
```kotlin
private suspend fun applyUnlockLogic(challenges: List<Challenge>): List<Challenge>
```
- Easy: Always unlocked
- Medium: Unlocked when all Easy completed
- Hard: Unlocked when all Easy + Medium completed

---

## 📊 Data Models

### **Challenge.kt**
```kotlin
data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val difficulty: String = "",        // "Easy", "Medium", "Hard"
    val courseId: String = "",
    val brokenCode: String = "",        // Starting code
    val correctOutput: String = "",     // Expected output
    val codePreview: String = "",       // Display preview
    val hint: String = "",
    val hints: List<String> = emptyList(),
    val category: String = "",
    val compilerType: String = "",      // "python", "java", "kotlin"
    var isUnlocked: Boolean = true      // Runtime field
)
```

### **TechnicalAssessmentProgress.kt**
```kotlin
data class TechnicalAssessmentProgress(
    @DocumentId
    val challengeId: String = "",

    val status: String = "not_started",     // "not_started", "in_progress", "completed"
    val attempts: Int = 0,
    val bestScore: Int = 0,
    val lastAttemptDate: Timestamp? = null, // Firebase Timestamp
    val timeTaken: Long = 0,                // milliseconds
    val userCode: String = "",
    val passed: Boolean = false,
    val challengeTitle: String = "",
    val compilerType: String = "",          // "python", "java", "kotlin"
    val updatedAt: Timestamp? = null
)
```

**Key Features:**
- Uses `@DocumentId` (no field in Firestore, extracted from path)
- `Timestamp?` for date consistency
- Formatted helper properties (`formattedTimeTaken`, `formattedLastAttemptDate`)
- Status text helper (`statusText`)

---

## 🎨 View Layer

### **AllAssessmentsActivity.kt**

**Purpose:** Main activity displaying all available technical assessments.

#### **Key Features:**

1. **Grid Layout**
   ```kotlin
   private val GRID_SPAN_COUNT = 2  // 2 columns
   ```

2. **Filtering System**
   - All / Easy / Medium / Hard
   - In Progress / Completed
   - Updates adapter dynamically

3. **Progress Refresh**
   ```kotlin
   override fun onResume() {
       super.onResume()
       refreshProgress()  // Auto-refresh when returning
   }
   ```

4. **Animations**
   - Initial load: Staggered slide-up fade-in
   - Items: Fade-slide-up animation
   - Skeleton loading shimmer

#### **Lifecycle Flow:**
```
onCreate()
  ↓
setupToolbar()
setupRecyclerView()
setupFilterChips()
animateInitialLoad()
  ↓
loadAssessments()
  ├─ Fetch challenges (background)
  ├─ Fetch user progress (background)
  ├─ Build progress map
  └─ Update adapter
  ↓
onResume()
  └─ refreshProgress()  // When returning from challenge
```

---

### **TechnicalAssesmentAdapter.kt**

**Purpose:** RecyclerView adapter for displaying challenge cards with progress indicators.

#### **View Types:**
1. **Loading Skeleton** - Shimmer animation during data fetch
2. **Challenge Item** - Actual challenge card

#### **Progress Indicators:**
```kotlin
when (progress?.status) {
    "completed" -> {
        statusTextView.text = "✓ Completed"
        statusTextView.setTextColor(success_green)
        scoreTextView.text = "Best: ${progress.bestScore}%"
    }
    "in_progress" -> {
        statusTextView.text = "⟳ In Progress"
        statusTextView.setTextColor(primary_blue)
        scoreTextView.text = "Best: ${progress.bestScore}% • ${progress.attempts} attempts"
    }
    else -> {
        // Hide indicators
    }
}
```

#### **Lock System:**
```kotlin
if (!challenge.isUnlocked) {
    lockOverlay.visibility = VISIBLE
    lockIcon.visibility = VISIBLE
    cardView.alpha = 0.6f
}
```

#### **Click Handling:**
```kotlin
itemView.setOnClickListener {
    when {
        !challenge.isUnlocked -> showLockedDialog()
        isCompleted -> showRetryDialog()
        else -> openCompiler(challenge)
    }
}
```

---

## 🔄 Complete User Flow

### **1. Viewing Challenges**
```
User opens app
  ↓
AllAssessmentsActivity.onCreate()
  ↓
Load challenges + progress from Firestore
  ↓
Display grid with progress indicators:
  ┌─────────────┐  ┌─────────────┐
  │ Fix Loop    │  │ Debug Code  │
  │ Medium      │  │ Easy        │
  │ ✓ Completed │  │ ⟳ Progress  │
  │ Best: 100%  │  │ Best: 50%   │
  └─────────────┘  └─────────────┘
```

### **2. Starting a Challenge**
```
User clicks challenge card
  ↓
Check if locked
  ├─ YES → Show locked dialog
  └─ NO  → Open UnifiedCompilerActivity
```

### **3. Solving Challenge**
```
UnifiedCompilerActivity
  ↓
User writes code
  ↓
Click "Run" button
  ↓
UnifiedAssessmentService.executeChallenge()
  ↓
Score >= 70%?
  ├─ YES → Save progress + Show success dialog
  └─ NO  → Show retry dialog (don't save)
```

### **4. Returning to List**
```
Challenge activity finishes
  ↓
AllAssessmentsActivity.onResume()
  ↓
refreshProgress()
  ↓
Adapter updates progress indicators
  ↓
Challenge now shows "✓ Completed"
```

---

## 🔥 Firestore Integration

### **Collections Used:**

#### **1. technical_assesment**
```
technical_assesment/
  └── {challengeId}
      ├── title: "Fix the Loop"
      ├── difficulty: "Medium"
      ├── courseId: "python"
      ├── compilerType: "python"
      ├── brokenCode: "for i in range..."
      ├── correctOutput: "0\n1\n2\n3\n4"
      ├── hints: ["Check loop range", "Fix index"]
      └── ...
```

#### **2. user_progress**
```
user_progress/
  └── {userId}/
      └── technical_assessment_progress/
          └── {challengeId}
              ├── status: "completed"
              ├── bestScore: 100
              ├── attempts: 2
              ├── lastAttemptDate: Timestamp
              ├── userCode: "for i in range(5)..."
              ├── passed: true
              ├── compilerType: "python"
              └── challengeTitle: "Fix the Loop"
```

**Note:** The `challengeId` is **NOT stored as a field** - it's the document ID (via `@DocumentId`).

---

## 🎯 Integration with Unified Compiler

### **Data Flow:**

```
AllAssessmentsActivity
  ↓
User clicks challenge
  ↓
TechnicalAssesmentAdapter.openCompiler()
  ↓
Launch UnifiedCompilerActivity with:
  - EXTRA_LANGUAGE: "python"/"java"/"kotlin"
  - EXTRA_CHALLENGE_ID: challengeId
  - EXTRA_INITIAL_CODE: brokenCode
  - EXTRA_CHALLENGE_HINTS: hints
  ↓
UnifiedCompilerActivity.executeCode()
  ↓
UnifiedAssessmentService.executeChallenge()
  ↓
CompilerFactory.getCompiler(language).compile(code)
  ↓
Validate test cases
  ↓
IF passed:
  UnifiedAssessmentService.saveProgress()
  ↓
  Show success dialog
  ↓
  Return to AllAssessmentsActivity
  ↓
  onResume() → refreshProgress()
  ↓
  Adapter shows updated progress
```

---

## 🎨 UI Components

### **Layout Files:**
- `activity_all_assessments.xml` - Main activity layout
- `item_assesment_card.xml` - Challenge card layout
- `item_assessment_skeleton.xml` - Loading skeleton

### **Progress Display:**
```xml
<!-- Status indicator -->
<TextView
    android:id="@+id/textStatus"
    android:text="✓ Completed"
    android:visibility="gone" />

<!-- Score display -->
<TextView
    android:id="@+id/textScore"
    android:text="Best: 100%"
    android:visibility="gone" />
```

### **Lock Overlay:**
```xml
<View
    android:id="@+id/lockOverlay"
    android:visibility="gone" />

<ImageView
    android:id="@+id/lockIcon"
    android:src="@android:drawable/ic_lock_lock"
    android:visibility="gone" />
```

---

## ⚙️ Key Algorithms

### **1. Progressive Difficulty Unlock**
```kotlin
private suspend fun applyUnlockLogic(challenges: List<Challenge>): List<Challenge> {
    val progressMap = getAllUserProgress().associateBy { it.challengeId }

    // Count completed by difficulty
    val completedEasy = challenges.count {
        it.difficulty == "Easy" && progressMap[it.id]?.passed == true
    }
    val completedMedium = challenges.count {
        it.difficulty == "Medium" && progressMap[it.id]?.passed == true
    }

    val easyCount = challenges.count { it.difficulty == "Easy" }
    val mediumCount = challenges.count { it.difficulty == "Medium" }

    return challenges.map { challenge ->
        val isUnlocked = when (challenge.difficulty.lowercase()) {
            "easy" -> true
            "medium" -> completedEasy >= easyCount
            "hard" -> (completedEasy >= easyCount) && (completedMedium >= mediumCount)
            else -> true
        }
        challenge.copy(isUnlocked = isUnlocked)
    }
}
```

### **2. Best Score Calculation**
```kotlin
val bestScore = if (existingProgress != null) {
    maxOf(existingProgress.bestScore, currentScore)
} else {
    currentScore
}
```

### **3. Attempt Counter**
```kotlin
// Using FieldValue.increment for thread-safe increments
"attempts" to FieldValue.increment(1)
```

---

## 🐛 Error Handling

### **1. Mixed Data Types**
```kotlin
val progressList = snapshot.documents.mapNotNull { doc ->
    try {
        doc.toObject(TechnicalAssessmentProgress::class.java)
    } catch (e: Exception) {
        Log.w(TAG, "⚠️ Skipping document ${doc.id}: ${e.message}")
        null  // Skip SQL or other incompatible documents
    }
}
```

### **2. Network Failures**
```kotlin
try {
    // Firestore operation
} catch (e: Exception) {
    Log.e(TAG, "❌ Error: ${e.message}", e)
    return emptyList()  // Graceful fallback
}
```

### **3. Empty States**
```kotlin
if (challenges.isEmpty()) {
    showEmptyState()
} else {
    hideEmptyState()
}
```

---

## 🔧 Configuration

### **Constants:**
```kotlin
companion object {
    private const val TAG = "TechnicalAssessmentService"
    private const val COLLECTION_TECHNICAL_ASSESSMENT = "technical_assesment"
    private const val COLLECTION_USER_PROGRESS = "user_progress"
    private const val SUB_COLLECTION_TECHNICAL_ASSESSMENT_PROGRESS = "technical_assessment_progress"
    private const val GRID_SPAN_COUNT = 2
}
```

---

## 📈 Performance Optimizations

### **1. Parallel Data Fetching**
```kotlin
val (challenges, userProgress) = withContext(Dispatchers.IO) {
    val challengesList = assessmentService.getChallengesForUser()
    val progressList = assessmentService.getAllUserProgress()
    Pair(challengesList, progressList)
}
```

### **2. Progress Map for O(1) Lookups**
```kotlin
progressMap = userProgress.associateBy { it.challengeId }
```

### **3. Lazy Loading with Skeleton**
```kotlin
adapter = TechnicalAssessmentAdapter(this, isLoading = true)
// Show skeleton while loading
```

### **4. Refresh on Resume (Not Reload)**
```kotlin
private fun refreshProgress() {
    // Only fetch progress, not all challenges
    val userProgress = assessmentService.getAllUserProgress()
    progressMap = userProgress.associateBy { it.challengeId }
    adapter.updateProgress(progressMap)
}
```

---

## 🧪 Testing Checklist

### **Challenge List:**
- [ ] Challenges load correctly
- [ ] Progress indicators show
- [ ] Filtering works (All/Easy/Medium/Hard)
- [ ] Locked challenges display overlay
- [ ] Animations play smoothly

### **Challenge Execution:**
- [ ] Opens UnifiedCompilerActivity
- [ ] Code executes correctly
- [ ] Success dialog appears (score >= 70%)
- [ ] Retry dialog appears (score < 70%)
- [ ] Progress saves to Firestore

### **Progress Tracking:**
- [ ] Best score updates
- [ ] Attempt count increments
- [ ] Status changes (not_started → in_progress → completed)
- [ ] Progress persists across sessions
- [ ] Progress shows in list after returning

### **Unlock Logic:**
- [ ] Easy challenges always unlocked
- [ ] Medium unlocks after all Easy completed
- [ ] Hard unlocks after Easy + Medium completed
- [ ] Locked dialog shows on click

---

## 🔗 Related Modules

| Module | Purpose | Link |
|--------|---------|------|
| **UNIFIEDCOMPILER** | Code execution engine | `../UNIFIEDCOMPILER/` |
| **SQLCOMPILER** | SQL challenge system | `../SQLCOMPILER/` |
| **GAMIFICATION** | XP & achievements | `../GAMIFICATION/` |
| **LEADERBOARDPAGE** | Rankings & stats | `../LEADERBOARDPAGE/` |

---

## 📝 Change Log

### **v2.0 (2025-11-28)**
- ✅ Integrated with UnifiedCompiler
- ✅ Added success/retry dialogs
- ✅ Fixed Timestamp data type consistency
- ✅ Added error handling for mixed data types
- ✅ Improved progress refresh logic
- ✅ Added comprehensive documentation

### **v1.0 (Initial Release)**
- Basic challenge listing
- Progress tracking
- Firebase integration
- Unlock logic

---

## 🤝 Contributing

When modifying this module:

1. **Maintain Data Consistency**: Always use `Timestamp?` for dates
2. **Use @DocumentId**: Never store `challengeId` as a field
3. **Handle Errors Gracefully**: Catch parsing exceptions
4. **Update Documentation**: Keep this README in sync
5. **Test All Flows**: Challenge list → Execution → Progress update

---

## 📧 Support

For issues or questions:
- Check logs with tag: `TechnicalAssessmentService`
- Review Firestore console for data integrity
- Verify user authentication status

---

**Last Updated:** 2025-11-28
**Version:** 2.0
**Author:** Claude Code Assistant
**Module:** PYTHONASSESMENT (Python/Java/Kotlin Technical Assessments)
