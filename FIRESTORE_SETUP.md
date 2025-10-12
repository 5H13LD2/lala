# Firestore Data Structure for Technical Assessment

## Collection: `technical_assesment`

**✅ CORRECT**: Your Firebase collection is named `technical_assesment` (without the 's')

### Sample Documents:

#### Document 1: `challenge_001`
```json
{
  "title": "Fix the Loop",
  "difficulty": "Easy",
  "courseId": "python_basics",
  "brokenCode": "for i in range(5):\n    print(f\"Line {i}: Hello\")\n    # Missing increment\n    i = i + 1",
  "correctOutput": "Line 0: Hello\nLine 1: Hello\nLine 2: Hello\nLine 3: Hello\nLine 4: Hello",
  "hint": "The loop variable i is being incremented manually, but range() already handles this.",
  "category": "Debugging",
  "status": "available",
  "createdAt": "2024-01-15T10:00:00Z"
}
```

#### Document 2: `challenge_002`
```json
{
  "title": "Variable Scope Issue",
  "difficulty": "Medium",
  "courseId": "python_basics",
  "brokenCode": "def calculate_sum():\n    total = 0\n    for num in numbers:\n        total += num\n    return total\n\nnumbers = [1, 2, 3, 4, 5]\nresult = calculate_sum()",
  "correctOutput": "15",
  "hint": "The variable 'numbers' is not accessible inside the function scope.",
  "category": "Logic",
  "status": "available",
  "createdAt": "2024-01-16T10:00:00Z"
}
```

#### Document 3: `challenge_003`
```json
{
  "title": "List Comprehension Error",
  "difficulty": "Hard",
  "courseId": "python_basics",
  "brokenCode": "numbers = [1, 2, 3, 4, 5]\nsquared = [x * x for x in numbers if x > 2]\nprint(squared)",
  "correctOutput": "[9, 16, 25]",
  "hint": "The list comprehension syntax is correct, but check the condition.",
  "category": "Syntax",
  "status": "taken",
  "createdAt": "2024-01-17T10:00:00Z"
}
```

#### Document 4: `challenge_004`
```json
{
  "title": "SQL Query Fix",
  "difficulty": "Easy",
  "courseId": "sql_basics",
  "brokenCode": "SELECT * FROM users WHERE age > 18 AND name LIKE 'J%'",
  "correctOutput": "All users over 18 with names starting with 'J'",
  "hint": "The SQL syntax looks correct. Check if the table and columns exist.",
  "category": "Query",
  "status": "available",
  "createdAt": "2024-01-18T10:00:00Z"
}
```

## User Document Structure

### Path: `users/{userId}`
```json
{
  "courseTaken": [
    {
      "courseId": "python_basics",
      "courseName": "Python Basics",
      "category": "Programming",
      "difficulty": "Beginner",
      "enrolledAt": 1705312800000
    },
    {
      "courseId": "sql_basics",
      "courseName": "SQL Basics",
      "category": "Database",
      "difficulty": "Beginner",
      "enrolledAt": 1705312800000
    }
  ]
}
```

## Testing Instructions

1. **Setup Firestore Data:**
   - Create the `technical_assessment` collection
   - Add the sample documents above
   - Ensure your user document has the `courseTaken` array

2. **Test Scenarios:**
   - User with enrolled courses should see matching challenges
   - User without enrolled courses should see empty list
   - Challenges with status "taken" should appear darker
   - Clicking "taken" challenges should show retry dialog
   - Clicking "available" challenges should open compiler

3. **Expected Behavior:**
   - Only challenges matching enrolled courseIds are displayed
   - Status field controls visual appearance and interaction
   - Dynamic loading from Firestore
   - Proper error handling for network issues
