package com.example.ringapp.`data`.local.dao

import android.database.Cursor
import androidx.room.CoroutinesRoom
import androidx.room.EntityInsertionAdapter
import androidx.room.RoomDatabase
import androidx.room.RoomSQLiteQuery
import androidx.room.RoomSQLiteQuery.Companion.acquire
import androidx.room.SharedSQLiteStatement
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteStatement
import com.example.ringapp.`data`.local.entities.AnalyticsDailyEntity
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
public class AnalyticsDailyDao_Impl(
  __db: RoomDatabase,
) : AnalyticsDailyDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfAnalyticsDailyEntity: EntityInsertionAdapter<AnalyticsDailyEntity>

  private val __preparedStmtOfClear: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfAnalyticsDailyEntity = object :
        EntityInsertionAdapter<AnalyticsDailyEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `analytics_daily` (`id`,`date`,`habitScore`,`taskScore`,`consistencyScore`,`streakScore`,`productivityScore`,`completionRate`,`habitCompletions`,`taskCompletions`,`activeHabits`,`activeTasks`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: AnalyticsDailyEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.date)
        statement.bindLong(3, entity.habitScore.toLong())
        statement.bindLong(4, entity.taskScore.toLong())
        statement.bindLong(5, entity.consistencyScore.toLong())
        statement.bindLong(6, entity.streakScore.toLong())
        statement.bindLong(7, entity.productivityScore.toLong())
        statement.bindLong(8, entity.completionRate.toLong())
        statement.bindLong(9, entity.habitCompletions.toLong())
        statement.bindLong(10, entity.taskCompletions.toLong())
        statement.bindLong(11, entity.activeHabits.toLong())
        statement.bindLong(12, entity.activeTasks.toLong())
        statement.bindLong(13, entity.createdAt)
        statement.bindLong(14, entity.updatedAt)
      }
    }
    this.__preparedStmtOfClear = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM analytics_daily"
        return _query
      }
    }
  }

  public override suspend fun upsert(day: AnalyticsDailyEntity): Unit = CoroutinesRoom.execute(__db,
      true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __insertionAdapterOfAnalyticsDailyEntity.insert(day)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun clear(): Unit = CoroutinesRoom.execute(__db, true, object :
      Callable<Unit> {
    public override fun call() {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfClear.acquire()
      try {
        __db.beginTransaction()
        try {
          _stmt.executeUpdateDelete()
          __db.setTransactionSuccessful()
        } finally {
          __db.endTransaction()
        }
      } finally {
        __preparedStmtOfClear.release(_stmt)
      }
    }
  })

  public override fun observeRange(from: Long, to: Long): Flow<List<AnalyticsDailyEntity>> {
    val _sql: String = "SELECT * FROM analytics_daily WHERE date BETWEEN ? AND ? ORDER BY date"
    val _statement: RoomSQLiteQuery = acquire(_sql, 2)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, from)
    _argIndex = 2
    _statement.bindLong(_argIndex, to)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("analytics_daily"), object :
        Callable<List<AnalyticsDailyEntity>> {
      public override fun call(): List<AnalyticsDailyEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfDate: Int = getColumnIndexOrThrow(_cursor, "date")
          val _cursorIndexOfHabitScore: Int = getColumnIndexOrThrow(_cursor, "habitScore")
          val _cursorIndexOfTaskScore: Int = getColumnIndexOrThrow(_cursor, "taskScore")
          val _cursorIndexOfConsistencyScore: Int = getColumnIndexOrThrow(_cursor,
              "consistencyScore")
          val _cursorIndexOfStreakScore: Int = getColumnIndexOrThrow(_cursor, "streakScore")
          val _cursorIndexOfProductivityScore: Int = getColumnIndexOrThrow(_cursor,
              "productivityScore")
          val _cursorIndexOfCompletionRate: Int = getColumnIndexOrThrow(_cursor, "completionRate")
          val _cursorIndexOfHabitCompletions: Int = getColumnIndexOrThrow(_cursor,
              "habitCompletions")
          val _cursorIndexOfTaskCompletions: Int = getColumnIndexOrThrow(_cursor, "taskCompletions")
          val _cursorIndexOfActiveHabits: Int = getColumnIndexOrThrow(_cursor, "activeHabits")
          val _cursorIndexOfActiveTasks: Int = getColumnIndexOrThrow(_cursor, "activeTasks")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: MutableList<AnalyticsDailyEntity> =
              ArrayList<AnalyticsDailyEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: AnalyticsDailyEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpDate: Long
            _tmpDate = _cursor.getLong(_cursorIndexOfDate)
            val _tmpHabitScore: Int
            _tmpHabitScore = _cursor.getInt(_cursorIndexOfHabitScore)
            val _tmpTaskScore: Int
            _tmpTaskScore = _cursor.getInt(_cursorIndexOfTaskScore)
            val _tmpConsistencyScore: Int
            _tmpConsistencyScore = _cursor.getInt(_cursorIndexOfConsistencyScore)
            val _tmpStreakScore: Int
            _tmpStreakScore = _cursor.getInt(_cursorIndexOfStreakScore)
            val _tmpProductivityScore: Int
            _tmpProductivityScore = _cursor.getInt(_cursorIndexOfProductivityScore)
            val _tmpCompletionRate: Int
            _tmpCompletionRate = _cursor.getInt(_cursorIndexOfCompletionRate)
            val _tmpHabitCompletions: Int
            _tmpHabitCompletions = _cursor.getInt(_cursorIndexOfHabitCompletions)
            val _tmpTaskCompletions: Int
            _tmpTaskCompletions = _cursor.getInt(_cursorIndexOfTaskCompletions)
            val _tmpActiveHabits: Int
            _tmpActiveHabits = _cursor.getInt(_cursorIndexOfActiveHabits)
            val _tmpActiveTasks: Int
            _tmpActiveTasks = _cursor.getInt(_cursorIndexOfActiveTasks)
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _item =
                AnalyticsDailyEntity(_tmpId,_tmpDate,_tmpHabitScore,_tmpTaskScore,_tmpConsistencyScore,_tmpStreakScore,_tmpProductivityScore,_tmpCompletionRate,_tmpHabitCompletions,_tmpTaskCompletions,_tmpActiveHabits,_tmpActiveTasks,_tmpCreatedAt,_tmpUpdatedAt)
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
