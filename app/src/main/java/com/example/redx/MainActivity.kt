package com.example.redx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.redx.theme.RedXTheme
import com.example.redx.ui.RedXMainScreen
import com.example.redx.ui.RedXViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: RedXViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            RedXTheme(appTheme = uiState.appTheme, fontScale = uiState.fontScale.scale) {
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
