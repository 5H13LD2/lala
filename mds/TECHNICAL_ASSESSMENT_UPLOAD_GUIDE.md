# Technical Assessment Upload Guide

## 📋 Overview

This guide explains how to upload **20 technical assessment challenges** to Firebase Firestore for your app.

**Collection Name:** `technical_assesment` (note: typo from your codebase)

---

## 📊 Challenge Breakdown

### By Difficulty:
- **Easy:** 8 challenges
- **Medium:** 10 challenges
- **Hard:** 2 challenges

### By Programming Language:
- **Python:** 10 challenges
- **Java:** 4 challenges
- **JavaScript:** 4 challenges
- **SQL:** 2 challenges

---

## 🚀 Method 1: Using Node.js Upload Script (Recommended)

### Step 1: Install Dependencies
```bash
npm install firebase-admin
```

### Step 2: Get Firebase Service Account Key
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project
3. Click **Settings** (⚙️) → **Project Settings**
4. Go to **Service Accounts** tab
5. Click **Generate New Private Key**
6. Save the JSON file to your project folder

### Step 3: Update Upload Script
Edit `upload_technical_assessments.js`:
```javascript
const serviceAccount = require('./your-firebase-key.json');
```

### Step 4: Run Upload
```bash
node upload_technical_assessments.js
```

**Expected Output:**
```
🚀 Starting Technical Assessment Upload...

✅ Queued: python_easy_001
   Title: Fix the Hello World
   Language: python | Difficulty: Easy

✅ Queued: python_easy_002
   Title: Fix Variable Assignment
   Language: python | Difficulty: Easy

...

✅ Successfully uploaded 20 technical assessments to Firestore!

📊 Upload Statistics:
═══════════════════════════════════════
Total Challenges: 20

By Difficulty:
  Easy: 8
  Medium: 10
  Hard: 2

By Programming Language:
  python: 10
  java: 4
  javascript: 4
  sql: 2
═══════════════════════════════════════

✅ Total challenges in Firestore: 20
🎉 Upload complete!
```

---

## 📝 Method 2: Manual Upload via Firebase Console

### Step 1: Open Firestore
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project
3. Click **Firestore Database**

### Step 2: Create Collection
1. Click **Start Collection** (if first time) or **Add Collection**
2. Collection ID: `technical_assesment`
3. Click **Next**

### Step 3: Add Documents

For each challenge in `technical_assessments.json`, add a document:

#### Example: Python Easy Challenge

**Document ID:** `python_easy_001`

**Fields:**
```
title (string): "Fix the Hello World"
difficulty (string): "Easy"
courseId (string): "python_course"
compilerType (string): "python"
brokenCode (string): "# Fix the code to print 'Hello, World!'\nprint('Hello World)"
correctOutput (string): "Hello, World!"
hint (string): "Check for missing closing quote and the comma after 'Hello'"
category (string): "Syntax Basics"
status (string): "available"
createdAt (string): "2025-01-15T10:00:00Z"
```

Repeat for all 20 challenges.

---

## 🔍 Data Structure Details

### Challenge Model (from your code)
```kotlin
data class Challenge(
    val id: String = "",                // Document ID
    val title: String = "",             // Challenge title
    val difficulty: String = "",        // "Easy", "Medium", "Hard"
    val courseId: String = "",          // Course this belongs to
    val compilerType: String = "",      // "python", "java", "javascript", "sql"
    val brokenCode: String = "",        // Code with bugs/errors
    val correctOutput: String = "",     // Expected output when fixed
    val hint: String = "",              // Hint for solving
    val category: String = "",          // Topic category
    val status: String = "available",   // Challenge status
    val createdAt: String = "",         // Timestamp
    val isUnlocked: Boolean = true      // Computed at runtime
)
```

### Important Notes:
- **compilerType** supports: `python`, `java`, `javascript`, `sql`, `kotlin`, `ruby`, `php`
- **difficulty** must be: `Easy`, `Medium`, or `Hard` (case-sensitive)
- **status** should be: `available` (other values: `locked`, `archived`)
- **courseId** must match courses in your users' `courseTaken` array

---

## 📚 Challenge Categories

