package com.example.redx

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.redx.theme.RedXTheme
import com.example.redx.ui.RedXMainScreen
import com.example.redx.ui.RedXViewModel
import com.example.redx.ui.components.LocalAutoplayVideos

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The UI is always dark, so system bar icons must always be light even when the
        // phone itself is in light mode (the default style would make them invisible).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            val viewModel: RedXViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            RedXTheme(appTheme = uiState.appTheme, fontScale = uiState.fontScale.scale) {
                CompositionLocalProvider(LocalAutoplayVideos provides uiState.autoplayVideos) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        RedXMainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
