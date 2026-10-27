// -----------------------------------------------------------------------------
// Project: Ring 2
// File: data\src\main\java\com\example\ringapp\data\local\entities\Enums.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.data.local.entities

// Handles the habit type logic for this component without modifying runtime behavior.
// Represents the habit type structure used by the application.
enum class HabitType { YES_NO, MEASURABLE }
// Represents the schedule type structure used by the application.
enum class ScheduleType { DAILY, ODD_DAYS, EVEN_DAYS, WEEKLY, MONTHLY, YEARLY, CUSTOM }
// Represents the task priority structure used by the application.
enum class TaskPriority { LOW, MEDIUM, HIGH }
// Handles the repeat type logic for this component without modifying runtime behavior.
// Represents the repeat type structure used by the application.
enum class RepeatType { NONE, DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM }
// Represents the transaction type structure used by the application.
enum class TransactionType {
    STARTING_POINTS, TASK_CREATE, TASK_COMPLETE, TASK_UNCOMPLETE, HABIT_CREATE, HABIT_COMPLETE, HABIT_UNCOMPLETE,
    STREAK_MILESTONE, MISSED_TARGET, CHANGE_TARGET, TASK_DELETE, HABIT_DELETE
}
// Handles the theme mode logic for this component without modifying runtime behavior.
// Represents the theme mode structure used by the application.
enum class ThemeMode { SYSTEM, LIGHT, DARK }
// Represents the achievement category structure used by the application.
enum class AchievementCategory { CONSISTENCY, HABITS, TASKS, STREAK, POINTS, LEVELS, MILESTONES }
// Represents the notification type structure used by the application.
enum class NotificationType { REMINDER, MOTIVATION }
// Handles the backup type logic for this component without modifying runtime behavior.
// Represents the backup type structure used by the application.
enum class BackupType { EXPORT, IMPORT }
// Represents the backup status structure used by the application.
enum class BackupStatus { SUCCESS, FAILED }
