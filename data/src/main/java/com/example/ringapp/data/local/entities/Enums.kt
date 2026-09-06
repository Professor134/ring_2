package com.example.ringapp.data.local.entities

enum class HabitType { YES_NO, MEASURABLE }
enum class ScheduleType { DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM }
enum class TaskPriority { LOW, MEDIUM, HIGH }
enum class RepeatType { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }
enum class TransactionType {
    STARTING_POINTS, TASK_CREATE, TASK_COMPLETE, HABIT_CREATE, HABIT_COMPLETE,
    STREAK_MILESTONE, MISSED_TARGET, CHANGE_TARGET, TASK_DELETE, HABIT_DELETE
}
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AchievementCategory { STREAK, HABITS_CREATED, TASKS_COMPLETED, POINTS_EARNED, CUSTOM }
enum class NotificationType { REMINDER, MOTIVATION }
enum class BackupType { EXPORT, IMPORT }
enum class BackupStatus { SUCCESS, FAILED }
