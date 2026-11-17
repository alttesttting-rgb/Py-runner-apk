# 💗 Py Runner

Pink-themed Android APK that runs Python (`.py`) scripts.  
Works on regular Android phones **and** Meta Quest (ARM64).

Python 3.12 is **bundled inside the APK** – no separate install needed.

## Features

- Pink-themed interface
- **SELECT A PY FILE** button (uses the system file picker)
- Run the selected script
- Live console output
- Error messages when a script fails
- Stop button
- ARM64 only (smaller APK, works on Quest)

---

## How to get the APK (no PC / no Android Studio needed)

### Option 1 – GitHub Actions (recommended)

1. Create a free account on [github.com](https://github.com) if you don’t have one.
2. Create a **new repository** (name it anything, e.g. `PyRunner`). Leave it public or private.
3. Upload **all the files** from this zip into the repository root  
   (drag-and-drop the contents of the unzipped folder).
4. Go to the **Actions** tab of your repository.
5. Select the workflow **“Build APK”** → click **Run workflow** → **Run workflow**.
6. Wait 4–8 minutes.
7. When it finishes (green checkmark), open that run and download the artifact called **PyRunner-debug**.
8. Unzip the downloaded file – you will get `app-debug.apk`.
9. Transfer the APK to your phone or Meta Quest and install it  
   (enable “Install from unknown sources” / “Unknown apps” if prompted).

That’s it – you now have the APK without ever installing Android Studio.

### Option 2 – Build on a computer later

If you ever get access to a PC or Mac:

1. Install [Android Studio](https://developer.android.com/studio).
2. Open this project folder in Android Studio.
3. Let it download the SDK and Gradle (first time takes a while).
4. Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. The APK will be in `app/build/outputs/apk/debug/`.

---

## Notes for Quest

- Install the APK via SideQuest, ADB, or the Quest browser + “unknown sources”.
- The app is a normal 2D Android app, so it appears as a floating window / panel in VR.
- Storage Access Framework is used, so you can pick `.py` files from the Quest’s file system or from a USB drive.

## Limitations

- “Stop” cancels the job that is waiting for the script; very long-running or blocked Python code may not stop instantly.
- Only pure-Python scripts + the standard library are supported out of the box. Extra packages can be added later in `app/build.gradle.kts`.

Enjoy! 💗
