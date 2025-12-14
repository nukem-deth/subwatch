# SubWatch (Android)

Track subscriptions and get notifications when they are about to expire.

## Features
- Beautiful list of subscriptions with: name, start/end dates, days left
- Red indicator when less than 5 days remain
- Add / edit / delete subscriptions
- Daily push notification when a subscription is expiring (≤5 days) or expires today
- Optional auto-icon by website favicon (enter a domain like `spotify.com`)

## Build APK online (phone-only) with GitHub Actions
See the section **"Build APK from a phone (no PC)"** below.

---

## Build APK from a phone (no PC)

### 1) Create a GitHub account (if you don't have one)
Use the GitHub mobile app or browser.

### 2) Create a new repository
Name it for example: `subwatch`

### 3) Upload this project ZIP contents
- Download the ZIP from ChatGPT
- In GitHub mobile: **Add file → Upload files** and upload the extracted folder contents
  - Make sure the repo root contains `gradlew`, `settings.gradle.kts`, and the `app/` folder.

### 4) Run the build
- Go to **Actions**
- Open workflow **Build Debug APK**
- Tap **Run workflow** (branch: main)

### 5) Download the APK
- Open the finished workflow run
- Scroll to **Artifacts**
- Download **SubWatch-debug-apk**
- Inside you'll find `app-debug.apk`

### 6) Install on Android
- Allow installs from your browser / file manager when prompted
- Install the APK

> Notes:
> - This builds a **debug** APK (easy to install). For Play Store you’d need a signed release build.
> - If GitHub Actions is disabled, enable it in repo settings.

---

## App permissions
- Android 13+ will ask for POST_NOTIFICATIONS permission.
