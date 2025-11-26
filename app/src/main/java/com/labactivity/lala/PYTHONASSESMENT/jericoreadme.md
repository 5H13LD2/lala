# PYTHONASSESMENT Package

## Overview
This package contains the legacy technical assessment system for Python challenges. It has been largely superseded by the UNIFIEDCOMPILER system but remains for backward compatibility.

## Package Structure

### Core Components

#### 1. AllAssessmentsActivity.kt
- Displays a list of all technical assessments
- Shows assessment progress and status
- Allows users to select and start assessments

#### 2. AllInterviewsActivity.kt
- Displays technical interview challenges
- Similar to AllAssessmentsActivity but for interview-specific challenges

#### 3. AssessmentStatusManager.kt
- Manages the status of user assessments
- Tracks progress (not_started, in_progress, completed)
- Handles status updates in Firebase

#### 4. PYTHONASSESMENT.kt
- Main assessment activity (legacy)
- Handles code execution for Python challenges
- Displays challenge details and user submissions

#### 5. TechnicalAssessmentProgress.kt
- Data model for tracking user progress on technical assessments
- Stores:
  - Challenge ID
  - User code
  - Status
  - Score
  - Execution time
  - Test case results
  - Submission timestamp

#### 6. TechnicalInterviewAdapter.kt
- RecyclerView adapter for displaying interview challenges
- Handles item clicks to launch challenge activities

#### 7. TechnicalAssesmentAdapter.kt
- RecyclerView adapter for displaying technical assessment challenges
- Shows challenge metadata (title, difficulty, status)

#### 8. TechnicalAssessmentService.kt
- Service for executing technical assessments
- Handles Firebase operations for progress tracking
- Awards XP for completed challenges

#### 9. challenge.kt
- Data model for challenge objects
- Contains challenge metadata:
  - Title
  - Description
  - Difficulty level
  - Test cases
  - Expected output

## Migration to UNIFIEDCOMPILER

This package is being phased out in favor of the UNIFIEDCOMPILER system, which:
- Supports multiple languages (Python, Java, Kotlin, SQL)
- Uses a unified challenge format (UnifiedChallenge)
- Has better logging and progress tracking
- Integrates with UnifiedAssessmentService

### Migration Path
1. Challenges are being migrated from this system to the `technical_assesment` Firebase collection
2. UnifiedCompilerActivity replaces PYTHONASSESMENT.kt
3. UnifiedAssessmentService replaces TechnicalAssessmentService.kt
4. UnifiedChallenge model replaces challenge.kt

## Firebase Collections

### Legacy Collection (This Package)
- Collection: `technical_assessments` (old format)
- Subcollection: `users/{userId}/python_assessment_progress`

### New Collection (UNIFIEDCOMPILER)
- Collection: `technical_assesment` (note the typo - kept for consistency)
- Subcollection: `users/{userId}/technical_assessment_progress`

## Usage

### Launching an Assessment (Legacy)
```kotlin
val intent = Intent(context, PYTHONASSESMENT::class.java).apply {
    putExtra("CHALLENGE_ID", challengeId)
    putExtra("CHALLENGE_TITLE", title)
}
startActivity(intent)
```

### Launching an Assessment (New - UNIFIEDCOMPILER)
```kotlin
val intent = Intent(context, UnifiedCompilerActivity::class.java).apply {
    putExtra(UnifiedCompilerActivity.EXTRA_CHALLENGE_ID, challengeId)
}
startActivity(intent)
```

## Key Differences from UNIFIEDCOMPILER

| Feature | PYTHONASSESMENT | UNIFIEDCOMPILER |
|---------|-----------------|-----------------|
| Languages | Python only | Python, Java, Kotlin, SQL |
| Compiler | Chaquopy direct | CompilerFactory + CourseCompiler interface |
| Progress Tracking | TechnicalAssessmentService | UnifiedAssessmentService |
| Challenge Model | challenge.kt | UnifiedChallenge |
| Test Validation | Basic | Comprehensive with detailed logging |
| XP System | Basic | Integrated with XPManager + Achievement system |
| Logging | Minimal | Comprehensive boxed logs |

## Future Plans
- Maintain for backward compatibility
- Gradually migrate existing challenges to UNIFIEDCOMPILER
- Consider deprecation once migration is complete
- Archive historical assessment data

## Notes
- This package uses Firebase Firestore for data storage
- Requires user authentication (FirebaseAuth)
- Uses Kotlin coroutines for async operations
- Chaquopy is used for Python code execution
