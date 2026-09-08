package com.example.ringapp.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=OFF")
        renameLegacyTable(db, "habit_progress")
        renameLegacyTable(db, "user_progress")
        renameLegacyTable(db, "profile")
        createV5Tables(db)
        copyLegacyRows(db)
        db.execSQL("PRAGMA foreign_keys=ON")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE profile ADD COLUMN dateOfBirth INTEGER")
        db.execSQL("ALTER TABLE profile ADD COLUMN gender TEXT")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS app_settings (id INTEGER NOT NULL PRIMARY KEY, onboardingCompleted INTEGER NOT NULL, notificationsEnabled INTEGER NOT NULL, taskRemindersEnabled INTEGER NOT NULL, habitRemindersEnabled INTEGER NOT NULL, motivationEnabled INTEGER NOT NULL, achievementNotificationsEnabled INTEGER NOT NULL, streakNotificationsEnabled INTEGER NOT NULL, morningMotivationTime INTEGER, eveningMotivationTime INTEGER, weekStartsOn INTEGER NOT NULL, firstDayOfMonth INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS habit_schedules (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, habitId INTEGER NOT NULL, scheduleType TEXT NOT NULL, interval INTEGER NOT NULL, daysOfWeek TEXT, dayOfMonth INTEGER, month INTEGER, dayOfYear INTEGER, isActive INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, FOREIGN KEY(habitId) REFERENCES habit(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_schedules_habitId ON habit_schedules(habitId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS task_reminders (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, taskId INTEGER NOT NULL, triggerAt INTEGER NOT NULL, reminderType TEXT NOT NULL, isEnabled INTEGER NOT NULL, alarmId INTEGER, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, FOREIGN KEY(taskId) REFERENCES task(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_task_reminders_taskId ON task_reminders(taskId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_task_reminders_triggerAt ON task_reminders(triggerAt)")
        db.execSQL("CREATE TABLE IF NOT EXISTS user_achievements (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, achievementId INTEGER NOT NULL, unlockedAt INTEGER, progress INTEGER NOT NULL, rewardTransactionId INTEGER, FOREIGN KEY(achievementId) REFERENCES achievement(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_user_achievements_achievementId ON user_achievements(achievementId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS analytics_daily (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date INTEGER NOT NULL, habitScore INTEGER NOT NULL, taskScore INTEGER NOT NULL, consistencyScore INTEGER NOT NULL, streakScore INTEGER NOT NULL, productivityScore INTEGER NOT NULL, completionRate INTEGER NOT NULL, habitCompletions INTEGER NOT NULL, taskCompletions INTEGER NOT NULL, activeHabits INTEGER NOT NULL, activeTasks INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_analytics_daily_date ON analytics_daily(date)")
    }
}

private fun createV5Tables(db: SupportSQLiteDatabase) {
    db.execSQL("CREATE TABLE IF NOT EXISTS category (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, color INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, deletedAt INTEGER)")
    db.execSQL("CREATE TABLE IF NOT EXISTS habit (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, description TEXT, categoryId INTEGER NOT NULL, type TEXT NOT NULL, target INTEGER NOT NULL, unit TEXT, scheduleType TEXT NOT NULL, scheduleDays TEXT, startDate INTEGER NOT NULL, currentStreak INTEGER NOT NULL, bestStreak INTEGER NOT NULL, totalCompletions INTEGER NOT NULL, color INTEGER NOT NULL, deletedAt INTEGER, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, FOREIGN KEY(categoryId) REFERENCES category(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_categoryId ON habit(categoryId)")
    db.execSQL("CREATE TABLE IF NOT EXISTS habit_progress (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, habitId INTEGER NOT NULL, date INTEGER NOT NULL, target INTEGER NOT NULL, actual INTEGER NOT NULL, percentage INTEGER NOT NULL, completed INTEGER NOT NULL, note TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, FOREIGN KEY(habitId) REFERENCES habit(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_habit_progress_habitId_date ON habit_progress(habitId, date)")
    db.execSQL("CREATE TABLE IF NOT EXISTS streak_cycle (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, habitId INTEGER NOT NULL, cycleNumber INTEGER NOT NULL, startTime INTEGER NOT NULL, endTime INTEGER, currentLength INTEGER NOT NULL, bestLength INTEGER NOT NULL, breakReason TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, FOREIGN KEY(habitId) REFERENCES habit(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_streak_cycle_habitId ON streak_cycle(habitId)")
    db.execSQL("CREATE TABLE IF NOT EXISTS streak_milestone (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, habitId INTEGER NOT NULL, cycleId INTEGER NOT NULL, milestoneLength INTEGER NOT NULL, achievedAt INTEGER NOT NULL, pointsAwarded INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, FOREIGN KEY(habitId) REFERENCES habit(id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(cycleId) REFERENCES streak_cycle(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_streak_milestone_habitId ON streak_milestone(habitId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_streak_milestone_cycleId ON streak_milestone(cycleId)")
    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_streak_milestone_habitId_cycleId_milestoneLength ON streak_milestone(habitId, cycleId, milestoneLength)")
    db.execSQL("CREATE TABLE IF NOT EXISTS task (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT, priority TEXT NOT NULL, dueDate INTEGER, dueTime INTEGER, repeatType TEXT NOT NULL, repeatInterval INTEGER NOT NULL, repeatEndDate INTEGER, completed INTEGER NOT NULL, completedAt INTEGER, reminderEnabled INTEGER NOT NULL, reminderTime INTEGER, parentTaskId INTEGER, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, deletedAt INTEGER, FOREIGN KEY(parentTaskId) REFERENCES task(id) ON UPDATE NO ACTION ON DELETE SET NULL)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_task_parentTaskId ON task(parentTaskId)")
    db.execSQL("CREATE TABLE IF NOT EXISTS point_transaction (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, amount INTEGER NOT NULL, type TEXT NOT NULL, description TEXT NOT NULL, habitId INTEGER, taskId INTEGER, cycleId INTEGER, uniqueReference TEXT NOT NULL, timestamp INTEGER NOT NULL, deviceId TEXT, createdAt INTEGER NOT NULL, FOREIGN KEY(habitId) REFERENCES habit(id) ON UPDATE NO ACTION ON DELETE SET NULL, FOREIGN KEY(taskId) REFERENCES task(id) ON UPDATE NO ACTION ON DELETE SET NULL, FOREIGN KEY(cycleId) REFERENCES streak_cycle(id) ON UPDATE NO ACTION ON DELETE SET NULL)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_point_transaction_habitId ON point_transaction(habitId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_point_transaction_taskId ON point_transaction(taskId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_point_transaction_cycleId ON point_transaction(cycleId)")
    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_point_transaction_uniqueReference ON point_transaction(uniqueReference)")
    db.execSQL("CREATE TABLE IF NOT EXISTS user_progress (userId INTEGER NOT NULL, currentPoints INTEGER NOT NULL, lifetimePoints INTEGER NOT NULL, level INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(userId))")
    db.execSQL("CREATE TABLE IF NOT EXISTS profile (userId INTEGER NOT NULL, name TEXT NOT NULL, avatarColor INTEGER NOT NULL, photoUri TEXT, themePreference TEXT NOT NULL, language TEXT NOT NULL, onboardingComplete INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(userId))")
    db.execSQL("CREATE TABLE IF NOT EXISTS achievement (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, category TEXT NOT NULL, threshold INTEGER NOT NULL, description TEXT NOT NULL, icon TEXT, pointsAwarded INTEGER NOT NULL, unlocked INTEGER NOT NULL, unlockedAt INTEGER, currentProgress INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
    db.execSQL("CREATE TABLE IF NOT EXISTS notification (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, message TEXT NOT NULL, type TEXT NOT NULL, relatedEntityId INTEGER, timestamp INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_timestamp ON notification(timestamp)")
    db.execSQL("CREATE TABLE IF NOT EXISTS backup_metadata (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, type TEXT NOT NULL, status TEXT NOT NULL, filePath TEXT, sizeBytes INTEGER, errorMessage TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
}

private fun copyLegacyRows(db: SupportSQLiteDatabase) {
    if (tableExists(db, "categories")) {
        db.execSQL("INSERT OR IGNORE INTO category(id, name, color, createdAt, updatedAt) SELECT id, name, color, 0, 0 FROM categories")
    }
    if (tableExists(db, "habits")) {
        db.execSQL("INSERT OR IGNORE INTO habit(id, name, description, categoryId, type, target, unit, scheduleType, startDate, currentStreak, bestStreak, totalCompletions, color, createdAt, updatedAt) SELECT id, name, description, categoryId, type, CAST(target AS INTEGER), unit, 'DAILY', startDate, currentStreak, bestStreak, totalCompletions, color, createdAt, updatedAt FROM habits")
    }
    if (tableExists(db, "legacy_habit_progress")) {
        db.execSQL("INSERT OR IGNORE INTO habit_progress(id, habitId, date, target, actual, percentage, completed, note, createdAt, updatedAt) SELECT id, habitId, date, CAST(target AS INTEGER), CAST(actualValue AS INTEGER), CAST(percentage AS INTEGER), completed, note, createdAt, updatedAt FROM legacy_habit_progress")
    }
    if (tableExists(db, "streak_cycles")) {
        db.execSQL("INSERT OR IGNORE INTO streak_cycle(id, habitId, cycleNumber, startTime, endTime, currentLength, bestLength, breakReason, createdAt, updatedAt) SELECT id, habitId, cycleNumber, startedAt, endedAt, currentLength, bestLength, breakReason, createdAt, createdAt FROM streak_cycles")
    }
    if (tableExists(db, "streak_milestones")) {
        db.execSQL("INSERT OR IGNORE INTO streak_milestone(id, habitId, cycleId, milestoneLength, achievedAt, pointsAwarded, createdAt, updatedAt) SELECT id, habitId, streakCycleId, milestone, awardedAt, reward, awardedAt, awardedAt FROM streak_milestones")
    }
    if (tableExists(db, "legacy_user_progress")) {
        db.execSQL("INSERT OR IGNORE INTO user_progress(userId, currentPoints, lifetimePoints, level, createdAt, updatedAt) SELECT id, currentPoints, lifetimeEarnedPoints, level, createdAt, updatedAt FROM legacy_user_progress")
    }
}

private fun renameLegacyTable(db: SupportSQLiteDatabase, tableName: String) {
    if (tableExists(db, tableName)) {
        db.execSQL("ALTER TABLE $tableName RENAME TO legacy_$tableName")
    }
}

private fun tableExists(db: SupportSQLiteDatabase, tableName: String): Boolean = db.query(
    "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
    arrayOf(tableName)
).use { it.moveToFirst() }
