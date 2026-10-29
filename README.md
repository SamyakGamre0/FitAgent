<<<<<<< HEAD
# FitAgent — Rule-Based Workout Planning & Performance Tracking Android App







**FitAgent** (`com.FitAgent`) is a 100% offline-first, native Android workout planning, custom routine building, and progressive overload tracking application built in **Kotlin** using **Material Design 3** and **AndroidX Room Persistence Library (SQLite Version 4)**.

Designed with a high-contrast **True-Black (`#000000`) & Aura Green (`#00FF94`)** OLED interface, FitAgent eliminates gym logging friction, subscription paywalls, and internet connectivity dependencies by executing all workout generation algorithms, unit conversions, and relational analytics locally on the device.

## Key Features

### 1. Biometric Onboarding & Live BMI Engine (`ProfileSetupActivity` & `UserProfile`)
- **Live BMI Calculation & WHO Classification**: Dynamically computes Body Mass Index ($\text{BMI} = \text{weight}_{\text{kg}} / \text{height}_{\text{m}}^2$) as the user types via `TextWatcher` and classifies into *Underweight* ($<18.5$), *Normal* ($18.5\text{--}24.9$), *Overweight* ($25.0\text{--}29.9$), or *Obese* ($\ge 30.0$).
- **Bidirectional Metric / Imperial Unit Converter (`UnitConverter.kt`)**: Supports instant one-tap toggling between `kg` $\leftrightarrow$ `lbs` ($1\text{ kg} = 2.20462\text{ lbs}$) and `cm` $\leftrightarrow$ `ft/in` while storing all biometric values canonically in metric (`cm` and `kg`) inside `SharedPreferences` to prevent floating-point rounding drift.
- **Locale-Aware Defaults**: Automatically detects the device's country code (`Locale.getDefault().country`) on first launch to set appropriate default measurement units.

### 2. Deterministic 4×4 Matrix Workout Generator (`WorkoutGenerator.kt`)
- Synthesizes **16 structured workout plans** instantaneously by mapping **4 primary fitness goals** against **4 equipment availability tiers**:
  - **Goals**: `Muscle Gain`, `Weight Loss`, `Build Endurance`, `General Fitness`
  - **Equipment Tiers**: `No Equipment`, `Dumbbells Only`, `Full Gym`, `Resistance Bands`
- Each generated plan delivers 5 targeted exercises complete with recommended sets, repetition/duration targets, target muscle group tags, and coaching form tips.

### 3. Custom Routine Builder & 100-Exercise Master Catalog (`ExerciseLibrary.kt`)
- **100 Pre-Indexed Exercises**: Parcelable in-memory catalog (`MASTER_LIST`) spanning **7 anatomical muscle groups**: *Chest (15)*, *Back (15)*, *Legs (20)*, *Shoulders (12)*, *Arms (18)*, *Core (12)*, and *Cardio (8)*.
- **Real-Time Filtering (`ExercisePickerActivity`)**: Combine instant keyword search with 8 horizontal category filter chips (`All`, `Chest`, `Back`, `Legs`, `Shoulders`, `Arms`, `Core`, `Cardio`).
- **Custom Routine Templates (`CreateRoutineActivity`)**: Configure custom routine titles, exercise order, default sets, target weights, target reps, and coaching notes backed by foreign-key `CASCADE` persistence.

### 4. Multi-Modal Active Workout Logger & Haptic Rest Timer (`LoggerActivity` & `LoggerAdapter`)
- **3 Exercise Logging Modalities**: Dynamically renders specialized input columns based on `ExerciseType`:
  - `WEIGHTED`: Set | Previous | Weight (`kg`/`lbs`) | Reps
  - `BODYWEIGHT`: Set | Previous | Reps
  - `TIMED`: Set | Previous | Seconds
- **Inline Progressive Overload (`PREVIOUS` Column)**: Queries the last 20 historical logs per exercise (`getRecentLogsForExercise()`) and displays the previous session's weight and reps directly beside each active set.
- **60-Second Haptic Countdown Rest Timer**: Modal rest timer dialog (`dialog_rest_timer.xml`) with `-15s` / `+15s` quick adjustments, skip option, and a `500ms` hardware vibration alert (`VibratorManager` / `VibrationEffect`) upon completion.
- **Automatic Routine Template Sync**: Completing a workout launched from a custom routine automatically updates the routine's default set counts, weights, and `lastUsedAt` timestamp while preserving any unperformed exercises.

