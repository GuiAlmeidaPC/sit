# Product Specification: "SIT" (Android Native Edition)

## 1. Product Overview
**Description:** A native Android interval running app with two workout setup modes: **Basic** and **Advanced**. In Basic mode, users set total workout time, number of sprints, sprint length, and rest length. In Advanced mode, users still set total workout time and a shared rest length, but can build a list of sprints with different durations for each sprint. In both cases, the app dynamically calculates the steady-pace "Running" intervals to fit the total time constraint exactly. Motivation is driven by sprint-only audio tracks (dog, horror, electro, or beep) that play exclusively during sprints.
**Platform:** Android (Exclusive).

## 2. Technical Stack Architecture
Building natively ensures precise timer execution and seamless audio mixing, avoiding the background-execution pitfalls common in cross-platform frameworks.

* **Language:** **Kotlin**.
* **UI Framework:** **Jetpack Compose**. A declarative approach perfectly suited for the dynamic, state-driven UI of a fitness timer (swapping colors and text based on the active interval).
* **State Management & Async Logic:** **Kotlin Coroutines and StateFlow**. The timer engine will emit a tick every second via a Flow, which the UI observes to update the countdowns and progress bars.
* **Background Execution:** **Foreground Service**. Crucial for Android fitness apps. The timer engine will live inside a Foreground Service tied to an ongoing system notification, preventing Android's Doze mode from killing the timer when the screen is locked.
* **Audio Playback:** **MediaPlayer**. For looping the `.mp3` or `.wav` chase assets stored locally in the `res/raw` directory.
* **Audio Ducking:** **AudioManager API**. Using `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` to temporarily lower the volume of background apps (like Spotify) during the sprint, and abandoning focus to restore volume during runs and rests.
* **Local Storage:** **Preferences DataStore**. Persists the user's last-used workout setup, selected mode, advanced sprint list, audio choice, theme, and language between sessions asynchronously.

## 3. Core Functional Requirements

### 3.1 Workout Configuration (Top-Down Calculation)
The user inputs their fixed constraints, and the app handles the rest.
* **Workout Mode Selector:** A pinned tab selector on the main screen with:
    * **Basic** — classic fixed sprint-count setup.
    * **Advanced** — list-based sprint setup with per-sprint durations.
* **Total Training Period:** Input for the complete workout duration in **HH:MM**.
* **Basic Mode Inputs:**
    * **Number of Sprints (Sets):** A numeric counter for the total sprint cycles (e.g., 5).
    * **Sprint Duration:** Input for one shared maximum-effort duration used by every sprint (e.g., 0:30).
    * **Rest Duration:** Input for the shared recovery period after every sprint (e.g., 1:30).
* **Advanced Mode Inputs:**
    * **Sprint List:** The user adds sprint entries to a list and can set a different duration for each sprint.
    * **Shared Rest Duration:** One rest value is still shared by every sprint.
    * **Add / Remove Sprint Controls:** Users can append or remove sprint rows directly on the setup screen.
* **Segmented Time Inputs:** Time values are edited in fixed two-part fields:
    * **Total workout:** `HH:MM`
    * **Sprint / Rest:** `MM:SS`
    * Each segment has a subtle unit label (`hr`, `min`, `sec`) and is edited independently with the numeric keyboard.
* **Auto-Calculated "Running" Duration (Read-Only):** The app automatically calculates and displays the steady-pace running time required between sprint cycles.
* **Audio Selector:** Choose the sprint motivation sound (**Dog Barking, Horror Chase, Electro Rush, Standard Beep**).
* **Theme Selector:** Choose a pre-defined color scheme that styles the entire app (Setup, Active Run, and Summary screens). Selection persists across sessions via DataStore. Pre-defined themes:
    * **Classic** — default traffic-light palette (Blue rest / Orange run / Red sprint).
    * **Classic Dark** — dark variant of the Classic palette.
    * **Neon** — high-contrast dark background with neon cyan / yellow / magenta state colors.
    * **Forest** — earthy palette with teal rest / olive run / crimson sprint.
    * **Forest Dark** — dark variant of the Forest palette.
    * **Mono** — grayscale-only palette (state changes communicated via brightness levels rather than hue, for accessibility).
    * **Mono Dark** — dark variant of the Mono palette.
    * **Glitter Pop** — playful theme aimed at teen girls: pastel pink background with lavender rest / sky-blue run / hot-pink sprint, plus rounded typography and sparkle accent icons.
    * **Glitter Pop Dark** — dark variant of Glitter Pop.
    The selected theme drives the `MaterialTheme` color scheme as well as the per-state colors used by `animateColorAsState` on the Active Run screen.
* **Language Selector:** Choose between **English** and **Portuguese**. Visible UI text and notifications follow the selected language.

### 3.2 The Auto-Calculation Logic
The calculation occurs via a ViewModel observing the user inputs:
1.  **Sprint Count** = number of sprints in Basic mode, or number of rows in the Advanced sprint list.
2.  **Total Sprint Time**
    * **Basic:** `Number of Sprints` × `Sprint Duration`
    * **Advanced:** sum of all sprint durations in the sprint list
3.  **Total Rest Time** = `Sprint Count` × `Rest Duration`
4.  **Total Running Time** = `Total Training Period` - (`Total Sprint Time` + `Total Rest Time`)
5.  **Individual Running Duration** = `Total Running Time` ÷ `Sprint Count`

### 3.3 State Machine & Sequence
* The workout builds an array of `N` cycles (where N is the sprint count in the selected mode).
* **Cycle Sequence:** `Running` (Steady) -> `Sprinting` (High Intensity) -> `Resting` (Recovery).
* **Sprint Duration Source:**
    * **Basic:** every `Sprinting` interval uses the shared sprint duration.
    * **Advanced:** each `Sprinting` interval uses its matching sprint-list entry.