### Python Challenges (10)
- Syntax Basics
- Variables
- Loops
- Data Structures (Lists, Dictionaries)
- Functions
- List Comprehensions
- String Formatting
- Recursion
- Object-Oriented Programming

### Java Challenges (4)
- Syntax Basics
- Variables
- Arrays
- Methods

### JavaScript Challenges (4)
- Variables
- Functions (Arrow Functions)
- Array Methods
- Destructuring

### SQL Challenges (2)
- SELECT Basics
- Filtering (WHERE)

---

## ✅ Verification Steps

After uploading:

### 1. Check Firestore Console
- Navigate to `technical_assesment` collection
- Verify 20 documents exist
- Check a few documents to ensure fields are correct

### 2. Test in Your App
1. Build and run the app
2. Navigate to Technical Assessment section
3. Challenges should load based on user's enrolled courses

### 3. Check Unlock Logic
From `TechnicalAssessmentService.kt`:
- **Easy challenges:** Always unlocked
- **Medium challenges:** Unlocked when ALL Easy are completed
- **Hard challenges:** Unlocked when ALL Easy AND Medium are completed

### 4. Verify Logcat
Filter by `TechnicalAssessmentService`:
```
✅ Found X challenges for enrolled courses
🔓 Easy: X total, all completed: true/false
🔓 Medium: X total, all completed: true/false
```

---

## 🎯 Challenge List Summary

| ID | Title | Language | Difficulty | Category |
|----|-------|----------|------------|----------|
| python_easy_001 | Fix the Hello World | Python | Easy | Syntax Basics |
| python_easy_002 | Fix Variable Assignment | Python | Easy | Variables |
| python_easy_003 | Fix the Loop | Python | Easy | Loops |
| python_easy_004 | Fix List Indexing | Python | Easy | Data Structures |
| python_medium_001 | Fix Function Definition | Python | Medium | Functions |
| python_medium_002 | Fix Dictionary Access | Python | Medium | Data Structures |
| python_medium_003 | Fix List Comprehension | Python | Medium | List Comprehensions |
| python_medium_004 | Fix String Formatting | Python | Medium | String Formatting |
| python_hard_001 | Fix Recursive Function | Python | Hard | Recursion |
| python_hard_002 | Fix Class Definition | Python | Hard | OOP |
| java_easy_001 | Fix Java Hello World | Java | Easy | Syntax Basics |
| java_easy_002 | Fix Variable Declaration | Java | Easy | Variables |
| java_medium_001 | Fix Array Access | Java | Medium | Arrays |
| java_medium_002 | Fix Method Return Type | Java | Medium | Methods |
| javascript_easy_001 | Fix JavaScript Variable | JavaScript | Easy | Variables |
| javascript_easy_002 | Fix Arrow Function | JavaScript | Easy | Functions |
| javascript_medium_001 | Fix Array Method | JavaScript | Medium | Array Methods |
| javascript_medium_002 | Fix Object Destructuring | JavaScript | Medium | Destructuring |
| sql_easy_001 | Fix SELECT Statement | SQL | Easy | SELECT Basics |
| sql_medium_001 | Fix WHERE Clause | SQL | Medium | Filtering |

---

## 🐛 Troubleshooting

### Issue: Challenges not showing in app
**Solutions:**
1. Check user is enrolled in matching courses (`courseId`)
2. Verify Firestore collection name is `technical_assesment` (with typo)
3. Check Logcat for error messages
4. Ensure user is authenticated

### Issue: All challenges are locked
**Solutions:**
1. Complete Easy challenges first to unlock Medium
2. Complete all Easy + Medium to unlock Hard
3. Check `TechnicalAssessmentService.kt` unlock logic

### Issue: Wrong output when running code
**Solutions:**
1. Verify `brokenCode` has intentional bugs
2. Check `correctOutput` matches expected result
3. Test compiler type matches the code language

---

## 📞 Need More Challenges?

Want more challenges or different topics? The structure is ready for:
- More advanced Python (async, decorators, generators)
- More Java (inheritance, interfaces, exceptions)
- More JavaScript (promises, async/await, closures)
- More SQL (JOINs, subqueries, aggregations)
- New languages (Kotlin, Ruby, PHP)

Let me know what you need! 🚀
