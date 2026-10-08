package com.example.ringapp.`data`.local.dao

import android.database.Cursor
import androidx.room.CoroutinesRoom
import androidx.room.EntityInsertionAdapter
import androidx.room.RoomDatabase
import androidx.room.RoomSQLiteQuery
import androidx.room.RoomSQLiteQuery.Companion.acquire
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteStatement
import com.example.ringapp.`data`.local.entities.AppSettingsEntity
import java.lang.Class
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.jvm.JvmStatic
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class AppSettingsDao_Impl(
  __db: RoomDatabase,
) : AppSettingsDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfAppSettingsEntity: EntityInsertionAdapter<AppSettingsEntity>
  init {
    this.__db = __db
    this.__insertionAdapterOfAppSettingsEntity = object :
        EntityInsertionAdapter<AppSettingsEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `app_settings` (`id`,`onboardingCompleted`,`notificationsEnabled`,`taskRemindersEnabled`,`habitRemindersEnabled`,`motivationEnabled`,`achievementNotificationsEnabled`,`streakNotificationsEnabled`,`morningMotivationTime`,`eveningMotivationTime`,`weekStartsOn`,`firstDayOfMonth`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: AppSettingsEntity) {
        statement.bindLong(1, entity.id.toLong())
        val _tmp: Int = if (entity.onboardingCompleted) 1 else 0
        statement.bindLong(2, _tmp.toLong())
        val _tmp_1: Int = if (entity.notificationsEnabled) 1 else 0
        statement.bindLong(3, _tmp_1.toLong())
        val _tmp_2: Int = if (entity.taskRemindersEnabled) 1 else 0
        statement.bindLong(4, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.habitRemindersEnabled) 1 else 0
        statement.bindLong(5, _tmp_3.toLong())
        val _tmp_4: Int = if (entity.motivationEnabled) 1 else 0
        statement.bindLong(6, _tmp_4.toLong())
        val _tmp_5: Int = if (entity.achievementNotificationsEnabled) 1 else 0
        statement.bindLong(7, _tmp_5.toLong())
        val _tmp_6: Int = if (entity.streakNotificationsEnabled) 1 else 0
        statement.bindLong(8, _tmp_6.toLong())
        val _tmpMorningMotivationTime: Long? = entity.morningMotivationTime
        if (_tmpMorningMotivationTime == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpMorningMotivationTime)
        }
        val _tmpEveningMotivationTime: Long? = entity.eveningMotivationTime
        if (_tmpEveningMotivationTime == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpEveningMotivationTime)
        }
        statement.bindLong(11, entity.weekStartsOn.toLong())
        statement.bindLong(12, entity.firstDayOfMonth.toLong())
        statement.bindLong(13, entity.updatedAt)
      }
    }
  }

  public override suspend fun upsert(settings: AppSettingsEntity): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __insertionAdapterOfAppSettingsEntity.insert(settings)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override fun observe(): Flow<AppSettingsEntity?> {
    val _sql: String = "SELECT * FROM app_settings WHERE id = 1 LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("app_settings"), object :
        Callable<AppSettingsEntity?> {
      public override fun call(): AppSettingsEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfOnboardingCompleted: Int = getColumnIndexOrThrow(_cursor,
              "onboardingCompleted")
          val _cursorIndexOfNotificationsEnabled: Int = getColumnIndexOrThrow(_cursor,
              "notificationsEnabled")
          val _cursorIndexOfTaskRemindersEnabled: Int = getColumnIndexOrThrow(_cursor,
              "taskRemindersEnabled")
          val _cursorIndexOfHabitRemindersEnabled: Int = getColumnIndexOrThrow(_cursor,
              "habitRemindersEnabled")
          val _cursorIndexOfMotivationEnabled: Int = getColumnIndexOrThrow(_cursor,
              "motivationEnabled")
          val _cursorIndexOfAchievementNotificationsEnabled: Int = getColumnIndexOrThrow(_cursor,
              "achievementNotificationsEnabled")
          val _cursorIndexOfStreakNotificationsEnabled: Int = getColumnIndexOrThrow(_cursor,
              "streakNotificationsEnabled")
          val _cursorIndexOfMorningMotivationTime: Int = getColumnIndexOrThrow(_cursor,
              "morningMotivationTime")
          val _cursorIndexOfEveningMotivationTime: Int = getColumnIndexOrThrow(_cursor,
              "eveningMotivationTime")
          val _cursorIndexOfWeekStartsOn: Int = getColumnIndexOrThrow(_cursor, "weekStartsOn")
          val _cursorIndexOfFirstDayOfMonth: Int = getColumnIndexOrThrow(_cursor, "firstDayOfMonth")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: AppSettingsEntity?
          if (_cursor.moveToFirst()) {
            val _tmpId: Int
            _tmpId = _cursor.getInt(_cursorIndexOfId)
            val _tmpOnboardingCompleted: Boolean
            val _tmp: Int
            _tmp = _cursor.getInt(_cursorIndexOfOnboardingCompleted)
            _tmpOnboardingCompleted = _tmp != 0
            val _tmpNotificationsEnabled: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfNotificationsEnabled)
            _tmpNotificationsEnabled = _tmp_1 != 0
            val _tmpTaskRemindersEnabled: Boolean
            val _tmp_2: Int
            _tmp_2 = _cursor.getInt(_cursorIndexOfTaskRemindersEnabled)
            _tmpTaskRemindersEnabled = _tmp_2 != 0
            val _tmpHabitRemindersEnabled: Boolean
            val _tmp_3: Int
            _tmp_3 = _cursor.getInt(_cursorIndexOfHabitRemindersEnabled)
            _tmpHabitRemindersEnabled = _tmp_3 != 0
            val _tmpMotivationEnabled: Boolean
            val _tmp_4: Int
            _tmp_4 = _cursor.getInt(_cursorIndexOfMotivationEnabled)
            _tmpMotivationEnabled = _tmp_4 != 0
            val _tmpAchievementNotificationsEnabled: Boolean
            val _tmp_5: Int
            _tmp_5 = _cursor.getInt(_cursorIndexOfAchievementNotificationsEnabled)
            _tmpAchievementNotificationsEnabled = _tmp_5 != 0
            val _tmpStreakNotificationsEnabled: Boolean
            val _tmp_6: Int
            _tmp_6 = _cursor.getInt(_cursorIndexOfStreakNotificationsEnabled)
            _tmpStreakNotificationsEnabled = _tmp_6 != 0
            val _tmpMorningMotivationTime: Long?
            if (_cursor.isNull(_cursorIndexOfMorningMotivationTime)) {
              _tmpMorningMotivationTime = null
            } else {
              _tmpMorningMotivationTime = _cursor.getLong(_cursorIndexOfMorningMotivationTime)
            }
            val _tmpEveningMotivationTime: Long?
            if (_cursor.isNull(_cursorIndexOfEveningMotivationTime)) {
              _tmpEveningMotivationTime = null
            } else {
              _tmpEveningMotivationTime = _cursor.getLong(_cursorIndexOfEveningMotivationTime)
            }
            val _tmpWeekStartsOn: Int
            _tmpWeekStartsOn = _cursor.getInt(_cursorIndexOfWeekStartsOn)
            val _tmpFirstDayOfMonth: Int
            _tmpFirstDayOfMonth = _cursor.getInt(_cursorIndexOfFirstDayOfMonth)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _result =
                AppSettingsEntity(_tmpId,_tmpOnboardingCompleted,_tmpNotificationsEnabled,_tmpTaskRemindersEnabled,_tmpHabitRemindersEnabled,_tmpMotivationEnabled,_tmpAchievementNotificationsEnabled,_tmpStreakNotificationsEnabled,_tmpMorningMotivationTime,_tmpEveningMotivationTime,_tmpWeekStartsOn,_tmpFirstDayOfMonth,_tmpUpdatedAt)
          } else {
            _result = null
          }
          return _result
        } finally {
          _cursor.close()
        }
      }

      protected fun finalize() {
        _statement.release()
      }
    })
  }

  public companion object {
    @JvmStatic
    public fun getRequiredConverters(): List<Class<*>> = emptyList()
  }
}
