# Version Management Guide

## Problem Solved
Before this update, you and your friend could build APKs from the same code but get different version numbers. This happened because the version was manually set to `versionCode = 1` and `versionName = "1.0"`.

## New Automatic Versioning System

Now the app version is **automatically** determined from your Git repository:

- **versionCode**: Number of git commits (e.g., 129)
- **versionName**: Latest git tag (e.g., "v1.0.0") or commit hash (e.g., "1.0.0-dc9d63e")

## Benefits

✅ **Consistent Versions**: Both you and your friend will build the same version from the same commit
✅ **No Manual Updates**: Version increments automatically with each commit
✅ **Easy Tracking**: Know exactly which git commit a build came from
✅ **Professional**: Follows industry best practices

## How It Works

### Building from the Same Commit
1. Both you and your friend pull the latest code: `git pull origin main`
2. Build the APK: `./gradlew assembleDebug`
3. **Result**: Both APKs will have:
   - Same versionCode (current: 129)
   - Same versionName (current: "v1.0.0")

### Creating a New Release Version

When you want to release a new version (e.g., v1.1.0):

```bash
# Make your changes and commit them
git add .
git commit -m "Add new features"

# Create a new version tag
git tag v1.1.0

# Push everything
git push origin main
git push origin v1.1.0
```

Now all builds will show versionName as "v1.1.0"!

### Without Tags

If you don't create a tag, the version will use the commit hash:
- versionName: "1.0.0-f393c70" (where f393c70 is the current commit)

## Current Version

- **versionCode**: 129 (number of commits)
- **versionName**: v1.0.0 (from git tag)

## How to Check Version in Your APK

### Method 1: In Android Studio
1. Build > Generate Signed Bundle/APK
2. Check the version in the build output

### Method 2: Using APK Analyzer
1. Build > Analyze APK
2. View the versionCode and versionName in the manifest

### Method 3: On Device
```kotlin
val versionCode = BuildConfig.VERSION_CODE
val versionName = BuildConfig.VERSION_NAME
```

## Troubleshooting

### Different versions despite same code?

**Check:**
1. Are you both on the same commit?
   ```bash
   git log -1
   ```
2. Are you both using the same git tags?
   ```bash
   git tag
   ```
3. Pull the latest tags:
   ```bash
   git fetch --tags
   ```

### Build fails with git error?

If you don't have git installed or are building on a CI/CD server without git:
- The build will fallback to versionCode = 1 and versionName = "1.0.0"
- Warning message will be shown during build

## Version History

| Tag     | versionCode | Description                          |
|---------|-------------|--------------------------------------|
| v1.0.0  | 129         | Initial release with auto-versioning |

## Next Steps for Releases

When ready for production releases:

1. **v1.0.x**: Bug fixes only
   ```bash
   git tag v1.0.1
   git push origin v1.0.1
   ```

2. **v1.x.0**: New features
   ```bash
   git tag v1.1.0
   git push origin v1.1.0
   ```

3. **vX.0.0**: Major changes
   ```bash
   git tag v2.0.0
   git push origin v2.0.0
   ```

## For Your Friend

Tell your friend to:
1. Pull the latest code: `git pull origin main`
2. Fetch all tags: `git fetch --tags`
3. Build: `./gradlew assembleDebug`

Both of you will now have **identical** APK versions! 🎉
