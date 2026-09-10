package com.example.ringapp.data.local.entities

enum class HabitType { YES_NO, MEASURABLE }
enum class ScheduleType { DAILY, ODD_DAYS, EVEN_DAYS, WEEKLY, MONTHLY, YEARLY, CUSTOM }
enum class TaskPriority { LOW, MEDIUM, HIGH }
enum class RepeatType { NONE, DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM }
enum class TransactionType {
    STARTING_POINTS, TASK_CREATE, TASK_COMPLETE, TASK_UNCOMPLETE, HABIT_CREATE, HABIT_COMPLETE, HABIT_UNCOMPLETE,
    STREAK_MILESTONE, MISSED_TARGET, CHANGE_TARGET, TASK_DELETE, HABIT_DELETE
}
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AchievementCategory { CONSISTENCY, HABITS, TASKS, STREAK, POINTS, LEVELS, MILESTONES }
enum class NotificationType { REMINDER, MOTIVATION }
enum class BackupType { EXPORT, IMPORT }
enum class BackupStatus { SUCCESS, FAILED }
