2. Update Firestore Rules
   Deploy the Firestore rules from the provided document to your Firebase Console:
   bashfirebase deploy --only firestore:rules
3. Deploy Cloud Functions
   Navigate to your functions directory and deploy:
   bashcd functions
   npm install
   firebase deploy --only functions
4. Update Your Layout XML
   Use the provided cardProblemOfDay XML layout in your home fragment layout.
5. Implement the Fragment
   Replace or update your HomeFragment.kt with the provided implementation.

Usage
For Users

View Active Problem: The daily problem card automatically appears when there's an active problem
See Countdown: Real-time countdown shows hours, minutes, and seconds remaining
Click to Solve: Tap the card to open the appropriate compiler (Python/Java/SQL)
Submit Solution: Code is automatically tracked in user's progress

For Admins (CMS)
Daily problems are created and managed through your CMS (Content Management System). The CMS should create documents in the daily_problem collection with the following structure:
json{
"courseId": "python_101",
"compilerType": "pythoncompiler",
"title": "Two Sum Problem",
"description": "Brief description...",
"problemStatement": "Full problem details...",
"difficulty": "easy",
"points": 10,
"testCases": [
{
"input": "test input",
"expectedOutput": "expected output",
"isHidden": false
}
],
"hints": ["Hint 1", "Hint 2"],
"createdAt": "SERVER_TIMESTAMP",
"expiredAt": "TIMESTAMP (createdAt + 24 hours)",
"isActive": true,
"tags": ["arrays", "strings"]
}
The Android app automatically fetches and displays these problems to users.

Firestore Data Structure
Collection: daily_problem
json{
"problemId": "auto-generated-id",
"courseId": "python_101",
"compilerType": "pythoncompiler",
"title": "Two Sum Problem",
"description": "Brief description...",
"problemStatement": "Full problem details...",
"difficulty": "easy",
"points": 10,
"testCases": [
{
"input": "test input",
"expectedOutput": "expected result",
"isHidden": false
}
],
"hints": ["Hint 1", "Hint 2"],
"createdAt": Timestamp,
"expiredAt": Timestamp,
"isActive": true,
"tags": ["arrays", "strings"]
}
Collection: users/{userId}/daily_problem_progress
json{
"problemId": "daily-problem-id",
"courseId": "python_101",
"status": "completed",
"code": "user's code submission",
"submittedAt": Timestamp,
"score": 10,
"executionTime": 123,
"testCasesPassed": 5,
"totalTestCases": 5
}

How It Works
1. Problem Lifecycle
   Create Problem → Active (24h) → Auto-Delete
   ↓
   User Sees Card
   ↓
   Countdown Timer
   ↓
   User Solves
   ↓
   Progress Saved
2. Automatic Expiration

Client-side: ViewModel filters problems where expiredAt > currentTime
Server-side: Cloud Function runs hourly to delete expired problems
UI: Card automatically hides when time expires

3. Real-time Updates

Uses Firestore snapshot listeners
Updates every second via coroutine
Automatically stops when problem expires

4. Compiler Integration
   When user clicks the problem card:
   kotlinprivate fun navigateToCompiler(problemId: String, courseId: String, compilerType: String) {
   when (CompilerType.fromString(compilerType)) {
   CompilerType.PYTHON -> {
   // Navigate to Python compiler with problemId
   findNavController().navigate(
   R.id.action_homeFragment_to_pythonCompilerFragment,
   Bundle().apply {
   putString("problemId", problemId)
   putString("courseId", courseId)
   }
   )
   }
   CompilerType.JAVA -> {
   // Navigate to Java compiler
   }
   CompilerType.SQL -> {
   // Navigate to SQL compiler
   }
   }
   }

Cloud Functions
Scheduled Functions

cleanupExpiredProblems - Runs every hour

Deletes problems where expiredAt <= now
Logs deletion count


archiveExpiredProblemProgress - Runs daily

