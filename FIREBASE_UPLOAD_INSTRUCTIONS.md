# How to Upload SQL Challenges to Firebase

## Method 1: Using Firebase Console (Easiest)

### Step 1: Open Firebase Console
1. Go to https://console.firebase.google.com
2. Select your project
3. Click on "Firestore Database" in the left menu

### Step 2: Create Collection
1. Click "Start collection"
2. Collection ID: `sql_challenges`
3. Click "Next"

### Step 3: Add Each Challenge Document
For each challenge in `sql_challenges.json`, create a document:

#### Challenge 1: sql_challenge_001
- **Document ID**: `sql_challenge_001`
- **Fields**: Click "Add field" for each:

```
title (string): "Select All Students"
description (string): "Write a SQL query to select all columns from the 'students' table. This is the most basic SELECT statement you'll use frequently."
difficulty (string): "Easy"
topic (string): "SELECT Basics"
courseId (string): "sql_course"
expected_query (string): "SELECT * FROM students;"
expected_result (map):
  ├─ columns (array): ["id", "name", "age", "grade"]
  └─ rows (array):
      ├─ [1, "Alice Johnson", 20, "A"]
      ├─ [2, "Bob Smith", 22, "B"]
      ├─ [3, "Charlie Brown", 21, "A"]
      ├─ [4, "Diana Prince", 23, "C"]
      └─ [5, "Eve Wilson", 20, "B"]
sample_table (map):
  ├─ name (string): "students"
  ├─ columns (array): ["id", "name", "age", "grade"]
  └─ rows (array):
      ├─ [1, "Alice Johnson", 20, "A"]
      ├─ [2, "Bob Smith", 22, "B"]
      ├─ [3, "Charlie Brown", 21, "A"]
      ├─ [4, "Diana Prince", 23, "C"]
      └─ [5, "Eve Wilson", 20, "B"]
additional_tables (array): []
hints (array): ["Use the SELECT keyword", "* means all columns", "Don't forget the FROM clause"]
createdAt (string): "2025-01-15T10:00:00Z"
updatedAt (string): "2025-01-15T10:00:00Z"
author (string): "System"
status (string): "active"
order (number): 1
tags (array): ["beginner", "select", "basic"]
testCases (array):
  └─ (map):
      ├─ id (number): 1
      ├─ description (string): "Should return all rows"
      ├─ expectedRowCount (number): 5
      └─ expectedColumnCount (number): 4
```

Repeat for all 8 challenges in the JSON file.

---

## Method 2: Using Node.js Upload Script (Faster)

### Prerequisites
1. Install Node.js from https://nodejs.org
2. Open terminal in the project folder

### Step 1: Install Firebase Admin SDK
```bash
npm install firebase-admin
```

### Step 2: Get Firebase Service Account Key
1. Go to Firebase Console → Project Settings
2. Click "Service Accounts" tab
3. Click "Generate new private key"
4. Save the JSON file to your project folder

### Step 3: Update Upload Script
Edit `upload_sql_challenges.js`:
```javascript
const serviceAccount = require('./your-downloaded-key.json');
```

### Step 4: Run Upload Script
```bash
node upload_sql_challenges.js
```

You should see:
```
🚀 Starting SQL Challenges Upload...

✅ Queued: sql_challenge_001 - Select All Students
✅ Queued: sql_challenge_002 - Filter Students by Grade
...
✅ Successfully uploaded 8 SQL challenges to Firestore!
🎉 Upload complete!
```

---

## Method 3: Import JSON (If Available)

Some Firebase projects allow JSON import:

1. Go to Firestore Database
2. Click on the three dots menu (⋮)
3. If "Import" option exists, select it
4. Upload `sql_challenges.json`

**Note**: This feature may not be available in all Firebase plans.

---

## Verification

After upload, verify in Firebase Console:

1. Go to Firestore Database
2. Expand `sql_challenges` collection
3. You should see 8 documents:
   - sql_challenge_001
   - sql_challenge_002
   - sql_challenge_003
   - sql_challenge_004
   - sql_challenge_005
   - sql_challenge_006
   - sql_challenge_007
   - sql_challenge_008

4. Click on any document to view its fields

---

## Testing in App

After uploading:

1. Build and run your Android app
2. Navigate to SQL challenges section
3. Challenges should load and display
4. Check Logcat for any errors

---

## Quick Reference: Challenge Summary

| ID | Title | Difficulty | Topic |
|----|-------|------------|-------|
| 001 | Select All Students | Easy | SELECT Basics |
| 002 | Filter Students by Grade | Easy | WHERE Clause |
| 003 | Count Total Products | Easy | Aggregate Functions |
| 004 | Order Products by Price | Easy | ORDER BY |
| 005 | Products in Electronics Category | Medium | Multiple Conditions |
| 006 | Average Product Price by Category | Medium | GROUP BY |
| 007 | Join Orders with Customers | Medium | INNER JOIN |
| 008 | Find Students with Highest Age | Hard | Subqueries |

---

## Troubleshooting

### Error: "Permission denied"
- Check Firestore Rules
- Make sure you have write access
- Service account key must have proper permissions

### Error: "Collection not found"
- Create the collection first in Firebase Console
- Use exact name: `sql_challenges`

### Data not showing in app
- Check app's Firestore query path
- Verify document IDs match
- Check Logcat for error messages
