package com.dewdrops.satvocabulary

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dewdrops.satvocabulary.ui.navigation.AppNavigation
import com.dewdrops.satvocabulary.ui.theme.SatVocabularyTheme
import com.dewdrops.satvocabulary.ui.viewmodel.VocabularyViewModel
import com.dewdrops.satvocabulary.utils.WordOfDayWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission granted or denied handling if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Schedule daily Word of the Day notification worker
        val dailyWorkRequest = PeriodicWorkRequestBuilder<WordOfDayWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "WordOfDayWork",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )

        val initialWordId = intent.getIntExtra("EXTRA_WORD_ID", -1)

        setContent {
            val viewModel: VocabularyViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            SatVocabularyTheme(themeMode = themeMode) {
                AppNavigation(viewModel = viewModel, initialWordId = if (initialWordId != -1) initialWordId else null)
            }
        }
    }
}
