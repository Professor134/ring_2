// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\widget\RingWidgetProvider.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.ringapp.R

// Handles the ring widget provider logic for this component without modifying runtime behavior.
// Represents the ring widget provider structure used by the application.
class RingWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.ring_widget).apply {
                setTextViewText(R.id.widget_summary, "Open RING to see today's progress")
            }
            manager.updateAppWidget(id, views)
        }
    }
}
