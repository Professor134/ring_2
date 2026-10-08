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
import com.example.ringapp.`data`.local.entities.RepeatType
import com.example.ringapp.`data`.local.entities.TaskEntity
import com.example.ringapp.`data`.local.entities.TaskPriority
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
public class TaskDao_Impl(
  __db: RoomDatabase,
) : TaskDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfTaskEntity: EntityInsertionAdapter<TaskEntity>

  private val __converters: Converters = Converters()

  private val __updateAdapterOfTaskEntity: EntityDeletionOrUpdateAdapter<TaskEntity>

  private val __preparedStmtOfSoftDelete: SharedSQLiteStatement

  private val __preparedStmtOfDeleteCompletedBefore: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfTaskEntity = object : EntityInsertionAdapter<TaskEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `task` (`id`,`title`,`description`,`priority`,`dueDate`,`dueTime`,`repeatType`,`repeatInterval`,`repeatDaysOfWeek`,`repeatDayOfMonth`,`repeatMonth`,`repeatEndDate`,`completed`,`completedAt`,`reminderEnabled`,`reminderTime`,`parentTaskId`,`createdAt`,`updatedAt`,`deletedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: TaskEntity) {
        statement.bindLong(1, entity.id)
        statement.bindString(2, entity.title)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(3)
        } else {
          statement.bindString(3, _tmpDescription)
        }
        val _tmp: String = __converters.priorityToString(entity.priority)
        statement.bindString(4, _tmp)
        val _tmpDueDate: Long? = entity.dueDate
        if (_tmpDueDate == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpDueDate)
        }
        val _tmpDueTime: Long? = entity.dueTime
        if (_tmpDueTime == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpDueTime)
        }
        val _tmp_1: String = __converters.repeatTypeToString(entity.repeatType)
        statement.bindString(7, _tmp_1)
        statement.bindLong(8, entity.repeatInterval.toLong())
        val _tmpRepeatDaysOfWeek: String? = entity.repeatDaysOfWeek
        if (_tmpRepeatDaysOfWeek == null) {
          statement.bindNull(9)
        } else {
          statement.bindString(9, _tmpRepeatDaysOfWeek)
        }
        val _tmpRepeatDayOfMonth: Int? = entity.repeatDayOfMonth
        if (_tmpRepeatDayOfMonth == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpRepeatDayOfMonth.toLong())
        }
        val _tmpRepeatMonth: Int? = entity.repeatMonth
        if (_tmpRepeatMonth == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpRepeatMonth.toLong())
        }
        val _tmpRepeatEndDate: Long? = entity.repeatEndDate
        if (_tmpRepeatEndDate == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpRepeatEndDate)
        }
        val _tmp_2: Int = if (entity.completed) 1 else 0
        statement.bindLong(13, _tmp_2.toLong())
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmpCompletedAt)
        }
        val _tmp_3: Int = if (entity.reminderEnabled) 1 else 0
        statement.bindLong(15, _tmp_3.toLong())
        val _tmpReminderTime: Long? = entity.reminderTime
        if (_tmpReminderTime == null) {
          statement.bindNull(16)
        } else {
          statement.bindLong(16, _tmpReminderTime)
        }
        val _tmpParentTaskId: Long? = entity.parentTaskId
        if (_tmpParentTaskId == null) {
          statement.bindNull(17)
        } else {
          statement.bindLong(17, _tmpParentTaskId)
        }
        statement.bindLong(18, entity.createdAt)
        statement.bindLong(19, entity.updatedAt)
        val _tmpDeletedAt: Long? = entity.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(20)
        } else {
          statement.bindLong(20, _tmpDeletedAt)
        }
      }
    }
    this.__updateAdapterOfTaskEntity = object : EntityDeletionOrUpdateAdapter<TaskEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `task` SET `id` = ?,`title` = ?,`description` = ?,`priority` = ?,`dueDate` = ?,`dueTime` = ?,`repeatType` = ?,`repeatInterval` = ?,`repeatDaysOfWeek` = ?,`repeatDayOfMonth` = ?,`repeatMonth` = ?,`repeatEndDate` = ?,`completed` = ?,`completedAt` = ?,`reminderEnabled` = ?,`reminderTime` = ?,`parentTaskId` = ?,`createdAt` = ?,`updatedAt` = ?,`deletedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: TaskEntity) {
        statement.bindLong(1, entity.id)
        statement.bindString(2, entity.title)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(3)
        } else {
          statement.bindString(3, _tmpDescription)
        }
        val _tmp: String = __converters.priorityToString(entity.priority)
        statement.bindString(4, _tmp)
        val _tmpDueDate: Long? = entity.dueDate
        if (_tmpDueDate == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpDueDate)
        }
        val _tmpDueTime: Long? = entity.dueTime
        if (_tmpDueTime == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpDueTime)
        }
        val _tmp_1: String = __converters.repeatTypeToString(entity.repeatType)
        statement.bindString(7, _tmp_1)
        statement.bindLong(8, entity.repeatInterval.toLong())
        val _tmpRepeatDaysOfWeek: String? = entity.repeatDaysOfWeek
        if (_tmpRepeatDaysOfWeek == null) {
          statement.bindNull(9)
        } else {
          statement.bindString(9, _tmpRepeatDaysOfWeek)
        }
        val _tmpRepeatDayOfMonth: Int? = entity.repeatDayOfMonth
        if (_tmpRepeatDayOfMonth == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpRepeatDayOfMonth.toLong())
        }
        val _tmpRepeatMonth: Int? = entity.repeatMonth
        if (_tmpRepeatMonth == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpRepeatMonth.toLong())
        }
        val _tmpRepeatEndDate: Long? = entity.repeatEndDate
        if (_tmpRepeatEndDate == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpRepeatEndDate)
        }
        val _tmp_2: Int = if (entity.completed) 1 else 0
        statement.bindLong(13, _tmp_2.toLong())
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmpCompletedAt)
        }
        val _tmp_3: Int = if (entity.reminderEnabled) 1 else 0
        statement.bindLong(15, _tmp_3.toLong())
        val _tmpReminderTime: Long? = entity.reminderTime
        if (_tmpReminderTime == null) {
          statement.bindNull(16)
        } else {
          statement.bindLong(16, _tmpReminderTime)
        }
        val _tmpParentTaskId: Long? = entity.parentTaskId
        if (_tmpParentTaskId == null) {
          statement.bindNull(17)
        } else {
          statement.bindLong(17, _tmpParentTaskId)
        }
        statement.bindLong(18, entity.createdAt)
        statement.bindLong(19, entity.updatedAt)
        val _tmpDeletedAt: Long? = entity.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(20)
        } else {
          statement.bindLong(20, _tmpDeletedAt)
        }
        statement.bindLong(21, entity.id)
      }
    }
    this.__preparedStmtOfSoftDelete = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "UPDATE task SET deletedAt = ?, updatedAt = ? WHERE id = ?"
        return _query
      }
    }
    this.__preparedStmtOfDeleteCompletedBefore = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM task WHERE completed = 1 AND completedAt < ?"
        return _query
      }
    }
  }

  public override suspend fun insert(task: TaskEntity): Long = CoroutinesRoom.execute(__db, true,
      object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfTaskEntity.insertAndReturnId(task)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(task: TaskEntity): Unit = CoroutinesRoom.execute(__db, true,
      object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __updateAdapterOfTaskEntity.handle(task)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun softDelete(taskId: Long, deletedAt: Long): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfSoftDelete.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, deletedAt)
      _argIndex = 2
      _stmt.bindLong(_argIndex, deletedAt)
      _argIndex = 3
      _stmt.bindLong(_argIndex, taskId)
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

  public override suspend fun deleteCompletedBefore(threshold: Long): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfDeleteCompletedBefore.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, threshold)
      try {
        __db.beginTransaction()
        try {
          _stmt.executeUpdateDelete()
          __db.setTransactionSuccessful()
        } finally {
          __db.endTransaction()
        }
      } finally {
        __preparedStmtOfDeleteCompletedBefore.release(_stmt)
      }
    }
  })

  public override fun observeActive(): Flow<List<TaskEntity>> {
    val _sql: String =
        "SELECT * FROM task WHERE deletedAt IS NULL ORDER BY completed, dueDate, title"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("task"), object :
        Callable<List<TaskEntity>> {
      public override fun call(): List<TaskEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfTitle: Int = getColumnIndexOrThrow(_cursor, "title")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfPriority: Int = getColumnIndexOrThrow(_cursor, "priority")
          val _cursorIndexOfDueDate: Int = getColumnIndexOrThrow(_cursor, "dueDate")
          val _cursorIndexOfDueTime: Int = getColumnIndexOrThrow(_cursor, "dueTime")
          val _cursorIndexOfRepeatType: Int = getColumnIndexOrThrow(_cursor, "repeatType")
          val _cursorIndexOfRepeatInterval: Int = getColumnIndexOrThrow(_cursor, "repeatInterval")
          val _cursorIndexOfRepeatDaysOfWeek: Int = getColumnIndexOrThrow(_cursor,
              "repeatDaysOfWeek")
          val _cursorIndexOfRepeatDayOfMonth: Int = getColumnIndexOrThrow(_cursor,
              "repeatDayOfMonth")
          val _cursorIndexOfRepeatMonth: Int = getColumnIndexOrThrow(_cursor, "repeatMonth")
          val _cursorIndexOfRepeatEndDate: Int = getColumnIndexOrThrow(_cursor, "repeatEndDate")
          val _cursorIndexOfCompleted: Int = getColumnIndexOrThrow(_cursor, "completed")
          val _cursorIndexOfCompletedAt: Int = getColumnIndexOrThrow(_cursor, "completedAt")
          val _cursorIndexOfReminderEnabled: Int = getColumnIndexOrThrow(_cursor, "reminderEnabled")
          val _cursorIndexOfReminderTime: Int = getColumnIndexOrThrow(_cursor, "reminderTime")
          val _cursorIndexOfParentTaskId: Int = getColumnIndexOrThrow(_cursor, "parentTaskId")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _result: MutableList<TaskEntity> = ArrayList<TaskEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: TaskEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpTitle: String
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle)
            val _tmpDescription: String?
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            }
            val _tmpPriority: TaskPriority
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfPriority)
            _tmpPriority = __converters.stringToPriority(_tmp)
            val _tmpDueDate: Long?
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate)
            }
            val _tmpDueTime: Long?
            if (_cursor.isNull(_cursorIndexOfDueTime)) {
              _tmpDueTime = null
            } else {
              _tmpDueTime = _cursor.getLong(_cursorIndexOfDueTime)
            }
            val _tmpRepeatType: RepeatType
            val _tmp_1: String
            _tmp_1 = _cursor.getString(_cursorIndexOfRepeatType)
            _tmpRepeatType = __converters.stringToRepeatType(_tmp_1)
            val _tmpRepeatInterval: Int
            _tmpRepeatInterval = _cursor.getInt(_cursorIndexOfRepeatInterval)
            val _tmpRepeatDaysOfWeek: String?
            if (_cursor.isNull(_cursorIndexOfRepeatDaysOfWeek)) {
              _tmpRepeatDaysOfWeek = null
            } else {
              _tmpRepeatDaysOfWeek = _cursor.getString(_cursorIndexOfRepeatDaysOfWeek)
            }
            val _tmpRepeatDayOfMonth: Int?
            if (_cursor.isNull(_cursorIndexOfRepeatDayOfMonth)) {
              _tmpRepeatDayOfMonth = null
            } else {
              _tmpRepeatDayOfMonth = _cursor.getInt(_cursorIndexOfRepeatDayOfMonth)
            }
            val _tmpRepeatMonth: Int?
            if (_cursor.isNull(_cursorIndexOfRepeatMonth)) {
              _tmpRepeatMonth = null
            } else {
              _tmpRepeatMonth = _cursor.getInt(_cursorIndexOfRepeatMonth)
            }
            val _tmpRepeatEndDate: Long?
            if (_cursor.isNull(_cursorIndexOfRepeatEndDate)) {
              _tmpRepeatEndDate = null
            } else {
              _tmpRepeatEndDate = _cursor.getLong(_cursorIndexOfRepeatEndDate)
            }
            val _tmpCompleted: Boolean
            val _tmp_2: Int
            _tmp_2 = _cursor.getInt(_cursorIndexOfCompleted)
            _tmpCompleted = _tmp_2 != 0
            val _tmpCompletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt)
            }
            val _tmpReminderEnabled: Boolean
            val _tmp_3: Int
            _tmp_3 = _cursor.getInt(_cursorIndexOfReminderEnabled)
            _tmpReminderEnabled = _tmp_3 != 0
            val _tmpReminderTime: Long?
            if (_cursor.isNull(_cursorIndexOfReminderTime)) {
              _tmpReminderTime = null
            } else {
              _tmpReminderTime = _cursor.getLong(_cursorIndexOfReminderTime)
            }
            val _tmpParentTaskId: Long?
            if (_cursor.isNull(_cursorIndexOfParentTaskId)) {
              _tmpParentTaskId = null
            } else {
              _tmpParentTaskId = _cursor.getLong(_cursorIndexOfParentTaskId)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            _item =
                TaskEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpPriority,_tmpDueDate,_tmpDueTime,_tmpRepeatType,_tmpRepeatInterval,_tmpRepeatDaysOfWeek,_tmpRepeatDayOfMonth,_tmpRepeatMonth,_tmpRepeatEndDate,_tmpCompleted,_tmpCompletedAt,_tmpReminderEnabled,_tmpReminderTime,_tmpParentTaskId,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt)
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

  public override fun observeById(taskId: Long): Flow<TaskEntity?> {
    val _sql: String = "SELECT * FROM task WHERE id = ? LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, taskId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("task"), object : Callable<TaskEntity?> {
      public override fun call(): TaskEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfTitle: Int = getColumnIndexOrThrow(_cursor, "title")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfPriority: Int = getColumnIndexOrThrow(_cursor, "priority")
          val _cursorIndexOfDueDate: Int = getColumnIndexOrThrow(_cursor, "dueDate")
          val _cursorIndexOfDueTime: Int = getColumnIndexOrThrow(_cursor, "dueTime")
          val _cursorIndexOfRepeatType: Int = getColumnIndexOrThrow(_cursor, "repeatType")
          val _cursorIndexOfRepeatInterval: Int = getColumnIndexOrThrow(_cursor, "repeatInterval")
          val _cursorIndexOfRepeatDaysOfWeek: Int = getColumnIndexOrThrow(_cursor,
              "repeatDaysOfWeek")
          val _cursorIndexOfRepeatDayOfMonth: Int = getColumnIndexOrThrow(_cursor,
              "repeatDayOfMonth")
          val _cursorIndexOfRepeatMonth: Int = getColumnIndexOrThrow(_cursor, "repeatMonth")
          val _cursorIndexOfRepeatEndDate: Int = getColumnIndexOrThrow(_cursor, "repeatEndDate")
          val _cursorIndexOfCompleted: Int = getColumnIndexOrThrow(_cursor, "completed")
          val _cursorIndexOfCompletedAt: Int = getColumnIndexOrThrow(_cursor, "completedAt")
          val _cursorIndexOfReminderEnabled: Int = getColumnIndexOrThrow(_cursor, "reminderEnabled")
          val _cursorIndexOfReminderTime: Int = getColumnIndexOrThrow(_cursor, "reminderTime")
          val _cursorIndexOfParentTaskId: Int = getColumnIndexOrThrow(_cursor, "parentTaskId")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _result: TaskEntity?
          if (_cursor.moveToFirst()) {
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpTitle: String
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle)
            val _tmpDescription: String?
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            }
            val _tmpPriority: TaskPriority
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfPriority)
            _tmpPriority = __converters.stringToPriority(_tmp)
            val _tmpDueDate: Long?
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate)
            }
            val _tmpDueTime: Long?
            if (_cursor.isNull(_cursorIndexOfDueTime)) {
              _tmpDueTime = null
            } else {
              _tmpDueTime = _cursor.getLong(_cursorIndexOfDueTime)
            }
            val _tmpRepeatType: RepeatType
            val _tmp_1: String
            _tmp_1 = _cursor.getString(_cursorIndexOfRepeatType)
            _tmpRepeatType = __converters.stringToRepeatType(_tmp_1)
            val _tmpRepeatInterval: Int
            _tmpRepeatInterval = _cursor.getInt(_cursorIndexOfRepeatInterval)
            val _tmpRepeatDaysOfWeek: String?
            if (_cursor.isNull(_cursorIndexOfRepeatDaysOfWeek)) {
              _tmpRepeatDaysOfWeek = null
            } else {
              _tmpRepeatDaysOfWeek = _cursor.getString(_cursorIndexOfRepeatDaysOfWeek)
            }
            val _tmpRepeatDayOfMonth: Int?
            if (_cursor.isNull(_cursorIndexOfRepeatDayOfMonth)) {
              _tmpRepeatDayOfMonth = null
            } else {
              _tmpRepeatDayOfMonth = _cursor.getInt(_cursorIndexOfRepeatDayOfMonth)
            }
            val _tmpRepeatMonth: Int?
            if (_cursor.isNull(_cursorIndexOfRepeatMonth)) {
              _tmpRepeatMonth = null
            } else {
              _tmpRepeatMonth = _cursor.getInt(_cursorIndexOfRepeatMonth)
            }
            val _tmpRepeatEndDate: Long?
            if (_cursor.isNull(_cursorIndexOfRepeatEndDate)) {
              _tmpRepeatEndDate = null
            } else {
              _tmpRepeatEndDate = _cursor.getLong(_cursorIndexOfRepeatEndDate)
            }
            val _tmpCompleted: Boolean
            val _tmp_2: Int
            _tmp_2 = _cursor.getInt(_cursorIndexOfCompleted)
            _tmpCompleted = _tmp_2 != 0
            val _tmpCompletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt)
            }
            val _tmpReminderEnabled: Boolean
            val _tmp_3: Int
            _tmp_3 = _cursor.getInt(_cursorIndexOfReminderEnabled)
            _tmpReminderEnabled = _tmp_3 != 0
            val _tmpReminderTime: Long?
            if (_cursor.isNull(_cursorIndexOfReminderTime)) {
              _tmpReminderTime = null
            } else {
              _tmpReminderTime = _cursor.getLong(_cursorIndexOfReminderTime)
            }
            val _tmpParentTaskId: Long?
            if (_cursor.isNull(_cursorIndexOfParentTaskId)) {
              _tmpParentTaskId = null
            } else {
              _tmpParentTaskId = _cursor.getLong(_cursorIndexOfParentTaskId)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            _result =
                TaskEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpPriority,_tmpDueDate,_tmpDueTime,_tmpRepeatType,_tmpRepeatInterval,_tmpRepeatDaysOfWeek,_tmpRepeatDayOfMonth,_tmpRepeatMonth,_tmpRepeatEndDate,_tmpCompleted,_tmpCompletedAt,_tmpReminderEnabled,_tmpReminderTime,_tmpParentTaskId,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt)
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

  public override fun observeToday(start: Long, end: Long): Flow<List<TaskEntity>> {
    val _sql: String =
        "SELECT * FROM task WHERE deletedAt IS NULL AND (dueDate IS NULL OR dueDate BETWEEN ? AND ?) ORDER BY completed, dueDate, title"
    val _statement: RoomSQLiteQuery = acquire(_sql, 2)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, start)
    _argIndex = 2
    _statement.bindLong(_argIndex, end)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("task"), object :
        Callable<List<TaskEntity>> {
      public override fun call(): List<TaskEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfTitle: Int = getColumnIndexOrThrow(_cursor, "title")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfPriority: Int = getColumnIndexOrThrow(_cursor, "priority")
          val _cursorIndexOfDueDate: Int = getColumnIndexOrThrow(_cursor, "dueDate")
          val _cursorIndexOfDueTime: Int = getColumnIndexOrThrow(_cursor, "dueTime")
          val _cursorIndexOfRepeatType: Int = getColumnIndexOrThrow(_cursor, "repeatType")
          val _cursorIndexOfRepeatInterval: Int = getColumnIndexOrThrow(_cursor, "repeatInterval")
          val _cursorIndexOfRepeatDaysOfWeek: Int = getColumnIndexOrThrow(_cursor,
              "repeatDaysOfWeek")
          val _cursorIndexOfRepeatDayOfMonth: Int = getColumnIndexOrThrow(_cursor,
              "repeatDayOfMonth")
          val _cursorIndexOfRepeatMonth: Int = getColumnIndexOrThrow(_cursor, "repeatMonth")
          val _cursorIndexOfRepeatEndDate: Int = getColumnIndexOrThrow(_cursor, "repeatEndDate")
          val _cursorIndexOfCompleted: Int = getColumnIndexOrThrow(_cursor, "completed")
          val _cursorIndexOfCompletedAt: Int = getColumnIndexOrThrow(_cursor, "completedAt")
          val _cursorIndexOfReminderEnabled: Int = getColumnIndexOrThrow(_cursor, "reminderEnabled")
          val _cursorIndexOfReminderTime: Int = getColumnIndexOrThrow(_cursor, "reminderTime")
          val _cursorIndexOfParentTaskId: Int = getColumnIndexOrThrow(_cursor, "parentTaskId")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _result: MutableList<TaskEntity> = ArrayList<TaskEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: TaskEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpTitle: String
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle)
            val _tmpDescription: String?
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            }
            val _tmpPriority: TaskPriority
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfPriority)
            _tmpPriority = __converters.stringToPriority(_tmp)
            val _tmpDueDate: Long?
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate)
            }
            val _tmpDueTime: Long?
            if (_cursor.isNull(_cursorIndexOfDueTime)) {
              _tmpDueTime = null
            } else {
              _tmpDueTime = _cursor.getLong(_cursorIndexOfDueTime)
            }
            val _tmpRepeatType: RepeatType
            val _tmp_1: String
            _tmp_1 = _cursor.getString(_cursorIndexOfRepeatType)
            _tmpRepeatType = __converters.stringToRepeatType(_tmp_1)
            val _tmpRepeatInterval: Int
            _tmpRepeatInterval = _cursor.getInt(_cursorIndexOfRepeatInterval)
            val _tmpRepeatDaysOfWeek: String?
            if (_cursor.isNull(_cursorIndexOfRepeatDaysOfWeek)) {
              _tmpRepeatDaysOfWeek = null
            } else {
              _tmpRepeatDaysOfWeek = _cursor.getString(_cursorIndexOfRepeatDaysOfWeek)
            }
            val _tmpRepeatDayOfMonth: Int?
            if (_cursor.isNull(_cursorIndexOfRepeatDayOfMonth)) {
              _tmpRepeatDayOfMonth = null
            } else {
              _tmpRepeatDayOfMonth = _cursor.getInt(_cursorIndexOfRepeatDayOfMonth)
            }
            val _tmpRepeatMonth: Int?
            if (_cursor.isNull(_cursorIndexOfRepeatMonth)) {
              _tmpRepeatMonth = null
            } else {
              _tmpRepeatMonth = _cursor.getInt(_cursorIndexOfRepeatMonth)
            }
            val _tmpRepeatEndDate: Long?
            if (_cursor.isNull(_cursorIndexOfRepeatEndDate)) {
              _tmpRepeatEndDate = null
            } else {
              _tmpRepeatEndDate = _cursor.getLong(_cursorIndexOfRepeatEndDate)
            }
            val _tmpCompleted: Boolean
            val _tmp_2: Int
            _tmp_2 = _cursor.getInt(_cursorIndexOfCompleted)
            _tmpCompleted = _tmp_2 != 0
            val _tmpCompletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt)
            }
            val _tmpReminderEnabled: Boolean
            val _tmp_3: Int
            _tmp_3 = _cursor.getInt(_cursorIndexOfReminderEnabled)
            _tmpReminderEnabled = _tmp_3 != 0
            val _tmpReminderTime: Long?
            if (_cursor.isNull(_cursorIndexOfReminderTime)) {
              _tmpReminderTime = null
            } else {
              _tmpReminderTime = _cursor.getLong(_cursorIndexOfReminderTime)
            }
            val _tmpParentTaskId: Long?
            if (_cursor.isNull(_cursorIndexOfParentTaskId)) {
              _tmpParentTaskId = null
            } else {
              _tmpParentTaskId = _cursor.getLong(_cursorIndexOfParentTaskId)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            _item =
                TaskEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpPriority,_tmpDueDate,_tmpDueTime,_tmpRepeatType,_tmpRepeatInterval,_tmpRepeatDaysOfWeek,_tmpRepeatDayOfMonth,_tmpRepeatMonth,_tmpRepeatEndDate,_tmpCompleted,_tmpCompletedAt,_tmpReminderEnabled,_tmpReminderTime,_tmpParentTaskId,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt)
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

  public override suspend fun activeReminders(): List<TaskEntity> {
    val _sql: String =
        "SELECT * FROM task WHERE deletedAt IS NULL AND reminderEnabled = 1 AND reminderTime IS NOT NULL AND completed = 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<List<TaskEntity>> {
      public override fun call(): List<TaskEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfTitle: Int = getColumnIndexOrThrow(_cursor, "title")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfPriority: Int = getColumnIndexOrThrow(_cursor, "priority")
          val _cursorIndexOfDueDate: Int = getColumnIndexOrThrow(_cursor, "dueDate")
          val _cursorIndexOfDueTime: Int = getColumnIndexOrThrow(_cursor, "dueTime")
          val _cursorIndexOfRepeatType: Int = getColumnIndexOrThrow(_cursor, "repeatType")
          val _cursorIndexOfRepeatInterval: Int = getColumnIndexOrThrow(_cursor, "repeatInterval")
          val _cursorIndexOfRepeatDaysOfWeek: Int = getColumnIndexOrThrow(_cursor,
              "repeatDaysOfWeek")
          val _cursorIndexOfRepeatDayOfMonth: Int = getColumnIndexOrThrow(_cursor,
              "repeatDayOfMonth")
          val _cursorIndexOfRepeatMonth: Int = getColumnIndexOrThrow(_cursor, "repeatMonth")
          val _cursorIndexOfRepeatEndDate: Int = getColumnIndexOrThrow(_cursor, "repeatEndDate")
          val _cursorIndexOfCompleted: Int = getColumnIndexOrThrow(_cursor, "completed")
          val _cursorIndexOfCompletedAt: Int = getColumnIndexOrThrow(_cursor, "completedAt")
          val _cursorIndexOfReminderEnabled: Int = getColumnIndexOrThrow(_cursor, "reminderEnabled")
          val _cursorIndexOfReminderTime: Int = getColumnIndexOrThrow(_cursor, "reminderTime")
          val _cursorIndexOfParentTaskId: Int = getColumnIndexOrThrow(_cursor, "parentTaskId")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _result: MutableList<TaskEntity> = ArrayList<TaskEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: TaskEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpTitle: String
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle)
            val _tmpDescription: String?
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            }
            val _tmpPriority: TaskPriority
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfPriority)
            _tmpPriority = __converters.stringToPriority(_tmp)
            val _tmpDueDate: Long?
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate)
            }
            val _tmpDueTime: Long?
            if (_cursor.isNull(_cursorIndexOfDueTime)) {
              _tmpDueTime = null
            } else {
              _tmpDueTime = _cursor.getLong(_cursorIndexOfDueTime)
            }
            val _tmpRepeatType: RepeatType
            val _tmp_1: String
            _tmp_1 = _cursor.getString(_cursorIndexOfRepeatType)
            _tmpRepeatType = __converters.stringToRepeatType(_tmp_1)
            val _tmpRepeatInterval: Int
            _tmpRepeatInterval = _cursor.getInt(_cursorIndexOfRepeatInterval)
            val _tmpRepeatDaysOfWeek: String?
            if (_cursor.isNull(_cursorIndexOfRepeatDaysOfWeek)) {
              _tmpRepeatDaysOfWeek = null
            } else {
              _tmpRepeatDaysOfWeek = _cursor.getString(_cursorIndexOfRepeatDaysOfWeek)
            }
            val _tmpRepeatDayOfMonth: Int?
            if (_cursor.isNull(_cursorIndexOfRepeatDayOfMonth)) {
              _tmpRepeatDayOfMonth = null
            } else {
              _tmpRepeatDayOfMonth = _cursor.getInt(_cursorIndexOfRepeatDayOfMonth)
            }
            val _tmpRepeatMonth: Int?
            if (_cursor.isNull(_cursorIndexOfRepeatMonth)) {
              _tmpRepeatMonth = null
            } else {
              _tmpRepeatMonth = _cursor.getInt(_cursorIndexOfRepeatMonth)
            }
            val _tmpRepeatEndDate: Long?
            if (_cursor.isNull(_cursorIndexOfRepeatEndDate)) {
              _tmpRepeatEndDate = null
            } else {
              _tmpRepeatEndDate = _cursor.getLong(_cursorIndexOfRepeatEndDate)
            }
            val _tmpCompleted: Boolean
            val _tmp_2: Int
            _tmp_2 = _cursor.getInt(_cursorIndexOfCompleted)
            _tmpCompleted = _tmp_2 != 0
            val _tmpCompletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt)
            }
            val _tmpReminderEnabled: Boolean
            val _tmp_3: Int
            _tmp_3 = _cursor.getInt(_cursorIndexOfReminderEnabled)
            _tmpReminderEnabled = _tmp_3 != 0
            val _tmpReminderTime: Long?
            if (_cursor.isNull(_cursorIndexOfReminderTime)) {
              _tmpReminderTime = null
            } else {
              _tmpReminderTime = _cursor.getLong(_cursorIndexOfReminderTime)
            }
            val _tmpParentTaskId: Long?
            if (_cursor.isNull(_cursorIndexOfParentTaskId)) {
              _tmpParentTaskId = null
            } else {
              _tmpParentTaskId = _cursor.getLong(_cursorIndexOfParentTaskId)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            _item =
                TaskEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpPriority,_tmpDueDate,_tmpDueTime,_tmpRepeatType,_tmpRepeatInterval,_tmpRepeatDaysOfWeek,_tmpRepeatDayOfMonth,_tmpRepeatMonth,_tmpRepeatEndDate,_tmpCompleted,_tmpCompletedAt,_tmpReminderEnabled,_tmpReminderTime,_tmpParentTaskId,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt)
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
