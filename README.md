# Schedule Tracker App & APK Project

A modern schedule tracking and routine management application built for Android.

## 🚀 1. Run the App Right Now (Instant Interactive Version)

You can launch and test the Schedule Tracker immediately without needing any extra disk space or waiting for compilers:

- **Double-click** `run_app.bat` inside this folder, or
- Open `web_app/index.html` directly in Chrome, Edge, or Firefox.

### Mobile / Phone Usage:
- Open `web_app/index.html` on your mobile browser (or host it for free on GitHub Pages / Vercel).
- Tap the browser menu (⋮ or Share) and select **"Add to Home Screen"** or **"Install App"**.
- It installs directly as a standalone mobile app on your phone's home screen with complete offline data persistence!

---

## 📱 2. How to Generate the Native Android `.apk`

Because this laptop has a **16 GB storage drive with ~100 MB free**, running a local Android SDK + Gradle compilation (which requires ~2 GB of temp build cache) would run out of disk space.

We have included an automated **GitHub Actions Workflow** (`.github/workflows/build-apk.yml`) to compile the `.apk` in the cloud for free:

### Quick Cloud Build Steps:
1. Create a free repository on [GitHub](https://github.com).
2. Upload or push the files from this `ScheduleTracker` folder to your repository.
3. Click the **Actions** tab on your GitHub repository.
4. The workflow **"Build Android APK"** will run automatically in 60 seconds!
5. When it finishes, click on the completed run and download **`ScheduleTracker-APK`** (containing `app-debug.apk`).
6. Send `app-debug.apk` to your Android phone via WhatsApp, Telegram, Google Drive, or USB cable, tap it, and select **Install**!

---

## 📂 Project Architecture

- **`app/src/main/java/com/scheduletracker/app/`**:
  - `MainActivity.kt`: Main UI coordinator with dynamic "Happening Now" status, day tabs, completion progress bar, and add/edit dialog.
  - `model/ScheduleItem.kt`: Data class for schedule routines, priorities, and timestamps.
  - `model/Category.kt`: Category definitions (Work, Study, Fitness, Personal, Meeting) with custom color themes.
  - `data/ScheduleDbHelper.kt`: Complete SQLite local database with offline persistence and preloaded starter schedule data.
  - `adapter/ScheduleAdapter.kt`: RecyclerView adapter with custom cards, strikethrough completed animations, and category color tags.
- **`app/src/main/res/`**:
  - `layout/activity_main.xml`: Modern Material 3 dashboard layout.
  - `layout/item_schedule.xml`: Schedule card layout.
  - `layout/dialog_add_schedule.xml`: Form dialog for creating/editing routines.
- **`web_app/index.html`**: Standalone, interactive HTML5 mobile-ready implementation with localStorage persistence.
