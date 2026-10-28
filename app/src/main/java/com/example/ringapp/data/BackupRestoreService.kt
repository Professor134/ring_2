// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\data\BackupRestoreService.kt
// Purpose: This source file was documented to explain its role without altering logic.
// -----------------------------------------------------------------------------

package com.example.ringapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Base64
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ringapp.data.db.AppDatabase
import com.example.ringapp.data.local.entities.TaskEntity
import com.example.ringapp.work.TaskReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
@Singleton
// Handles the backup restore service logic for this component without modifying runtime behavior.
// Represents the backup restore service structure used by the application.
class BackupRestoreService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val preferences: PreferencesDataStore
) {
    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val db = database.openHelper.readableDatabase
        val tables = tableNames(db)
        val tableData = JSONObject()
        tables.forEach { table ->
            val rows = JSONArray()
            db.query("SELECT * FROM ${quoteIdentifier(table)}").use { cursor ->
                while (cursor.moveToNext()) {
                    rows.put(cursor.toJsonObject())
                }
            }
            tableData.put(table, rows)
        }

        val settings = JSONObject()
        preferences.exportSnapshot().forEach { (key, value) -> settings.put(key, value) }
        val backup = JSONObject()
            .put("format", FORMAT_NAME)
            .put("formatVersion", FORMAT_VERSION)
            .put("createdAt", System.currentTimeMillis())
            .put("appVersion", context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown")
            .put("databaseVersion", db.query("PRAGMA user_version").use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            })
            .put("preferences", settings)
            .put("tables", tableData)
        val contents = backup.toString().toByteArray(StandardCharsets.UTF_8)
        if (contents.size > MAX_BACKUP_BYTES) {
            throw IOException("This backup is too large to export as one file.")
        }
        val output = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw IOException("Could not open the selected file for writing.")
        output.use { it.write(contents) }
    }

    suspend fun restoreFrom(uri: Uri) = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use(::readBackupBytes)
            ?: throw IOException("Could not open the selected backup file.")
        val backup = try {
            JSONObject(String(bytes, StandardCharsets.UTF_8))
        } catch (failure: Exception) {
            throw IOException("The selected file is not a valid RING backup.", failure)
        }
        if (backup.optString("format") != FORMAT_NAME || backup.optInt("formatVersion") != FORMAT_VERSION) {
            throw IOException("This backup format is not supported by this version of RING.")
        }

        val tables = backup.optJSONObject("tables")
            ?: throw IOException("The backup is missing its data tables.")
        val tableData = parseAndValidateTables(database.openHelper.readableDatabase, tables)
        val settings = parseAndValidateSettings(backup.optJSONObject("preferences")
            ?: throw IOException("The backup is missing its app settings."))
        val oldReminders = database.taskDao().activeReminders()

        replaceDatabase(tableData)
        try {
            preferences.replaceSnapshot(settings)
        } catch (failure: Exception) {
            throw IOException(
                "Your data was restored, but some app settings could not be restored.",
                failure
            )
        }
        refreshTaskReminders(oldReminders, database.taskDao().activeReminders())
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the replace database flow without changing runtime behavior.
    private fun replaceDatabase(tableData: Map<String, List<JSONObject>>) {
        val db = database.openHelper.writableDatabase
        val liveTables = tableNames(db).toSet()
        db.beginTransaction()
        try {
            db.execSQL("PRAGMA defer_foreign_keys = ON")
            deletionOrder(db, liveTables).forEach { table -> db.delete(table, null, null) }
            insertionOrder(db, tableData.keys).forEach { table ->
                tableData[table].orEmpty().forEach { row ->
                    val values = row.toContentValues()
                    val inserted = db.insert(table, SQLiteDatabase.CONFLICT_ABORT, values)
                    if (inserted == -1L) {
                        throw IOException("A record in the backup could not be restored.")
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private suspend fun refreshTaskReminders(previous: List<TaskEntity>, restored: List<TaskEntity>) {
        try {
            val restoredIds = restored.mapTo(mutableSetOf()) { it.id }
            previous.filterNot { it.id in restoredIds }.forEach { TaskReminderScheduler.cancel(context, it.id) }
            val remindersEnabled = preferences.taskRemindersEnabled.first()
            restored.forEach { task -> TaskReminderScheduler.sync(context, task, remindersEnabled) }
        } catch (failure: Exception) {
            throw IOException(
                "Your data was restored, but task reminders could not be refreshed.",
                failure
            )
        }
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the parse and validate tables flow without changing runtime behavior.
    private fun parseAndValidateTables(
        db: SupportSQLiteDatabase,
        tables: JSONObject
    ): Map<String, List<JSONObject>> {
        val liveTables = tableNames(db).toSet()
        val result = linkedMapOf<String, List<JSONObject>>()
        val keys = tables.keys()
        while (keys.hasNext()) {
            val table = keys.next()
            if (table !in liveTables) {
                throw IOException("The backup contains a data table this app does not recognize.")
            }
            val rows = tables.optJSONArray(table)
                ?: throw IOException("The backup contains invalid data for a table.")
            val columns = tableColumns(db, table)
            val validatedRows = ArrayList<JSONObject>(rows.length())
            for (rowIndex in 0 until rows.length()) {
                val row = rows.optJSONObject(rowIndex)
                    ?: throw IOException("The backup contains an invalid data record.")
                val rowKeys = row.keys()
                while (rowKeys.hasNext()) {
                    if (rowKeys.next() !in columns) {
                        throw IOException("The backup contains a column this app does not recognize.")
                    }
                }
                validatedRows += row
            }
            result[table] = validatedRows
        }
        val requiredTables = setOf("category", "habit", "profile", "task", "user_progress")
        if (!result.keys.containsAll(requiredTables)) {
            throw IOException("The backup is missing required RING data.")
        }
        return result
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the parse and validate settings flow without changing runtime behavior.
    private fun parseAndValidateSettings(settings: JSONObject): Map<String, Any> {
        val result = linkedMapOf<String, Any>()
        val keys = settings.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = settings.get(key)
            if (value === JSONObject.NULL) throw IOException("The backup contains an invalid app setting.")
            result[key] = value
        }
        return result
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the table names flow without changing runtime behavior.
    private fun tableNames(db: SupportSQLiteDatabase): List<String> {
        val result = mutableListOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type = 'table' ORDER BY name").use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(0)
                if (!name.startsWith("sqlite_") && name != "android_metadata" && name != "room_master_table") {
                    result += name
                }
            }
        }
        return result
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the table columns flow without changing runtime behavior.
    private fun tableColumns(db: SupportSQLiteDatabase, table: String): Set<String> {
        val columns = mutableSetOf<String>()
        db.query("PRAGMA table_info(${quoteIdentifier(table)})").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) columns += cursor.getString(nameIndex)
        }
        return columns
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the deletion order flow without changing runtime behavior.
    private fun deletionOrder(db: SupportSQLiteDatabase, tables: Set<String>): List<String> =
        tableOrder(db, tables, childrenFirst = true)

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the insertion order flow without changing runtime behavior.
    private fun insertionOrder(db: SupportSQLiteDatabase, tables: Set<String>): List<String> =
        tableOrder(db, tables, childrenFirst = false)

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the table order flow without changing runtime behavior.
    private fun tableOrder(
        db: SupportSQLiteDatabase,
        tables: Set<String>,
        childrenFirst: Boolean
    ): List<String> {
        val parents = tables.associateWith { table ->
            val references = mutableSetOf<String>()
            db.query("PRAGMA foreign_key_list(${quoteIdentifier(table)})").use { cursor ->
                val parentIndex = cursor.getColumnIndexOrThrow("table")
                while (cursor.moveToNext()) {
                    cursor.getString(parentIndex)?.takeIf { it in tables }?.let(references::add)
                }
            }
            references
        }
        val ordered = mutableListOf<String>()
        val visited = mutableSetOf<String>()
        // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.
// Manages the visit flow without changing runtime behavior.
        fun visit(table: String) {
            if (!visited.add(table)) return
            val dependencies = if (childrenFirst) {
                tables.filter { table in parents[it].orEmpty() }
            } else {
                parents[table].orEmpty().toList()
            }
            dependencies.forEach(::visit)
            ordered += table
        }
        tables.sorted().forEach(::visit)
        return ordered
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the cursor flow without changing runtime behavior.
    private fun Cursor.toJsonObject(): JSONObject {
        val row = JSONObject()
        for (index in 0 until columnCount) {
            val value: Any = when (getType(index)) {
                Cursor.FIELD_TYPE_NULL -> JSONObject.NULL
                Cursor.FIELD_TYPE_INTEGER -> getLong(index)
                Cursor.FIELD_TYPE_FLOAT -> getDouble(index)
                Cursor.FIELD_TYPE_STRING -> getString(index)
                Cursor.FIELD_TYPE_BLOB -> JSONObject().put(
                    BLOB_MARKER,
                    Base64.encodeToString(getBlob(index), Base64.NO_WRAP)
                )
                else -> throw IOException("A database value could not be exported.")
            }
            row.put(getColumnName(index), value)
        }
        return row
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the j s o n object flow without changing runtime behavior.
    private fun JSONObject.toContentValues(): ContentValues {
        val values = ContentValues()
        val keys = keys()
        while (keys.hasNext()) {
            val key = keys.next()
            when (val value = get(key)) {
                JSONObject.NULL -> values.putNull(key)
                is Boolean -> values.put(key, if (value) 1 else 0)
                is Int -> values.put(key, value)
                is Long -> values.put(key, value)
                is Double -> values.put(key, value)
                is String -> values.put(key, value)
                is JSONObject -> {
                    if (value.length() != 1 || !value.has(BLOB_MARKER)) {
                        throw IOException("The backup contains an unsupported value.")
                    }
                    val encoded = value.optString(BLOB_MARKER)
                    val decoded = try {
                        Base64.decode(encoded, Base64.DEFAULT)
                    } catch (failure: IllegalArgumentException) {
                        throw IOException("The backup contains invalid binary data.", failure)
                    }
                    values.put(key, decoded)
                }
                else -> throw IOException("The backup contains an unsupported value.")
            }
        }
        return values
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the read backup bytes flow without changing runtime behavior.
    private fun readBackupBytes(input: java.io.InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            if (total > MAX_BACKUP_BYTES) throw IOException("The selected backup file is too large.")
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    // Documentation note: this declaration is part of the application's structure and behavior remains unchanged.

// Manages the quote identifier flow without changing runtime behavior.
    private fun quoteIdentifier(identifier: String): String =
        "\"${identifier.replace("\"", "\"\"")}\""

    private companion object {
        const val FORMAT_NAME = "ring-backup"
        const val FORMAT_VERSION = 1
        const val MAX_BACKUP_BYTES = 100 * 1024 * 1024
        const val BLOB_MARKER = "__ring_blob_base64"
    }
}
