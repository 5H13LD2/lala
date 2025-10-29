# 📤 How to Upload SQL Challenges to Firebase

## Problem
You can't see `sql_challenge_01` to `sql_challenge_10` in your Firestore database because they haven't been uploaded yet.

---

## ✅ Solution: 3 Methods to Upload

### 🔥 Method 1: Firebase Console (Manual Upload) - **EASIEST**

This is the fastest way if you don't have admin access set up yet.

#### Step-by-Step Instructions:

1. **Open Firebase Console**
   - Go to: https://console.firebase.google.com/
   - Select your project
   - Click **"Firestore Database"** in the left sidebar

2. **Navigate to Collection**
   - If `technical_assesment` collection doesn't exist, click **"Start collection"**
   - Collection ID: `technical_assesment` (use existing or create new)
   - If it exists, just click on it

3. **Add First Challenge**
   - Click **"Add document"**
   - At the top right, click the **"{ } Code"** button (switches to JSON mode)
   - Open the file: `firebase_data/sql_challenge_01_select_all.json`
   - **Copy ALL the content** from that file
   - **Paste** into the Firebase text area
   - Document ID: Type `sql_01` (or leave blank for auto-generated ID)
   - Click **"Save"**

4. **Repeat for All 10 Challenges**
   - Do the same for:
     - `sql_challenge_02_where_filter.json` → Document ID: `sql_02`
     - `sql_challenge_03_select_columns.json` → Document ID: `sql_03`
     - `sql_challenge_04_order_by.json` → Document ID: `sql_04`
     - `sql_challenge_05_count.json` → Document ID: `sql_05`
     - `sql_challenge_06_avg.json` → Document ID: `sql_06`
     - `sql_challenge_07_inner_join.json` → Document ID: `sql_07`
     - `sql_challenge_08_group_by.json` → Document ID: `sql_08`
     - `sql_challenge_09_having.json` → Document ID: `sql_09`
     - `sql_challenge_10_subquery.json` → Document ID: `sql_10`

5. **Verify Upload**
   - You should see 10 documents in `technical_assesment` collection
   - Click on `sql_01` and verify fields are correct
   - **IMPORTANT**: Expand `expected_result` → `rows` → `0`
   - You should see **individual items** (numbers and strings), **NOT** a single string

---

### 🤖 Method 2: From Android App (Admin Upload)

If you have admin access configured, you can upload directly from the app.

#### Prerequisites:
- Your Firebase user must have `admin: true` custom claim
- See "Setting Up Admin Access" section below

#### Steps:

1. **Create a Temporary Admin Button**

Add this to any Activity (like MainActivity or create a hidden admin panel):

```kotlin
import com.labactivity.lala.SQLCOMPILER.admin.AdminChallengeUploader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.Button
import android.widget.Toast

// In your Activity onCreate or similar
val uploadButton = findViewById<Button>(R.id.adminUploadButton)

uploadButton.setOnClickListener {
    CoroutineScope(Dispatchers.Main).launch {
        val uploader = AdminChallengeUploader(this@YourActivity)

        // Check if user is admin
        if (!uploader.isAdmin()) {
            Toast.makeText(
                this@YourActivity,
                "Error: You must be an admin to upload challenges",
                Toast.LENGTH_LONG
            ).show()
            return@launch
        }

        Toast.makeText(
            this@YourActivity,
            "Uploading challenges...",
            Toast.LENGTH_SHORT
        ).show()

        // Upload sample challenges
        val results = uploader.uploadSampleChallenges()

        if (results.isNotEmpty()) {
            Toast.makeText(
                this@YourActivity,
                "✅ Successfully uploaded ${results.size} challenges!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this@YourActivity,
                "❌ Upload failed. Check logs.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
```

2. **Run the App and Click the Button**

---

### 💻 Method 3: Node.js Script (Batch Upload)

If you have Node.js installed, this is the fastest way to upload all 10 at once.

#### Prerequisites:
- Node.js installed
- Firebase Admin SDK service account key

#### Steps:

1. **Get Service Account Key**
   - Go to Firebase Console → Project Settings (gear icon)
   - Click **"Service accounts"** tab
   - Click **"Generate new private key"**
   - Save the file as `serviceAccountKey.json` in your project root

2. **Install Firebase Admin SDK**

```bash
npm install firebase-admin
```

3. **Create Upload Script**

Create a file named `upload_sql_challenges.js` in your project root:

```javascript
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Initialize Firebase Admin with your service account
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Path to JSON files
const dataDir = './firebase_data';

async function uploadChallenges() {
  const files = fs.readdirSync(dataDir)
    .filter(f => f.startsWith('sql_challenge_') && f.endsWith('.json'));

  console.log(`\n📤 Found ${files.length} challenge files to upload\n`);

  for (const file of files) {
    try {
      const filePath = path.join(dataDir, file);
      const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));

      // Generate document ID from filename
      // sql_challenge_01_select_all.json → sql_01
      const docId = file.replace('sql_challenge_', '').split('_')[0].padStart(2, '0');
      const fullDocId = `sql_${docId}`;

      await db.collection('technical_assesment').doc(fullDocId).set(data);

      console.log(`✅ Uploaded: ${file} → Document ID: ${fullDocId}`);
    } catch (error) {
      console.error(`❌ Error uploading ${file}:`, error.message);
    }
  }

  console.log('\n🎉 Upload complete!\n');
  process.exit(0);
}

uploadChallenges();
```

4. **Run the Script**

```bash
node upload_sql_challenges.js
```

