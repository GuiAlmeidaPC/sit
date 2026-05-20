# SIT (Sprint Interval Timer)

A native Android interval running app built with Kotlin, Jetpack Compose, and Coroutines. SIT takes a unique "Top-Down Calculation" approach to interval running: you set your total workout time constraints and sprint preferences, and the app automatically calculates the required steady-state running time to fit your goals perfectly.

## Features
- **Auto-Calculated Intervals:** Set your total time constraint, add sprints and rests, and let the app dynamically calculate your steady-paced "Running" intervals.
- **Two Flexible Modes:**
  - **Basic Mode:** Simple fixed-count sprints with a shared duration.
  - **Advanced Mode:** Build a custom sequence of sprints with unique durations for each sprint in your list.
- **Dynamic UX:** Huge, highly visible countdowns with dynamically shifting background colors (Blue for Rest, Yellow for Run, Flashing Red for Sprint) powered by Jetpack Compose.
- **Audio Motivation:** Exclusive sprint-only motivational audio tracks (e.g., Dog Barking, Horror Chase) using the AudioManager API with audio ducking to smoothly fade background music like Spotify.
- **Background Execution:** Fully integrated Foreground Service so your timer keeps perfectly tracking your workout even when the screen is locked or the app is in the background.
- **Customization:** Choose between multiple stylish UI themes (Classic, Neon, Forest, Mono, etc., including dark modes) and toggle between English and Portuguese localization.

## Tech Stack
- **Language:** Kotlin 
- **UI Framework:** Jetpack Compose & Material 3
- **State Management & Async:** Kotlin Coroutines and StateFlow
- **Architecture:** Domain-driven logic purely decoupled from UI and heavily unit-tested with JUnit 5.
- **Background Processes:** Android Foreground Services
- **Storage:** Preferences DataStore for session persistence

## Building & Running

### Requirements
- Java 21 runtime
- Android SDK (`platform-tools`)

### Run Unit Tests
```bash
./gradlew test --quiet
```

### Build Debug APK
```bash
./gradlew :app:assembleDebug --quiet
```
The output APK will be available at `app/build/outputs/apk/debug/app-debug.apk`.

### Install on Emulator
Ensure your Android emulator (e.g., `emulator-5554`) is running, then execute:
```bash
~/Android/Sdk/platform-tools/adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
```
Then, you can launch the app directly via ADB:
```bash
~/Android/Sdk/platform-tools/adb -s emulator-5554 shell am start -n com.sit/.MainActivity
```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
MIT License
