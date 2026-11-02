# RING 2 — Android Habit & Task Management Ecosystem

**RING 2** is a futuristic, high-performance Android productivity application engineered with **Kotlin**, **Jetpack Compose**, **Room Database**, **Hilt Dependency Injection**, **DataStore**, and **WorkManager**. 

It provides an integrated ecosystem combining **habit consistency tracking**, **task management**, **gamified Elite Points economy**, **biological metric tracking (BMI)**, **step counting**, and **Neural Alerts**.

---

## 1. Executive Summary & App Versioning

- **Application Name**: RING
- **App Version Name**: `1.0.0.0`
- **Build Code**: `versionCode = 1`
- **Room Database Schema**: `Version 9` (Supports automated incremental migrations 1 → 9)
- **Target SDK**: `API 35` (Android 15)
- **Min SDK**: `API 24` (Android 7.0 Nougat+)
- **Kotlin Version**: `2.0.21`
- **Android Gradle Plugin**: `8.5.2`

---

## 2. Feature & Architecture Breakdown

### 2.1 Persistent Onboarding & First-Launch Experience
- **Animated Splash Screen**: Custom glowing logo rotation (`ic_launcher_foreground`) with smooth spring transition.
- **3-Screen Sequence**:
  1. **Screen 1 (Welcome)**: Introduction to the RING neural productivity paradigm.
  2. **Screen 2 (User Profile)**: Name entry, Gender selection, Date of Birth picker with real-time automated age calculation and confirmation step.
  3. **Screen 3 (Physical Metrics)**: Initial weight (kg) and height (cm) setup.
- **DataStore & Room Persistence**: Sets `onboardingComplete = true`. Subsequent app launches bypass onboarding and navigate directly to the main dashboard.

---

### 2.2 Elite Points Economy & Gamification Rules
Users start with a baseline balance of **500 Elite Points** upon first launch.

| Event / Action | Point Value / Adjustment | Rules & Idempotency Notes |
| :--- | :--- | :--- |
| **Habit Creation** | `-25 Pts` | Requires minimum 25 points balance before creation. |
| **Task Creation** | `-1 Pt` | Requires minimum 1 point balance before creation. |
| **Task Completion** | `+3 Pts` | Awarded upon marking task completed. |
| **Reverting Task Completion** | `-3 Pts` | Revokes points if task is unmarked incomplete. |
| **Task Deletion (Incomplete)** | `-5 Pts` | Charged when deleting an unfinished task. |
| **Task Deletion (Completed)** | `0 Pts` | Free deletion for completed tasks. |
| **Habit Deletion** | `-30 Pts` | Charged when terminating a habit. |
| **Lowering Habit Target** | `-10 Pts` | Penalizes reducing target requirements. |
| **Increasing Habit Target** | `0 Pts` | Free to increase target difficulty. |
| **Yes/No Habit Complete** | `+3 Pts` | Awarded on completion; `-3 Pts` if reverted. |
| **Measurable Partial Progress** | `+1 Pt` | Awarded when `0 < actual < target`. Reconciled cleanly upon state change. |
| **Measurable Full Completion** | `(1.5 × streak) + 3` | Awarded when `actual >= target` (or `3 Pts` if streak == 0). Reconciles previous partial rewards. |
| **Step Tracking Rewards** | `+1 Pt / 1,000 steps × streak` | Calculated dynamically per 1,000 step milestone multiplied by active streak (`maxOf(1, streak)`). |
| **Skipped-Day Step Penalty** | `-10 Pts` | Deducted for missed step goals on past days (`checkDate < today`) starting ON OR AFTER app installation date. Never penalizes days prior to launch or today. |
| **Overdue Task Penalty** | `-2 Pts` | Deducted once per overdue task occurrence (`task-overdue-penalty-$taskId`). Stored state prevents duplicate deductions. |

*Note: Elite Points balance is enforced with a zero floor (`balance >= 0`). Every point change is recorded as a `PointTransactionEntity` for full transparency in Transaction Logs.*

---

### 2.3 Habit System & Configuration Order

#### Field Ordering in Add Habit Screen:
1. **Habit Name** (e.g. "Morning Pushups")
2. **Description / Protocol Note**
3. **Category Selector**:
   - **Gym** (`0xFFFF1744` - Vibrant Electric Red)
   - **Health** (`0xFF00A84F` - Base Theme Green)
   - **Study** (`0xFF00B0FF` - Vibrant Electric Blue)
   - **Personal** (`0xFFFFEA00` - Vibrant Electric Yellow)
   - **Work** (`0xFFD500F9` - Vibrant Electric Magenta)
4. **Repeat Schedule**:
   - `Daily`, `Weekly`, `Even Days`, `Odd Days`, `Monthly`, `Yearly`, `Custom N Days`.
