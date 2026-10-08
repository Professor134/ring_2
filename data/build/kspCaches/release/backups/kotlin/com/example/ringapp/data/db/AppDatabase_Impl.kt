package com.example.ringapp.`data`.db

import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import androidx.room.RoomOpenHelper
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.ringapp.`data`.local.dao.AchievementDao
import com.example.ringapp.`data`.local.dao.AchievementDao_Impl
import com.example.ringapp.`data`.local.dao.AnalyticsDailyDao
import com.example.ringapp.`data`.local.dao.AnalyticsDailyDao_Impl
import com.example.ringapp.`data`.local.dao.AppSettingsDao
import com.example.ringapp.`data`.local.dao.AppSettingsDao_Impl
import com.example.ringapp.`data`.local.dao.CategoryDao
import com.example.ringapp.`data`.local.dao.CategoryDao_Impl
import com.example.ringapp.`data`.local.dao.HabitDao
import com.example.ringapp.`data`.local.dao.HabitDao_Impl
import com.example.ringapp.`data`.local.dao.HabitScheduleDao
import com.example.ringapp.`data`.local.dao.HabitScheduleDao_Impl
import com.example.ringapp.`data`.local.dao.PointTransactionDao
import com.example.ringapp.`data`.local.dao.PointTransactionDao_Impl
import com.example.ringapp.`data`.local.dao.ProfileDao
import com.example.ringapp.`data`.local.dao.ProfileDao_Impl
import com.example.ringapp.`data`.local.dao.TaskDao
import com.example.ringapp.`data`.local.dao.TaskDao_Impl
import com.example.ringapp.`data`.local.dao.TaskReminderDao
import com.example.ringapp.`data`.local.dao.TaskReminderDao_Impl
import com.example.ringapp.`data`.local.dao.UserAchievementDao
import com.example.ringapp.`data`.local.dao.UserAchievementDao_Impl
import com.example.ringapp.`data`.local.dao.UserProgressDao
import com.example.ringapp.`data`.local.dao.UserProgressDao_Impl
import java.lang.Class
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import javax.`annotation`.processing.Generated
import kotlin.Any
import kotlin.Boolean
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.Set

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class AppDatabase_Impl : AppDatabase() {
  private val _habitDao: Lazy<HabitDao> = lazy {
    HabitDao_Impl(this)
  }


  private val _taskDao: Lazy<TaskDao> = lazy {
    TaskDao_Impl(this)
  }


  private val _categoryDao: Lazy<CategoryDao> = lazy {
    CategoryDao_Impl(this)
  }


  private val _pointTransactionDao: Lazy<PointTransactionDao> = lazy {
    PointTransactionDao_Impl(this)
  }


  private val _profileDao: Lazy<ProfileDao> = lazy {
    ProfileDao_Impl(this)
  }


  private val _userProgressDao: Lazy<UserProgressDao> = lazy {
    UserProgressDao_Impl(this)
  }


  private val _achievementDao: Lazy<AchievementDao> = lazy {
    AchievementDao_Impl(this)
  }


  private val _appSettingsDao: Lazy<AppSettingsDao> = lazy {
    AppSettingsDao_Impl(this)
  }


  private val _habitScheduleDao: Lazy<HabitScheduleDao> = lazy {
    HabitScheduleDao_Impl(this)
  }


  private val _taskReminderDao: Lazy<TaskReminderDao> = lazy {
    TaskReminderDao_Impl(this)
  }


  private val _userAchievementDao: Lazy<UserAchievementDao> = lazy {
    UserAchievementDao_Impl(this)
  }


  private val _analyticsDailyDao: Lazy<AnalyticsDailyDao> = lazy {
    AnalyticsDailyDao_Impl(this)
  }


  protected override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper {
    val _openCallback: SupportSQLiteOpenHelper.Callback = RoomOpenHelper(config, object :
        RoomOpenHelper.Delegate(7) {
      public override fun createAllTables(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `habit` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT, `categoryId` INTEGER, `type` TEXT NOT NULL, `target` REAL NOT NULL, `unit` TEXT, `scheduleType` TEXT NOT NULL, `scheduleDays` TEXT, `startDate` INTEGER NOT NULL, `currentStreak` INTEGER NOT NULL, `bestStreak` INTEGER NOT NULL, `totalCompletions` INTEGER NOT NULL, `color` INTEGER NOT NULL, `deletedAt` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`categoryId`) REFERENCES `category`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_categoryId` ON `habit` (`categoryId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `habit_progress` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habitId` INTEGER NOT NULL, `date` INTEGER NOT NULL, `target` REAL NOT NULL, `actual` REAL NOT NULL, `percentage` INTEGER NOT NULL, `completed` INTEGER NOT NULL, `note` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`habitId`) REFERENCES `habit`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_progress_habitId_date` ON `habit_progress` (`habitId`, `date`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `streak_cycle` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habitId` INTEGER NOT NULL, `cycleNumber` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER, `currentLength` INTEGER NOT NULL, `bestLength` INTEGER NOT NULL, `breakReason` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`habitId`) REFERENCES `habit`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_streak_cycle_habitId` ON `streak_cycle` (`habitId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `streak_milestone` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habitId` INTEGER NOT NULL, `cycleId` INTEGER NOT NULL, `milestoneLength` INTEGER NOT NULL, `achievedAt` INTEGER NOT NULL, `pointsAwarded` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`habitId`) REFERENCES `habit`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`cycleId`) REFERENCES `streak_cycle`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_streak_milestone_habitId` ON `streak_milestone` (`habitId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_streak_milestone_cycleId` ON `streak_milestone` (`cycleId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_streak_milestone_habitId_cycleId_milestoneLength` ON `streak_milestone` (`habitId`, `cycleId`, `milestoneLength`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `task` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT, `priority` TEXT NOT NULL, `dueDate` INTEGER, `dueTime` INTEGER, `repeatType` TEXT NOT NULL, `repeatInterval` INTEGER NOT NULL, `repeatDaysOfWeek` TEXT, `repeatDayOfMonth` INTEGER, `repeatMonth` INTEGER, `repeatEndDate` INTEGER, `completed` INTEGER NOT NULL, `completedAt` INTEGER, `reminderEnabled` INTEGER NOT NULL, `reminderTime` INTEGER, `parentTaskId` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER, FOREIGN KEY(`parentTaskId`) REFERENCES `task`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_parentTaskId` ON `task` (`parentTaskId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `point_transaction` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `description` TEXT NOT NULL, `habitId` INTEGER, `taskId` INTEGER, `cycleId` INTEGER, `uniqueReference` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `deviceId` TEXT, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`habitId`) REFERENCES `habit`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`taskId`) REFERENCES `task`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`cycleId`) REFERENCES `streak_cycle`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_point_transaction_habitId` ON `point_transaction` (`habitId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_point_transaction_taskId` ON `point_transaction` (`taskId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_point_transaction_cycleId` ON `point_transaction` (`cycleId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_point_transaction_uniqueReference` ON `point_transaction` (`uniqueReference`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_progress` (`userId` INTEGER NOT NULL, `currentPoints` INTEGER NOT NULL, `lifetimePoints` INTEGER NOT NULL, `level` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`userId`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `profile` (`userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `avatarColor` INTEGER NOT NULL, `photoUri` TEXT, `dateOfBirth` INTEGER, `gender` TEXT, `themePreference` TEXT NOT NULL, `language` TEXT NOT NULL, `onboardingComplete` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`userId`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `category` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` INTEGER NOT NULL, `icon` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deletedAt` INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `achievement` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `threshold` INTEGER NOT NULL, `description` TEXT NOT NULL, `icon` TEXT, `pointsAwarded` INTEGER NOT NULL, `unlocked` INTEGER NOT NULL, `unlockedAt` INTEGER, `currentProgress` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `notification` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `type` TEXT NOT NULL, `relatedEntityId` INTEGER, `timestamp` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notification_timestamp` ON `notification` (`timestamp`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `backup_metadata` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `type` TEXT NOT NULL, `status` TEXT NOT NULL, `filePath` TEXT, `sizeBytes` INTEGER, `errorMessage` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `app_settings` (`id` INTEGER NOT NULL, `onboardingCompleted` INTEGER NOT NULL, `notificationsEnabled` INTEGER NOT NULL, `taskRemindersEnabled` INTEGER NOT NULL, `habitRemindersEnabled` INTEGER NOT NULL, `motivationEnabled` INTEGER NOT NULL, `achievementNotificationsEnabled` INTEGER NOT NULL, `streakNotificationsEnabled` INTEGER NOT NULL, `morningMotivationTime` INTEGER, `eveningMotivationTime` INTEGER, `weekStartsOn` INTEGER NOT NULL, `firstDayOfMonth` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `habit_schedules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habitId` INTEGER NOT NULL, `scheduleType` TEXT NOT NULL, `interval` INTEGER NOT NULL, `daysOfWeek` TEXT, `dayOfMonth` INTEGER, `month` INTEGER, `dayOfYear` INTEGER, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`habitId`) REFERENCES `habit`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_schedules_habitId` ON `habit_schedules` (`habitId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `task_reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER NOT NULL, `triggerAt` INTEGER NOT NULL, `reminderType` TEXT NOT NULL, `isEnabled` INTEGER NOT NULL, `alarmId` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`taskId`) REFERENCES `task`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_reminders_taskId` ON `task_reminders` (`taskId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_reminders_triggerAt` ON `task_reminders` (`triggerAt`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_achievements` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `achievementId` INTEGER NOT NULL, `unlockedAt` INTEGER, `progress` INTEGER NOT NULL, `rewardTransactionId` INTEGER, FOREIGN KEY(`achievementId`) REFERENCES `achievement`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_achievements_achievementId` ON `user_achievements` (`achievementId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `analytics_daily` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` INTEGER NOT NULL, `habitScore` INTEGER NOT NULL, `taskScore` INTEGER NOT NULL, `consistencyScore` INTEGER NOT NULL, `streakScore` INTEGER NOT NULL, `productivityScore` INTEGER NOT NULL, `completionRate` INTEGER NOT NULL, `habitCompletions` INTEGER NOT NULL, `taskCompletions` INTEGER NOT NULL, `activeHabits` INTEGER NOT NULL, `activeTasks` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_analytics_daily_date` ON `analytics_daily` (`date`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd4cf7cbc5876f6165cb4e3a9f9d5e7b6')")
      }

      public override fun dropAllTables(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `habit`")
        db.execSQL("DROP TABLE IF EXISTS `habit_progress`")
        db.execSQL("DROP TABLE IF EXISTS `streak_cycle`")
        db.execSQL("DROP TABLE IF EXISTS `streak_milestone`")
        db.execSQL("DROP TABLE IF EXISTS `task`")
        db.execSQL("DROP TABLE IF EXISTS `point_transaction`")
        db.execSQL("DROP TABLE IF EXISTS `user_progress`")
        db.execSQL("DROP TABLE IF EXISTS `profile`")
        db.execSQL("DROP TABLE IF EXISTS `category`")
        db.execSQL("DROP TABLE IF EXISTS `achievement`")
        db.execSQL("DROP TABLE IF EXISTS `notification`")
        db.execSQL("DROP TABLE IF EXISTS `backup_metadata`")
        db.execSQL("DROP TABLE IF EXISTS `app_settings`")
        db.execSQL("DROP TABLE IF EXISTS `habit_schedules`")
        db.execSQL("DROP TABLE IF EXISTS `task_reminders`")
        db.execSQL("DROP TABLE IF EXISTS `user_achievements`")
        db.execSQL("DROP TABLE IF EXISTS `analytics_daily`")
        val _callbacks: List<RoomDatabase.Callback>? = mCallbacks
        if (_callbacks != null) {
          for (_callback: RoomDatabase.Callback in _callbacks) {
            _callback.onDestructiveMigration(db)
          }
        }
      }

      public override fun onCreate(db: SupportSQLiteDatabase) {
        val _callbacks: List<RoomDatabase.Callback>? = mCallbacks
        if (_callbacks != null) {
          for (_callback: RoomDatabase.Callback in _callbacks) {
            _callback.onCreate(db)
          }
        }
      }

      public override fun onOpen(db: SupportSQLiteDatabase) {
        mDatabase = db
        db.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(db)
        val _callbacks: List<RoomDatabase.Callback>? = mCallbacks
        if (_callbacks != null) {
          for (_callback: RoomDatabase.Callback in _callbacks) {
            _callback.onOpen(db)
          }
        }
      }

      public override fun onPreMigrate(db: SupportSQLiteDatabase) {
        dropFtsSyncTriggers(db)
      }

      public override fun onPostMigrate(db: SupportSQLiteDatabase) {
      }

      public override fun onValidateSchema(db: SupportSQLiteDatabase):
          RoomOpenHelper.ValidationResult {
        val _columnsHabit: HashMap<String, TableInfo.Column> = HashMap<String, TableInfo.Column>(17)
        _columnsHabit.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("description", TableInfo.Column("description", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("categoryId", TableInfo.Column("categoryId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("target", TableInfo.Column("target", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("unit", TableInfo.Column("unit", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("scheduleType", TableInfo.Column("scheduleType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("scheduleDays", TableInfo.Column("scheduleDays", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("startDate", TableInfo.Column("startDate", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("currentStreak", TableInfo.Column("currentStreak", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("bestStreak", TableInfo.Column("bestStreak", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("totalCompletions", TableInfo.Column("totalCompletions", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("color", TableInfo.Column("color", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("deletedAt", TableInfo.Column("deletedAt", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabit.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHabit: HashSet<TableInfo.ForeignKey> = HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysHabit.add(TableInfo.ForeignKey("category", "RESTRICT", "NO ACTION",
            listOf("categoryId"), listOf("id")))
        val _indicesHabit: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesHabit.add(TableInfo.Index("index_habit_categoryId", false, listOf("categoryId"),
            listOf("ASC")))
        val _infoHabit: TableInfo = TableInfo("habit", _columnsHabit, _foreignKeysHabit,
            _indicesHabit)
        val _existingHabit: TableInfo = read(db, "habit")
        if (!_infoHabit.equals(_existingHabit)) {
          return RoomOpenHelper.ValidationResult(false, """
              |habit(com.example.ringapp.data.local.entities.HabitEntity).
              | Expected:
              |""".trimMargin() + _infoHabit + """
              |
              | Found:
              |""".trimMargin() + _existingHabit)
        }
        val _columnsHabitProgress: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(10)
        _columnsHabitProgress.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitProgress.put("habitId", TableInfo.Column("habitId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitProgress.put("date", TableInfo.Column("date", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitProgress.put("target", TableInfo.Column("target", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitProgress.put("actual", TableInfo.Column("actual", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitProgress.put("percentage", TableInfo.Column("percentage", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitProgress.put("completed", TableInfo.Column("completed", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitProgress.put("note", TableInfo.Column("note", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitProgress.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitProgress.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHabitProgress: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysHabitProgress.add(TableInfo.ForeignKey("habit", "CASCADE", "NO ACTION",
            listOf("habitId"), listOf("id")))
        val _indicesHabitProgress: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesHabitProgress.add(TableInfo.Index("index_habit_progress_habitId_date", true,
            listOf("habitId", "date"), listOf("ASC", "ASC")))
        val _infoHabitProgress: TableInfo = TableInfo("habit_progress", _columnsHabitProgress,
            _foreignKeysHabitProgress, _indicesHabitProgress)
        val _existingHabitProgress: TableInfo = read(db, "habit_progress")
        if (!_infoHabitProgress.equals(_existingHabitProgress)) {
          return RoomOpenHelper.ValidationResult(false, """
              |habit_progress(com.example.ringapp.data.local.entities.HabitProgressEntity).
              | Expected:
              |""".trimMargin() + _infoHabitProgress + """
              |
              | Found:
              |""".trimMargin() + _existingHabitProgress)
        }
        val _columnsStreakCycle: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(10)
        _columnsStreakCycle.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakCycle.put("habitId", TableInfo.Column("habitId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakCycle.put("cycleNumber", TableInfo.Column("cycleNumber", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakCycle.put("startTime", TableInfo.Column("startTime", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakCycle.put("endTime", TableInfo.Column("endTime", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakCycle.put("currentLength", TableInfo.Column("currentLength", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakCycle.put("bestLength", TableInfo.Column("bestLength", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakCycle.put("breakReason", TableInfo.Column("breakReason", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakCycle.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakCycle.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysStreakCycle: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysStreakCycle.add(TableInfo.ForeignKey("habit", "CASCADE", "NO ACTION",
            listOf("habitId"), listOf("id")))
        val _indicesStreakCycle: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesStreakCycle.add(TableInfo.Index("index_streak_cycle_habitId", false,
            listOf("habitId"), listOf("ASC")))
        val _infoStreakCycle: TableInfo = TableInfo("streak_cycle", _columnsStreakCycle,
            _foreignKeysStreakCycle, _indicesStreakCycle)
        val _existingStreakCycle: TableInfo = read(db, "streak_cycle")
        if (!_infoStreakCycle.equals(_existingStreakCycle)) {
          return RoomOpenHelper.ValidationResult(false, """
              |streak_cycle(com.example.ringapp.data.local.entities.StreakCycleEntity).
              | Expected:
              |""".trimMargin() + _infoStreakCycle + """
              |
              | Found:
              |""".trimMargin() + _existingStreakCycle)
        }
        val _columnsStreakMilestone: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(8)
        _columnsStreakMilestone.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakMilestone.put("habitId", TableInfo.Column("habitId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakMilestone.put("cycleId", TableInfo.Column("cycleId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakMilestone.put("milestoneLength", TableInfo.Column("milestoneLength",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakMilestone.put("achievedAt", TableInfo.Column("achievedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakMilestone.put("pointsAwarded", TableInfo.Column("pointsAwarded", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakMilestone.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStreakMilestone.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysStreakMilestone: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(2)
        _foreignKeysStreakMilestone.add(TableInfo.ForeignKey("habit", "CASCADE", "NO ACTION",
            listOf("habitId"), listOf("id")))
        _foreignKeysStreakMilestone.add(TableInfo.ForeignKey("streak_cycle", "CASCADE", "NO ACTION",
            listOf("cycleId"), listOf("id")))
        val _indicesStreakMilestone: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(3)
        _indicesStreakMilestone.add(TableInfo.Index("index_streak_milestone_habitId", false,
            listOf("habitId"), listOf("ASC")))
        _indicesStreakMilestone.add(TableInfo.Index("index_streak_milestone_cycleId", false,
            listOf("cycleId"), listOf("ASC")))
        _indicesStreakMilestone.add(TableInfo.Index("index_streak_milestone_habitId_cycleId_milestoneLength",
            true, listOf("habitId", "cycleId", "milestoneLength"), listOf("ASC", "ASC", "ASC")))
        val _infoStreakMilestone: TableInfo = TableInfo("streak_milestone", _columnsStreakMilestone,
            _foreignKeysStreakMilestone, _indicesStreakMilestone)
        val _existingStreakMilestone: TableInfo = read(db, "streak_milestone")
        if (!_infoStreakMilestone.equals(_existingStreakMilestone)) {
          return RoomOpenHelper.ValidationResult(false, """
              |streak_milestone(com.example.ringapp.data.local.entities.StreakMilestoneEntity).
              | Expected:
              |""".trimMargin() + _infoStreakMilestone + """
              |
              | Found:
              |""".trimMargin() + _existingStreakMilestone)
        }
        val _columnsTask: HashMap<String, TableInfo.Column> = HashMap<String, TableInfo.Column>(20)
        _columnsTask.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("description", TableInfo.Column("description", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("priority", TableInfo.Column("priority", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("dueDate", TableInfo.Column("dueDate", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("dueTime", TableInfo.Column("dueTime", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("repeatType", TableInfo.Column("repeatType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("repeatInterval", TableInfo.Column("repeatInterval", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("repeatDaysOfWeek", TableInfo.Column("repeatDaysOfWeek", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("repeatDayOfMonth", TableInfo.Column("repeatDayOfMonth", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("repeatMonth", TableInfo.Column("repeatMonth", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("repeatEndDate", TableInfo.Column("repeatEndDate", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("completed", TableInfo.Column("completed", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("completedAt", TableInfo.Column("completedAt", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("reminderEnabled", TableInfo.Column("reminderEnabled", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("reminderTime", TableInfo.Column("reminderTime", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("parentTaskId", TableInfo.Column("parentTaskId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTask.put("deletedAt", TableInfo.Column("deletedAt", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTask: HashSet<TableInfo.ForeignKey> = HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysTask.add(TableInfo.ForeignKey("task", "SET NULL", "NO ACTION",
            listOf("parentTaskId"), listOf("id")))
        val _indicesTask: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesTask.add(TableInfo.Index("index_task_parentTaskId", false, listOf("parentTaskId"),
            listOf("ASC")))
        val _infoTask: TableInfo = TableInfo("task", _columnsTask, _foreignKeysTask, _indicesTask)
        val _existingTask: TableInfo = read(db, "task")
        if (!_infoTask.equals(_existingTask)) {
          return RoomOpenHelper.ValidationResult(false, """
              |task(com.example.ringapp.data.local.entities.TaskEntity).
              | Expected:
              |""".trimMargin() + _infoTask + """
              |
              | Found:
              |""".trimMargin() + _existingTask)
        }
        val _columnsPointTransaction: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(11)
        _columnsPointTransaction.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPointTransaction.put("amount", TableInfo.Column("amount", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPointTransaction.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPointTransaction.put("description", TableInfo.Column("description", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPointTransaction.put("habitId", TableInfo.Column("habitId", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPointTransaction.put("taskId", TableInfo.Column("taskId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPointTransaction.put("cycleId", TableInfo.Column("cycleId", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPointTransaction.put("uniqueReference", TableInfo.Column("uniqueReference", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPointTransaction.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPointTransaction.put("deviceId", TableInfo.Column("deviceId", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPointTransaction.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPointTransaction: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(3)
        _foreignKeysPointTransaction.add(TableInfo.ForeignKey("habit", "SET NULL", "NO ACTION",
            listOf("habitId"), listOf("id")))
        _foreignKeysPointTransaction.add(TableInfo.ForeignKey("task", "SET NULL", "NO ACTION",
            listOf("taskId"), listOf("id")))
        _foreignKeysPointTransaction.add(TableInfo.ForeignKey("streak_cycle", "SET NULL",
            "NO ACTION", listOf("cycleId"), listOf("id")))
        val _indicesPointTransaction: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(4)
        _indicesPointTransaction.add(TableInfo.Index("index_point_transaction_habitId", false,
            listOf("habitId"), listOf("ASC")))
        _indicesPointTransaction.add(TableInfo.Index("index_point_transaction_taskId", false,
            listOf("taskId"), listOf("ASC")))
        _indicesPointTransaction.add(TableInfo.Index("index_point_transaction_cycleId", false,
            listOf("cycleId"), listOf("ASC")))
        _indicesPointTransaction.add(TableInfo.Index("index_point_transaction_uniqueReference",
            true, listOf("uniqueReference"), listOf("ASC")))
        val _infoPointTransaction: TableInfo = TableInfo("point_transaction",
            _columnsPointTransaction, _foreignKeysPointTransaction, _indicesPointTransaction)
        val _existingPointTransaction: TableInfo = read(db, "point_transaction")
        if (!_infoPointTransaction.equals(_existingPointTransaction)) {
          return RoomOpenHelper.ValidationResult(false, """
              |point_transaction(com.example.ringapp.data.local.entities.PointTransactionEntity).
              | Expected:
              |""".trimMargin() + _infoPointTransaction + """
              |
              | Found:
              |""".trimMargin() + _existingPointTransaction)
        }
        val _columnsUserProgress: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(6)
        _columnsUserProgress.put("userId", TableInfo.Column("userId", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProgress.put("currentPoints", TableInfo.Column("currentPoints", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProgress.put("lifetimePoints", TableInfo.Column("lifetimePoints", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProgress.put("level", TableInfo.Column("level", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProgress.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserProgress.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUserProgress: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(0)
        val _indicesUserProgress: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(0)
        val _infoUserProgress: TableInfo = TableInfo("user_progress", _columnsUserProgress,
            _foreignKeysUserProgress, _indicesUserProgress)
        val _existingUserProgress: TableInfo = read(db, "user_progress")
        if (!_infoUserProgress.equals(_existingUserProgress)) {
          return RoomOpenHelper.ValidationResult(false, """
              |user_progress(com.example.ringapp.data.local.entities.UserProgressEntity).
              | Expected:
              |""".trimMargin() + _infoUserProgress + """
              |
              | Found:
              |""".trimMargin() + _existingUserProgress)
        }
        val _columnsProfile: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(11)
        _columnsProfile.put("userId", TableInfo.Column("userId", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProfile.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProfile.put("avatarColor", TableInfo.Column("avatarColor", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProfile.put("photoUri", TableInfo.Column("photoUri", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProfile.put("dateOfBirth", TableInfo.Column("dateOfBirth", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProfile.put("gender", TableInfo.Column("gender", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProfile.put("themePreference", TableInfo.Column("themePreference", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProfile.put("language", TableInfo.Column("language", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProfile.put("onboardingComplete", TableInfo.Column("onboardingComplete", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProfile.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsProfile.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProfile: HashSet<TableInfo.ForeignKey> = HashSet<TableInfo.ForeignKey>(0)
        val _indicesProfile: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(0)
        val _infoProfile: TableInfo = TableInfo("profile", _columnsProfile, _foreignKeysProfile,
            _indicesProfile)
        val _existingProfile: TableInfo = read(db, "profile")
        if (!_infoProfile.equals(_existingProfile)) {
          return RoomOpenHelper.ValidationResult(false, """
              |profile(com.example.ringapp.data.local.entities.ProfileEntity).
              | Expected:
              |""".trimMargin() + _infoProfile + """
              |
              | Found:
              |""".trimMargin() + _existingProfile)
        }
        val _columnsCategory: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(7)
        _columnsCategory.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategory.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategory.put("color", TableInfo.Column("color", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategory.put("icon", TableInfo.Column("icon", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategory.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategory.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategory.put("deletedAt", TableInfo.Column("deletedAt", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCategory: HashSet<TableInfo.ForeignKey> = HashSet<TableInfo.ForeignKey>(0)
        val _indicesCategory: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(0)
        val _infoCategory: TableInfo = TableInfo("category", _columnsCategory, _foreignKeysCategory,
            _indicesCategory)
        val _existingCategory: TableInfo = read(db, "category")
        if (!_infoCategory.equals(_existingCategory)) {
          return RoomOpenHelper.ValidationResult(false, """
              |category(com.example.ringapp.data.local.entities.CategoryEntity).
              | Expected:
              |""".trimMargin() + _infoCategory + """
              |
              | Found:
              |""".trimMargin() + _existingCategory)
        }
        val _columnsAchievement: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(11)
        _columnsAchievement.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAchievement.put("category", TableInfo.Column("category", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAchievement.put("threshold", TableInfo.Column("threshold", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAchievement.put("description", TableInfo.Column("description", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAchievement.put("icon", TableInfo.Column("icon", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAchievement.put("pointsAwarded", TableInfo.Column("pointsAwarded", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAchievement.put("unlocked", TableInfo.Column("unlocked", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAchievement.put("unlockedAt", TableInfo.Column("unlockedAt", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAchievement.put("currentProgress", TableInfo.Column("currentProgress", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAchievement.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAchievement.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAchievement: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(0)
        val _indicesAchievement: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(0)
        val _infoAchievement: TableInfo = TableInfo("achievement", _columnsAchievement,
            _foreignKeysAchievement, _indicesAchievement)
        val _existingAchievement: TableInfo = read(db, "achievement")
        if (!_infoAchievement.equals(_existingAchievement)) {
          return RoomOpenHelper.ValidationResult(false, """
              |achievement(com.example.ringapp.data.local.entities.AchievementEntity).
              | Expected:
              |""".trimMargin() + _infoAchievement + """
              |
              | Found:
              |""".trimMargin() + _existingAchievement)
        }
        val _columnsNotification: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(7)
        _columnsNotification.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotification.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotification.put("message", TableInfo.Column("message", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotification.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotification.put("relatedEntityId", TableInfo.Column("relatedEntityId", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotification.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotification.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysNotification: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(0)
        val _indicesNotification: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesNotification.add(TableInfo.Index("index_notification_timestamp", false,
            listOf("timestamp"), listOf("ASC")))
        val _infoNotification: TableInfo = TableInfo("notification", _columnsNotification,
            _foreignKeysNotification, _indicesNotification)
        val _existingNotification: TableInfo = read(db, "notification")
        if (!_infoNotification.equals(_existingNotification)) {
          return RoomOpenHelper.ValidationResult(false, """
              |notification(com.example.ringapp.data.local.entities.NotificationRecordEntity).
              | Expected:
              |""".trimMargin() + _infoNotification + """
              |
              | Found:
              |""".trimMargin() + _existingNotification)
        }
        val _columnsBackupMetadata: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(9)
        _columnsBackupMetadata.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBackupMetadata.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBackupMetadata.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBackupMetadata.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBackupMetadata.put("filePath", TableInfo.Column("filePath", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBackupMetadata.put("sizeBytes", TableInfo.Column("sizeBytes", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBackupMetadata.put("errorMessage", TableInfo.Column("errorMessage", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBackupMetadata.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBackupMetadata.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBackupMetadata: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(0)
        val _indicesBackupMetadata: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(0)
        val _infoBackupMetadata: TableInfo = TableInfo("backup_metadata", _columnsBackupMetadata,
            _foreignKeysBackupMetadata, _indicesBackupMetadata)
        val _existingBackupMetadata: TableInfo = read(db, "backup_metadata")
        if (!_infoBackupMetadata.equals(_existingBackupMetadata)) {
          return RoomOpenHelper.ValidationResult(false, """
              |backup_metadata(com.example.ringapp.data.local.entities.BackupMetadataEntity).
              | Expected:
              |""".trimMargin() + _infoBackupMetadata + """
              |
              | Found:
              |""".trimMargin() + _existingBackupMetadata)
        }
        val _columnsAppSettings: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(13)
        _columnsAppSettings.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("onboardingCompleted", TableInfo.Column("onboardingCompleted",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("notificationsEnabled", TableInfo.Column("notificationsEnabled",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("taskRemindersEnabled", TableInfo.Column("taskRemindersEnabled",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("habitRemindersEnabled", TableInfo.Column("habitRemindersEnabled",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("motivationEnabled", TableInfo.Column("motivationEnabled",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("achievementNotificationsEnabled",
            TableInfo.Column("achievementNotificationsEnabled", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("streakNotificationsEnabled",
            TableInfo.Column("streakNotificationsEnabled", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("morningMotivationTime", TableInfo.Column("morningMotivationTime",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("eveningMotivationTime", TableInfo.Column("eveningMotivationTime",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("weekStartsOn", TableInfo.Column("weekStartsOn", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("firstDayOfMonth", TableInfo.Column("firstDayOfMonth", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAppSettings.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAppSettings: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(0)
        val _indicesAppSettings: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(0)
        val _infoAppSettings: TableInfo = TableInfo("app_settings", _columnsAppSettings,
            _foreignKeysAppSettings, _indicesAppSettings)
        val _existingAppSettings: TableInfo = read(db, "app_settings")
        if (!_infoAppSettings.equals(_existingAppSettings)) {
          return RoomOpenHelper.ValidationResult(false, """
              |app_settings(com.example.ringapp.data.local.entities.AppSettingsEntity).
              | Expected:
              |""".trimMargin() + _infoAppSettings + """
              |
              | Found:
              |""".trimMargin() + _existingAppSettings)
        }
        val _columnsHabitSchedules: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(11)
        _columnsHabitSchedules.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitSchedules.put("habitId", TableInfo.Column("habitId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitSchedules.put("scheduleType", TableInfo.Column("scheduleType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitSchedules.put("interval", TableInfo.Column("interval", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitSchedules.put("daysOfWeek", TableInfo.Column("daysOfWeek", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitSchedules.put("dayOfMonth", TableInfo.Column("dayOfMonth", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitSchedules.put("month", TableInfo.Column("month", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitSchedules.put("dayOfYear", TableInfo.Column("dayOfYear", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitSchedules.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitSchedules.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHabitSchedules.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHabitSchedules: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysHabitSchedules.add(TableInfo.ForeignKey("habit", "CASCADE", "NO ACTION",
            listOf("habitId"), listOf("id")))
        val _indicesHabitSchedules: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesHabitSchedules.add(TableInfo.Index("index_habit_schedules_habitId", false,
            listOf("habitId"), listOf("ASC")))
        val _infoHabitSchedules: TableInfo = TableInfo("habit_schedules", _columnsHabitSchedules,
            _foreignKeysHabitSchedules, _indicesHabitSchedules)
        val _existingHabitSchedules: TableInfo = read(db, "habit_schedules")
        if (!_infoHabitSchedules.equals(_existingHabitSchedules)) {
          return RoomOpenHelper.ValidationResult(false, """
              |habit_schedules(com.example.ringapp.data.local.entities.HabitScheduleEntity).
              | Expected:
              |""".trimMargin() + _infoHabitSchedules + """
              |
              | Found:
              |""".trimMargin() + _existingHabitSchedules)
        }
        val _columnsTaskReminders: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(8)
        _columnsTaskReminders.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskReminders.put("taskId", TableInfo.Column("taskId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskReminders.put("triggerAt", TableInfo.Column("triggerAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskReminders.put("reminderType", TableInfo.Column("reminderType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskReminders.put("isEnabled", TableInfo.Column("isEnabled", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskReminders.put("alarmId", TableInfo.Column("alarmId", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskReminders.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTaskReminders.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTaskReminders: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysTaskReminders.add(TableInfo.ForeignKey("task", "CASCADE", "NO ACTION",
            listOf("taskId"), listOf("id")))
        val _indicesTaskReminders: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(2)
        _indicesTaskReminders.add(TableInfo.Index("index_task_reminders_taskId", false,
            listOf("taskId"), listOf("ASC")))
        _indicesTaskReminders.add(TableInfo.Index("index_task_reminders_triggerAt", false,
            listOf("triggerAt"), listOf("ASC")))
        val _infoTaskReminders: TableInfo = TableInfo("task_reminders", _columnsTaskReminders,
            _foreignKeysTaskReminders, _indicesTaskReminders)
        val _existingTaskReminders: TableInfo = read(db, "task_reminders")
        if (!_infoTaskReminders.equals(_existingTaskReminders)) {
          return RoomOpenHelper.ValidationResult(false, """
              |task_reminders(com.example.ringapp.data.local.entities.TaskReminderEntity).
              | Expected:
              |""".trimMargin() + _infoTaskReminders + """
              |
              | Found:
              |""".trimMargin() + _existingTaskReminders)
        }
        val _columnsUserAchievements: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(5)
        _columnsUserAchievements.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserAchievements.put("achievementId", TableInfo.Column("achievementId", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserAchievements.put("unlockedAt", TableInfo.Column("unlockedAt", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserAchievements.put("progress", TableInfo.Column("progress", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserAchievements.put("rewardTransactionId", TableInfo.Column("rewardTransactionId",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUserAchievements: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysUserAchievements.add(TableInfo.ForeignKey("achievement", "CASCADE", "NO ACTION",
            listOf("achievementId"), listOf("id")))
        val _indicesUserAchievements: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesUserAchievements.add(TableInfo.Index("index_user_achievements_achievementId", true,
            listOf("achievementId"), listOf("ASC")))
        val _infoUserAchievements: TableInfo = TableInfo("user_achievements",
            _columnsUserAchievements, _foreignKeysUserAchievements, _indicesUserAchievements)
        val _existingUserAchievements: TableInfo = read(db, "user_achievements")
        if (!_infoUserAchievements.equals(_existingUserAchievements)) {
          return RoomOpenHelper.ValidationResult(false, """
              |user_achievements(com.example.ringapp.data.local.entities.UserAchievementEntity).
              | Expected:
              |""".trimMargin() + _infoUserAchievements + """
              |
              | Found:
              |""".trimMargin() + _existingUserAchievements)
        }
        val _columnsAnalyticsDaily: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(14)
        _columnsAnalyticsDaily.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("date", TableInfo.Column("date", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("habitScore", TableInfo.Column("habitScore", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("taskScore", TableInfo.Column("taskScore", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("consistencyScore", TableInfo.Column("consistencyScore",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("streakScore", TableInfo.Column("streakScore", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("productivityScore", TableInfo.Column("productivityScore",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("completionRate", TableInfo.Column("completionRate", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("habitCompletions", TableInfo.Column("habitCompletions",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("taskCompletions", TableInfo.Column("taskCompletions", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("activeHabits", TableInfo.Column("activeHabits", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("activeTasks", TableInfo.Column("activeTasks", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAnalyticsDaily.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAnalyticsDaily: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(0)
        val _indicesAnalyticsDaily: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesAnalyticsDaily.add(TableInfo.Index("index_analytics_daily_date", true,
            listOf("date"), listOf("ASC")))
        val _infoAnalyticsDaily: TableInfo = TableInfo("analytics_daily", _columnsAnalyticsDaily,
            _foreignKeysAnalyticsDaily, _indicesAnalyticsDaily)
        val _existingAnalyticsDaily: TableInfo = read(db, "analytics_daily")
        if (!_infoAnalyticsDaily.equals(_existingAnalyticsDaily)) {
          return RoomOpenHelper.ValidationResult(false, """
              |analytics_daily(com.example.ringapp.data.local.entities.AnalyticsDailyEntity).
              | Expected:
              |""".trimMargin() + _infoAnalyticsDaily + """
              |
              | Found:
              |""".trimMargin() + _existingAnalyticsDaily)
        }
        return RoomOpenHelper.ValidationResult(true, null)
      }
    }, "d4cf7cbc5876f6165cb4e3a9f9d5e7b6", "49ea1b342e8dd04b2e7498ae53c7b383")
    val _sqliteConfig: SupportSQLiteOpenHelper.Configuration =
        SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build()
    val _helper: SupportSQLiteOpenHelper = config.sqliteOpenHelperFactory.create(_sqliteConfig)
    return _helper
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: HashMap<String, String> = HashMap<String, String>(0)
    val _viewTables: HashMap<String, Set<String>> = HashMap<String, Set<String>>(0)
    return InvalidationTracker(this, _shadowTablesMap, _viewTables,
        "habit","habit_progress","streak_cycle","streak_milestone","task","point_transaction","user_progress","profile","category","achievement","notification","backup_metadata","app_settings","habit_schedules","task_reminders","user_achievements","analytics_daily")
  }

  public override fun clearAllTables() {
    super.assertNotMainThread()
    val _db: SupportSQLiteDatabase = super.openHelper.writableDatabase
    val _supportsDeferForeignKeys: Boolean = android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.LOLLIPOP
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE")
      }
      super.beginTransaction()
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE")
      }
      _db.execSQL("DELETE FROM `habit`")
      _db.execSQL("DELETE FROM `habit_progress`")
      _db.execSQL("DELETE FROM `streak_cycle`")
      _db.execSQL("DELETE FROM `streak_milestone`")
      _db.execSQL("DELETE FROM `task`")
      _db.execSQL("DELETE FROM `point_transaction`")
      _db.execSQL("DELETE FROM `user_progress`")
      _db.execSQL("DELETE FROM `profile`")
      _db.execSQL("DELETE FROM `category`")
      _db.execSQL("DELETE FROM `achievement`")
      _db.execSQL("DELETE FROM `notification`")
      _db.execSQL("DELETE FROM `backup_metadata`")
      _db.execSQL("DELETE FROM `app_settings`")
      _db.execSQL("DELETE FROM `habit_schedules`")
      _db.execSQL("DELETE FROM `task_reminders`")
      _db.execSQL("DELETE FROM `user_achievements`")
      _db.execSQL("DELETE FROM `analytics_daily`")
      super.setTransactionSuccessful()
    } finally {
      super.endTransaction()
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE")
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close()
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM")
      }
    }
  }

  protected override fun getRequiredTypeConverters(): Map<Class<out Any>, List<Class<out Any>>> {
    val _typeConvertersMap: HashMap<Class<out Any>, List<Class<out Any>>> =
        HashMap<Class<out Any>, List<Class<out Any>>>()
    _typeConvertersMap.put(HabitDao::class.java, HabitDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TaskDao::class.java, TaskDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CategoryDao::class.java, CategoryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PointTransactionDao::class.java,
        PointTransactionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ProfileDao::class.java, ProfileDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(UserProgressDao::class.java,
        UserProgressDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(AchievementDao::class.java, AchievementDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(AppSettingsDao::class.java, AppSettingsDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(HabitScheduleDao::class.java,
        HabitScheduleDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TaskReminderDao::class.java,
        TaskReminderDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(UserAchievementDao::class.java,
        UserAchievementDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(AnalyticsDailyDao::class.java,
        AnalyticsDailyDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecs(): Set<Class<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: HashSet<Class<out AutoMigrationSpec>> =
        HashSet<Class<out AutoMigrationSpec>>()
    return _autoMigrationSpecsSet
  }

  public override
      fun getAutoMigrations(autoMigrationSpecs: Map<Class<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = ArrayList<Migration>()
    return _autoMigrations
  }

  public override fun habitDao(): HabitDao = _habitDao.value

  public override fun taskDao(): TaskDao = _taskDao.value

  public override fun categoryDao(): CategoryDao = _categoryDao.value

  public override fun pointTransactionDao(): PointTransactionDao = _pointTransactionDao.value

  public override fun profileDao(): ProfileDao = _profileDao.value

  public override fun userProgressDao(): UserProgressDao = _userProgressDao.value

  public override fun achievementDao(): AchievementDao = _achievementDao.value

  public override fun appSettingsDao(): AppSettingsDao = _appSettingsDao.value

  public override fun habitScheduleDao(): HabitScheduleDao = _habitScheduleDao.value

  public override fun taskReminderDao(): TaskReminderDao = _taskReminderDao.value

  public override fun userAchievementDao(): UserAchievementDao = _userAchievementDao.value

  public override fun analyticsDailyDao(): AnalyticsDailyDao = _analyticsDailyDao.value
}
