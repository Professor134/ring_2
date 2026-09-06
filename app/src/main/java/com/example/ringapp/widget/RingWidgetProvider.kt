package com.example.ringapp.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.ringapp.R

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
