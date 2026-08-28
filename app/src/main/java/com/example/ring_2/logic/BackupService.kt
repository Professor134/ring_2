package com.example.ring_2.logic

import android.content.Context
import android.net.Uri
import com.example.ring_2.data.AppDatabase
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
        // App should be restarted after import
    }
}