You should see:
```
📤 Found 10 challenge files to upload

✅ Uploaded: sql_challenge_01_select_all.json → Document ID: sql_01
✅ Uploaded: sql_challenge_02_where_filter.json → Document ID: sql_02
...
✅ Uploaded: sql_challenge_10_subquery.json → Document ID: sql_10

🎉 Upload complete!
```

---

## 🔐 Setting Up Admin Access (For Method 2)

To use the Android app upload method, you need to set admin custom claims.

### Option A: Using Firebase Admin SDK (Node.js)

```javascript
const admin = require('firebase-admin');

admin.initializeApp({
  credential: admin.credential.cert('./serviceAccountKey.json')
});

async function makeAdmin(uid) {
  await admin.auth().setCustomUserClaims(uid, { admin: true });
  console.log(`✅ Admin claim set for user ${uid}`);
}

// Replace with your Firebase user UID
makeAdmin('YOUR_USER_UID_HERE');
```

### Option B: Using Firebase Cloud Functions

Deploy a Cloud Function:

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.makeAdmin = functions.https.onCall(async (data, context) => {
  // Only allow if already admin
  if (context.auth.token.admin !== true) {
    throw new functions.https.HttpsError(
      'permission-denied',
      'Only admins can grant admin access'
    );
  }

  const uid = data.uid;
  await admin.auth().setCustomUserClaims(uid, { admin: true });
  return { message: `User ${uid} is now an admin` };
});
```

### How to Get Your User UID:

1. **Firebase Console Method**:
   - Go to Firebase Console → Authentication
   - Find your account in the users list
   - Copy the UID

2. **In Your App**:
```kotlin
val uid = FirebaseAuth.getInstance().currentUser?.uid
Log.d("USER_ID", "My UID: $uid")
```

### ⚠️ Important After Setting Admin Claim:
The user **MUST sign out and sign back in** for the custom claim to take effect!

---

## ✅ Verification Checklist

After uploading, verify everything is correct:

### In Firebase Console:

1. [ ] Open Firestore Database
2. [ ] See `technical_assesment` collection
3. [ ] See 10 documents: `sql_01` through `sql_10`
4. [ ] Click on `sql_01` to open it
5. [ ] Check these fields exist:
   - [ ] `title` = "Select All Students"
   - [ ] `difficulty` = "Easy"
   - [ ] `status` = "active"
   - [ ] `category` = "SQL"
   - [ ] `expected_result` (map)
   - [ ] `sample_table` (map)
   - [ ] `hints` (array)

6. [ ] **CRITICAL CHECK**: Expand `expected_result` → `rows` → `0`
   - ✅ **Correct**: You see items like `0: 1`, `1: "Jerico"`, `2: 20`
   - ❌ **Wrong**: You see a string like `"[1, "Jerico", 20]"`

If you see the wrong format, you need to fix it manually or re-upload using the JSON files.

### In Your Android App:

After uploading, test the app:

1. [ ] Run your app
2. [ ] Navigate to SQL Challenges section (you need to add navigation first)
3. [ ] You should see 10 challenges listed
4. [ ] Click on one challenge
5. [ ] Challenge details should load
6. [ ] You can write and execute SQL queries

---

## 🚨 Troubleshooting

### Issue: "Permission Denied"

**Cause**: Firestore security rules are blocking writes.

**Solution**:
1. Go to Firebase Console → Firestore Database → Rules
2. Temporarily change to:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```
3. Publish the rules
4. Upload your challenges
5. **IMPORTANT**: Change rules back to secure rules from `firestore_security_rules.txt`

### Issue: "Collection Not Found"

**Cause**: Collection doesn't exist yet.

**Solution**:
The collection will be created automatically when you add the first document. Just follow the upload steps.

### Issue: "Rows Showing as Strings"

**Cause**: You manually typed in Firebase Console instead of using JSON mode.

**Solution**:
1. Delete the document
2. Use the **"{ } Code"** button in Firebase Console
3. Paste the entire JSON from the file

### Issue: "Challenges Not Appearing in App"

**Possible Causes**:
1. Firestore rules blocking reads
2. User not authenticated
3. Wrong collection name (should be `technical_assesment`)
4. Challenge `status` is not "active"

**Solution**:
1. Check Firestore rules allow reads
2. Make sure user is logged in
3. Check collection name matches in code and Firebase
4. Verify `status: "active"` in Firebase documents

---

## 📱 Next Steps After Upload

Once challenges are uploaded:

1. **Add Navigation to SQL Challenges**

In your main activity or navigation:

```kotlin
sqlChallengesButton.setOnClickListener {
    startActivity(Intent(this, AllSQLChallengesActivity::class.java))
}
```

2. **Add to AndroidManifest.xml**

```xml
<activity
    android:name=".SQLCOMPILER.AllSQLChallengesActivity"
    android:exported="false" />

<activity
    android:name=".SQLCOMPILER.SQLChallengeActivity"
    android:exported="false" />
```

3. **Test the Flow**
   - Open app
   - Navigate to SQL Challenges
   - See list of 10 challenges
   - Click one
   - Execute SQL queries
   - See results

---

## 🎉 Success!

After following any of these methods, you should have:

✅ 10 SQL challenges in Firestore
✅ Challenges visible in Firebase Console
✅ Challenges loadable in your Android app
✅ Users can practice SQL queries
✅ Progress tracking works

**Recommended**: Use **Method 1 (Firebase Console)** if you're doing this for the first time. It's the simplest and doesn't require any additional setup.

---

## 💡 Need Help?

If you still can't see the challenges:

1. Share a screenshot of your Firebase Console showing the `technical_assesment` collection
2. Share any error messages from the app logs
3. Verify your Firestore security rules

I can help debug the specific issue!