5. **Weekday Selector** (Weekly schedule only - Mon through Sun).
6. **Habit Type**:
   - **Symmetric Weekly**: Same daily target across all selected days.
   - **Asymmetric Weekly**: Independent daily target table per weekday.
7. **Unit & Target Configuration Table**:
   - `Active Days` | `Reps` | `Sets`
   - Habits configured with 1 rep & 1 set automatically assign `HabitType.YES_NO`.

#### Card Interaction & Direct Analytics Navigation:
- Clicking any habit card on the Home or Habit List screen navigates directly to `PersonalAnalyticsScreen`, bypassing intermediate detail views.
- Long habit titles truncate (`TextOverflow.Ellipsis`) without overlapping streak counters (`🔥 X`).

---

### 2.4 Task Creation, Repeat Schedules & Chronology

#### Add Task Screen Ordering:
1. **Task Name**
2. **Description / Note**
3. **Priority Selector**: `Low` (`0xFF00A84F`), `Medium` (`0xFFFFEA00`), `High` (`0xFFFF1744`)
4. **Reminder Settings**: Switch + Trigger Date & Time picker
5. **Repeat Settings**: `None`, `Daily`, `Weekly`, `Monthly`, `Yearly`, `Custom`
6. **Due Date & Due Time Controls**:
   - **Default Schedule Behavior**: If no due date/time is selected, automatically schedules due date to **7 days after task creation at 20:00**, communicated clearly to the user.
7. **Subtask & Checklist Section**: Interactive checklist input with completion checkboxes.

#### Repeatable Tasks & Historical Iteration Records:
- Repeatable tasks **do not** create duplicate task entities in Room DB.
- On completion, the existing `TaskEntity` record advances its due date to the next scheduled date (`ScheduleEngine.getNextActiveDueDate`).
- Historical completion records, subtask states, and penalties are archived into `repeatHistory` JSON strings and displayed in the **Historical Iterations Log** section on `TaskDetailScreen`.

---

### 2.5 Chronology Logs & 7 Date States

The `HabitCalendar` and `TaskCalendar` render monthly grid logs using strict date-state color rules:

1. **Pre-installation / Start Date (`date < startDate`)**: Gray border (`0x808080`), faded gray background.
2. **Inactive Dates**: White border (`0x80FFFFFF`), faded gray background.
3. **Future Inactive Dates**: White border (`0x80FFFFFF`), faded gray background.
4. **Future Active Scheduled Dates**: Blue border (`0xFF2196F3`), faded blue background.
5. **Active Zero Progress**: Red border (`0xFFFF3131`), faded red background.
6. **Active Partial Progress (`0 < actual < target`)**: Orange border (`0xFFFF9800`), faded orange background.
7. **Active Completed Progress (`actual >= target`)**: Green border (`0xFF00A84F`), faded green background.

*Editing Historical Progress*: Users can tap any past active date cell in the Chronology Log (`date >= habitStartDate` AND `date <= today` AND `isActive`) to open `ProgressDialog` and update or record historical progress. Updates recalculate streaks and reconcile Elite Points.

---

### 2.6 Dual Circular Progress Indicator
Custom-drawn concentric rings in `PersonalAnalyticsScreen`:
- **Outer Ring**: Completed sets relative to target sets (`completedSets / targetSets`).
- **Inner Ring**: Repetitions within current set relative to `repsPerSet`.
- **Center Element**: Opaque round button displaying current progress, total target, and set count (e.g. `Sets: 0 / 4`, `Sets: 1 / 4`, or `Sets: 5 / 4` for over-achievement).
- **Dynamic Color Intensity**: Arc opacity smoothly scales from `0.4f` at 0% to `1.0f` at 100% progress.
- **Viewport-Aware Bounce Animation**: Progress rings execute a slow, bouncy spring loading animation (`Spring.DampingRatioMediumBouncy`) upon entering view.

---

### 2.7 BMI Special Tracking Feature
- **Special Habit Entity**: `id = 2L`, Name: `BMI`, Category: `Health`, Dedicated Bright Orange Theme (`0xFFFF6D00`).
- **Gamification Exclusion**: Informational health feature (0 points, no streak penalties, no missed day penalties).
- **Initialization**: Automatically populated from onboarding weight & height.
- **Historical Persistence**: Appends measurements to `bodyMeasurements` JSON array without overwriting past entries.
- **30-Day Reminder Notifications**: Schedules 30-day recurring update reminder notifications via `BmiReminderScheduler` and `BmiReminderReceiver`, rescheduled on device reboot via `ReminderRescheduleWorker`.
- **BMI-Only Line Chart**: `BmiAnalyticsScreen` plots historical BMI over actual measurement dates with dynamic Y-axis scaling.

---