### 5. Progress Analytics, Streak Engine & Undoable History (`ProgressFragment` & `HistoryActivity`)
- **Top 3 Personal Record (PR) Lifts (`HomeFragment`)**: Uses nested SQLite `TRIM(REPLACE(...))` string cleaning to strip `(Set 1)`–`(Set 10)` suffixes and rank the user's Top 3 most frequent exercises with their all-time best weight/reps or hold duration.
- **Consecutive Day Streak Engine**: Parses session dates into normalized midnight epoch timestamps (`86,400,000 ms` intervals) sorted descending to accurately track consecutive daily training streaks.
- **Swipe-to-Delete with 5-Second Undo**: `ItemTouchHelper` left/right swipe gesture in `HistoryActivity` with confirmation dialog and a 5-second `Snackbar` undo window before committing database deletion.

## System Architecture & Database Schema

FitAgent follows a clean **Three-Tier Native Android Architecture**:
1. **Presentation Layer**: 10 `AppCompatActivity` controllers, 4 `Fragment` tabs hosted inside `MainActivity` via `BottomNavigationView`, and 8 `RecyclerView.Adapter` classes styled with Material Design 3 Dark tokens.
2. **Domain & Utility Layer**: Stateless Kotlin singleton services (`WorkoutGenerator`, `UnitConverter`, `ProfileManager`, and `ExerciseLibrary`).
3. **Persistence Layer (`AppDatabase.kt` — Room SQLite Version 4)**:
   - `routines`: Stores custom routine headers (`id`, `name`, `createdAt`, `lastUsedAt`, `isPrebuilt`).
   - `routine_exercises`: Stores ordered exercises per routine (`id`, `routineId [FK CASCADE]`, `exerciseName`, `muscleGroup`, `orderIndex`, `defaultSets`, `defaultReps`, `defaultWeight`, `unit`, `notes`).
   - `workout_sessions`: Stores completed workout summaries (`id`, `date`, `goal`, `equipment`, `duration`, `totalVolume`, `exerciseCount`, `routineId [FK Nullable]`).
   - `workout_logs`: Stores individual set records (`id`, `sessionId`, `exerciseName`, `muscleGroup`, `sets`, `weight`, `reps`, `unit`, `volume`).

## Project Package Structure

