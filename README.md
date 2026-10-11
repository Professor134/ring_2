# Ring 2

Ring 2 is a productivity and habit-tracking Android application built with Kotlin, Jetpack Compose, Hilt, Room, and DataStore. The application focuses on habit consistency, task management, progress tracking, analytics, gamified engagement, and personalized user experience.

This repository was documented file-by-file with explanatory comments only; no application logic was changed during the documentation pass.

## 1. Project overview

Ring 2 is designed as a personal growth assistant that combines three major workflows:

- Habit tracking and streak progression
- Task management and reminders
- Profile, analytics, and gamification systems

The app encourages users to maintain routines, complete daily tasks, and accumulate points through productive behavior.

## 2. Product goals

The main goals of the application are:

- Help users build and maintain healthy routines
- Track daily progress with consistency and measurable goals
- Turn productivity into a motivating system using points, streaks, and levels
- Offer lightweight analytics for individual habits and broader activity trends
- Provide reminders and background maintenance so the app actively supports daily usage

## 3. Technology stack

- Language: Kotlin
- UI: Jetpack Compose
- Navigation: Navigation Compose
- Dependency injection: Hilt
- Local persistence: Room database
- Preferences: DataStore
- Background work: WorkManager
- Architecture: MVVM + repository + use case pattern
- Testing: JUnit and Compose UI test support
- Build system: Gradle (Kotlin DSL)

## 4. Project structure

```text
ring_2/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/ringapp/
│   │   │   │   ├── data/
│   │   │   │   ├── di/
│   │   │   │   ├── domain/
│   │   │   │   ├── navigation/
│   │   │   │   ├── ui/
│   │   │   │   ├── widget/
│   │   │   │   ├── work/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── Navigation.kt
│   │   │   │   └── RingApplication.kt
│   │   │   ├── AndroidManifest.xml
│   │   │   └── res/
│   │   ├── test/
│   │   └── androidTest/
│   └── build.gradle.kts
├── core/
│   └── src/main/java/com/example/ringapp/core
├── data/
│   └── src/main/java/com/example/ringapp/data
├── domain/
│   └── src/main/java/com/example/ringapp/domain
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle.properties
└── README.md
```

## 5. Module responsibilities

### app module
The app module is the user-facing layer and contains the Compose UI, navigation, lifecycle integration, Hilt configuration, and background workers.

Main folders:

- `ui/` – screens, view models, theme, shared UI components
- `navigation/` – app routing and destination definitions
- `work/` – recurring maintenance, reminders, schedulers, and worker factory logic
- `data/` – app-level initialization, preferences, backup, reset handling
- `widget/` – home screen widget provider
- `di/` – dependency injection modules

### data module
The data module stores database entities, DAO interfaces, repository implementations, and migration logic.

This module is responsible for:

- Room database setup and migration support
- Habit, task, profile, point, and analytics storage
- Repository layer access patterns used by the app and domain layers
- Background data persistence and local transaction state

### domain module
The domain module holds the business rules and engine logic that define the app's behavior.

This includes:

- Use cases for creating, updating, completing, and observing habits/tasks
- Progress calculation engines for streaks, analytics, and rewards
- Scheduling logic for recurring habits and tasks

### core module
The core module is a small foundation layer for shared application-level definitions and supporting functionality.

## 6. Main application flow

The project follows a layered pattern:

1. UI layer displays screens and collects interactions.
2. ViewModels expose state and transform app events.
3. Use cases coordinate domain logic.
4. Repositories handle data access and transactional updates.
5. Room database and DataStore persist application state.
6. WorkManager / alarm infrastructure triggers reminders and maintenance tasks.

This keeps the code organized and separated by responsibility.

## 7. Key classes and responsibilities

### Application bootstrap

- `MainActivity.kt` – application entry point, permission checks, startup seeding, and lifecycle setup
- `RingApplication.kt` – Hilt application bootstrap and WorkManager configuration
- `Navigation.kt` – entry point used to render the navigation graph

### Navigation

- `navigation/AppNavGraph.kt` – central Compose navigation graph
- `navigation/Screen.kt` – screen route definitions
- `navigation/NavigationDestination.kt` – navigation destinations for the tab-based UI
- `navigation/BottomNavigationItem.kt` – bottom navigation item definitions

### Core UI state

- `ui/AppViewModel.kt` – global app state for onboarding, theme, sync configuration, and initialization
- `ui/theme/*` – color palette, typography, shapes, spacing, themes, and view-specific styling
- `ui/home/*` – home dashboard with habits, tasks, points, and progress summary
- `ui/tasks/*` – task creation, viewing, editing, and list management
- `ui/habits/*` – habit creation, detail views, list management, and habit scheduling
- `ui/profile/*` – user profile settings, notifications, backup/restore, and UI personalization
- `ui/analytics/*` – analytics screens and BMI/progress tracking
- `ui/onboarding/OnboardingScreen.kt` – first-run onboarding experience

### Data layer