### 2.8 Neural Alerts Control Center (`NotificationsScreen.kt`)
Comprehensive notification management in Profile:
- **System Alerts Main Switch**: Global notification delivery pause/resume.
- **Audio & Signal Protocols**: Displays Custom Notification Tone status (`res/raw/ring_notification.mp3` or System Default fallback) and Haptic Vibration toggle (`vibrationEnabled`).
- **Feature Alert Toggles**:
  - Daily Motivation Quotes toggle (`MotivationWorker`)
  - BMI 30-Day Health Alert toggle (`BmiReminderReceiver`)
  - Habit Schedule Reminders toggle
  - Task Reminders list & per-task toggle controls

---

### 2.9 Custom Notification Tone & Fallback Architecture
- **Raw Sound Location**: `app/src/main/res/raw/ring_notification.mp3` (or `.ogg` / `.wav`).
- **Dynamic Lookup**: `context.resources.getIdentifier("ring_notification", "raw", context.packageName)`.
- **Fallback Mechanism**: If custom file is absent in `res/raw/`, `ReminderAlarmReceiver`, `MotivationWorker`, and `BmiReminderReceiver` automatically fall back to `RingtoneManager.TYPE_NOTIFICATION`.

---

### 2.10 Offline Local Storage & Backup/Restore Architecture
- **Room Database (`AppDatabase.kt`)**: Schema Version 9. Stores habits, progress, tasks, profile, point transactions, categories, and achievements.
- **JSON Backup & Restore (`BackupRestoreService.kt`)**:
  - Exports complete app snapshot (Habits, Tasks, Categories, Profile, Point History, Progress) into a single JSON file.
  - Validates and restores snapshot data with atomic database transactions.

---

## 3. Project Directory Structure

```text
ring_2/
├── app/
│   ├── src/main/java/com/example/ringapp/
│   │   ├── data/
│   │   │   ├── PreferencesDataStore.kt
│   │   │   ├── BackupRestoreService.kt
│   │   │   ├── FirstLaunchInitializer.kt
│   │   │   └── ResetUserDataService.kt
│   │   ├── navigation/
│   │   │   ├── AppNavGraph.kt
│   │   │   ├── Screen.kt
│   │   │   └── NavigationDestination.kt
│   │   ├── ui/
│   │   │   ├── analytics/ (AnalyticsScreens.kt, BmiTracking.kt, AnalyticsViewModel.kt)
│   │   │   ├── habits/ (AddHabitScreen.kt, HabitListScreen.kt, HabitDetailScreen.kt)
│   │   │   ├── home/ (HomeScreen.kt, HomeViewModel.kt)
│   │   │   ├── onboarding/ (OnboardingScreen.kt, RingSplashScreen.kt)
│   │   │   ├── profile/ (ProfileScreen.kt, NotificationsScreen.kt, AppearanceScreen.kt, BackupRestoreScreen.kt)
│   │   │   ├── tasks/ (AddTaskScreen.kt, TaskDetailScreen.kt, TasksScreen.kt)
│   │   │   └── theme/ (Color.kt, RingTheme.kt, AppBackground.kt)
│   │   ├── work/ (WorkScheduler.kt, DailyMaintenanceWorker.kt, MotivationWorker.kt, ReminderAlarmReceiver.kt, BmiReminderReceiver.kt)
│   │   └── MainActivity.kt
│   └── src/main/res/
│       ├── drawable/ (ic_launcher_foreground.xml, ic_profile_avatar.xml, ring_leaf_wallpaper.jpg)
│       └── raw/ (ring_notification_readme.txt)
├── data/
│   └── src/main/java/com/example/ringapp/data/
│       ├── db/ (AppDatabase.kt, Converters.kt, Migrations.kt)
│       ├── local/dao/ (HabitDao.kt, TaskDao.kt, PointDao.kt, ProfileDao.kt, etc.)
│       ├── local/entities/ (HabitEntity.kt, TaskEntity.kt, PointTransactionEntity.kt, ProfileEntity.kt, etc.)
│       └── repository/ (HabitRepository.kt, TaskRepository.kt, PointRepository.kt, ProfileRepository.kt, etc.)
├── domain/
│   └── src/main/java/com/example/ringapp/domain/
│       ├── engine/ (ScheduleEngine.kt, StreakEngine.kt, AnalyticsEngine.kt, GamificationEngine.kt, AchievementEngine.kt)
│       └── usecase/ (CreateHabitUseCase.kt, CompleteTaskUseCase.kt, RecordHabitProgressUseCase.kt, etc.)
├── core/
└── build.gradle.kts
```

---

## 4. Build & Testing Instructions

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17
- Android SDK Platform API 35

### Commands

```bash
# Clean and assemble debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew test

# Synchronize Gradle
./gradlew --refresh-dependencies
```
