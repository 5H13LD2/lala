# SQL Challenges Firestore Schema

## Collection: `sql_challenges/{challengeId}`

### Document Structure

```json
{
  "id": "sql_001",
  "title": "Select All Students",
  "description": "Write an SQL query to display all students from the students table.",
  "difficulty": "Easy",
  "topic": "SELECT Basics",
  "courseId": "sql_fundamentals",

  "expectedQuery": "SELECT * FROM students;",

  "expectedResult": {
    "columns": ["id", "name", "age"],
    "rows": [
      [1, "Jerico", 20],
      [2, "Maria", 21],
      [3, "John", 22]
    ]
  },

  "sampleTable": {
    "name": "students",
    "columns": ["id", "name", "age"],
    "rows": [
      [1, "Jerico", 20],
      [2, "Maria", 21],
      [3, "John", 22]
    ]
  },

  "hints": [
    "Use SELECT * to get all columns",
    "Don't forget the FROM clause",
    "End your query with a semicolon"
  ],

  "createdAt": "2025-10-28T00:00:00Z",
  "updatedAt": "2025-10-28T00:00:00Z",
  "author": "Jerico Jimenez",
  "status": "active",
  "order": 1,

  "tags": ["SELECT", "basic", "fundamentals"],

  "testCases": [
    {
      "id": 1,
      "description": "Should return all student records",
      "expectedRowCount": 3,
      "expectedColumnCount": 3
    }
  ]
}
```

## Example Documents

### Document 1: Basic SELECT
```json
{
  "id": "sql_001",
  "title": "Select All Students",
  "description": "Write an SQL query to display all students from the students table.",
  "difficulty": "Easy",
  "topic": "SELECT Basics",
  "courseId": "sql_fundamentals",
  "expectedQuery": "SELECT * FROM students;",
  "expectedResult": {
    "columns": ["id", "name", "age"],
    "rows": [
      [1, "Jerico", 20],
      [2, "Maria", 21],
      [3, "John", 22]
    ]
  },
  "sampleTable": {
    "name": "students",
    "columns": ["id", "name", "age"],
    "rows": [
      [1, "Jerico", 20],
      [2, "Maria", 21],
      [3, "John", 22]
    ]
  },
  "hints": [
    "Use SELECT * to get all columns",
    "Don't forget the FROM clause"
  ],
  "createdAt": "2025-10-28T10:00:00Z",
  "updatedAt": "2025-10-28T10:00:00Z",
  "author": "Jerico Jimenez",
  "status": "active",
  "order": 1,
  "tags": ["SELECT", "basic"]
}
```

### Document 2: WHERE Clause
```json
{
  "id": "sql_002",
  "title": "Filter Students by Age",
  "description": "Write an SQL query to display students who are 21 years old or older.",
  "difficulty": "Easy",
  "topic": "WHERE Clause",
  "courseId": "sql_fundamentals",
  "expectedQuery": "SELECT * FROM students WHERE age >= 21;",
  "expectedResult": {
    "columns": ["id", "name", "age"],
    "rows": [
      [2, "Maria", 21],
      [3, "John", 22]
    ]
  },
  "sampleTable": {
    "name": "students",
    "columns": ["id", "name", "age"],
    "rows": [
      [1, "Jerico", 20],
      [2, "Maria", 21],
      [3, "John", 22]
    ]
  },
  "hints": [
    "Use the WHERE clause to filter results",
    "Use >= operator for greater than or equal to"
  ],
  "createdAt": "2025-10-28T10:05:00Z",
  "updatedAt": "2025-10-28T10:05:00Z",
  "author": "Jerico Jimenez",
  "status": "active",
  "order": 2,
  "tags": ["WHERE", "filtering", "operators"]
}
```

