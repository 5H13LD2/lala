# 🚀 Complete Setup Guide: Dynamic SQL Challenges System

## 📋 Table of Contents

1. [Overview](#overview)
2. [What Was Built](#what-was-built)
3. [Firebase Setup](#firebase-setup)
4. [Uploading Challenges](#uploading-challenges)
5. [App Integration](#app-integration)
6. [Admin Setup](#admin-setup)
7. [Testing](#testing)
8. [Troubleshooting](#troubleshooting)

---

## 📖 Overview

This system allows you to:
- ✅ Store SQL challenges in Firebase Firestore
- ✅ Dynamically fetch and display challenges in your app
- ✅ Execute user SQL queries and validate results
- ✅ Track user progress and scores
- ✅ Add/edit/remove challenges without app updates
- ✅ Support multiple difficulty levels and topics
- ✅ Admin-controlled content management

---

## 🏗️ What Was Built

### 1. **Data Models** (`SQLCOMPILER/models/`)
- `SQLChallenge` - Main challenge data structure
- `ExpectedResult` - Expected query output
- `TableData` - Sample table structure and data
- `SQLChallengeProgress` - User progress tracking
- `SQLChallengeStats` - User statistics

### 2. **Services** (`SQLCOMPILER/services/`)
- `FirestoreSQLHelper` - All Firestore operations (CRUD)
  - Fetch challenges (all, by course, by difficulty, by topic)
  - Track user progress
  - Save scores and attempts
  - Calculate statistics

### 3. **Admin Tools** (`SQLCOMPILER/admin/`)
- `AdminChallengeUploader` - Upload/update/delete challenges
  - Batch upload functionality
  - Pre-configured sample challenges
  - Validation before upload

### 4. **Activities**
- `SQLChallengeActivity` - Execute individual challenges
  - Dynamic database setup per challenge
  - Query execution and validation
  - Hint system
  - Progress tracking
  - Integration with `QueryEvaluator`

- `AllSQLChallengesActivity` - Browse all challenges
  - Grid layout with 2 columns
  - Filter by difficulty, topic
  - Search functionality
  - Progress indicators
  - Statistics dashboard

### 5. **Adapters** (`SQLCOMPILER/adapters/`)
- `SQLChallengeAdapter` - RecyclerView adapter
  - Loading skeleton animation
  - Challenge cards with metadata
  - Progress badges
  - Retry dialogs for completed challenges

### 6. **Utilities** (`SQLCOMPILER/utils/`)
- `FirestoreDataConverter` - Handle Firebase data parsing
  - Converts documents to Kotlin objects
  - Handles edge cases (rows as strings vs arrays)

### 7. **Layouts** (`res/layout/`)
- `activity_sql_challenge.xml` - Challenge execution screen
- `activity_all_sql_challenges.xml` - Challenge listing screen
- `item_sql_challenge.xml` - Challenge card
- `item_sql_challenge_skeleton.xml` - Loading state

### 8. **Firebase Structure**
- Collection: `technical_assesment/{challengeId}`
- Sub-collection: `users/{userId}/sql_progress/{challengeId}`

---

## 🔥 Firebase Setup

### Step 1: Update Firestore Security Rules

1. Go to Firebase Console → Firestore Database → Rules
2. Replace with the content from `firestore_security_rules.txt`
3. Click "Publish"

Key rules:
```javascript
// SQL Challenges - Read for authenticated, Write for admins only
match /sql_challenges/{challengeId} {
  allow read: if request.auth != null && resource.data.status == 'active';
  allow write: if request.auth != null && request.auth.token.admin == true;
}

// User Progress - Users can read/write their own
match /users/{userId}/sql_progress/{challengeId} {
  allow read, write: if request.auth != null && request.auth.uid == userId;
}
```

### Step 2: Create Indexes (Optional but Recommended)

For better query performance, create these composite indexes:

1. Go to Firestore Database → Indexes
2. Create these indexes:

| Collection | Fields | Query Scope |
|------------|--------|-------------|
| `technical_assesment` | `status ASC`, `courseId ASC`, `order ASC` | Collection |
| `technical_assesment` | `status ASC`, `difficulty ASC`, `order ASC` | Collection |
| `technical_assesment` | `status ASC`, `topic ASC`, `order ASC` | Collection |

Firebase will also auto-create indexes based on error messages if you try to query without them.

---

## 📤 Uploading Challenges

### Option 1: Use Pre-Made JSON Files (Fastest)

I've created 10 ready-to-upload JSON files in `firebase_data/`:

1. Open Firebase Console → Firestore Database
2. Go to `technical_assesment` collection (create if doesn't exist)
3. For each JSON file:
   - Click "Add document"
   - Click the **"{ } Code"** button (top-right)
   - Copy entire JSON content
   - Paste into Firebase
   - Click "Save"

**Files available:**
- `sql_challenge_01_select_all.json` - Basic SELECT
- `sql_challenge_02_where_filter.json` - WHERE clause
- `sql_challenge_03_select_columns.json` - Specific columns
- `sql_challenge_04_order_by.json` - Sorting
- `sql_challenge_05_count.json` - COUNT function
- `sql_challenge_06_avg.json` - AVG function
- `sql_challenge_07_inner_join.json` - INNER JOIN
- `sql_challenge_08_group_by.json` - GROUP BY
- `sql_challenge_09_having.json` - HAVING clause
- `sql_challenge_10_subquery.json` - Subqueries

See `firebase_data/UPLOAD_GUIDE.md` for detailed instructions.

### Option 2: Use AdminChallengeUploader (Programmatic)

Add this code to an admin screen in your app:

```kotlin
import com.labactivity.lala.SQLCOMPILER.admin.AdminChallengeUploader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// In your admin Activity
val uploader = AdminChallengeUploader(this)

CoroutineScope(Dispatchers.Main).launch {
    // Upload all sample challenges
    val results = uploader.uploadSampleChallenges()

    Toast.makeText(this@YourActivity,
        "Uploaded ${results.size} challenges!",
        Toast.LENGTH_LONG).show()
}
```

**Note**: User must have admin custom claim set (see Admin Setup section).

---

## 📱 App Integration

### Step 1: Add Activities to AndroidManifest.xml

Add these activity declarations:

```xml
<activity
    android:name=".SQLCOMPILER.AllSQLChallengesActivity"
    android:exported="false"
    android:theme="@style/Theme.YourApp" />

<activity
    android:name=".SQLCOMPILER.SQLChallengeActivity"
    android:exported="false"
    android:theme="@style/Theme.YourApp" />
```

### Step 2: Launch AllSQLChallengesActivity

From any Activity (e.g., your main menu or SQL section):

```kotlin
// Open SQL Challenges List
val intent = Intent(this, AllSQLChallengesActivity::class.java)
startActivity(intent)
```

Example: Add to your homepage or navigation menu:

```kotlin
binding.sqlChallengesButton.setOnClickListener {
    startActivity(Intent(this, AllSQLChallengesActivity::class.java))
}
```

### Step 3: Verify Dependencies

Ensure your `build.gradle` has:

```gradle
dependencies {
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-firestore-ktx'
    implementation 'com.google.firebase:firebase-auth-ktx'

    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3'

    // RecyclerView & CardView
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.cardview:cardview:1.0.0'
}
```

### Step 4: Test the Flow

1. Run your app
2. Navigate to SQL Challenges
3. You should see the list of challenges
4. Click on a challenge
5. Write and execute SQL queries
6. See results and validation

---

## 👨‍💼 Admin Setup

### Setting Admin Custom Claims

To allow users to upload/edit challenges, they need the `admin` custom claim.

#### Method 1: Using Firebase Admin SDK (Node.js)

```javascript
const admin = require('firebase-admin');

admin.initializeApp({
  credential: admin.credential.cert('path/to/serviceAccountKey.json')
});

// Set admin claim for a user
async function makeAdmin(uid) {
  await admin.auth().setCustomUserClaims(uid, { admin: true });
  console.log(`Admin claim set for user ${uid}`);
}

// Example: Make your account admin
makeAdmin('YOUR_FIREBASE_USER_UID');
```

#### Method 2: Using Cloud Functions

```javascript
exports.makeAdmin = functions.https.onCall(async (data, context) => {
  // Only existing admins can make others admin
  if (context.auth.token.admin !== true) {
    throw new functions.https.HttpsError(
      'permission-denied',
      'Only admins can make others admin'
    );
  }

  const uid = data.uid;
  await admin.auth().setCustomUserClaims(uid, { admin: true });

  return { message: `User ${uid} is now an admin` };
});
```

#### Method 3: Manual via Firebase Console Extension

Install the "Set Custom Claims" extension from Firebase Extensions marketplace.

### Checking Admin Status in App

```kotlin
import com.labactivity.lala.SQLCOMPILER.services.FirestoreSQLHelper

val firestoreHelper = FirestoreSQLHelper.getInstance()

CoroutineScope(Dispatchers.Main).launch {
    val isAdmin = firestoreHelper.isUserAdmin()

    if (isAdmin) {
        // Show admin features
        adminButton.visibility = View.VISIBLE
    }
}
```

---

## 🧪 Testing

### Test 1: Fetch Challenges

```kotlin
val firestoreHelper = FirestoreSQLHelper.getInstance()

CoroutineScope(Dispatchers.Main).launch {
    val challenges = firestoreHelper.getAllChallenges()

    Log.d("TEST", "Fetched ${challenges.size} challenges")
    challenges.forEach { challenge ->
        Log.d("TEST", "Challenge: ${challenge.title}, Difficulty: ${challenge.difficulty}")
    }
}
```

### Test 2: Execute Challenge

```kotlin
// Assuming you have a challenge loaded
val challenge = ... // from Firestore

// Setup database
val database = DatabaseHelper(context).writableDatabase
challenge.getAllTables().forEach { tableData ->
    // Create table
    database.execSQL(tableData.generateCreateTableSQL())

    // Insert data
    tableData.generateInsertSQL().forEach { sql ->
        database.execSQL(sql)
    }
}

// Execute user query
val queryEvaluator = QueryEvaluator(database)
val userQuery = "SELECT * FROM students WHERE age >= 21"

val expectedResult = challenge.expectedResult.toQueryResult()
val evaluation = queryEvaluator.evaluateQuery(userQuery, expectedResult)

Log.d("TEST", "Correct: ${evaluation.isCorrect}")
Log.d("TEST", "Feedback: ${evaluation.feedback}")
```

### Test 3: Save Progress

```kotlin
val firestoreHelper = FirestoreSQLHelper.getInstance()

CoroutineScope(Dispatchers.Main).launch {
    val success = firestoreHelper.updateProgressAfterAttempt(
        challengeId = "sql_01",
        passed = true,
        score = 100,
        userQuery = "SELECT * FROM students;",
        timeTaken = 45
    )

    Log.d("TEST", "Progress saved: $success")
}
```

---

## 🔧 Troubleshooting

### Issue 1: Challenges Not Appearing

**Symptoms**: AllSQLChallengesActivity shows "No challenges available"

**Solutions**:
1. Check if challenges are uploaded to Firestore
2. Verify `status` field is set to `"active"`
3. Check Firestore rules allow read access
4. Verify user is authenticated
5. Check logs for Firestore errors

```kotlin
// Add logging in FirestoreSQLHelper
Log.d("FirestoreSQLHelper", "Attempting to fetch challenges...")
```

### Issue 2: Permission Denied

**Symptoms**: Error: "PERMISSION_DENIED: Missing or insufficient permissions"

**Solutions**:
1. Check Firestore security rules
2. Verify user is authenticated: `FirebaseAuth.getInstance().currentUser != null`
3. For admin operations, verify custom claim: `firestoreHelper.isUserAdmin()`
4. Check if the document's `status` field is `"active"`

### Issue 3: Rows Parsing Error

**Symptoms**: Error parsing challenge, rows showing as strings

**Solutions**:
1. This means Firebase rows are stored as strings instead of arrays
2. Follow instructions in `CORRECT_FIREBASE_STRUCTURE.md`
3. Delete and re-upload challenges using the JSON files
4. OR use `FirestoreDataConverter` which handles both formats

### Issue 4: Query Evaluation Fails

**Symptoms**: All queries return "incorrect" even when right

**Solutions**:
1. Check table data was inserted correctly
2. Verify column names match between `expectedResult` and actual table
3. Check data types match (String vs Int)
4. Add logging to QueryEvaluator to see actual vs expected results

```kotlin
Log.d("QueryEvaluator", "Expected columns: ${expectedResult.columns}")
Log.d("QueryEvaluator", "Actual columns: ${actualResult.columns}")
Log.d("QueryEvaluator", "Expected rows: ${expectedResult.rows}")
Log.d("QueryEvaluator", "Actual rows: ${actualResult.rows}")
```

### Issue 5: App Crashes on Challenge Open

**Symptoms**: App crashes when opening SQLChallengeActivity

**Solutions**:
1. Check challenge ID was passed in intent
2. Verify challenge exists in Firestore
3. Check DatabaseHelper can create tables
4. Verify layout file `activity_sql_challenge.xml` exists
5. Check all required View IDs are in the layout

### Issue 6: Admin Upload Fails

**Symptoms**: Cannot upload challenges even with admin claim

**Solutions**:
1. Verify admin custom claim is set: Call `firestoreHelper.isUserAdmin()`
2. User must sign out and sign back in after claim is set
3. Check Firestore rules have admin check: `request.auth.token.admin == true`
4. Verify challenge data is valid (use `validateChallenge()`)

---

## 📊 Usage Analytics

To track how users interact with challenges:

### Track Challenge Opens

```kotlin
// In SQLChallengeActivity
Firebase.analytics.logEvent("sql_challenge_opened") {
    param("challenge_id", challengeId ?: "")
    param("challenge_title", currentChallenge?.title ?: "")
    param("difficulty", currentChallenge?.difficulty ?: "")
}
```

### Track Challenge Completion

```kotlin
// After successful completion
Firebase.analytics.logEvent("sql_challenge_completed") {
    param("challenge_id", challengeId ?: "")
    param("time_taken", timeTaken)
    param("attempts", attemptsCount)
}
```

---

## 🎯 Best Practices

### For Admins Creating Challenges:

1. **Clear Descriptions**: Make sure users know exactly what to do
2. **Realistic Data**: Use meaningful sample data
3. **Progressive Difficulty**: Start easy, build up complexity
4. **Good Hints**: Provide 2-3 hints that don't give away the answer
5. **Test Queries**: Always test the expected query yourself
6. **Consistent Naming**: Use clear, consistent table and column names

### For Developers:

1. **Error Handling**: Always wrap Firestore calls in try-catch
2. **Loading States**: Show skeleton loaders while fetching
3. **Offline Support**: Cache challenges locally (future enhancement)
4. **Progress Sync**: Sync progress when user comes online
5. **Logging**: Use detailed logs during development

---

## 🚀 Next Steps / Future Enhancements

### Possible Additions:

1. **Leaderboard**: Show top scores for each challenge
2. **Hints Cost**: Deduct points for using hints
3. **Time Limits**: Add optional time limits for challenges
4. **Achievements**: Badges for completing all challenges in a topic
5. **Challenge of the Day**: Daily featured challenge
6. **User-Generated**: Allow users to create and share challenges
7. **Offline Mode**: Download challenges for offline practice
8. **Code Highlighting**: Syntax highlighting for SQL queries
9. **Query History**: Show user's previous attempts
10. **Explanations**: Add detailed explanations for solutions

---

## 📞 Support

### Files to Reference:

- `FIRESTORE_SQL_SCHEMA.md` - Complete Firestore structure
- `CORRECT_FIREBASE_STRUCTURE.md` - How to fix structure issues
- `firebase_data/UPLOAD_GUIDE.md` - Detailed upload instructions
- `firestore_security_rules.txt` - Security rules to copy

### Key Classes:

- `FirestoreSQLHelper` - All Firestore operations
- `SQLChallengeActivity` - Challenge execution
- `AllSQLChallengesActivity` - Challenge listing
- `AdminChallengeUploader` - Admin uploads
- `QueryEvaluator` - Query validation (existing)

---

## ✅ Verification Checklist

Before considering setup complete:

- [ ] Firestore security rules updated and published
- [ ] At least 1 challenge uploaded to `technical_assesment` collection
- [ ] Challenge document has correct structure (arrays, not strings)
- [ ] Activities added to AndroidManifest.xml
- [ ] Can open AllSQLChallengesActivity from app
- [ ] Challenges appear in the list
- [ ] Can click and open a challenge
- [ ] Can write and execute a query
- [ ] Query validation works (correct query shows success)
- [ ] Progress is saved to Firestore
- [ ] Statistics show on AllSQLChallengesActivity
- [ ] (Optional) Admin can upload new challenges

---

## 🎉 Congratulations!

Your dynamic SQL challenges system is now complete! Users can practice SQL, track their progress, and you can add new challenges anytime without updating the app.

Happy coding! 🚀