- `data/PreferencesDataStore.kt` – persistent user preferences and app initialization state
- `data/FirstLaunchInitializer.kt` – first-run initialization logic
- `data/BackupRestoreService.kt` – backup and restore service
- `data/ResetUserDataService.kt` – data reset logic
- `data/db/AppDatabase.kt` – Room database definition and migration configuration
- `data/db/Converters.kt` – type conversion logic for database-backed values
- `data/db/Migrations.kt` – upgrade/migration definitions for schema changes

### Repositories

The repository directory contains the access points for core data operations:

- `HabitRepository.kt`
- `TaskRepository.kt`
- `CategoryRepository.kt`
- `ProfileRepository.kt`
- `UserDataRepository.kt`
- `UserProgressRepository.kt`
- `AchievementRepository.kt`
- `PointRepository.kt`

These classes centralize database queries and transactional updates, allowing the UI and domain layers to work with cleaner abstractions.

### Domain engine and business logic

- `domain/engine/AnalyticsEngine.kt` – analytics computations
- `domain/engine/GamificationEngine.kt` – scoring, achievements, and progression systems
- `domain/engine/ScheduleEngine.kt` – recurring-time logic for activity scheduling
- `domain/engine/StreakEngine.kt` – streak tracking and milestone logic
- `domain/engine/AchievementEngine.kt` – achievement evaluation

### Use cases

The domain use-case layer is responsible for actions like:

- creating or updating habits/tasks
- tracking progress completion
- observing categories, profile data, and daily progress
- toggling habits and completing tasks
- updating profile and user progress state

Examples include:

- `CreateHabitUseCase.kt`
- `UpdateHabitUseCase.kt`
- `CreateTaskUseCase.kt`
- `CompleteTaskUseCase.kt`
- `ObserveTodayDataUseCase.kt`
- `ObserveUserProgressUseCase.kt`
- `RecordHabitProgressUseCase.kt`

### Background workers and reminders

- `work/WorkScheduler.kt` – scheduler for recurring maintenance tasks
- `work/DailyMaintenanceWorker.kt` – daily maintenance tasks
- `work/MotivationWorker.kt` – motivational reminder generation
- `work/ReminderAlarmReceiver.kt` – reminder alert handling
- `work/ReminderRescheduleWorker.kt` – rescheduling of reminders
- `work/TaskReminderScheduler.kt` – task reminder scheduling
- `work/BootReceiver.kt` – runtime startup work trigger
- `work/RingWorkerFactory.kt` – custom WorkManager factory for DI-backed workers

### Widget and OS integration

- `widget/RingWidgetProvider.kt` – app widget provider for lightweight glance-like home widget information

## 8. Data model overview

The app uses a Room database with a rich set of entities for core app content.

Primary entities include:

- `HabitEntity` – habit definition, recurrence, target metrics, and streak metadata
- `TaskEntity` – task information with recurrence and due date rules
- `ProfileEntity` – user profile information
- `UserProgressEntity` – persistent user progression state
- `UserAchievementEntity` – achievement ownership and unlock state
- `PointTransactionEntity` – points history and reward adjustments
- `HabitProgressEntity` – daily or periodic habit progress records
- `CategoryEntity` – habit/task grouping categories
- `AppSettingsEntity` – app-level configuration
- `AnalyticsDailyEntity` – aggregated analytics records

## 9. Features included

- Habit creation, editing, toggling, completion, and analytics
- Task management with reminders and due-date logic
- User points and score tracking tied to behavior
- Daily and long-term streak computation
- Habit schedule types: daily, weekly, monthly, yearly, odd/even/custom patterns
- Personal profile customization and appearance settings
- Backup/restore workflow support
- Onboarding and initialization flow
- Notification-related reminders and background maintenance
- Widget support for quick access to app information

## 10. App behavior and UX notes

The app uses a futuristic, neon-inspired visual style with a premium productivity aesthetic. Colors, cards, glow effects, dark theme support, and smooth animations are implemented to make daily planning feel visually engaging without sacrificing clarity.

The design encourages users to see progress, streaks, and points as tangible feedback to maintain momentum.

## 11. Setup instructions

### Prerequisites

- Android Studio
- JDK 17
- Android SDK with API level 35 configured
- Gradle wrapper included in the repository

### Steps

1. Open the project in Android Studio.
2. Let Gradle sync complete.
3. Select an Android emulator or connected device.
4. Run the application from the `app` module.

### Build commands

```bash
./gradlew build
./gradlew :app:assembleDebug
./gradlew :app:compileDebugKotlin
./gradlew test
```

## 12. Validation note

The app was verified to compile successfully after the documentation pass. Only explanatory comments were added; code logic remains unchanged.

## 13. Summary

Ring 2 is a practical Android habit and productivity app with a strong focus on progress visibility, personalization, and daily accountability. The architecture is modular and organized by responsibility: UI, navigation, domain rules, repository access, and persistent local storage.

The project is well suited for extension with additional features such as cloud sync, calendar integration, AI-based suggestions, richer analytics, or stronger onboarding flows.

## 14. File documentation status

All source and resource files in the repository have been annotated with file-level documentation comments to make the project easier to understand while preserving the original behavior.
