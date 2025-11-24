# 📤 Firebase Upload Guide for SQL Challenges

## Files Ready for Upload

I've created **10 SQL challenge JSON files** that are ready to upload to your `technical_assesment` collection in Firebase Firestore.

### ✅ Files Created:

1. `sql_challenge_01_select_all.json` - SELECT * basics
2. `sql_challenge_02_where_filter.json` - WHERE clause filtering
3. `sql_challenge_03_select_columns.json` - Selecting specific columns
4. `sql_challenge_04_order_by.json` - Sorting with ORDER BY
5. `sql_challenge_05_count.json` - COUNT aggregate function
6. `sql_challenge_06_avg.json` - AVG aggregate function
7. `sql_challenge_07_inner_join.json` - INNER JOIN multiple tables
8. `sql_challenge_08_group_by.json` - GROUP BY with COUNT
9. `sql_challenge_09_having.json` - HAVING clause for filtering groups
10. `sql_challenge_10_subquery.json` - Subqueries (advanced)

---

## 🔥 Method 1: Manual Upload via Firebase Console (Easiest)

### Step-by-Step Instructions:

1. **Open Firebase Console**
   - Go to https://console.firebase.google.com/
   - Select your project
   - Click "Firestore Database" in the left menu

2. **Create/Select Collection**
   - If `technical_assesment` collection doesn't exist, click "Start collection"
   - Collection ID: `technical_assesment`
   - If it exists, click on it

3. **Upload Each Challenge**

   For each JSON file:

   **Option A: Copy-Paste Method**
   - Click "Add document"
   - Document ID: Leave blank for auto-generated, or use: `sql_001`, `sql_002`, etc.
   - Click the **"{ } Code"** button (top right)
   - Copy the entire content from one JSON file
   - Paste into the Firebase editor
   - Click "Save"

   **Option B: Field-by-Field Method** (More tedious but precise)
   - Click "Add document"
   - Add each field manually by clicking "Add field"
   - Copy values from the JSON file
   - **IMPORTANT**: For `rows` arrays, make sure to create **nested arrays**, not strings!

4. **Verify Upload**
   - After uploading, click on the document
   - Expand `expected_result` → `rows` → `0`
   - You should see **individual array items** (numbers and strings), not a single string

---

## 🚀 Method 2: Programmatic Upload Using Admin SDK (Recommended)

If you have Node.js installed, this is the fastest method:

### Step 1: Install Firebase Admin SDK

```bash
npm install firebase-admin
```

### Step 2: Get Service Account Key

1. Go to Firebase Console → Project Settings (gear icon)
2. Go to "Service accounts" tab
3. Click "Generate new private key"
4. Save the JSON file as `serviceAccountKey.json`

### Step 3: Create Upload Script

Create a file `upload_challenges.js`:

```javascript
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Initialize Firebase Admin
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Directory containing JSON files
const dataDir = './firebase_data';

// Upload function
async function uploadChallenges() {
  const files = fs.readdirSync(dataDir).filter(f => f.endsWith('.json') && !f.includes('UPLOAD'));

  console.log(`Found ${files.length} challenge files to upload\n`);

  for (const file of files) {
    try {
      const filePath = path.join(dataDir, file);
      const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));

      // Auto-generate document ID or use filename
      const docId = file.replace('.json', '').replace('sql_challenge_', 'sql_');

      await db.collection('technical_assesment').doc(docId).set(data);

      console.log(`✅ Uploaded: ${file} → Document ID: ${docId}`);
    } catch (error) {
      console.error(`❌ Error uploading ${file}:`, error.message);
    }
  }

  console.log('\n🎉 Upload complete!');
  process.exit(0);
}

uploadChallenges();
```

### Step 4: Run the Script

```bash
node upload_challenges.js
```

---

## 🤖 Method 3: Upload from Android App (Using AdminChallengeUploader)

If you want to upload directly from your Android app:

### Step 1: Ensure User Has Admin Privileges

First, set admin custom claim for your user (using Firebase Admin SDK on server or Cloud Functions):

```javascript
// On your server or Cloud Functions
admin.auth().setCustomUserClaims(uid, { admin: true });
```

### Step 2: Use AdminChallengeUploader in Your App

Add this code to any Activity (create a temporary admin panel):

