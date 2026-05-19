# Product Specification: "SIT" (Android Native Edition)

## 1. Product Overview
**Description:** A native Android interval running app where users set their total workout time, desired number of sprints, sprint length, and rest length. The app dynamically calculates the steady-pace "Running" intervals to fit the total time constraint. Motivation is driven by "chase" sounds (e.g., barking dog, horror effects) that play exclusively during sprints.
**Platform:** Android (Exclusive).

## 2. Technical Stack Architecture
Building natively ensures precise timer execution and seamless audio mixing, avoiding the background-execution pitfalls common in cross-platform frameworks.

* **Language:** **Kotlin**.
* **UI Framework:** **Jetpack Compose**. A declarative approach perfectly suited for the dynamic, state-driven UI of a fitness timer (swapping colors and text based on the active interval).
* **State Management & Async Logic:** **Kotlin Coroutines and StateFlow**. The timer engine will emit a tick every second via a Flow, which the UI observes to update the countdowns and progress bars.
* **Background Execution:** **Foreground Service**. Crucial for Android fitness apps. The timer engine will live inside a Foreground Service tied to an ongoing system notification, preventing Android's Doze mode from killing the timer when the screen is locked.
* **Audio Playback:** **MediaPlayer**. For looping the `.mp3` or `.wav` chase assets stored locally in the `res/raw` directory.
* **Audio Ducking:** **AudioManager API**. Using `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` to temporarily lower the volume of background apps (like Spotify) during the sprint, and abandoning focus to restore volume during runs and rests.
* **Local Storage:** **Preferences DataStore**. To persist the user's last-used workout configuration and audio choice between sessions asynchronously.

## 3. Core Functional Requirements

### 3.1 Workout Configuration (Top-Down Calculation)
The user inputs their fixed constraints, and the app handles the rest.
* **Total Training Period:** Input for the complete workout duration (e.g., 30:00).
* **Number of Sprints (Sets):** A numeric counter for the total sprint cycles (e.g., 5).
* **Sprint Duration:** Input for the maximum-effort period (e.g., 0:30).
* **Rest Duration:** Input for the recovery period after a sprint (e.g., 1:30).
* **Auto-Calculated "Running" Duration (Read-Only):** The app automatically calculates and displays the steady-pace running time required between sets.
* **Audio Selector:** Choose the sprint motivation sound (Dog Barking, Horror Chase, Standard Beep).
* **Theme Selector:** Choose a pre-defined color scheme that styles the entire app (Setup, Active Run, and Summary screens). Selection persists across sessions via DataStore. Pre-defined themes:
    * **Classic** — default traffic-light palette (Blue rest / Orange run / Red sprint).
    * **Neon** — high-contrast dark background with neon cyan / yellow / magenta state colors.
    * **Forest** — earthy palette with teal rest / olive run / crimson sprint.
    * **Mono** — grayscale-only palette (state changes communicated via brightness levels rather than hue, for accessibility).
    * **Glitter Pop** — playful theme aimed at teen girls: pastel pink background with lavender rest / sky-blue run / hot-pink sprint, plus rounded typography and sparkle accent icons.
    The selected theme drives the `MaterialTheme` color scheme as well as the per-state colors used by `animateColorAsState` on the Active Run screen.

### 3.2 The Auto-Calculation Logic
The calculation occurs via a ViewModel observing the user inputs:
1.  **Total Sprint Time** = `Number of Sprints` × `Sprint Duration`
2.  **Total Rest Time** = `Number of Sprints` × `Rest Duration`
3.  **Total Running Time** = `Total Training Period` - (`Total Sprint Time` + `Total Rest Time`)
4.  **Individual Running Duration** = `Total Running Time` ÷ `Number of Sprints`

### 3.3 State Machine & Sequence
* The workout builds an array of `N` cycles (where N is the Number of Sprints).
* **Cycle Sequence:** `Running` (Steady) -> `Sprinting` (High Intensity) -> `Resting` (Recovery).
* **Completion:** The workout concludes the moment the final `Resting` period of the final cycle hits 0:00.

## 4. User Interface (UI) & App Flow

### Screen 1: Setup / Home Screen
* **Header:** SIT Logo.
* **Inputs (Compose Number Pickers/Sliders):**
    * Total Workout Time (MM:SS)
    * Number of Sprints
    * Sprint Duration (MM:SS)
    * Rest Duration (MM:SS)
* **Dynamic Info Card:** A Compose `Card` that reacts to input changes: 
    * *"Workout: **[N]** cycles of **[Calc. Run Time]** Run ➔ **[Sprint Time]** Sprint ➔ **[Rest Time]** Rest."*
* **Sound Selection:** Row of selectable icons for the audio track.
* **Theme Selection:** Row of selectable color-swatch chips (one per pre-defined theme) that live-previews by re-skinning the Setup screen the moment a theme is tapped.
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
* **Uneven Math Remainder:** If the remaining running time doesn't divide perfectly into whole seconds, round the `Individual Running Duration` to the nearest whole second, and silently add/subtract the remainder to the very first `Running` interval to ensure the Total Training Period is exact.
* **Service Lifecycle:** Ensure the Foreground Service is properly stopped and destroyed via `stopForeground(STOP_FOREGROUND_REMOVE)` and `stopSelf()` when the user finishes the workout or manually cancels it, to prevent memory leaks and ghost notifications.