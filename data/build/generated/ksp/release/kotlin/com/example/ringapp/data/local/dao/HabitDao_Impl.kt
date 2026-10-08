package com.example.ringapp.`data`.local.dao

import android.database.Cursor
import android.os.CancellationSignal
import androidx.room.CoroutinesRoom
import androidx.room.CoroutinesRoom.Companion.execute
import androidx.room.EntityDeletionOrUpdateAdapter
import androidx.room.EntityInsertionAdapter
import androidx.room.RoomDatabase
import androidx.room.RoomSQLiteQuery
import androidx.room.RoomSQLiteQuery.Companion.acquire
import androidx.room.SharedSQLiteStatement
import androidx.room.util.createCancellationSignal
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteStatement
import com.example.ringapp.`data`.db.Converters
import com.example.ringapp.`data`.local.entities.HabitEntity
import com.example.ringapp.`data`.local.entities.HabitProgressEntity
import com.example.ringapp.`data`.local.entities.HabitType
import com.example.ringapp.`data`.local.entities.ScheduleType
import java.lang.Class
import java.util.ArrayList
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
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
public class HabitDao_Impl(
  __db: RoomDatabase,
) : HabitDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfHabitEntity: EntityInsertionAdapter<HabitEntity>

  private val __converters: Converters = Converters()

  private val __insertionAdapterOfHabitProgressEntity: EntityInsertionAdapter<HabitProgressEntity>

  private val __updateAdapterOfHabitEntity: EntityDeletionOrUpdateAdapter<HabitEntity>

  private val __preparedStmtOfSoftDelete: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfHabitEntity = object : EntityInsertionAdapter<HabitEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `habit` (`id`,`name`,`description`,`categoryId`,`type`,`target`,`unit`,`scheduleType`,`scheduleDays`,`startDate`,`currentStreak`,`bestStreak`,`totalCompletions`,`color`,`deletedAt`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: HabitEntity) {
        statement.bindLong(1, entity.id)
        statement.bindString(2, entity.name)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(3)
        } else {
          statement.bindString(3, _tmpDescription)
        }
        val _tmpCategoryId: Long? = entity.categoryId
        if (_tmpCategoryId == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpCategoryId)
        }
        val _tmp: String = __converters.habitTypeToString(entity.type)
        statement.bindString(5, _tmp)
        statement.bindDouble(6, entity.target)
        val _tmpUnit: String? = entity.unit
        if (_tmpUnit == null) {
          statement.bindNull(7)
        } else {
          statement.bindString(7, _tmpUnit)
        }
        val _tmp_1: String = __converters.scheduleTypeToString(entity.scheduleType)
        statement.bindString(8, _tmp_1)
        val _tmpScheduleDays: List<Int>? = entity.scheduleDays
        val _tmp_2: String? = __converters.fromIntList(_tmpScheduleDays)
        if (_tmp_2 == null) {
          statement.bindNull(9)
        } else {
          statement.bindString(9, _tmp_2)
        }
        statement.bindLong(10, entity.startDate)
        statement.bindLong(11, entity.currentStreak.toLong())
        statement.bindLong(12, entity.bestStreak.toLong())
        statement.bindLong(13, entity.totalCompletions.toLong())
        statement.bindLong(14, entity.color.toLong())
        val _tmpDeletedAt: Long? = entity.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpDeletedAt)
        }
        statement.bindLong(16, entity.createdAt)
        statement.bindLong(17, entity.updatedAt)
      }
    }
    this.__insertionAdapterOfHabitProgressEntity = object :
        EntityInsertionAdapter<HabitProgressEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `habit_progress` (`id`,`habitId`,`date`,`target`,`actual`,`percentage`,`completed`,`note`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: HabitProgressEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.habitId)
        statement.bindLong(3, entity.date)
        statement.bindDouble(4, entity.target)
        statement.bindDouble(5, entity.actual)
        statement.bindLong(6, entity.percentage.toLong())
        val _tmp: Int = if (entity.completed) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(8)
        } else {
          statement.bindString(8, _tmpNote)
        }
        statement.bindLong(9, entity.createdAt)
        statement.bindLong(10, entity.updatedAt)
      }
    }
    this.__updateAdapterOfHabitEntity = object : EntityDeletionOrUpdateAdapter<HabitEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `habit` SET `id` = ?,`name` = ?,`description` = ?,`categoryId` = ?,`type` = ?,`target` = ?,`unit` = ?,`scheduleType` = ?,`scheduleDays` = ?,`startDate` = ?,`currentStreak` = ?,`bestStreak` = ?,`totalCompletions` = ?,`color` = ?,`deletedAt` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: HabitEntity) {
        statement.bindLong(1, entity.id)
        statement.bindString(2, entity.name)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(3)
        } else {
          statement.bindString(3, _tmpDescription)
        }
        val _tmpCategoryId: Long? = entity.categoryId
        if (_tmpCategoryId == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpCategoryId)
        }
        val _tmp: String = __converters.habitTypeToString(entity.type)
        statement.bindString(5, _tmp)
        statement.bindDouble(6, entity.target)
        val _tmpUnit: String? = entity.unit
        if (_tmpUnit == null) {
          statement.bindNull(7)
        } else {
          statement.bindString(7, _tmpUnit)
        }
        val _tmp_1: String = __converters.scheduleTypeToString(entity.scheduleType)
        statement.bindString(8, _tmp_1)
        val _tmpScheduleDays: List<Int>? = entity.scheduleDays
        val _tmp_2: String? = __converters.fromIntList(_tmpScheduleDays)
        if (_tmp_2 == null) {
          statement.bindNull(9)
        } else {
          statement.bindString(9, _tmp_2)
        }
        statement.bindLong(10, entity.startDate)
        statement.bindLong(11, entity.currentStreak.toLong())
        statement.bindLong(12, entity.bestStreak.toLong())
        statement.bindLong(13, entity.totalCompletions.toLong())
        statement.bindLong(14, entity.color.toLong())
        val _tmpDeletedAt: Long? = entity.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpDeletedAt)
        }
        statement.bindLong(16, entity.createdAt)
        statement.bindLong(17, entity.updatedAt)
        statement.bindLong(18, entity.id)
      }
    }
    this.__preparedStmtOfSoftDelete = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "UPDATE habit SET deletedAt = ?, updatedAt = ? WHERE id = ?"
        return _query
      }
    }
  }

  public override suspend fun insert(habit: HabitEntity): Long = CoroutinesRoom.execute(__db, true,
      object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfHabitEntity.insertAndReturnId(habit)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun upsertProgress(progress: HabitProgressEntity): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __insertionAdapterOfHabitProgressEntity.insert(progress)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(habit: HabitEntity): Unit = CoroutinesRoom.execute(__db, true,
      object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __updateAdapterOfHabitEntity.handle(habit)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun softDelete(habitId: Long, deletedAt: Long): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfSoftDelete.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, deletedAt)
      _argIndex = 2
      _stmt.bindLong(_argIndex, deletedAt)
      _argIndex = 3
      _stmt.bindLong(_argIndex, habitId)
      try {
        __db.beginTransaction()
        try {
          _stmt.executeUpdateDelete()
          __db.setTransactionSuccessful()
        } finally {
          __db.endTransaction()
        }
      } finally {
        __preparedStmtOfSoftDelete.release(_stmt)
      }
    }
  })

  public override fun observeActive(): Flow<List<HabitEntity>> {
    val _sql: String = "SELECT * FROM habit WHERE deletedAt IS NULL ORDER BY name"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("habit"), object :
        Callable<List<HabitEntity>> {
      public override fun call(): List<HabitEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfCategoryId: Int = getColumnIndexOrThrow(_cursor, "categoryId")
          val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
          val _cursorIndexOfTarget: Int = getColumnIndexOrThrow(_cursor, "target")
          val _cursorIndexOfUnit: Int = getColumnIndexOrThrow(_cursor, "unit")
          val _cursorIndexOfScheduleType: Int = getColumnIndexOrThrow(_cursor, "scheduleType")
          val _cursorIndexOfScheduleDays: Int = getColumnIndexOrThrow(_cursor, "scheduleDays")
          val _cursorIndexOfStartDate: Int = getColumnIndexOrThrow(_cursor, "startDate")
          val _cursorIndexOfCurrentStreak: Int = getColumnIndexOrThrow(_cursor, "currentStreak")
          val _cursorIndexOfBestStreak: Int = getColumnIndexOrThrow(_cursor, "bestStreak")
          val _cursorIndexOfTotalCompletions: Int = getColumnIndexOrThrow(_cursor,
              "totalCompletions")
          val _cursorIndexOfColor: Int = getColumnIndexOrThrow(_cursor, "color")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: MutableList<HabitEntity> = ArrayList<HabitEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: HabitEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpDescription: String?
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            }
            val _tmpCategoryId: Long?
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId)
            }
            val _tmpType: HabitType
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfType)
            _tmpType = __converters.stringToHabitType(_tmp)
            val _tmpTarget: Double
            _tmpTarget = _cursor.getDouble(_cursorIndexOfTarget)
            val _tmpUnit: String?
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit)
            }
            val _tmpScheduleType: ScheduleType
            val _tmp_1: String
            _tmp_1 = _cursor.getString(_cursorIndexOfScheduleType)
            _tmpScheduleType = __converters.stringToScheduleType(_tmp_1)
            val _tmpScheduleDays: List<Int>?
            val _tmp_2: String?
            if (_cursor.isNull(_cursorIndexOfScheduleDays)) {
              _tmp_2 = null
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfScheduleDays)
            }
            _tmpScheduleDays = __converters.toIntList(_tmp_2)
            val _tmpStartDate: Long
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate)
            val _tmpCurrentStreak: Int
            _tmpCurrentStreak = _cursor.getInt(_cursorIndexOfCurrentStreak)
            val _tmpBestStreak: Int
            _tmpBestStreak = _cursor.getInt(_cursorIndexOfBestStreak)
            val _tmpTotalCompletions: Int
            _tmpTotalCompletions = _cursor.getInt(_cursorIndexOfTotalCompletions)
            val _tmpColor: Int
            _tmpColor = _cursor.getInt(_cursorIndexOfColor)
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _item =
                HabitEntity(_tmpId,_tmpName,_tmpDescription,_tmpCategoryId,_tmpType,_tmpTarget,_tmpUnit,_tmpScheduleType,_tmpScheduleDays,_tmpStartDate,_tmpCurrentStreak,_tmpBestStreak,_tmpTotalCompletions,_tmpColor,_tmpDeletedAt,_tmpCreatedAt,_tmpUpdatedAt)
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

  public override fun observeById(habitId: Long): Flow<HabitEntity?> {
    val _sql: String = "SELECT * FROM habit WHERE id = ? LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, habitId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("habit"), object : Callable<HabitEntity?>
        {
      public override fun call(): HabitEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfCategoryId: Int = getColumnIndexOrThrow(_cursor, "categoryId")
          val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
          val _cursorIndexOfTarget: Int = getColumnIndexOrThrow(_cursor, "target")
          val _cursorIndexOfUnit: Int = getColumnIndexOrThrow(_cursor, "unit")
          val _cursorIndexOfScheduleType: Int = getColumnIndexOrThrow(_cursor, "scheduleType")
          val _cursorIndexOfScheduleDays: Int = getColumnIndexOrThrow(_cursor, "scheduleDays")
          val _cursorIndexOfStartDate: Int = getColumnIndexOrThrow(_cursor, "startDate")
          val _cursorIndexOfCurrentStreak: Int = getColumnIndexOrThrow(_cursor, "currentStreak")
          val _cursorIndexOfBestStreak: Int = getColumnIndexOrThrow(_cursor, "bestStreak")
          val _cursorIndexOfTotalCompletions: Int = getColumnIndexOrThrow(_cursor,
              "totalCompletions")
          val _cursorIndexOfColor: Int = getColumnIndexOrThrow(_cursor, "color")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: HabitEntity?
          if (_cursor.moveToFirst()) {
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpDescription: String?
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            }
            val _tmpCategoryId: Long?
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId)
            }
            val _tmpType: HabitType
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfType)
            _tmpType = __converters.stringToHabitType(_tmp)
            val _tmpTarget: Double
            _tmpTarget = _cursor.getDouble(_cursorIndexOfTarget)
            val _tmpUnit: String?
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit)
            }
            val _tmpScheduleType: ScheduleType
            val _tmp_1: String
            _tmp_1 = _cursor.getString(_cursorIndexOfScheduleType)
            _tmpScheduleType = __converters.stringToScheduleType(_tmp_1)
            val _tmpScheduleDays: List<Int>?
            val _tmp_2: String?
            if (_cursor.isNull(_cursorIndexOfScheduleDays)) {
              _tmp_2 = null
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfScheduleDays)
            }
            _tmpScheduleDays = __converters.toIntList(_tmp_2)
            val _tmpStartDate: Long
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate)
            val _tmpCurrentStreak: Int
            _tmpCurrentStreak = _cursor.getInt(_cursorIndexOfCurrentStreak)
            val _tmpBestStreak: Int
            _tmpBestStreak = _cursor.getInt(_cursorIndexOfBestStreak)
            val _tmpTotalCompletions: Int
            _tmpTotalCompletions = _cursor.getInt(_cursorIndexOfTotalCompletions)
            val _tmpColor: Int
            _tmpColor = _cursor.getInt(_cursorIndexOfColor)
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _result =
                HabitEntity(_tmpId,_tmpName,_tmpDescription,_tmpCategoryId,_tmpType,_tmpTarget,_tmpUnit,_tmpScheduleType,_tmpScheduleDays,_tmpStartDate,_tmpCurrentStreak,_tmpBestStreak,_tmpTotalCompletions,_tmpColor,_tmpDeletedAt,_tmpCreatedAt,_tmpUpdatedAt)
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

  public override fun observeProgress(
    habitId: Long,
    from: Long,
    to: Long,
  ): Flow<List<HabitProgressEntity>> {
    val _sql: String =
        "SELECT * FROM habit_progress WHERE habitId = ? AND date BETWEEN ? AND ? ORDER BY date"
    val _statement: RoomSQLiteQuery = acquire(_sql, 3)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, habitId)
    _argIndex = 2
    _statement.bindLong(_argIndex, from)
    _argIndex = 3
    _statement.bindLong(_argIndex, to)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("habit_progress"), object :
        Callable<List<HabitProgressEntity>> {
      public override fun call(): List<HabitProgressEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfHabitId: Int = getColumnIndexOrThrow(_cursor, "habitId")
          val _cursorIndexOfDate: Int = getColumnIndexOrThrow(_cursor, "date")
          val _cursorIndexOfTarget: Int = getColumnIndexOrThrow(_cursor, "target")
          val _cursorIndexOfActual: Int = getColumnIndexOrThrow(_cursor, "actual")
          val _cursorIndexOfPercentage: Int = getColumnIndexOrThrow(_cursor, "percentage")
          val _cursorIndexOfCompleted: Int = getColumnIndexOrThrow(_cursor, "completed")
          val _cursorIndexOfNote: Int = getColumnIndexOrThrow(_cursor, "note")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: MutableList<HabitProgressEntity> =
              ArrayList<HabitProgressEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: HabitProgressEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpHabitId: Long
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId)
            val _tmpDate: Long
            _tmpDate = _cursor.getLong(_cursorIndexOfDate)
            val _tmpTarget: Double
            _tmpTarget = _cursor.getDouble(_cursorIndexOfTarget)
            val _tmpActual: Double
            _tmpActual = _cursor.getDouble(_cursorIndexOfActual)
            val _tmpPercentage: Int
            _tmpPercentage = _cursor.getInt(_cursorIndexOfPercentage)
            val _tmpCompleted: Boolean
            val _tmp: Int
            _tmp = _cursor.getInt(_cursorIndexOfCompleted)
            _tmpCompleted = _tmp != 0
            val _tmpNote: String?
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _item =
                HabitProgressEntity(_tmpId,_tmpHabitId,_tmpDate,_tmpTarget,_tmpActual,_tmpPercentage,_tmpCompleted,_tmpNote,_tmpCreatedAt,_tmpUpdatedAt)
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

  public override fun observeProgressForDate(date: Long): Flow<List<HabitProgressEntity>> {
    val _sql: String = "SELECT * FROM habit_progress WHERE date = ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, date)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("habit_progress"), object :
        Callable<List<HabitProgressEntity>> {
      public override fun call(): List<HabitProgressEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfHabitId: Int = getColumnIndexOrThrow(_cursor, "habitId")
          val _cursorIndexOfDate: Int = getColumnIndexOrThrow(_cursor, "date")
          val _cursorIndexOfTarget: Int = getColumnIndexOrThrow(_cursor, "target")
          val _cursorIndexOfActual: Int = getColumnIndexOrThrow(_cursor, "actual")
          val _cursorIndexOfPercentage: Int = getColumnIndexOrThrow(_cursor, "percentage")
          val _cursorIndexOfCompleted: Int = getColumnIndexOrThrow(_cursor, "completed")
          val _cursorIndexOfNote: Int = getColumnIndexOrThrow(_cursor, "note")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: MutableList<HabitProgressEntity> =
              ArrayList<HabitProgressEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: HabitProgressEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpHabitId: Long
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId)
            val _tmpDate: Long
            _tmpDate = _cursor.getLong(_cursorIndexOfDate)
            val _tmpTarget: Double
            _tmpTarget = _cursor.getDouble(_cursorIndexOfTarget)
            val _tmpActual: Double
            _tmpActual = _cursor.getDouble(_cursorIndexOfActual)
            val _tmpPercentage: Int
            _tmpPercentage = _cursor.getInt(_cursorIndexOfPercentage)
            val _tmpCompleted: Boolean
            val _tmp: Int
            _tmp = _cursor.getInt(_cursorIndexOfCompleted)
            _tmpCompleted = _tmp != 0
            val _tmpNote: String?
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _item =
                HabitProgressEntity(_tmpId,_tmpHabitId,_tmpDate,_tmpTarget,_tmpActual,_tmpPercentage,_tmpCompleted,_tmpNote,_tmpCreatedAt,_tmpUpdatedAt)
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

  public override fun observeProgressRange(from: Long, to: Long): Flow<List<HabitProgressEntity>> {
    val _sql: String = "SELECT * FROM habit_progress WHERE date BETWEEN ? AND ? ORDER BY date"
    val _statement: RoomSQLiteQuery = acquire(_sql, 2)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, from)
    _argIndex = 2
    _statement.bindLong(_argIndex, to)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("habit_progress"), object :
        Callable<List<HabitProgressEntity>> {
      public override fun call(): List<HabitProgressEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfHabitId: Int = getColumnIndexOrThrow(_cursor, "habitId")
          val _cursorIndexOfDate: Int = getColumnIndexOrThrow(_cursor, "date")
          val _cursorIndexOfTarget: Int = getColumnIndexOrThrow(_cursor, "target")
          val _cursorIndexOfActual: Int = getColumnIndexOrThrow(_cursor, "actual")
          val _cursorIndexOfPercentage: Int = getColumnIndexOrThrow(_cursor, "percentage")
          val _cursorIndexOfCompleted: Int = getColumnIndexOrThrow(_cursor, "completed")
          val _cursorIndexOfNote: Int = getColumnIndexOrThrow(_cursor, "note")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: MutableList<HabitProgressEntity> =
              ArrayList<HabitProgressEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: HabitProgressEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpHabitId: Long
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId)
            val _tmpDate: Long
            _tmpDate = _cursor.getLong(_cursorIndexOfDate)
            val _tmpTarget: Double
            _tmpTarget = _cursor.getDouble(_cursorIndexOfTarget)
            val _tmpActual: Double
            _tmpActual = _cursor.getDouble(_cursorIndexOfActual)
            val _tmpPercentage: Int
            _tmpPercentage = _cursor.getInt(_cursorIndexOfPercentage)
            val _tmpCompleted: Boolean
            val _tmp: Int
            _tmp = _cursor.getInt(_cursorIndexOfCompleted)
            _tmpCompleted = _tmp != 0
            val _tmpNote: String?
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _item =
                HabitProgressEntity(_tmpId,_tmpHabitId,_tmpDate,_tmpTarget,_tmpActual,_tmpPercentage,_tmpCompleted,_tmpNote,_tmpCreatedAt,_tmpUpdatedAt)
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

  public override suspend fun progressForDate(habitId: Long, date: Long): HabitProgressEntity? {
    val _sql: String = "SELECT * FROM habit_progress WHERE habitId = ? AND date = ? LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 2)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, habitId)
    _argIndex = 2
    _statement.bindLong(_argIndex, date)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<HabitProgressEntity?> {
      public override fun call(): HabitProgressEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfHabitId: Int = getColumnIndexOrThrow(_cursor, "habitId")
          val _cursorIndexOfDate: Int = getColumnIndexOrThrow(_cursor, "date")
          val _cursorIndexOfTarget: Int = getColumnIndexOrThrow(_cursor, "target")
          val _cursorIndexOfActual: Int = getColumnIndexOrThrow(_cursor, "actual")
          val _cursorIndexOfPercentage: Int = getColumnIndexOrThrow(_cursor, "percentage")
          val _cursorIndexOfCompleted: Int = getColumnIndexOrThrow(_cursor, "completed")
          val _cursorIndexOfNote: Int = getColumnIndexOrThrow(_cursor, "note")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: HabitProgressEntity?
          if (_cursor.moveToFirst()) {
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpHabitId: Long
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId)
            val _tmpDate: Long
            _tmpDate = _cursor.getLong(_cursorIndexOfDate)
            val _tmpTarget: Double
            _tmpTarget = _cursor.getDouble(_cursorIndexOfTarget)
            val _tmpActual: Double
            _tmpActual = _cursor.getDouble(_cursorIndexOfActual)
            val _tmpPercentage: Int
            _tmpPercentage = _cursor.getInt(_cursorIndexOfPercentage)
            val _tmpCompleted: Boolean
            val _tmp: Int
            _tmp = _cursor.getInt(_cursorIndexOfCompleted)
            _tmpCompleted = _tmp != 0
            val _tmpNote: String?
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _result =
                HabitProgressEntity(_tmpId,_tmpHabitId,_tmpDate,_tmpTarget,_tmpActual,_tmpPercentage,_tmpCompleted,_tmpNote,_tmpCreatedAt,_tmpUpdatedAt)
          } else {
            _result = null
          }
          return _result
        } finally {
          _cursor.close()
          _statement.release()
        }
      }
    })
  }

  public override suspend fun getProgressForHabit(habitId: Long): List<HabitProgressEntity> {
    val _sql: String = "SELECT * FROM habit_progress WHERE habitId = ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, habitId)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<List<HabitProgressEntity>> {
      public override fun call(): List<HabitProgressEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfHabitId: Int = getColumnIndexOrThrow(_cursor, "habitId")
          val _cursorIndexOfDate: Int = getColumnIndexOrThrow(_cursor, "date")
          val _cursorIndexOfTarget: Int = getColumnIndexOrThrow(_cursor, "target")
          val _cursorIndexOfActual: Int = getColumnIndexOrThrow(_cursor, "actual")
          val _cursorIndexOfPercentage: Int = getColumnIndexOrThrow(_cursor, "percentage")
          val _cursorIndexOfCompleted: Int = getColumnIndexOrThrow(_cursor, "completed")
          val _cursorIndexOfNote: Int = getColumnIndexOrThrow(_cursor, "note")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: MutableList<HabitProgressEntity> =
              ArrayList<HabitProgressEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: HabitProgressEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpHabitId: Long
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId)
            val _tmpDate: Long
            _tmpDate = _cursor.getLong(_cursorIndexOfDate)
            val _tmpTarget: Double
            _tmpTarget = _cursor.getDouble(_cursorIndexOfTarget)
            val _tmpActual: Double
            _tmpActual = _cursor.getDouble(_cursorIndexOfActual)
            val _tmpPercentage: Int
            _tmpPercentage = _cursor.getInt(_cursorIndexOfPercentage)
            val _tmpCompleted: Boolean
            val _tmp: Int
            _tmp = _cursor.getInt(_cursorIndexOfCompleted)
            _tmpCompleted = _tmp != 0
            val _tmpNote: String?
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _item =
                HabitProgressEntity(_tmpId,_tmpHabitId,_tmpDate,_tmpTarget,_tmpActual,_tmpPercentage,_tmpCompleted,_tmpNote,_tmpCreatedAt,_tmpUpdatedAt)
            _result.add(_item)
          }
          return _result
        } finally {
          _cursor.close()
          _statement.release()
        }
      }
    })
  }

  public companion object {
    @JvmStatic
    public fun getRequiredConverters(): List<Class<*>> = emptyList()
  }
}