```kotlin
import com.labactivity.lala.SQLCOMPILER.admin.AdminChallengeUploader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminUploadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uploadButton = findViewById<Button>(R.id.uploadButton)
        val uploader = AdminChallengeUploader(this)

        uploadButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                // Check admin status
                if (!uploader.isAdmin()) {
                    Toast.makeText(this@AdminUploadActivity,
                        "You must be an admin to upload challenges",
                        Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Upload sample challenges
                val results = uploader.uploadSampleChallenges()

                Toast.makeText(this@AdminUploadActivity,
                    "Uploaded ${results.size} challenges successfully!",
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}
```

---

## ✅ Verification Checklist

After uploading, verify each document:

1. [ ] Open Firebase Console → Firestore Database
2. [ ] Navigate to `technical_assesment` collection
3. [ ] Click on any document (e.g., `sql_01`)
4. [ ] Check these fields exist:
   - [ ] `title` (string)
   - [ ] `description` (string)
   - [ ] `difficulty` (string: Easy/Medium/Hard)
   - [ ] `topic` (string)
   - [ ] `courseId` (string)
   - [ ] `expected_query` (string)
   - [ ] `expected_result` (map)
   - [ ] `sample_table` (map)
   - [ ] `hints` (array)
   - [ ] `tags` (array)
   - [ ] `category` (string: "SQL")
   - [ ] `status` (string: "active")
   - [ ] `order` (number)

5. [ ] **CRITICAL**: Expand `expected_result` → `rows` → `0`
   - You should see **individual items** like:
     - `0: 1` (number)
     - `1: "Jerico"` (string)
     - `2: 20` (number)
   - **NOT** a single string like `"[1, "Jerico", 20]"`

6. [ ] **CRITICAL**: Expand `sample_table` → `rows` → `0`
   - Same check as above - should be separate items, not a string

---

## 🎯 Expected Result

After successful upload, you should have:

- **10 documents** in `technical_assesment` collection
- Each with `category: "SQL"` to distinguish from Python challenges
- All documents have `status: "active"` so they appear in the app
- Difficulty levels: 6 Easy, 3 Medium, 1 Hard
- Topics covered: SELECT, WHERE, ORDER BY, Aggregates, JOINS, GROUP BY, Subqueries

---

## 🔧 Troubleshooting

### Issue: "rows is showing as string"
**Solution**: You're manually entering data in Firebase Console. When adding `rows`:
1. Click "Add field"
2. Type: **array** (not string!)
3. Add item 0: Type **array** (not string!)
4. Inside item 0, add the actual values as separate items

### Issue: "Permission denied"
**Solution**:
1. Check your Firestore security rules allow writes
2. Make sure you're authenticated
3. For admin operations, ensure your user has `admin: true` custom claim

### Issue: "Cannot find collection"
**Solution**: Collection will be auto-created when you add the first document. If using Node.js script, it will create it automatically.

---

## 📊 Integration with Your App

Once uploaded, your app will automatically:

1. **Fetch challenges** using `FirestoreSQLHelper.getAllChallenges()`
2. **Display in AllSQLChallengesActivity** with filtering by difficulty/topic
3. **Open SQLChallengeActivity** when user clicks a challenge
4. **Execute queries** using `QueryEvaluator`
5. **Track progress** in `users/{userId}/sql_progress/{challengeId}`

---

## 🎉 Next Steps

1. Upload the challenges using your preferred method above
2. Run your Android app
3. Navigate to the SQL challenges section
4. You should see all 10 challenges listed
5. Click one to start solving!

---

## 💡 Adding More Challenges

To add more challenges later:

1. Copy any existing JSON file
2. Modify the fields (title, description, expected_result, etc.)
3. Increment the `order` field
4. Upload using any method above

Example template:

```json
{
  "title": "Your Challenge Title",
  "description": "Detailed description of what to do",
  "difficulty": "Easy|Medium|Hard",
  "topic": "Your Topic",
  "courseId": "sql_fundamentals",
  "expected_query": "SELECT ...",
  "expected_result": {
    "columns": ["col1", "col2"],
    "rows": [
      [val1, val2],
      [val3, val4]
    ]
  },
  "sample_table": {
    "name": "table_name",
    "columns": ["col1", "col2"],
    "rows": [
      [val1, val2]
    ]
  },
  "hints": ["Hint 1", "Hint 2"],
  "createdAt": "2025-10-28T08:00:00Z",
  "updatedAt": "2025-10-28T08:00:00Z",
  "author": "Your Name",
  "status": "active",
  "order": 11,
  "tags": ["tag1", "tag2"],
  "category": "SQL"
}
```

---

Need help? Check the files in `firebase_data/` for working examples!
