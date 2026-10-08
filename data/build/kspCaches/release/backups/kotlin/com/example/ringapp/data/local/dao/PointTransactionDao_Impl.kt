package com.example.ringapp.`data`.local.dao

import android.database.Cursor
import android.os.CancellationSignal
import androidx.room.CoroutinesRoom
import androidx.room.CoroutinesRoom.Companion.execute
import androidx.room.EntityInsertionAdapter
import androidx.room.RoomDatabase
import androidx.room.RoomSQLiteQuery
import androidx.room.RoomSQLiteQuery.Companion.acquire
import androidx.room.util.createCancellationSignal
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteStatement
import com.example.ringapp.`data`.db.Converters
import com.example.ringapp.`data`.local.entities.PointTransactionEntity
import com.example.ringapp.`data`.local.entities.TransactionType
import java.lang.Class
import java.util.ArrayList
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.jvm.JvmStatic
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class PointTransactionDao_Impl(
  __db: RoomDatabase,
) : PointTransactionDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfPointTransactionEntity:
      EntityInsertionAdapter<PointTransactionEntity>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertionAdapterOfPointTransactionEntity = object :
        EntityInsertionAdapter<PointTransactionEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `point_transaction` (`id`,`amount`,`type`,`description`,`habitId`,`taskId`,`cycleId`,`uniqueReference`,`timestamp`,`deviceId`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement,
          entity: PointTransactionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.amount.toLong())
        val _tmp: String = __converters.transactionTypeToString(entity.type)
        statement.bindString(3, _tmp)
        statement.bindString(4, entity.description)
        val _tmpHabitId: Long? = entity.habitId
        if (_tmpHabitId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpHabitId)
        }
        val _tmpTaskId: Long? = entity.taskId
        if (_tmpTaskId == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpTaskId)
        }
        val _tmpCycleId: Long? = entity.cycleId
        if (_tmpCycleId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpCycleId)
        }
        statement.bindString(8, entity.uniqueReference)
        statement.bindLong(9, entity.timestamp)
        val _tmpDeviceId: String? = entity.deviceId
        if (_tmpDeviceId == null) {
          statement.bindNull(10)
        } else {
          statement.bindString(10, _tmpDeviceId)
        }
        statement.bindLong(11, entity.createdAt)
      }
    }
  }

  public override suspend fun insert(transaction: PointTransactionEntity): Long =
      CoroutinesRoom.execute(__db, true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long =
            __insertionAdapterOfPointTransactionEntity.insertAndReturnId(transaction)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun findByReference(reference: String): PointTransactionEntity? {
    val _sql: String = "SELECT * FROM point_transaction WHERE uniqueReference = ? LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindString(_argIndex, reference)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<PointTransactionEntity?> {
      public override fun call(): PointTransactionEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfAmount: Int = getColumnIndexOrThrow(_cursor, "amount")
          val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfHabitId: Int = getColumnIndexOrThrow(_cursor, "habitId")
          val _cursorIndexOfTaskId: Int = getColumnIndexOrThrow(_cursor, "taskId")
          val _cursorIndexOfCycleId: Int = getColumnIndexOrThrow(_cursor, "cycleId")
          val _cursorIndexOfUniqueReference: Int = getColumnIndexOrThrow(_cursor, "uniqueReference")
          val _cursorIndexOfTimestamp: Int = getColumnIndexOrThrow(_cursor, "timestamp")
          val _cursorIndexOfDeviceId: Int = getColumnIndexOrThrow(_cursor, "deviceId")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _result: PointTransactionEntity?
          if (_cursor.moveToFirst()) {
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpAmount: Int
            _tmpAmount = _cursor.getInt(_cursorIndexOfAmount)
            val _tmpType: TransactionType
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfType)
            _tmpType = __converters.stringToTransactionType(_tmp)
            val _tmpDescription: String
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            val _tmpHabitId: Long?
            if (_cursor.isNull(_cursorIndexOfHabitId)) {
              _tmpHabitId = null
            } else {
              _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId)
            }
            val _tmpTaskId: Long?
            if (_cursor.isNull(_cursorIndexOfTaskId)) {
              _tmpTaskId = null
            } else {
              _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId)
            }
            val _tmpCycleId: Long?
            if (_cursor.isNull(_cursorIndexOfCycleId)) {
              _tmpCycleId = null
            } else {
              _tmpCycleId = _cursor.getLong(_cursorIndexOfCycleId)
            }
            val _tmpUniqueReference: String
            _tmpUniqueReference = _cursor.getString(_cursorIndexOfUniqueReference)
            val _tmpTimestamp: Long
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp)
            val _tmpDeviceId: String?
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            _result =
                PointTransactionEntity(_tmpId,_tmpAmount,_tmpType,_tmpDescription,_tmpHabitId,_tmpTaskId,_tmpCycleId,_tmpUniqueReference,_tmpTimestamp,_tmpDeviceId,_tmpCreatedAt)
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

  public override fun observeForHabit(habitId: Long): Flow<List<PointTransactionEntity>> {
    val _sql: String = "SELECT * FROM point_transaction WHERE habitId = ? ORDER BY timestamp DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, habitId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("point_transaction"), object :
        Callable<List<PointTransactionEntity>> {
      public override fun call(): List<PointTransactionEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfAmount: Int = getColumnIndexOrThrow(_cursor, "amount")
          val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfHabitId: Int = getColumnIndexOrThrow(_cursor, "habitId")
          val _cursorIndexOfTaskId: Int = getColumnIndexOrThrow(_cursor, "taskId")
          val _cursorIndexOfCycleId: Int = getColumnIndexOrThrow(_cursor, "cycleId")
          val _cursorIndexOfUniqueReference: Int = getColumnIndexOrThrow(_cursor, "uniqueReference")
          val _cursorIndexOfTimestamp: Int = getColumnIndexOrThrow(_cursor, "timestamp")
          val _cursorIndexOfDeviceId: Int = getColumnIndexOrThrow(_cursor, "deviceId")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _result: MutableList<PointTransactionEntity> =
              ArrayList<PointTransactionEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: PointTransactionEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpAmount: Int
            _tmpAmount = _cursor.getInt(_cursorIndexOfAmount)
            val _tmpType: TransactionType
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfType)
            _tmpType = __converters.stringToTransactionType(_tmp)
            val _tmpDescription: String
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            val _tmpHabitId: Long?
            if (_cursor.isNull(_cursorIndexOfHabitId)) {
              _tmpHabitId = null
            } else {
              _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId)
            }
            val _tmpTaskId: Long?
            if (_cursor.isNull(_cursorIndexOfTaskId)) {
              _tmpTaskId = null
            } else {
              _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId)
            }
            val _tmpCycleId: Long?
            if (_cursor.isNull(_cursorIndexOfCycleId)) {
              _tmpCycleId = null
            } else {
              _tmpCycleId = _cursor.getLong(_cursorIndexOfCycleId)
            }
            val _tmpUniqueReference: String
            _tmpUniqueReference = _cursor.getString(_cursorIndexOfUniqueReference)
            val _tmpTimestamp: Long
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp)
            val _tmpDeviceId: String?
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            _item =
                PointTransactionEntity(_tmpId,_tmpAmount,_tmpType,_tmpDescription,_tmpHabitId,_tmpTaskId,_tmpCycleId,_tmpUniqueReference,_tmpTimestamp,_tmpDeviceId,_tmpCreatedAt)
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

  public override fun observeAll(): Flow<List<PointTransactionEntity>> {
    val _sql: String = "SELECT * FROM point_transaction ORDER BY timestamp DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("point_transaction"), object :
        Callable<List<PointTransactionEntity>> {
      public override fun call(): List<PointTransactionEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfAmount: Int = getColumnIndexOrThrow(_cursor, "amount")
          val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfHabitId: Int = getColumnIndexOrThrow(_cursor, "habitId")
          val _cursorIndexOfTaskId: Int = getColumnIndexOrThrow(_cursor, "taskId")
          val _cursorIndexOfCycleId: Int = getColumnIndexOrThrow(_cursor, "cycleId")
          val _cursorIndexOfUniqueReference: Int = getColumnIndexOrThrow(_cursor, "uniqueReference")
          val _cursorIndexOfTimestamp: Int = getColumnIndexOrThrow(_cursor, "timestamp")
          val _cursorIndexOfDeviceId: Int = getColumnIndexOrThrow(_cursor, "deviceId")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _result: MutableList<PointTransactionEntity> =
              ArrayList<PointTransactionEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: PointTransactionEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpAmount: Int
            _tmpAmount = _cursor.getInt(_cursorIndexOfAmount)
            val _tmpType: TransactionType
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfType)
            _tmpType = __converters.stringToTransactionType(_tmp)
            val _tmpDescription: String
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            val _tmpHabitId: Long?
            if (_cursor.isNull(_cursorIndexOfHabitId)) {
              _tmpHabitId = null
            } else {
              _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId)
            }
            val _tmpTaskId: Long?
            if (_cursor.isNull(_cursorIndexOfTaskId)) {
              _tmpTaskId = null
            } else {
              _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId)
            }
            val _tmpCycleId: Long?
            if (_cursor.isNull(_cursorIndexOfCycleId)) {
              _tmpCycleId = null
            } else {
              _tmpCycleId = _cursor.getLong(_cursorIndexOfCycleId)
            }
            val _tmpUniqueReference: String
            _tmpUniqueReference = _cursor.getString(_cursorIndexOfUniqueReference)
            val _tmpTimestamp: Long
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp)
            val _tmpDeviceId: String?
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            _item =
                PointTransactionEntity(_tmpId,_tmpAmount,_tmpType,_tmpDescription,_tmpHabitId,_tmpTaskId,_tmpCycleId,_tmpUniqueReference,_tmpTimestamp,_tmpDeviceId,_tmpCreatedAt)
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
