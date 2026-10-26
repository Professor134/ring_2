package com.example.ringapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.ringapp.data.repository.HabitRepository
import com.example.ringapp.domain.engine.StepTrackerManager
import com.example.ringapp.ui.AppViewModel
import com.example.ringapp.ui.theme.RingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var stepTrackerManager: StepTrackerManager
    @Inject lateinit var habitRepository: HabitRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkPermissions()
        
        lifecycleScope.launch { 
            try {
                habitRepository.seedDefaults()
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error seeding defaults", e)
            }
        }
        
        stepTrackerManager.startTracking()
        
        setContent {
            val state by hiltViewModel<AppViewModel>().state.collectAsStateWithLifecycle()
            RingTheme(state.theme) { RingNavigation() }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 100)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stepTrackerManager.stopTracking()
    }
}
