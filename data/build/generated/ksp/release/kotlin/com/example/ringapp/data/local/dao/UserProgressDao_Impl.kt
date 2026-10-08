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
import com.example.ringapp.`data`.local.entities.UserProgressEntity
import java.lang.Class
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
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
public class UserProgressDao_Impl(
  __db: RoomDatabase,
) : UserProgressDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfUserProgressEntity: EntityInsertionAdapter<UserProgressEntity>
  init {
    this.__db = __db
    this.__insertionAdapterOfUserProgressEntity = object :
        EntityInsertionAdapter<UserProgressEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `user_progress` (`userId`,`currentPoints`,`lifetimePoints`,`level`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: UserProgressEntity) {
        statement.bindLong(1, entity.userId.toLong())
        statement.bindLong(2, entity.currentPoints.toLong())
        statement.bindLong(3, entity.lifetimePoints.toLong())
        statement.bindLong(4, entity.level.toLong())
        statement.bindLong(5, entity.createdAt)
        statement.bindLong(6, entity.updatedAt)
      }
    }
  }

  public override suspend fun insert(progress: UserProgressEntity): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __insertionAdapterOfUserProgressEntity.insert(progress)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun getCurrent(): UserProgressEntity? {
    val _sql: String = "SELECT * FROM user_progress WHERE userId = 1 LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<UserProgressEntity?> {
      public override fun call(): UserProgressEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfUserId: Int = getColumnIndexOrThrow(_cursor, "userId")
          val _cursorIndexOfCurrentPoints: Int = getColumnIndexOrThrow(_cursor, "currentPoints")
          val _cursorIndexOfLifetimePoints: Int = getColumnIndexOrThrow(_cursor, "lifetimePoints")
          val _cursorIndexOfLevel: Int = getColumnIndexOrThrow(_cursor, "level")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: UserProgressEntity?
          if (_cursor.moveToFirst()) {
            val _tmpUserId: Int
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId)
            val _tmpCurrentPoints: Int
            _tmpCurrentPoints = _cursor.getInt(_cursorIndexOfCurrentPoints)
            val _tmpLifetimePoints: Int
            _tmpLifetimePoints = _cursor.getInt(_cursorIndexOfLifetimePoints)
            val _tmpLevel: Int
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel)
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _result =
                UserProgressEntity(_tmpUserId,_tmpCurrentPoints,_tmpLifetimePoints,_tmpLevel,_tmpCreatedAt,_tmpUpdatedAt)
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

  public override fun observeCurrent(): Flow<UserProgressEntity?> {
    val _sql: String = "SELECT * FROM user_progress WHERE userId = 1 LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("user_progress"), object :
        Callable<UserProgressEntity?> {
      public override fun call(): UserProgressEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfUserId: Int = getColumnIndexOrThrow(_cursor, "userId")
          val _cursorIndexOfCurrentPoints: Int = getColumnIndexOrThrow(_cursor, "currentPoints")
          val _cursorIndexOfLifetimePoints: Int = getColumnIndexOrThrow(_cursor, "lifetimePoints")
          val _cursorIndexOfLevel: Int = getColumnIndexOrThrow(_cursor, "level")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: UserProgressEntity?
          if (_cursor.moveToFirst()) {
            val _tmpUserId: Int
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId)
            val _tmpCurrentPoints: Int
            _tmpCurrentPoints = _cursor.getInt(_cursorIndexOfCurrentPoints)
            val _tmpLifetimePoints: Int
            _tmpLifetimePoints = _cursor.getInt(_cursorIndexOfLifetimePoints)
            val _tmpLevel: Int
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel)
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _result =
                UserProgressEntity(_tmpUserId,_tmpCurrentPoints,_tmpLifetimePoints,_tmpLevel,_tmpCreatedAt,_tmpUpdatedAt)
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
