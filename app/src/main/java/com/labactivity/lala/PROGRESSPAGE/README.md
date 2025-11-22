# User Progress Tracking System

This module implements a comprehensive login tracking system that monitors and displays user engagement through daily login patterns.

## Features

### 1. Daily Login Tracking
- Automatically records user logins each day
- Prevents duplicate entries for the same day
- Stores login history in Firestore

### 2. Weekly Progress View
- Shows current week (Monday-Sunday) login status
- Displays achievement count (e.g., "5/7 Achieved")
- Visual circle indicators for each day of the week
- Green circles indicate days with login activity
- Gray circles indicate days without login

### 3. Historical Calendar View
- Displays past 4 months of login history
- Scrollable horizontal calendar grid
- Visual representation of login patterns
- 7-column grid (Sunday-Saturday)

### 4. Streak Calculation
- **Current Streak**: Consecutive days from today going backwards
- **Longest Streak**: Maximum consecutive login days ever achieved
- **Weekly Achievement**: Number of days logged in during current week

## Architecture

### Data Models

#### UserProgress
```kotlin
data class UserProgress(
    val userId: String,
    val loginDates: List<Timestamp>,
    val lastLoginDate: Timestamp?,
    val currentStreak: Int,
    val longestStreak: Int,
    val weeklyTarget: Int = 7,
    val weeklyAchievement: Int
)
```

#### CalendarDay
```kotlin
data class CalendarDay(
    val timestamp: Timestamp,
    val isActive: Boolean,
    val dayOfMonth: Int,
    val month: Int,
    val year: Int
)
```

#### WeekStatus
```kotlin
data class WeekStatus(
    val monday: Boolean,
    val tuesday: Boolean,
    val wednesday: Boolean,
    val thursday: Boolean,
    val friday: Boolean,
    val saturday: Boolean,
    val sunday: Boolean
)
```

### Components

#### UserProgressActivity
Main activity that:
- Records login when user opens the page
- Loads and displays user progress data
- Updates weekly circles and calendar view
- Handles bottom navigation

#### ProgressService
Service class that:
- Interacts with Firestore
- Records login dates
- Calculates streaks
- Prevents duplicate date entries

#### CalendarAdapter
RecyclerView adapter for displaying calendar grids:
- Shows active/inactive days
- Handles empty cells for calendar alignment
- Updates circle colors based on login status

## Firestore Structure

```
users/
  └── {userId}/
      └── login_tracking/
          └── progress/
              ├── userId: string
              ├── loginDates: array of timestamps
              ├── lastLoginDate: timestamp
              ├── currentStreak: number
              ├── longestStreak: number
              ├── weeklyTarget: number (default: 7)
              └── weeklyAchievement: number
```

## Security Rules

The Firestore rules ensure:
- Only authenticated users can read/write their own login tracking data
- Users cannot access other users' login data

```javascript
match /users/{userId}/login_tracking/{progressId} {
  allow read, write: if request.auth != null && request.auth.uid == userId;
}
```

## UI Components

### Weekly Target Card
- Shows current week date range
- Displays achievement count (X/7)
- 7 circular indicators for each day of the week

### Daily Login Card
- Shows "Past 4 months" duration
- Horizontal scrollable calendar view
- Grid layout with 7 columns per month
- Month labels (Aug, Sep, Oct, Nov)

### Bottom Navigation
Includes navigation to:
- Home (MainActivity4)
- Profile (ProfileMainActivity5)
- Settings (SettingsActivity)
- Growth/Progress (UserProgressActivity) - current page

## Usage

### Navigation
Users can access the User Progress page by tapping the "Growth" icon in the bottom navigation bar.

### Automatic Login Recording
When the UserProgressActivity is opened:
1. It automatically records the current day as a login
2. Checks if the day was already recorded (prevents duplicates)
3. Updates streak calculations
4. Refreshes the UI to show updated progress

### Data Refresh
- Data is loaded in `onCreate()`
- Data is refreshed in `onResume()` when user returns to the activity

## Implementation Details

### Date Normalization
All dates are normalized to midnight (00:00:00) to ensure consistent day comparison:
```kotlin
val today = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.time
```

### Week Calculation
Weeks are calculated from Monday to Sunday:
- Sunday is treated as the last day of the week
- Current week is calculated based on offset from Monday

### Calendar Grid Alignment
The calendar grid accounts for the first day of the month:
- Empty cells are added before the 1st day to align with proper weekday
- Grid always has 7 columns (Sunday-Saturday)

## Future Enhancements

Potential improvements:
1. Add monthly view with more detailed statistics
2. Implement achievements/badges for streak milestones
3. Add sharing functionality for progress
4. Include charts/graphs for visual analytics
5. Add notifications/reminders to maintain streaks
6. Export login history as CSV/PDF
