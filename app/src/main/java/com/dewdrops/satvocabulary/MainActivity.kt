package com.dewdrops.satvocabulary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dewdrops.satvocabulary.ui.navigation.AppNavigation
import com.dewdrops.satvocabulary.ui.theme.SatVocabularyTheme
import com.dewdrops.satvocabulary.ui.viewmodel.VocabularyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: VocabularyViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            SatVocabularyTheme(themeMode = themeMode) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
