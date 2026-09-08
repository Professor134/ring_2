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
import com.example.ringapp.`data`.local.entities.ProfileEntity
import com.example.ringapp.`data`.local.entities.ThemeMode
import java.lang.Class
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class ProfileDao_Impl(
  __db: RoomDatabase,
) : ProfileDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfProfileEntity: EntityInsertionAdapter<ProfileEntity>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertionAdapterOfProfileEntity = object : EntityInsertionAdapter<ProfileEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `profile` (`userId`,`name`,`avatarColor`,`photoUri`,`dateOfBirth`,`gender`,`themePreference`,`language`,`onboardingComplete`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: ProfileEntity) {
        statement.bindLong(1, entity.userId.toLong())
        statement.bindString(2, entity.name)
        statement.bindLong(3, entity.avatarColor.toLong())
        val _tmpPhotoUri: String? = entity.photoUri
        if (_tmpPhotoUri == null) {
          statement.bindNull(4)
        } else {
          statement.bindString(4, _tmpPhotoUri)
        }
        val _tmpDateOfBirth: Long? = entity.dateOfBirth
        if (_tmpDateOfBirth == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpDateOfBirth)
        }
        val _tmpGender: String? = entity.gender
        if (_tmpGender == null) {
          statement.bindNull(6)
        } else {
          statement.bindString(6, _tmpGender)
        }
        val _tmp: String = __converters.themeModeToString(entity.themePreference)
        statement.bindString(7, _tmp)
        statement.bindString(8, entity.language)
        val _tmp_1: Int = if (entity.onboardingComplete) 1 else 0
        statement.bindLong(9, _tmp_1.toLong())
        statement.bindLong(10, entity.createdAt)
        statement.bindLong(11, entity.updatedAt)
      }
    }
  }

  public override suspend fun insert(profile: ProfileEntity): Unit = CoroutinesRoom.execute(__db,
      true, object : Callable<Unit> {
    public override fun call() {
      __db.beginTransaction()
      try {
        __insertionAdapterOfProfileEntity.insert(profile)
        __db.setTransactionSuccessful()
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun getCurrent(): ProfileEntity? {
    val _sql: String = "SELECT * FROM profile WHERE userId = 1 LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<ProfileEntity?> {
      public override fun call(): ProfileEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfUserId: Int = getColumnIndexOrThrow(_cursor, "userId")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfAvatarColor: Int = getColumnIndexOrThrow(_cursor, "avatarColor")
          val _cursorIndexOfPhotoUri: Int = getColumnIndexOrThrow(_cursor, "photoUri")
          val _cursorIndexOfDateOfBirth: Int = getColumnIndexOrThrow(_cursor, "dateOfBirth")
          val _cursorIndexOfGender: Int = getColumnIndexOrThrow(_cursor, "gender")
          val _cursorIndexOfThemePreference: Int = getColumnIndexOrThrow(_cursor, "themePreference")
          val _cursorIndexOfLanguage: Int = getColumnIndexOrThrow(_cursor, "language")
          val _cursorIndexOfOnboardingComplete: Int = getColumnIndexOrThrow(_cursor,
              "onboardingComplete")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: ProfileEntity?
          if (_cursor.moveToFirst()) {
            val _tmpUserId: Int
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpAvatarColor: Int
            _tmpAvatarColor = _cursor.getInt(_cursorIndexOfAvatarColor)
            val _tmpPhotoUri: String?
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri)
            }
            val _tmpDateOfBirth: Long?
            if (_cursor.isNull(_cursorIndexOfDateOfBirth)) {
              _tmpDateOfBirth = null
            } else {
              _tmpDateOfBirth = _cursor.getLong(_cursorIndexOfDateOfBirth)
            }
            val _tmpGender: String?
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender)
            }
            val _tmpThemePreference: ThemeMode
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfThemePreference)
            _tmpThemePreference = __converters.stringToThemeMode(_tmp)
            val _tmpLanguage: String
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage)
            val _tmpOnboardingComplete: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfOnboardingComplete)
            _tmpOnboardingComplete = _tmp_1 != 0
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _result =
                ProfileEntity(_tmpUserId,_tmpName,_tmpAvatarColor,_tmpPhotoUri,_tmpDateOfBirth,_tmpGender,_tmpThemePreference,_tmpLanguage,_tmpOnboardingComplete,_tmpCreatedAt,_tmpUpdatedAt)
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

  public override fun observeCurrent(): Flow<ProfileEntity?> {
    val _sql: String = "SELECT * FROM profile WHERE userId = 1 LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("profile"), object :
        Callable<ProfileEntity?> {
      public override fun call(): ProfileEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfUserId: Int = getColumnIndexOrThrow(_cursor, "userId")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfAvatarColor: Int = getColumnIndexOrThrow(_cursor, "avatarColor")
          val _cursorIndexOfPhotoUri: Int = getColumnIndexOrThrow(_cursor, "photoUri")
          val _cursorIndexOfDateOfBirth: Int = getColumnIndexOrThrow(_cursor, "dateOfBirth")
          val _cursorIndexOfGender: Int = getColumnIndexOrThrow(_cursor, "gender")
          val _cursorIndexOfThemePreference: Int = getColumnIndexOrThrow(_cursor, "themePreference")
          val _cursorIndexOfLanguage: Int = getColumnIndexOrThrow(_cursor, "language")
          val _cursorIndexOfOnboardingComplete: Int = getColumnIndexOrThrow(_cursor,
              "onboardingComplete")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: ProfileEntity?
          if (_cursor.moveToFirst()) {
            val _tmpUserId: Int
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpAvatarColor: Int
            _tmpAvatarColor = _cursor.getInt(_cursorIndexOfAvatarColor)
            val _tmpPhotoUri: String?
            if (_cursor.isNull(_cursorIndexOfPhotoUri)) {
              _tmpPhotoUri = null
            } else {
              _tmpPhotoUri = _cursor.getString(_cursorIndexOfPhotoUri)
            }
            val _tmpDateOfBirth: Long?
            if (_cursor.isNull(_cursorIndexOfDateOfBirth)) {
              _tmpDateOfBirth = null
            } else {
              _tmpDateOfBirth = _cursor.getLong(_cursorIndexOfDateOfBirth)
            }
            val _tmpGender: String?
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender)
            }
            val _tmpThemePreference: ThemeMode
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfThemePreference)
            _tmpThemePreference = __converters.stringToThemeMode(_tmp)
            val _tmpLanguage: String
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage)
            val _tmpOnboardingComplete: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfOnboardingComplete)
            _tmpOnboardingComplete = _tmp_1 != 0
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _result =
                ProfileEntity(_tmpUserId,_tmpName,_tmpAvatarColor,_tmpPhotoUri,_tmpDateOfBirth,_tmpGender,_tmpThemePreference,_tmpLanguage,_tmpOnboardingComplete,_tmpCreatedAt,_tmpUpdatedAt)
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