Archives old progress data (optional)
Keeps historical records



Triggered Functions

onDailyProblemCreated - Triggers on new problem

Can send notifications
Update analytics


updateUserXPOnProblemCompletion - Triggers on submission

Awards XP points
Updates leaderboard
Increments completion count



Callable Functions

manualCleanupExpiredProblems - Admin only

Manually trigger cleanup
Returns count of deleted problems




Testing
Test Problem Creation
kotlin// In your test or debug screen
lifecycleScope.launch {
val helper = DailyProblemAdminHelper()

    // Create test problem with 1 hour duration
    val testProblem = helper.createSamplePythonProblem()
    helper.createDailyProblem(testProblem, durationHours = 1).onSuccess {
        Toast.makeText(context, "Test problem created", Toast.LENGTH_SHORT).show()
    }
}
Test Problem Expiration
kotlin// Create a problem that expires in 5 minutes
val shortProblem = DailyProblem(/* ... */)
helper.createDailyProblem(shortProblem, durationHours = 0) // Less than 1 hour

// Manually set expiration
val calendar = Calendar.getInstance()
calendar.add(Calendar.MINUTE, 5)
// Update expiredAt field

Troubleshooting
Problem Card Not Showing

Check Firestore rules are deployed
Verify user is authenticated
Check if problem hasn't expired
Look at Logcat for errors

kotlin// Add debug logging
viewModel.activeProblem.collect { problem ->
Log.d("HomeFragment", "Active problem: ${problem?.title}")
}
Countdown Not Updating

Ensure lifecycle is in STARTED state
Check if viewModel is properly initialized
Verify timestamp is in the future

Solutions Not Saving

Check user authentication
Verify Firestore rules allow write
Ensure all required fields are present

kotlin// Log submission attempt
viewModel.submitSolution(/* ... */).also {
Log.d("Submit", "Attempting to submit solution")
}

Security Considerations
✅ Firestore Rules: Only admins can create/update/delete problems
✅ User Progress: Users can only read/write their own progress
✅ Input Validation: All fields validated in Firestore rules
✅ Token Verification: Admin functions check for admin token

Future Enhancements
Suggested Features

Streak Tracking: Track consecutive days solved
Difficulty Levels: Award more points for harder problems
Hints System: Unlock hints for points
Discussion Board: Let users discuss solutions after expiry
Push Notifications: Notify users of new daily problems
Achievement Badges: Award badges for milestones
Solution Sharing: Share solutions after problem expires
Leaderboard Integration: Rank users by daily problem completions

Implementation Examples
Streak Tracking
kotlin// In Cloud Function
exports.updateUserStreak = functions.firestore
.document('users/{userId}/daily_problem_progress/{problemId}')
.onCreate(async (snap, context) => {
const userId = context.params.userId;
const data = snap.data();

        if (data.status === 'completed') {
            const userRef = admin.firestore().collection('users').doc(userId);
            const userData = (await userRef.get()).data();
            
            const lastCompleted = userData.lastProblemCompletedAt;
            const now = admin.firestore.Timestamp.now();
            
            // Check if completed within 24 hours
            const hoursDiff = (now.toMillis() - lastCompleted.toMillis()) / (1000 * 60 * 60);
            
            if (hoursDiff <= 24) {
                // Increment streak
                await userRef.update({
                    currentStreak: admin.firestore.FieldValue.increment(1),
                    lastProblemCompletedAt: now
                });
            } else {
                // Reset streak
                await userRef.update({
                    currentStreak: 1,
                    lastProblemCompletedAt: now
                });
            }
        }
    });

Support
For issues or questions:

Check Firestore Console for data structure
Review Logcat for error messages
Verify all dependencies are installed
Ensure Cloud Functions are deployed


License
This implementation is provided as-is for your learning platform.

Credits
Built with:

Firebase Firestore
Firebase Cloud Functions
Kotlin Coroutines
Android Jetpack
Material Design Components