```text
FitAgent-main/
=======
FitAgent — Rule-Based Workout Planning & Performance Tracking Android AppFitAgent (com.FitAgent) is a 100% offline-first, native Android workout planning, custom routine building, and progressive overload tracking application built in Kotlin using Material Design 3 and AndroidX Room Persistence Library (SQLite Version 4).Designed with a high-contrast True-Black (#000000) & Aura Green (#00FF94) OLED interface, FitAgent eliminates gym logging friction, subscription paywalls, and internet connectivity dependencies by executing all workout generation algorithms, unit conversions, and relational analytics locally on the device.Key Features1. Biometric Onboarding & Live BMI Engine (ProfileSetupActivity & UserProfile)Live BMI Calculation & WHO Classification: Dynamically computes Body Mass Index ($\text{BMI} = \text{weight}_{\text{kg}} / \text{height}_{\text{m}}^2$) as the user types via TextWatcher and classifies into Underweight ($<18.5$), Normal ($18.5\text{--}24.9$), Overweight ($25.0\text{--}29.9$), or Obese ($\ge 30.0$).Bidirectional Metric / Imperial Unit Converter (UnitConverter.kt): Supports instant one-tap toggling between kg $\leftrightarrow$ lbs ($1\text{ kg} = 2.20462\text{ lbs}$) and cm $\leftrightarrow$ ft/in while storing all biometric values canonically in metric (cm and kg) inside SharedPreferences to prevent floating-point rounding drift.Locale-Aware Defaults: Automatically detects the device's country code (Locale.getDefault().country) on first launch to set appropriate default measurement units.2. Deterministic 4×4 Matrix Workout Generator (WorkoutGenerator.kt)Synthesizes 16 structured workout plans instantaneously by mapping 4 primary fitness goals against 4 equipment availability tiers:Goals: Muscle Gain, Weight Loss, Build Endurance, General FitnessEquipment Tiers: No Equipment, Dumbbells Only, Full Gym, Resistance BandsEach generated plan delivers 5 targeted exercises complete with recommended sets, repetition/duration targets, target muscle group tags, and coaching form tips.3. Custom Routine Builder & 100-Exercise Master Catalog (ExerciseLibrary.kt)100 Pre-Indexed Exercises: Parcelable in-memory catalog (MASTER_LIST) spanning 7 anatomical muscle groups: Chest (15), Back (15), Legs (20), Shoulders (12), Arms (18), Core (12), and Cardio (8).Real-Time Filtering (ExercisePickerActivity): Combine instant keyword search with 8 horizontal category filter chips (All, Chest, Back, Legs, Shoulders, Arms, Core, Cardio).Custom Routine Templates (CreateRoutineActivity): Configure custom routine titles, exercise order, default sets, target weights, target reps, and coaching notes backed by foreign-key CASCADE persistence.4. Multi-Modal Active Workout Logger & Haptic Rest Timer (LoggerActivity & LoggerAdapter)3 Exercise Logging Modalities: Dynamically renders specialized input columns based on ExerciseType:WEIGHTED: Set | Previous | Weight (kg/lbs) | RepsBODYWEIGHT: Set | Previous | RepsTIMED: Set | Previous | SecondsInline Progressive Overload (PREVIOUS Column): Queries the last 20 historical logs per exercise (getRecentLogsForExercise()) and displays the previous session's weight and reps directly beside each active set.60-Second Haptic Countdown Rest Timer: Modal rest timer dialog (dialog_rest_timer.xml) with -15s / +15s quick adjustments, skip option, and a 500ms hardware vibration alert (VibratorManager / VibrationEffect) upon completion.Automatic Routine Template Sync: Completing a workout launched from a custom routine automatically updates the routine's default set counts, weights, and lastUsedAt timestamp while preserving any unperformed exercises.5. Progress Analytics, Streak Engine & Undoable History (ProgressFragment & HistoryActivity)Top 3 Personal Record (PR) Lifts (HomeFragment): Uses nested SQLite TRIM(REPLACE(...)) string cleaning to strip (Set 1)–(Set 10) suffixes and rank the user's Top 3 most frequent exercises with their all-time best weight/reps or hold duration.Consecutive Day Streak Engine: Parses session dates into normalized midnight epoch timestamps (86,400,000 ms intervals) sorted descending to accurately track consecutive daily training streaks.Swipe-to-Delete with 5-Second Undo: ItemTouchHelper left/right swipe gesture in HistoryActivity with confirmation dialog and a 5-second Snackbar undo window before committing database deletion.System Architecture & Database SchemaFitAgent follows a clean Three-Tier Native Android Architecture:Presentation Layer: 10 AppCompatActivity controllers, 4 Fragment tabs hosted inside MainActivity via BottomNavigationView, and 8 RecyclerView.Adapter classes styled with Material Design 3 Dark tokens.Domain & Utility Layer: Stateless Kotlin singleton services (WorkoutGenerator, UnitConverter, ProfileManager, and ExerciseLibrary).Persistence Layer (AppDatabase.kt — Room SQLite Version 4):routines: Stores custom routine headers (id, name, createdAt, lastUsedAt, isPrebuilt).routine_exercises: Stores ordered exercises per routine (id, routineId [FK CASCADE], exerciseName, muscleGroup, orderIndex, defaultSets, defaultReps, defaultWeight, unit, notes).workout_sessions: Stores completed workout summaries (id, date, goal, equipment, duration, totalVolume, exerciseCount, routineId [FK Nullable]).workout_logs: Stores individual set records (id, sessionId, exerciseName, muscleGroup, sets, weight, reps, unit, volume).Project Package StructureFitAgent-main/
>>>>>>> 7336085526d5f19110126e33df62a3c6fb23fc7e
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/FitAgent/
│       │   │   ├── adapters/          # 8 RecyclerView Adapters (Logger, Routine, History, Picker, TopLift...)
│       │   │   ├── data/              # ExerciseLibrary.kt (100 Master Exercises across 7 muscle groups)
│       │   │   ├── database/          # AppDatabase.kt (Room v4, Migrations 2->3 & 3->4), WorkoutDao, RoutineDao
│       │   │   ├── fragments/         # HomeFragment, WorkoutFragment, ProgressFragment, ProfileFragment
│       │   │   ├── models/            # Room Entities & Data Models (UserProfile, Exercise, Routine, WorkoutLog...)
│       │   │   ├── utils/             # WorkoutGenerator.kt, UnitConverter.kt, ProfileManager.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── ProfileSetupActivity.kt
│       │   │   ├── EditProfileActivity.kt
│       │   │   ├── WorkoutPlanActivity.kt
│       │   │   ├── CreateRoutineActivity.kt
│       │   │   ├── ExercisePickerActivity.kt
│       │   │   ├── LoggerActivity.kt
│       │   │   ├── HistoryActivity.kt
│       │   │   ├── HistoryDetailActivity.kt
│       │   │   ├── SessionDetailAvtivity.kt
│       │   │   └── ProgressActivity.kt
│       │   └── res/
│       │       ├── drawable/          # Muscle group icons & custom UI backgrounds
│       │       ├── layout/            # 22 XML Material 3 Dark layouts & dialogs
│       │       ├── menu/              # bottom_nav_menu.xml
│       │       ├── values/            # colors.xml (#000000 & #00FF94), strings.xml, themes.xml
│       │       └── xml/               # backup_rules.xml & data_extraction_rules.xml
│       ├── test/                      # Local JVM Unit Tests (JUnit 4)
│       └── androidTest/               # Instrumented Android Tests (Espresso)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
<<<<<<< HEAD
```

## Tech Stack & Dependencies

