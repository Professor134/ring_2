package com.example.ringapp.`data`.local.dao

import android.database.Cursor
import android.os.CancellationSignal
import androidx.room.CoroutinesRoom
import androidx.room.CoroutinesRoom.Companion.execute
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
import com.example.ringapp.`data`.local.entities.NotificationType
import com.example.ringapp.`data`.local.entities.TaskReminderEntity
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

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class TaskReminderDao_Impl(
  __db: RoomDatabase,
) : TaskReminderDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfTaskReminderEntity: EntityInsertionAdapter<TaskReminderEntity>

  private val __converters: Converters = Converters()

  private val __preparedStmtOfDisableForTask: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfTaskReminderEntity = object :
        EntityInsertionAdapter<TaskReminderEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `task_reminders` (`id`,`taskId`,`triggerAt`,`reminderType`,`isEnabled`,`alarmId`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: TaskReminderEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.taskId)
        statement.bindLong(3, entity.triggerAt)
        val _tmp: String = __converters.notificationTypeToString(entity.reminderType)
        statement.bindString(4, _tmp)
        val _tmp_1: Int = if (entity.isEnabled) 1 else 0
        statement.bindLong(5, _tmp_1.toLong())
        val _tmpAlarmId: Int? = entity.alarmId
        if (_tmpAlarmId == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpAlarmId.toLong())
        }
        statement.bindLong(7, entity.createdAt)
        statement.bindLong(8, entity.updatedAt)
      }
    }
    this.__preparedStmtOfDisableForTask = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String =
            "UPDATE task_reminders SET isEnabled = 0, updatedAt = ? WHERE taskId = ?"
        return _query
      }
    }
  }

  public override suspend fun upsert(reminder: TaskReminderEntity): Long =
      CoroutinesRoom.execute(__db, true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfTaskReminderEntity.insertAndReturnId(reminder)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun disableForTask(taskId: Long, updatedAt: Long): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfDisableForTask.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, updatedAt)
      _argIndex = 2
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
        __preparedStmtOfDisableForTask.release(_stmt)
      }
    }
  })

  public override suspend fun activeFrom(now: Long): List<TaskReminderEntity> {
    val _sql: String =
        "SELECT * FROM task_reminders WHERE isEnabled = 1 AND triggerAt >= ? ORDER BY triggerAt"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, now)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<List<TaskReminderEntity>> {
      public override fun call(): List<TaskReminderEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfTaskId: Int = getColumnIndexOrThrow(_cursor, "taskId")
          val _cursorIndexOfTriggerAt: Int = getColumnIndexOrThrow(_cursor, "triggerAt")
          val _cursorIndexOfReminderType: Int = getColumnIndexOrThrow(_cursor, "reminderType")
          val _cursorIndexOfIsEnabled: Int = getColumnIndexOrThrow(_cursor, "isEnabled")
          val _cursorIndexOfAlarmId: Int = getColumnIndexOrThrow(_cursor, "alarmId")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: MutableList<TaskReminderEntity> =
              ArrayList<TaskReminderEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: TaskReminderEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpTaskId: Long
            _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId)
            val _tmpTriggerAt: Long
            _tmpTriggerAt = _cursor.getLong(_cursorIndexOfTriggerAt)
            val _tmpReminderType: NotificationType
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfReminderType)
            _tmpReminderType = __converters.stringToNotificationType(_tmp)
            val _tmpIsEnabled: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled)
            _tmpIsEnabled = _tmp_1 != 0
            val _tmpAlarmId: Int?
            if (_cursor.isNull(_cursorIndexOfAlarmId)) {
              _tmpAlarmId = null
            } else {
              _tmpAlarmId = _cursor.getInt(_cursorIndexOfAlarmId)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _item =
                TaskReminderEntity(_tmpId,_tmpTaskId,_tmpTriggerAt,_tmpReminderType,_tmpIsEnabled,_tmpAlarmId,_tmpCreatedAt,_tmpUpdatedAt)
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
