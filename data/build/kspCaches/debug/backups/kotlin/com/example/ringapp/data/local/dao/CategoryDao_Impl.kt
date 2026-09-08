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
import com.example.ringapp.`data`.local.entities.CategoryEntity
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
public class CategoryDao_Impl(
  __db: RoomDatabase,
) : CategoryDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfCategoryEntity: EntityInsertionAdapter<CategoryEntity>
  init {
    this.__db = __db
    this.__insertionAdapterOfCategoryEntity = object : EntityInsertionAdapter<CategoryEntity>(__db)
        {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `category` (`id`,`name`,`color`,`createdAt`,`updatedAt`,`deletedAt`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: CategoryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindString(2, entity.name)
        statement.bindLong(3, entity.color.toLong())
        statement.bindLong(4, entity.createdAt)
        statement.bindLong(5, entity.updatedAt)
        val _tmpDeletedAt: Long? = entity.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpDeletedAt)
        }
      }
    }
  }

  public override suspend fun insertAll(categories: List<CategoryEntity>): Unit =
      CoroutinesRoom.execute(__db, true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __insertionAdapterOfCategoryEntity.insert(categories)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun insert(category: CategoryEntity): Long = CoroutinesRoom.execute(__db,
      true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfCategoryEntity.insertAndReturnId(category)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override fun observeAll(): Flow<List<CategoryEntity>> {
    val _sql: String = "SELECT * FROM category WHERE deletedAt IS NULL ORDER BY name"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("category"), object :
        Callable<List<CategoryEntity>> {
      public override fun call(): List<CategoryEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfColor: Int = getColumnIndexOrThrow(_cursor, "color")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _result: MutableList<CategoryEntity> = ArrayList<CategoryEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: CategoryEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpColor: Int
            _tmpColor = _cursor.getInt(_cursorIndexOfColor)
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
                CategoryEntity(_tmpId,_tmpName,_tmpColor,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt)
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
