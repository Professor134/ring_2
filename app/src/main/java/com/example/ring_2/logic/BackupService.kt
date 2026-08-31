package com.example.ring_2.logic

import android.content.Context
import android.net.Uri
import com.example.ring_2.data.model.HabitEntity
import com.example.ring_2.data.model.TaskEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileInputStream
import java.io.FileOutputStream

object BackupService {
    
    fun exportDatabase(context: Context, destinationUri: Uri) {
        val dbFile = context.getDatabasePath("ring_database")
        if (dbFile.exists()) {
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output)
                }
            }
        }
    }
    
    fun importDatabase(context: Context, sourceUri: Uri) {
        val dbFile = context.getDatabasePath("ring_database")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun exportAsJson(habits: List<HabitEntity>, tasks: List<TaskEntity>): String {
        val data = mapOf(
            "habits" to habits,
            "tasks" to tasks
        )
        return Json.encodeToString(data)
    }

    fun exportAsCsv(habits: List<HabitEntity>): String {
        val sb = StringBuilder()
        sb.append("Name,Type,Target,Unit,Streak\n")
        habits.forEach {
            sb.append("${it.name},${it.type},${it.target},${it.unit},${it.currentStreak}\n")
        }
        return sb.toString()
    }
}
