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
import com.example.ringapp.`data`.local.entities.AchievementCategory
import com.example.ringapp.`data`.local.entities.AchievementEntity
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
public class AchievementDao_Impl(
  __db: RoomDatabase,
) : AchievementDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfAchievementEntity: EntityInsertionAdapter<AchievementEntity>

  private val __converters: Converters = Converters()

  private val __updateAdapterOfAchievementEntity: EntityDeletionOrUpdateAdapter<AchievementEntity>
  init {
    this.__db = __db
    this.__insertionAdapterOfAchievementEntity = object :
        EntityInsertionAdapter<AchievementEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `achievement` (`id`,`category`,`threshold`,`description`,`icon`,`pointsAwarded`,`unlocked`,`unlockedAt`,`currentProgress`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: AchievementEntity) {
        statement.bindLong(1, entity.id)
        val _tmp: String = __converters.achievementCategoryToString(entity.category)
        statement.bindString(2, _tmp)
        statement.bindLong(3, entity.threshold.toLong())
        statement.bindString(4, entity.description)
        val _tmpIcon: String? = entity.icon
        if (_tmpIcon == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpIcon)
        }
        statement.bindLong(6, entity.pointsAwarded.toLong())
        val _tmp_1: Int = if (entity.unlocked) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        val _tmpUnlockedAt: Long? = entity.unlockedAt
        if (_tmpUnlockedAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpUnlockedAt)
        }
        statement.bindLong(9, entity.currentProgress.toLong())
        statement.bindLong(10, entity.createdAt)
        statement.bindLong(11, entity.updatedAt)
      }
    }
    this.__updateAdapterOfAchievementEntity = object :
        EntityDeletionOrUpdateAdapter<AchievementEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `achievement` SET `id` = ?,`category` = ?,`threshold` = ?,`description` = ?,`icon` = ?,`pointsAwarded` = ?,`unlocked` = ?,`unlockedAt` = ?,`currentProgress` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: AchievementEntity) {
        statement.bindLong(1, entity.id)
        val _tmp: String = __converters.achievementCategoryToString(entity.category)
        statement.bindString(2, _tmp)
        statement.bindLong(3, entity.threshold.toLong())
        statement.bindString(4, entity.description)
        val _tmpIcon: String? = entity.icon
        if (_tmpIcon == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpIcon)
        }
        statement.bindLong(6, entity.pointsAwarded.toLong())
        val _tmp_1: Int = if (entity.unlocked) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        val _tmpUnlockedAt: Long? = entity.unlockedAt
        if (_tmpUnlockedAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpUnlockedAt)
        }
        statement.bindLong(9, entity.currentProgress.toLong())
        statement.bindLong(10, entity.createdAt)
        statement.bindLong(11, entity.updatedAt)
        statement.bindLong(12, entity.id)
      }
    }
  }

  public override suspend fun insertAll(achievements: List<AchievementEntity>): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __insertionAdapterOfAchievementEntity.insert(achievements)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(achievement: AchievementEntity): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __updateAdapterOfAchievementEntity.handle(achievement)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override fun observeAll(): Flow<List<AchievementEntity>> {
    val _sql: String = "SELECT * FROM achievement ORDER BY unlocked DESC, category, threshold"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("achievement"), object :
        Callable<List<AchievementEntity>> {
      public override fun call(): List<AchievementEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfCategory: Int = getColumnIndexOrThrow(_cursor, "category")
          val _cursorIndexOfThreshold: Int = getColumnIndexOrThrow(_cursor, "threshold")
          val _cursorIndexOfDescription: Int = getColumnIndexOrThrow(_cursor, "description")
          val _cursorIndexOfIcon: Int = getColumnIndexOrThrow(_cursor, "icon")
          val _cursorIndexOfPointsAwarded: Int = getColumnIndexOrThrow(_cursor, "pointsAwarded")
          val _cursorIndexOfUnlocked: Int = getColumnIndexOrThrow(_cursor, "unlocked")
          val _cursorIndexOfUnlockedAt: Int = getColumnIndexOrThrow(_cursor, "unlockedAt")
          val _cursorIndexOfCurrentProgress: Int = getColumnIndexOrThrow(_cursor, "currentProgress")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: MutableList<AchievementEntity> =
              ArrayList<AchievementEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: AchievementEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpCategory: AchievementCategory
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfCategory)
            _tmpCategory = __converters.stringToAchievementCategory(_tmp)
            val _tmpThreshold: Int
            _tmpThreshold = _cursor.getInt(_cursorIndexOfThreshold)
            val _tmpDescription: String
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription)
            val _tmpIcon: String?
            if (_cursor.isNull(_cursorIndexOfIcon)) {
              _tmpIcon = null
            } else {
              _tmpIcon = _cursor.getString(_cursorIndexOfIcon)
            }
            val _tmpPointsAwarded: Int
            _tmpPointsAwarded = _cursor.getInt(_cursorIndexOfPointsAwarded)
            val _tmpUnlocked: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfUnlocked)
            _tmpUnlocked = _tmp_1 != 0
            val _tmpUnlockedAt: Long?
            if (_cursor.isNull(_cursorIndexOfUnlockedAt)) {
              _tmpUnlockedAt = null
            } else {
              _tmpUnlockedAt = _cursor.getLong(_cursorIndexOfUnlockedAt)
            }
            val _tmpCurrentProgress: Int
            _tmpCurrentProgress = _cursor.getInt(_cursorIndexOfCurrentProgress)
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _item =
                AchievementEntity(_tmpId,_tmpCategory,_tmpThreshold,_tmpDescription,_tmpIcon,_tmpPointsAwarded,_tmpUnlocked,_tmpUnlockedAt,_tmpCurrentProgress,_tmpCreatedAt,_tmpUpdatedAt)
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
