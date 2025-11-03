# Forgot Password Feature

## Overview
This folder contains the implementation of the Forgot Password functionality using Firebase Authentication's password reset feature.

## Files Created
1. **ForgotPasswordActivity.kt** - Main activity handling password reset logic
2. **activity_forgot_password.xml** - UI layout for the password reset screen
3. **ic_lock_reset.xml** - Lock icon drawable
4. **ic_email.xml** - Email icon drawable

## Features
- ✅ Email validation with proper error messages
- ✅ Firebase Auth integration for password reset
- ✅ Loading states with progress indicator
- ✅ Success confirmation UI
- ✅ Material Design 3 components
- ✅ Responsive layout that works across different screen sizes
- ✅ Back navigation to login screen

## How to Integrate with Login Activity

To add a "Forgot Password?" link to your login screen, add the following code to your **MainActivity.kt** (or your login activity):

### Step 1: Add TextView to Login Layout
Add this TextView to your `activity_main.xml` (login layout):

```xml
<TextView
    android:id="@+id/textForgotPassword"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Forgot Password?"
    android:textSize="14sp"
    android:textColor="@color/primary"
    android:textStyle="bold"
    android:padding="8dp"
    android:clickable="true"
    android:focusable="true"
    android:background="?attr/selectableItemBackground"
    android:layout_gravity="end" />
```

### Step 2: Add Click Listener in MainActivity
Add this code to your login activity's `onCreate()` method:

```kotlin
import android.content.Intent
import com.labactivity.lala.FORGOTPASSWORD.ForgotPasswordActivity

// Inside onCreate() or setupClickListeners()
binding.textForgotPassword.setOnClickListener {
    val intent = Intent(this, ForgotPasswordActivity::class.java)
    startActivity(intent)
}
```

## How It Works

1. **User enters email**: The user enters their registered email address
2. **Validation**: Email is validated for proper format
3. **Firebase sends reset email**: Firebase Authentication sends a password reset link to the email
4. **Success confirmation**: User sees a success message and can return to login
5. **Email link**: User clicks the link in their email to reset password on Firebase's hosted page

## Error Handling

The activity handles common Firebase Auth errors:
- No user found with email
- Invalid email format
- Network errors
- Generic Firebase errors

## Customization

You can customize the following:
- **Colors**: Modify colors in `res/values/colors.xml`
- **Strings**: Extract hardcoded strings to `res/values/strings.xml` for localization
- **Success message**: Modify the success card content in the layout
- **Email validation**: Add custom validation rules in `validateEmail()` method

## Testing

1. Run the app and navigate to the Forgot Password screen
2. Enter a registered email address
3. Click "Send Reset Link"
4. Check the email inbox (and spam folder)
5. Click the reset link in the email
6. Set a new password on Firebase's password reset page
7. Return to app and login with new password

## Firebase Setup Requirements

Ensure Firebase Authentication is properly configured:
1. Firebase Auth is enabled in Firebase Console
2. Email/Password authentication provider is enabled
3. Email template is configured (optional, Firebase provides default)
4. `google-services.json` is properly placed in the app directory

## Security Notes

- Password reset links expire after a certain time (default 1 hour)
- Links can only be used once
- Firebase handles all security aspects of password reset
- The app never handles actual passwords, only triggers the reset email