### Document 3: JOIN Operation
```json
{
  "id": "sql_003",
  "title": "Join Students and Courses",
  "description": "Write an SQL query to display student names along with their enrolled course names.",
  "difficulty": "Medium",
  "topic": "JOINS",
  "courseId": "sql_fundamentals",
  "expectedQuery": "SELECT s.name, c.course_name FROM students s INNER JOIN enrollments e ON s.id = e.student_id INNER JOIN courses c ON e.course_id = c.id;",
  "expectedResult": {
    "columns": ["name", "course_name"],
    "rows": [
      ["Jerico", "SQL Fundamentals"],
      ["Maria", "Python Basics"],
      ["John", "SQL Fundamentals"]
    ]
  },
  "sampleTable": {
    "name": "students",
    "columns": ["id", "name", "age"],
    "rows": [
      [1, "Jerico", 20],
      [2, "Maria", 21],
      [3, "John", 22]
    ]
  },
  "additionalTables": [
    {
      "name": "enrollments",
      "columns": ["id", "student_id", "course_id"],
      "rows": [
        [1, 1, 1],
        [2, 2, 2],
        [3, 3, 1]
      ]
    },
    {
      "name": "courses",
      "columns": ["id", "course_name"],
      "rows": [
        [1, "SQL Fundamentals"],
        [2, "Python Basics"]
      ]
    }
  ],
  "hints": [
    "You need to join three tables: students, enrollments, and courses",
    "Use INNER JOIN to connect related tables",
    "Use table aliases (s, e, c) for cleaner code"
  ],
  "createdAt": "2025-10-28T10:10:00Z",
  "updatedAt": "2025-10-28T10:10:00Z",
  "author": "Jerico Jimenez",
  "status": "active",
  "order": 3,
  "tags": ["JOIN", "INNER JOIN", "intermediate"]
}
```

## Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | String | Yes | Unique challenge identifier |
| `title` | String | Yes | Challenge title/name |
| `description` | String | Yes | Detailed description of what to do |
| `difficulty` | String | Yes | "Easy", "Medium", "Hard" |
| `topic` | String | Yes | Main SQL topic (e.g., "SELECT Basics", "JOINS") |
| `courseId` | String | Yes | Related course ID for filtering |
| `expectedQuery` | String | Yes | The correct SQL query (for reference) |
| `expectedResult` | Object | Yes | Expected output with columns and rows |
| `sampleTable` | Object | Yes | Primary table structure and data |
| `additionalTables` | Array | No | Additional tables for complex queries (JOINs) |
| `hints` | Array | Yes | Array of hint strings |
| `createdAt` | String | Yes | ISO timestamp of creation |
| `updatedAt` | String | Yes | ISO timestamp of last update |
| `author` | String | Yes | Creator's name |
| `status` | String | Yes | "active", "draft", "archived" |
| `order` | Number | Yes | Display order within topic |
| `tags` | Array | No | Searchable tags |
| `testCases` | Array | No | Additional test case metadata |

## Indexes to Create

For optimal query performance, create these indexes in Firestore:

1. **Composite Index**: `courseId ASC, difficulty ASC, order ASC`
2. **Composite Index**: `status ASC, courseId ASC, order ASC`
3. **Composite Index**: `topic ASC, difficulty ASC, order ASC`
4. **Single Field Index**: `createdAt DESC`

## Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // SQL Challenges - Read for authenticated users, Write for admins only
    match /sql_challenges/{challengeId} {
      // Anyone authenticated can read active challenges
      allow read: if request.auth != null &&
                     resource.data.status == 'active';

      // Only admins can create, update, or delete challenges
      allow write: if request.auth != null &&
                      request.auth.token.admin == true;
    }

    // User progress tracking for SQL challenges
    match /users/{userId}/sql_progress/{challengeId} {
      // Users can read and write their own progress
      allow read, write: if request.auth != null &&
                            request.auth.uid == userId;
    }
  }
}
```

## User Progress Sub-Collection

### Collection: `users/{userId}/sql_progress/{challengeId}`

```json
{
  "challengeId": "sql_001",
  "status": "completed",
  "attempts": 3,
  "bestScore": 100,
  "lastAttemptDate": "2025-10-28T12:00:00Z",
  "timeTaken": 300,
  "userQuery": "SELECT * FROM students;",
  "passed": true
}
```

## Query Patterns

### Fetch all active challenges for a course
```kotlin
db.collection("sql_challenges")
  .whereEqualTo("status", "active")
  .whereEqualTo("courseId", courseId)
  .orderBy("order", Query.Direction.ASCENDING)
  .get()
```

### Fetch challenges by difficulty
```kotlin
db.collection("sql_challenges")
  .whereEqualTo("status", "active")
  .whereEqualTo("difficulty", "Easy")
  .orderBy("order", Query.Direction.ASCENDING)
  .get()
```

### Fetch challenges by topic
```kotlin
db.collection("sql_challenges")
  .whereEqualTo("status", "active")
  .whereEqualTo("topic", "JOINS")
  .orderBy("order", Query.Direction.ASCENDING)
  .get()
```
