package com.example.ringapp.`data`.local.dao

import android.database.Cursor
import androidx.room.CoroutinesRoom
import androidx.room.EntityDeletionOrUpdateAdapter
import androidx.room.EntityInsertionAdapter
import androidx.room.RoomDatabase
import androidx.room.RoomSQLiteQuery
import androidx.room.RoomSQLiteQuery.Companion.acquire
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteStatement
import com.example.ringapp.`data`.db.Converters
import com.example.ringapp.`data`.local.entities.HabitScheduleEntity
import com.example.ringapp.`data`.local.entities.ScheduleType
import java.lang.Class
import java.util.ArrayList
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.jvm.JvmStatic
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class HabitScheduleDao_Impl(
  __db: RoomDatabase,
) : HabitScheduleDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfHabitScheduleEntity: EntityInsertionAdapter<HabitScheduleEntity>

  private val __converters: Converters = Converters()

  private val __updateAdapterOfHabitScheduleEntity:
      EntityDeletionOrUpdateAdapter<HabitScheduleEntity>
  init {
    this.__db = __db
    this.__insertionAdapterOfHabitScheduleEntity = object :
        EntityInsertionAdapter<HabitScheduleEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `habit_schedules` (`id`,`habitId`,`scheduleType`,`interval`,`daysOfWeek`,`dayOfMonth`,`month`,`dayOfYear`,`isActive`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: HabitScheduleEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.habitId)
        val _tmp: String = __converters.scheduleTypeToString(entity.scheduleType)
        statement.bindString(3, _tmp)
        statement.bindLong(4, entity.interval.toLong())
        val _tmpDaysOfWeek: String? = entity.daysOfWeek
        if (_tmpDaysOfWeek == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpDaysOfWeek)
        }
        val _tmpDayOfMonth: Int? = entity.dayOfMonth
        if (_tmpDayOfMonth == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpDayOfMonth.toLong())
        }
        val _tmpMonth: Int? = entity.month
        if (_tmpMonth == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpMonth.toLong())
        }
        val _tmpDayOfYear: Int? = entity.dayOfYear
        if (_tmpDayOfYear == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpDayOfYear.toLong())
        }
        val _tmp_1: Int = if (entity.isActive) 1 else 0
        statement.bindLong(9, _tmp_1.toLong())
        statement.bindLong(10, entity.createdAt)
        statement.bindLong(11, entity.updatedAt)
      }
    }
    this.__updateAdapterOfHabitScheduleEntity = object :
        EntityDeletionOrUpdateAdapter<HabitScheduleEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `habit_schedules` SET `id` = ?,`habitId` = ?,`scheduleType` = ?,`interval` = ?,`daysOfWeek` = ?,`dayOfMonth` = ?,`month` = ?,`dayOfYear` = ?,`isActive` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: HabitScheduleEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.habitId)
        val _tmp: String = __converters.scheduleTypeToString(entity.scheduleType)
        statement.bindString(3, _tmp)
        statement.bindLong(4, entity.interval.toLong())
        val _tmpDaysOfWeek: String? = entity.daysOfWeek
        if (_tmpDaysOfWeek == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpDaysOfWeek)
        }
        val _tmpDayOfMonth: Int? = entity.dayOfMonth
        if (_tmpDayOfMonth == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpDayOfMonth.toLong())
        }
        val _tmpMonth: Int? = entity.month
        if (_tmpMonth == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpMonth.toLong())
        }
        val _tmpDayOfYear: Int? = entity.dayOfYear
        if (_tmpDayOfYear == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpDayOfYear.toLong())
        }
        val _tmp_1: Int = if (entity.isActive) 1 else 0
        statement.bindLong(9, _tmp_1.toLong())
        statement.bindLong(10, entity.createdAt)
        statement.bindLong(11, entity.updatedAt)
        statement.bindLong(12, entity.id)
      }
    }
  }

  public override suspend fun upsert(schedule: HabitScheduleEntity): Long =
      CoroutinesRoom.execute(__db, true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfHabitScheduleEntity.insertAndReturnId(schedule)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(schedule: HabitScheduleEntity): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __updateAdapterOfHabitScheduleEntity.handle(schedule)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override fun observeActive(habitId: Long): Flow<List<HabitScheduleEntity>> {
    val _sql: String = "SELECT * FROM habit_schedules WHERE habitId = ? AND isActive = 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, habitId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("habit_schedules"), object :
        Callable<List<HabitScheduleEntity>> {
      public override fun call(): List<HabitScheduleEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfHabitId: Int = getColumnIndexOrThrow(_cursor, "habitId")
          val _cursorIndexOfScheduleType: Int = getColumnIndexOrThrow(_cursor, "scheduleType")
          val _cursorIndexOfInterval: Int = getColumnIndexOrThrow(_cursor, "interval")
          val _cursorIndexOfDaysOfWeek: Int = getColumnIndexOrThrow(_cursor, "daysOfWeek")
          val _cursorIndexOfDayOfMonth: Int = getColumnIndexOrThrow(_cursor, "dayOfMonth")
          val _cursorIndexOfMonth: Int = getColumnIndexOrThrow(_cursor, "month")
          val _cursorIndexOfDayOfYear: Int = getColumnIndexOrThrow(_cursor, "dayOfYear")
          val _cursorIndexOfIsActive: Int = getColumnIndexOrThrow(_cursor, "isActive")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: MutableList<HabitScheduleEntity> =
              ArrayList<HabitScheduleEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: HabitScheduleEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpHabitId: Long
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId)
            val _tmpScheduleType: ScheduleType
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfScheduleType)
            _tmpScheduleType = __converters.stringToScheduleType(_tmp)
            val _tmpInterval: Int
            _tmpInterval = _cursor.getInt(_cursorIndexOfInterval)
            val _tmpDaysOfWeek: String?
            if (_cursor.isNull(_cursorIndexOfDaysOfWeek)) {
              _tmpDaysOfWeek = null
            } else {
              _tmpDaysOfWeek = _cursor.getString(_cursorIndexOfDaysOfWeek)
            }
            val _tmpDayOfMonth: Int?
            if (_cursor.isNull(_cursorIndexOfDayOfMonth)) {
              _tmpDayOfMonth = null
            } else {
              _tmpDayOfMonth = _cursor.getInt(_cursorIndexOfDayOfMonth)
            }
            val _tmpMonth: Int?
            if (_cursor.isNull(_cursorIndexOfMonth)) {
              _tmpMonth = null
            } else {
              _tmpMonth = _cursor.getInt(_cursorIndexOfMonth)
            }
            val _tmpDayOfYear: Int?
            if (_cursor.isNull(_cursorIndexOfDayOfYear)) {
              _tmpDayOfYear = null
            } else {
              _tmpDayOfYear = _cursor.getInt(_cursorIndexOfDayOfYear)
            }
            val _tmpIsActive: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive)
            _tmpIsActive = _tmp_1 != 0
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _item =
                HabitScheduleEntity(_tmpId,_tmpHabitId,_tmpScheduleType,_tmpInterval,_tmpDaysOfWeek,_tmpDayOfMonth,_tmpMonth,_tmpDayOfYear,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt)
            _result.add(_item)
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