| Category | Technology / Library | Version |
| :--- | :--- | :--- |
| **Language & Runtime** | Kotlin (JVM Target 11) | `1.9.24` |
| **Build System** | Android Gradle Plugin (AGP) & KAPT | `8.5.2` |
| **Android SDK** | Min SDK `24` (Android 7.0) / Target & Compile SDK `36` | API `24–36` |
| **UI Framework** | Material Components for Android, ConstraintLayout, RecyclerView, CardView | `1.11.0` / `2.1.4` |
| **Local Database ORM** | Jetpack Room (`room-runtime`, `room-ktx`, `room-compiler`) | `2.6.1` |
| **Concurrency & Streams** | Kotlin Coroutines (`kotlinx-coroutines-android`) & Kotlin `Flow` | `1.7.3` |
| **Lifecycle Management** | AndroidX Lifecycle (`lifecycle-viewmodel-ktx`, `lifecycle-livedata-ktx`) | `2.7.0` |
| **Testing** | JUnit 4, AndroidX Test Ext JUnit, Espresso Core | `4.13.2` / `3.5.1` |

## Getting Started & Build Instructions

### Prerequisites
- **Android Studio** (Koala / Ladybug or newer)
- **JDK 11** or higher
- Android Emulator or physical Android device (API 24+) with USB debugging enabled

### Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/<your-username>/FitAgent.git
   cd FitAgent
   ```
2. **Open in Android Studio**:
   - Launch Android Studio $\rightarrow$ **Open** $\rightarrow$ select the `FitAgent` root folder.
   - Allow Gradle to sync and KAPT to generate the Room DAO implementations (`AppDatabase_Impl`).
3. **Build and Run via CLI or IDE**:
   ```bash
   # Compile Debug APK
   ./gradlew assembleDebug

   # Install directly onto a connected Android device/emulator
   ./gradlew installDebug
   ```
   The compiled APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Author & Academic Credits

- **Developer**: Samyak Pradeep Gamre (Roll No. 19 | Seat No. 510512906)
- **Degree Program**: Third Year B.Sc. Computer Science (T.Y.B.Sc. CS — Semester V)
- **Project Guide**: Prof. Jagruti Raut, Department of Computer Science
- **Institution**: Vishnu Waman Thakur Charitable Trust's Bhaskar Waman Thakur College of Science, Yashvant Keshav Patil College of Commerce, Vidhya Dayanand Patil College of Arts (**VIVA College**), Virar (West), Affiliated to the **University of Mumbai**
- **Academic Year**: 2026–2027
=======
Tech Stack & DependenciesCategoryTechnology / LibraryVersionLanguage & RuntimeKotlin (JVM Target 11)1.9.24Build SystemAndroid Gradle Plugin (AGP) & KAPT8.5.2Android SDKMin SDK 24 (Android 7.0) / Target & Compile SDK 36API 24–36UI FrameworkMaterial Components for Android, ConstraintLayout, RecyclerView, CardView1.11.0 / 2.1.4Local Database ORMJetpack Room (room-runtime, room-ktx, room-compiler)2.6.1Concurrency & StreamsKotlin Coroutines (kotlinx-coroutines-android) & Kotlin Flow1.7.3Lifecycle ManagementAndroidX Lifecycle (lifecycle-viewmodel-ktx, lifecycle-livedata-ktx)2.7.0TestingJUnit 4, AndroidX Test Ext JUnit, Espresso Core4.13.2 / 3.5.1Getting Started & Build InstructionsPrerequisitesAndroid Studio (Koala / Ladybug or newer)JDK 11 or higherAndroid Emulator or physical Android device (API 24+) with USB debugging enabledInstallationClone the repository:git clone https://github.com/<your-username>/FitAgent.git
cd FitAgent
Open in Android Studio:Launch Android Studio $\rightarrow$ Open $\rightarrow$ select the FitAgent root folder.Allow Gradle to sync and KAPT to generate the Room DAO implementations (AppDatabase_Impl).Build and Run via CLI or IDE:# Compile Debug APK
./gradlew assembleDebug

# Install directly onto a connected Android device/emulator
./gradlew installDebug
The compiled APK will be generated at app/build/outputs/apk/debug/app-debug.apk.Author & Academic CreditsDeveloper: Samyak Pradeep Gamre (Roll No. 19 | Seat No. 510512906)Degree Program: Third Year B.Sc. Computer Science (T.Y.B.Sc. CS — Semester V)Project Guide: Prof. Jagruti Raut, Department of Computer ScienceInstitution: Vishnu Waman Thakur Charitable Trust's Bhaskar Waman Thakur College of Science, Yashvant Keshav Patil College of Commerce, Vidhya Dayanand Patil College of Arts (VIVA College), Virar (West), Affiliated to the University of MumbaiAcademic Year: 2026–2027
>>>>>>> 7336085526d5f19110126e33df62a3c6fb23fc7e
