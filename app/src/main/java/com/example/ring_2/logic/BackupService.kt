package com.example.ring_2.logic

import android.content.Context
import android.net.Uri
import com.example.ring_2.data.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileInputStream
import java.io.FileOutputStream

@Serializable
data class FullBackupData(
    val habits: List<HabitEntity>,
    val progress: List<HabitProgressEntity>,
    val tasks: List<TaskEntity>,
    val transactions: List<PointTransactionEntity>,
    val userProgress: UserProgressEntity?,
    val profile: ProfileEntity?,
    val categories: List<Category>,
    val achievements: List<Achievement>,
    val notifications: List<NotificationEntity>
)

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

    fun exportAsJson(
        habits: List<HabitEntity>, 
        progress: List<HabitProgressEntity>,
        tasks: List<TaskEntity>,
        transactions: List<PointTransactionEntity>,
        userProgress: UserProgressEntity?,
        profile: ProfileEntity?,
        categories: List<Category>,
        achievements: List<Achievement>,
        notifications: List<NotificationEntity>
    ): String {
        val data = FullBackupData(
            habits, progress, tasks, transactions, userProgress, profile, categories, achievements, notifications
        )
        val json = Json { prettyPrint = true }
        return json.encodeToString(data)
    }

    fun exportAsCsv(habits: List<HabitEntity>): String {
        val sb = StringBuilder()
        sb.append("Name,Description,CategoryID,Type,Target,Unit,RepeatType,StartDate,Color,Streak,BestStreak,TotalCompletions\n")
        habits.forEach {
            sb.append("${it.name},${it.description},${it.categoryId},${it.type},${it.target},${it.unit},${it.repeatType},${it.startDate},${it.color},${it.currentStreak},${it.bestStreak},${it.totalCompletions}\n")
        }
        return sb.toString()
    }
}