* **Completion:** The workout concludes the moment the final `Resting` period of the final cycle hits 0:00.

## 4. User Interface (UI) & App Flow

### Screen 1: Setup / Home Screen
* **Header:** SIT Logo.
* **Pinned Mode Tabs:** A sticky `Basic` / `Advanced` tab row remains visible while the rest of the form scrolls.
* **Inputs:**
    * Total Workout Time (`HH:MM`)
    * **Basic:** Number of Sprints, Sprint Duration (`MM:SS`), Rest Duration (`MM:SS`)
    * **Advanced:** Sprint List (`MM:SS` per row), Add Sprint button, Remove Sprint button per row, shared Rest Duration (`MM:SS`)
* **Time Editing UX:** Segmented time editors where each part can be tapped independently and edited with the numeric keyboard. The unit labels are shown discreetly below the segment.
* **Dynamic Info Card:** A Compose `Card` that reacts to input changes: 
    * **Basic:** *"Workout: **[N]** cycles of **[Calc. Run Time]** Run ➔ **[Sprint Time]** Sprint ➔ **[Rest Time]** Rest."*
    * **Advanced:** summary includes the sprint list rather than one shared sprint duration.
* **Sound Selection:** Row of selectable icons for the audio track.
* **Theme Selection:** Theme selection lives inside the settings drawer and live-previews by re-skinning the UI the moment a theme is tapped.
* **Language Selection:** Language selection lives inside the settings drawer.
* **Action:** "START WORKOUT" `Button`.

### Screen 2: Active Run Screen
* **Visual Dominance:** A massive Text composable for the *current interval state* countdown.
* **Workout Progress:** A `LinearProgressIndicator` tracking the Total Training Period.
* **Current State Indicator:** The background color of the screen animates using `animateColorAsState`:
    * *Blue/Green:* "REST / RECOVER"
    * *Yellow/Orange:* "RUN / STEADY PACE"
    * *Red (Flashing effect):* "SPRINT! GO GO GO!"
* **Controls:** Pause, and a long-press Stop button.

### Component: Ongoing System Notification
* Required by the Foreground Service.
* Displays the app icon, the current state (e.g., "Sprinting!"), and the current interval's countdown timer directly on the lock screen.
* Includes a "Stop" action button within the notification itself.

### Screen 3: Summary Screen
* **Stats:** Total time run, cycles completed.
* **Action:** "Done" (Returns to Home Screen and tears down the Foreground Service).

## 5. Logic & Edge Cases to Handle

* **Invalid Time Constraints:** If the `Total Sprint Time` + `Total Rest Time` exceeds the `Total Training Period`, the `Calculated Running Duration` becomes negative. The UI must instantly display an error state and disable the Start button.
* **Advanced Sprint List Must Not Be Empty:** Advanced mode requires at least one sprint row.
* **Sprint Durations Must Be Positive:** In both Basic and Advanced mode, every sprint duration must be positive.
* **Uneven Math Remainder:** If the remaining running time doesn't divide perfectly into whole seconds, round the `Individual Running Duration` to the nearest whole second, and silently add/subtract the remainder to the very first `Running` interval to ensure the Total Training Period is exact.
* **Mode Persistence:** Basic and Advanced share some persisted values (e.g. total workout time, rest, audio), but Basic sprint settings and the Advanced sprint list must not overwrite each other.
* **Service Lifecycle:** Ensure the Foreground Service is properly stopped and destroyed via `stopForeground(STOP_FOREGROUND_REMOVE)` and `stopSelf()` when the user finishes the workout or manually cancels it, to prevent memory leaks and ghost notifications.

## 6. Build, Install, and Emulator Run Instructions

### 6.1 Java Runtime
This project should be built with Java 21 in this environment.

```bash
export JAVA_HOME=/home/gui/.antigravity/extensions/redhat.java-1.49.0-linux-x64/jre/21.0.9-linux-x86_64
export PATH="$JAVA_HOME/bin:$PATH"
```

### 6.2 Run Tests
```bash
./gradlew test --quiet
```

### 6.3 Build the Debug APK
```bash
./gradlew :app:assembleDebug --quiet
```

Generated APK:

```bash
app/build/outputs/apk/debug/app-debug.apk
```

Optional renamed copy:

```bash
cp app/build/outputs/apk/debug/app-debug.apk app/build/outputs/apk/debug/sit-app.apk
```

### 6.4 Install on the Android Emulator
ADB is available at:

```bash
~/Android/Sdk/platform-tools/adb
```

Install the debug build:

```bash
~/Android/Sdk/platform-tools/adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
```

### 6.5 Launch / Relaunch the App in the Emulator
Force-stop the app and relaunch it:

```bash
~/Android/Sdk/platform-tools/adb -s emulator-5554 shell am force-stop com.sit
~/Android/Sdk/platform-tools/adb -s emulator-5554 shell monkey -p com.sit -c android.intent.category.LAUNCHER 1 >/dev/null
```

Or start the main activity directly:

```bash
~/Android/Sdk/platform-tools/adb -s emulator-5554 shell am start -n com.sit/.MainActivity
```

### 6.6 Build, Install, and Launch in One Sequence
```bash
export JAVA_HOME=/home/gui/.antigravity/extensions/redhat.java-1.49.0-linux-x64/jre/21.0.9-linux-x86_64
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew test :app:installDebug --quiet && \
~/Android/Sdk/platform-tools/adb -s emulator-5554 shell am force-stop com.sit && \
~/Android/Sdk/platform-tools/adb -s emulator-5554 shell monkey -p com.sit -c android.intent.category.LAUNCHER 1 >/dev/null
```
