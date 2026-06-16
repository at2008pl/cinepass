# Firebase Cloud Messaging (FCM) Setup Guide

This document explains how to set up Firebase Cloud Messaging for push notifications in the CinePass app.

## Prerequisites

- A Google Firebase account and project
- Android app registered in Firebase Console
- Firebase Admin SDK (for backend push notification sending)

## Setup Steps

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a new project"
3. Name it "cinepass-rs3" (or your preferred name)
4. Enable Google Analytics if desired
5. Click "Create project"

### 2. Register Android App in Firebase

1. In Firebase Console, click "Add app" → "Android"
2. Enter package name: `com.cinepass`
3. Enter app nickname: "CinePass" (optional)
4. Click "Register app"
5. Download the `google-services.json` file
6. Copy it to: `app/google-services.json`

### 3. Replace google-services.json

The file `app/google-services.json` currently contains a template. Replace it with the actual file downloaded from Firebase Console:

```bash
# On Windows
copy "path\to\your\downloads\google-services.json" "app\google-services.json"
```

### 4. Enable Firebase Messaging API

1. In Firebase Console, go to "Cloud Messaging" tab
2. Click the settings icon (⚙️) next to "Cloud Messaging API"
3. Enable "Cloud Messaging API"

### 5. Create Service Account (Backend)

To send push notifications from your backend:

1. Go to Firebase Project Settings (click ⚙️ icon in top-left)
2. Go to "Service accounts" tab
3. Click "Generate new private key"
4. Save the JSON file securely
5. Use this in your backend's `fanverse_web` project

## Code Implementation

### Android Setup (Already Integrated)

The following components are already implemented:

- **FirebaseMessagingService**: Handles incoming FCM messages and displays notifications
- **UserPrefs**: Stores FCM token for later API calls
- **MainActivity**: Requests notification permissions and initializes FCM
- **AndroidManifest.xml**: Service declaration and notification permission

### Message Format

Push notifications should follow this JSON format:

```json
{
  "notification": {
    "title": "Offer Claimed!",
    "body": "You've successfully claimed 'Movie Ticket Discount'"
  },
  "data": {
    "type": "offer_claimed",
    "target": "wallet",
    "offer_id": "123"
  },
  "android": {
    "priority": "high"
  }
}
```

### Supported Notification Types

- `offer_claimed` - When user claims an offer
- `referral_earned` - When someone uses referral code
- `wallet_updated` - Coin balance updates
- `reward_redeemed` - When user redeems coins
- `general` - Default notifications

### Target Routes (for Deep Linking)

- `wallet` - Opens Wallet screen
- `referral` - Opens Referral screen
- `offers` - Opens Offers section
- `home` - Opens Home screen

## Backend Integration (Node.js)

### Install Firebase Admin SDK

```bash
npm install firebase-admin
```

### Initialize Firebase Admin SDK

```javascript
import admin from 'firebase-admin';
import serviceAccount from './firebase-service-account.json' assert { type: 'json' };

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://cinepass-rs3.firebaseio.com'
});
```

### Send Push Notification

```javascript
async function sendNotification(fcmToken, title, body, data) {
  const message = {
    notification: {
      title: title,
      body: body,
    },
    data: data,
    token: fcmToken,
    android: {
      priority: 'high',
      notification: {
        clickAction: 'FLUTTER_NOTIFICATION_CLICK'
      }
    }
  };

  try {
    const response = await admin.messaging().send(message);
    console.log('Notification sent:', response);
    return response;
  } catch (error) {
    console.error('Error sending notification:', error);
    throw error;
  }
}
```

### Send to Multiple Users (Topic Subscription)

To send to users based on topics (e.g., "all_users", "premium_users"):

```javascript
async function subscribeToTopic(fcmTokens, topic) {
  try {
    const response = await admin.messaging().subscribeToTopic(fcmTokens, topic);
    console.log('Subscribed to topic:', response);
    return response;
  } catch (error) {
    console.error('Error subscribing to topic:', error);
    throw error;
  }
}

async function sendToTopic(topic, title, body, data) {
  const message = {
    notification: {
      title: title,
      body: body,
    },
    data: data,
    topic: topic,
    android: {
      priority: 'high'
    }
  };

  try {
    const response = await admin.messaging().send(message);
    console.log('Message sent to topic:', response);
    return response;
  } catch (error) {
    console.error('Error sending to topic:', error);
    throw error;
  }
}
```

## Testing Notifications

### Option 1: Firebase Console

1. Go to Engagement → Cloud Messaging in Firebase Console
2. Click "Send your first message"
3. Enter notification title and body
4. Click "Send test message"
5. Copy/paste the FCM token from Logcat
6. Click "Send"

### Option 2: Using FCM REST API

```bash
curl -X POST "https://fcm.googleapis.com/v1/projects/cinepass-rs3/messages:send" \
  -H "Authorization: Bearer $(gcloud auth application-default print-access-token)" \
  -H "Content-Type: application/json" \
  -d '{
    "message": {
      "notification": {
        "title": "Test Notification",
        "body": "This is a test notification"
      },
      "data": {
        "type": "general",
        "target": "home"
      },
      "token": "YOUR_FCM_TOKEN"
    }
  }'
```

## Troubleshooting

### Notifications Not Appearing

1. **Check Android 13+ Permission**: Verify that `POST_NOTIFICATIONS` permission is granted
   - Settings → Apps → CinePass → Permissions → Notifications

2. **Check Logcat**:
   ```bash
   adb logcat | grep FCM
   ```
   - Should show: "FCM Token obtained: ..."
   - Should show: "Notification displayed..."

3. **Verify google-services.json** is in `app/` directory (not root)

4. **Check Firebase Console** → Cloud Messaging tab:
   - Ensure API is enabled
   - Check "Logs" section for delivery status

### FCM Token Not Obtained

1. Ensure `google-services.json` is correctly placed
2. Verify your Google Play Services version
3. Check if app has internet permission

## API Integration Points

### When to Send Notifications

1. **Offer Claimed**: Send when user successfully claims an offer
   ```
   POST /app/offers/{id}/claim
   → Trigger notification on success
   ```

2. **Referral Earned**: Send when referral code is used during registration
   ```
   POST /app/auth/register (with referral_code)
   → Trigger notification for referrer
   ```

3. **Coins Updated**: Send when coin balance changes
   - Manual top-up
   - Offer redemption
   - Referral bonus

## Security Best Practices

1. **Store FCM Tokens Securely**
   - Keep tokens in local storage (handled by `UserPrefs`)
   - Update tokens in backend when refreshed

2. **Validate Messages**
   - Always verify message source in production
   - Use message signing/verification if needed

3. **Rate Limiting**
   - Implement cooldown periods for notifications
   - Avoid spamming users

4. **Analytics**
   - Track notification delivery and engagement
   - Monitor error rates in Cloud Messaging

## Next Steps

1. Download `google-services.json` from Firebase Console and place in `app/` directory
2. Sync Gradle files in Android Studio
3. Build and run the app
4. Check Logcat for FCM token
5. Test sending notifications from Firebase Console
6. Integrate with backend to send notifications on user actions

## Support

For more information:
- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Android Firebase Setup](https://firebase.google.com/docs/android/setup)
- [FCM API Reference](https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages)
