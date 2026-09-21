package com.example.ringapp.domain.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.ringapp.data.PreferencesDataStore
import com.example.ringapp.data.repository.HabitRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepTrackerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitRepository: HabitRepository,
    private val preferences: PreferencesDataStore
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private var lastAccelY = 0f
    private var shakeThreshold = 12f
    private var lastStepTime = 0L

    fun startTracking() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACTIVITY_RECOGNITION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.e("StepTracker", "Activity recognition permission not granted")
                    return
                }
            }
            
            stepSensor?.let {
                val registered = sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                Log.d("StepTracker", "Step counter sensor registered: $registered")
            } ?: Log.e("StepTracker", "Step counter sensor not available")

            detectorSensor?.let {
                val registered = sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                Log.d("StepTracker", "Step detector sensor registered: $registered")
            }

            accelSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        } catch (e: Exception) {
            Log.e("StepTracker", "Error starting step tracking", e)
        }
    }

    fun stopTracking() {
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            Log.e("StepTracker", "Error stopping step tracking", e)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalStepsSinceReboot = event.values[0].toInt()
                updateSteps(totalStepsSinceReboot)
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                incrementStepManually()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val y = event.values[1]
                val deltaY = Math.abs(y - lastAccelY)
                if (deltaY > shakeThreshold) {
                    val now = System.currentTimeMillis()
                    if (now - lastStepTime > 350) { 
                        incrementStepManually()
                        lastStepTime = now
                    }
                }
                lastAccelY = y
            }
        }
    }

    private fun incrementStepManually() {
        scope.launch {
            try {
                val current = habitRepository.observeActive().first().find { it.isStepsHabit() }
                val date = todayTimestamp()
                val existing = habitRepository.getProgressForDate(current?.id ?: 0, date)
                val newSteps = (existing?.actual?.toInt() ?: 0) + 1
                Log.d("StepTracker", "Incrementing steps to $newSteps")
                habitRepository.recordSteps(newSteps)
            } catch (e: Exception) {
                Log.e("StepTracker", "Error with manual increment", e)
            }
        }
    }

    private fun updateSteps(totalStepsSinceReboot: Int) {
        scope.launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = sdf.format(Date())
                
                val savedDay = preferences.stepsDay.first()
                var baseSteps = preferences.stepsBase.first()

                if (today != savedDay) {
                    baseSteps = totalStepsSinceReboot
                    preferences.setStepsDay(today)
                    preferences.setStepsBase(baseSteps)
                } else if (baseSteps == -1 || totalStepsSinceReboot < baseSteps) {
                    baseSteps = totalStepsSinceReboot
                    preferences.setStepsBase(baseSteps)
                }

                val dailySteps = (totalStepsSinceReboot - baseSteps).coerceAtLeast(0)
                Log.d("StepTracker", "Steps update (Counter): Total=$totalStepsSinceReboot, Base=$baseSteps, Daily=$dailySteps")
                habitRepository.recordSteps(dailySteps)
            } catch (e: Exception) {
                Log.e("StepTracker", "Error processing step sensor change", e)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    
    private fun todayTimestamp(): Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
