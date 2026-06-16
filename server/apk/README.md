# APK Files

Place your built APK file here named `cinepass.apk`

## How to build the APK:

1. In Android Studio, go to: **Build > Build Bundle(s) / APK(s) > Build APK(s)**
2. Wait for the build to complete
3. Click "locate" in the notification or go to: `app/build/outputs/apk/debug/app-debug.apk`
4. Copy the APK file to this folder and rename it to `cinepass.apk`

## Or build from command line:

```bash
cd d:\cinepass
./gradlew assembleDebug
copy app\build\outputs\apk\debug\app-debug.apk server\apk\cinepass.apk
```

## File Structure:
```
server/
  apk/
    cinepass.apk  <- Place your APK file here
    README.md     <- This file
```

## Testing:

After placing the APK file, test the download:
```bash
# Start the server
cd server
npm start

# Open in browser:
http://localhost:4000/download?ref=RS3_ABC123
```

The download page will:
1. Display the referral code: RS3_ABC123
2. Auto-copy it to clipboard
3. Provide a download button for the APK
4. Show installation instructions
