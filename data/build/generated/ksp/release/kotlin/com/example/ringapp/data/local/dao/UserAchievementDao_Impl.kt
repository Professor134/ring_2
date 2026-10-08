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
import androidx.room.util.createCancellationSignal
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteStatement
import com.example.ringapp.`data`.local.entities.UserAchievementEntity
import java.lang.Class
import java.util.ArrayList
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
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
public class UserAchievementDao_Impl(
  __db: RoomDatabase,
) : UserAchievementDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfUserAchievementEntity:
      EntityInsertionAdapter<UserAchievementEntity>

  private val __updateAdapterOfUserAchievementEntity:
      EntityDeletionOrUpdateAdapter<UserAchievementEntity>
  init {
    this.__db = __db
    this.__insertionAdapterOfUserAchievementEntity = object :
        EntityInsertionAdapter<UserAchievementEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `user_achievements` (`id`,`achievementId`,`unlockedAt`,`progress`,`rewardTransactionId`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement,
          entity: UserAchievementEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.achievementId)
        val _tmpUnlockedAt: Long? = entity.unlockedAt
        if (_tmpUnlockedAt == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpUnlockedAt)
        }
        statement.bindLong(4, entity.progress.toLong())
        val _tmpRewardTransactionId: Long? = entity.rewardTransactionId
        if (_tmpRewardTransactionId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpRewardTransactionId)
        }
      }
    }
    this.__updateAdapterOfUserAchievementEntity = object :
        EntityDeletionOrUpdateAdapter<UserAchievementEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `user_achievements` SET `id` = ?,`achievementId` = ?,`unlockedAt` = ?,`progress` = ?,`rewardTransactionId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement,
          entity: UserAchievementEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.achievementId)
        val _tmpUnlockedAt: Long? = entity.unlockedAt
        if (_tmpUnlockedAt == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpUnlockedAt)
        }
        statement.bindLong(4, entity.progress.toLong())
        val _tmpRewardTransactionId: Long? = entity.rewardTransactionId
        if (_tmpRewardTransactionId == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpRewardTransactionId)
        }
        statement.bindLong(6, entity.id)
      }
    }
  }

  public override suspend fun insert(state: UserAchievementEntity): Long =
      CoroutinesRoom.execute(__db, true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfUserAchievementEntity.insertAndReturnId(state)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(state: UserAchievementEntity): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __updateAdapterOfUserAchievementEntity.handle(state)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override fun observeAll(): Flow<List<UserAchievementEntity>> {
    val _sql: String = "SELECT * FROM user_achievements ORDER BY unlockedAt DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("user_achievements"), object :
        Callable<List<UserAchievementEntity>> {
      public override fun call(): List<UserAchievementEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfAchievementId: Int = getColumnIndexOrThrow(_cursor, "achievementId")
          val _cursorIndexOfUnlockedAt: Int = getColumnIndexOrThrow(_cursor, "unlockedAt")
          val _cursorIndexOfProgress: Int = getColumnIndexOrThrow(_cursor, "progress")
          val _cursorIndexOfRewardTransactionId: Int = getColumnIndexOrThrow(_cursor,
              "rewardTransactionId")
          val _result: MutableList<UserAchievementEntity> =
              ArrayList<UserAchievementEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: UserAchievementEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpAchievementId: Long
            _tmpAchievementId = _cursor.getLong(_cursorIndexOfAchievementId)
            val _tmpUnlockedAt: Long?
            if (_cursor.isNull(_cursorIndexOfUnlockedAt)) {
              _tmpUnlockedAt = null
            } else {
              _tmpUnlockedAt = _cursor.getLong(_cursorIndexOfUnlockedAt)
            }
            val _tmpProgress: Int
            _tmpProgress = _cursor.getInt(_cursorIndexOfProgress)
            val _tmpRewardTransactionId: Long?
            if (_cursor.isNull(_cursorIndexOfRewardTransactionId)) {
              _tmpRewardTransactionId = null
            } else {
              _tmpRewardTransactionId = _cursor.getLong(_cursorIndexOfRewardTransactionId)
            }
            _item =
                UserAchievementEntity(_tmpId,_tmpAchievementId,_tmpUnlockedAt,_tmpProgress,_tmpRewardTransactionId)
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

  public override suspend fun find(achievementId: Long): UserAchievementEntity? {
    val _sql: String = "SELECT * FROM user_achievements WHERE achievementId = ? LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, achievementId)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<UserAchievementEntity?> {
      public override fun call(): UserAchievementEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfAchievementId: Int = getColumnIndexOrThrow(_cursor, "achievementId")
          val _cursorIndexOfUnlockedAt: Int = getColumnIndexOrThrow(_cursor, "unlockedAt")
          val _cursorIndexOfProgress: Int = getColumnIndexOrThrow(_cursor, "progress")
          val _cursorIndexOfRewardTransactionId: Int = getColumnIndexOrThrow(_cursor,
              "rewardTransactionId")
          val _result: UserAchievementEntity?
          if (_cursor.moveToFirst()) {
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpAchievementId: Long
            _tmpAchievementId = _cursor.getLong(_cursorIndexOfAchievementId)
            val _tmpUnlockedAt: Long?
            if (_cursor.isNull(_cursorIndexOfUnlockedAt)) {
              _tmpUnlockedAt = null
            } else {
              _tmpUnlockedAt = _cursor.getLong(_cursorIndexOfUnlockedAt)
            }
            val _tmpProgress: Int
            _tmpProgress = _cursor.getInt(_cursorIndexOfProgress)
            val _tmpRewardTransactionId: Long?
            if (_cursor.isNull(_cursorIndexOfRewardTransactionId)) {
              _tmpRewardTransactionId = null
            } else {
              _tmpRewardTransactionId = _cursor.getLong(_cursorIndexOfRewardTransactionId)
            }
            _result =
                UserAchievementEntity(_tmpId,_tmpAchievementId,_tmpUnlockedAt,_tmpProgress,_tmpRewardTransactionId)
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

  public companion object {
    @JvmStatic
    public fun getRequiredConverters(): List<Class<*>> = emptyList()
  }
}
