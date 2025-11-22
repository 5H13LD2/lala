# Daily Problem - Firestore Sample Data

This guide provides 3 complete sample problems to add to your Firestore `daily_problem` collection.

## Collection Structure

**Collection Name**: `daily_problem`

**Location**: Firestore Console → Your Project → Firestore Database

---

## How to Add Problems to Firestore

### Method 1: Firebase Console (Manual)

1. Open **Firebase Console** → **Firestore Database**
2. Click **"Start collection"** (if `daily_problem` doesn't exist)
   - Collection ID: `daily_problem`
3. Click **"Add document"**
4. Use **Auto-ID** or enter the `problemId` as Document ID
5. Add each field manually using the field types specified below

### Method 2: Using Firebase Admin SDK (Recommended for bulk import)

```kotlin
// Example code to add problems programmatically
val db = FirebaseFirestore.getInstance()

val problem = hashMapOf(
    "problemId" to "daily_java_001",
    "courseId" to "java_fundamentals",
    "compilerType" to "javacompiler",
    // ... rest of fields
)

db.collection("daily_problem")
    .document("daily_java_001")
    .set(problem)
```

---

## Sample Problem 1: Java - Two Sum (Easy)

### Document ID
`daily_java_001`

### Fields

| Field Name | Type | Value |
|------------|------|-------|
| `problemId` | string | `daily_java_001` |
| `courseId` | string | `java_fundamentals` |
| `compilerType` | string | `javacompiler` |
| `title` | string | `Two Sum` |
| `description` | string | `Given an array of integers nums and an integer target, return indices of the two numbers that add up to target.` |
| `problemStatement` | string | See below ⬇️ |
| `difficulty` | string | `easy` |
| `points` | number | `10` |
| `testCases` | array | See below ⬇️ |
| `hints` | array | See below ⬇️ |
| `createdAt` | timestamp | Current date/time |
| `expiredAt` | timestamp | 24 hours from createdAt |
| `isActive` | boolean | `true` |
| `tags` | array | `["array", "hash-table", "easy", "java"]` |

#### problemStatement (string)
```
Write a Java class with a method that finds two numbers in an array that sum to a target value.

Constraints:
- 2 <= nums.length <= 100
- Each input has exactly one solution
- You may not use the same element twice

Example:
Input: nums = [2,7,11,15], target = 9
Output: [0,1]
Explanation: nums[0] + nums[1] == 9, so return [0, 1]
```

#### testCases (array of maps)

**Test Case 1** (map):
- `input` (string): `[2,7,11,15], 9`
- `expectedOutput` (string): `[0, 1]`
- `isHidden` (boolean): `false`

**Test Case 2** (map):
- `input` (string): `[3,2,4], 6`
- `expectedOutput` (string): `[1, 2]`
- `isHidden` (boolean): `false`

**Test Case 3** (map):
- `input` (string): `[1,5,3,7,9], 12`
- `expectedOutput` (string): `[2, 4]`
- `isHidden` (boolean): `true`

#### hints (array of strings)
```
[
  "Use a HashMap to store numbers you've seen",
  "For each number, check if target - number exists in the HashMap",
  "Return indices when you find a match"
]
```

#### Sample Solution Code (for reference)
```java
public class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (map.containsKey(complement)) {
                return new int[] { map.get(complement), i };
            }
            map.put(nums[i], i);
        }
        return new int[] {};
    }
}
```

---

## Sample Problem 2: Python - Valid Palindrome (Medium)

### Document ID
`daily_python_001`

### Fields

| Field Name | Type | Value |
|------------|------|-------|
| `problemId` | string | `daily_python_001` |
| `courseId` | string | `python_basics` |
| `compilerType` | string | `pythoncompiler` |
| `title` | string | `Valid Palindrome` |
| `description` | string | `A phrase is a palindrome if it reads the same forward and backward after converting all uppercase letters to lowercase and removing all non-alphanumeric characters.` |
| `problemStatement` | string | See below ⬇️ |
| `difficulty` | string | `medium` |
| `points` | number | `15` |
| `testCases` | array | See below ⬇️ |
| `hints` | array | See below ⬇️ |
| `createdAt` | timestamp | Current date/time |
| `expiredAt` | timestamp | 24 hours from createdAt |
| `isActive` | boolean | `true` |
| `tags` | array | `["string", "two-pointers", "medium", "python"]` |

#### problemStatement (string)
```
Write a Python function that checks if a given string is a valid palindrome.

Constraints:
- 1 <= s.length <= 200
- s consists only of printable ASCII characters
- Ignore spaces, punctuation, and capitalization

Example:
Input: "A man, a plan, a canal: Panama"
Output: True
Explanation: "amanaplanacanalpanama" is a palindrome
```

#### testCases (array of maps)

**Test Case 1** (map):
- `input` (string): `A man, a plan, a canal: Panama`
- `expectedOutput` (string): `True`
- `isHidden` (boolean): `false`

**Test Case 2** (map):
- `input` (string): `race a car`
- `expectedOutput` (string): `False`
- `isHidden` (boolean): `false`

**Test Case 3** (map):
- `input` (string): `Was it a car or a cat I saw?`
- `expectedOutput` (string): `True`
- `isHidden` (boolean): `true`

#### hints (array of strings)
```
[
  "First, clean the string by removing non-alphanumeric characters",
  "Convert the string to lowercase",
  "Compare the string with its reverse",
  "You can use two pointers - one from start, one from end"
]
```

#### Sample Solution Code (for reference)
```python
def isPalindrome(s):
    # Remove non-alphanumeric and convert to lowercase
    cleaned = ''.join(char.lower() for char in s if char.isalnum())

    # Check if palindrome
    return cleaned == cleaned[::-1]

# Alternative: Two pointer approach
def isPalindrome2(s):
    left, right = 0, len(s) - 1

    while left < right:
        while left < right and not s[left].isalnum():
            left += 1
        while left < right and not s[right].isalnum():
            right -= 1

        if s[left].lower() != s[right].lower():
            return False

        left += 1
        right -= 1

    return True
```

---

## Sample Problem 3: SQL - Find High Earners (Easy)

### Document ID
`daily_sql_001`

### Fields

| Field Name | Type | Value |
|------------|------|-------|
| `problemId` | string | `daily_sql_001` |
| `courseId` | string | `sql_basics` |
| `compilerType` | string | `sqlcompiler` |
| `title` | string | `Find High Earners` |
| `description` | string | `Write a SQL query to find all employees who earn more than the average salary in their department.` |
| `problemStatement` | string | See below ⬇️ |
| `difficulty` | string | `easy` |
| `points` | number | `10` |
| `testCases` | array | See below ⬇️ |
| `hints` | array | See below ⬇️ |
| `createdAt` | timestamp | Current date/time |
| `expiredAt` | timestamp | 24 hours from createdAt |
| `isActive` | boolean | `true` |
| `tags` | array | `["sql", "aggregate-functions", "subquery", "easy"]` |

#### problemStatement (string)
```
Given a table 'employees' with columns:
- employee_id (INT)
- name (VARCHAR)
- department (VARCHAR)
- salary (DECIMAL)

Write a query to return the name, department, and salary of employees whose salary is above their department's average.

Example:
Input: employees table with 10 rows
Output: Employees earning above department average

Order results by department ASC, salary DESC
```

#### testCases (array of maps)

**Test Case 1** (map):
- `input` (string): `SELECT * FROM employees WHERE department='Engineering'`
- `expectedOutput` (string): `3 rows returned`
- `isHidden` (boolean): `false`

**Test Case 2** (map):
- `input` (string): `SELECT AVG(salary) FROM employees GROUP BY department`
- `expectedOutput` (string): `Department averages calculated`
- `isHidden` (boolean): `false`

**Test Case 3** (map):
- `input` (string): `Full query with subquery`
- `expectedOutput` (string): `5 employees above average`
- `isHidden` (boolean): `true`

#### hints (array of strings)
```
[
  "Use a subquery to calculate the average salary per department",
  "Join the employees table with the department averages",
  "Use WHERE clause to filter employees above average",
  "Remember to GROUP BY department when calculating averages"
]
```

#### Sample Solution Code (for reference)
```sql
-- Solution 1: Using subquery with JOIN
SELECT e.name, e.department, e.salary
FROM employees e
JOIN (
    SELECT department, AVG(salary) as avg_salary
    FROM employees
    GROUP BY department
) dept_avg ON e.department = dept_avg.department
WHERE e.salary > dept_avg.avg_salary
ORDER BY e.department ASC, e.salary DESC;

-- Solution 2: Using correlated subquery
SELECT name, department, salary
FROM employees e1
WHERE salary > (
    SELECT AVG(salary)
    FROM employees e2
    WHERE e1.department = e2.department
)
ORDER BY department ASC, salary DESC;
```

---

## Compiler Types Reference

Based on your codebase structure:

| Compiler Type | Package | Used For |
|---------------|---------|----------|
| `javacompiler` | `com.labactivity.lala.JAVACOMPILER` | Java code execution using `JavaRunner` |
| `pythoncompiler` | `com.labactivity.lala.PYTHONASSESMENT` | Python code execution (Chaquopy) |
| `sqlcompiler` | `com.labactivity.lala.SQLCOMPILER` | SQL query execution using `QueryEvaluator` |

---

## Important Notes

### 1. Course IDs
Replace the generic course IDs (`java_fundamentals`, `python_basics`, `sql_basics`) with actual course IDs from your Firestore `users/{userId}/courseTaken` collection.

To find your actual course IDs:
1. Go to Firestore Console
2. Navigate to `users/{any-user-id}`
3. Look at the `courseTaken` array
4. Find courseIds that contain "java", "python", or "sql"

Example actual courseIds might be:
- `course_java_101`
- `course_python_beginner`
- `course_sql_fundamentals`

### 2. Timestamp Fields
When adding via Firebase Console:
- Select field type: **timestamp**
- For `createdAt`: Click "Set to current time" or choose a specific date/time
- For `expiredAt`: Set to 24 hours after `createdAt`

### 3. Problem Visibility Logic
From `MainActivity4.kt:287-318` and `DailyProblemViewModel`:
- Only **active** problems with `isActive = true` are shown
- Problems are hidden after `expiredAt` timestamp
- Only ONE active problem is shown at a time (the most recent)

### 4. User Progress Tracking
When a user submits a solution, progress is saved to:
- Collection: `users/{userId}/daily_problem_progress/{problemId}`
- Fields: `code`, `status`, `score`, `executionTime`, `testCasesPassed`, `totalTestCases`, `submittedAt`

### 5. Security Rules
Already configured in `firestore.rules`:
```javascript
// Global daily problems (read-only for users, admin-write)
match /daily_problem/{docId} {
  allow read: if request.auth != null;
  allow write: if request.auth.token.admin == true;
}

// User-specific progress
match /users/{userId}/daily_problem_progress/{problemId} {
  allow read, write: if request.auth != null && request.auth.uid == userId;
}
```

---

## Testing the Implementation

### 1. Add a Problem to Firestore
Follow the steps above to add at least one problem.

### 2. Verify Display on MainActivity4
- Open your app
- Navigate to home (MainActivity4)
- You should see the "Problem of the Day" card with:
  - Problem title
  - Problem description
  - Countdown timer (hours, minutes, seconds)

### 3. Test Problem Flow
- Click the card → Opens `ProblemOfDayActivity`
- Tab 1 (Problem): View problem statement, difficulty, test cases
- Tab 2 (Editor): Write and run code
- Tab 3 (Solution): View solution (after attempt or expiration)

### 4. Test Submission
- Write code in the editor
- Click "Run" to test execution
- Click "Submit" to validate and save progress
- Check Firestore `users/{userId}/daily_problem_progress/` for saved submission

---

## Quick Copy-Paste JSON (for testing)

If your Firestore console supports JSON import, use this:

```json
{
  "daily_java_001": {
    "problemId": "daily_java_001",
    "courseId": "java_fundamentals",
    "compilerType": "javacompiler",
    "title": "Two Sum",
    "description": "Given an array of integers nums and an integer target, return indices of the two numbers that add up to target.",
    "problemStatement": "Write a Java class with a method that finds two numbers in an array that sum to a target value.\n\nConstraints:\n- 2 <= nums.length <= 100\n- Each input has exactly one solution\n- You may not use the same element twice\n\nExample:\nInput: nums = [2,7,11,15], target = 9\nOutput: [0,1]\nExplanation: nums[0] + nums[1] == 9, so return [0, 1]",
    "difficulty": "easy",
    "points": 10,
    "testCases": [
      {
        "input": "[2,7,11,15], 9",
        "expectedOutput": "[0, 1]",
        "isHidden": false
      },
      {
        "input": "[3,2,4], 6",
        "expectedOutput": "[1, 2]",
        "isHidden": false
      },
      {
        "input": "[1,5,3,7,9], 12",
        "expectedOutput": "[2, 4]",
        "isHidden": true
      }
    ],
    "hints": [
      "Use a HashMap to store numbers you've seen",
      "For each number, check if target - number exists in the HashMap",
      "Return indices when you find a match"
    ],
    "isActive": true,
    "tags": ["array", "hash-table", "easy", "java"]
  }
}
```

**Note**: Replace timestamps with actual Firestore timestamp objects when adding manually.

---

## Troubleshooting

### Problem Card Not Showing
1. Check Firestore: Does `daily_problem` collection exist?
2. Check `isActive` field is `true`
3. Check `expiredAt` is in the future
4. Check logs: `MainActivity4` logs problem fetch status

### Countdown Timer Shows 00:00:00
- Verify `expiredAt` timestamp is set correctly
- Check `DailyProblemViewModel:89-115` for timer calculation logic

### Editor Not Executing Code
- **Java**: Check `JavaRunner` is available
- **Python**: Check Chaquopy is configured (currently shows placeholder)
- **SQL**: Check `QueryEvaluator` implementation

### Submission Not Saving
- Check user is authenticated (`FirebaseAuth.getInstance().currentUser`)
- Check Firestore rules allow write to `users/{userId}/daily_problem_progress/`
- Check network connection

---

## Next Steps

1. **Add Problems**: Use this guide to add the 3 sample problems
2. **Test Flow**: Verify display → open → solve → submit
3. **Customize**: Modify problems for your actual courses
4. **Scale**: Add more problems for different days
5. **Automate**: Consider cloud function to activate new problem daily

---

**File Location**: `app/src/main/java/com/labactivity/lala/DAILYPROBLEMPAGE/FIRESTORE_SAMPLE_DATA.md`

**Last Updated**: 2025-11-22
