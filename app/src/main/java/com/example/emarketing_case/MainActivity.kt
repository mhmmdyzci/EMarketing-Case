package com.example.emarketing_case

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.emarketing_case.presentation.EMarketingCaseApp
import com.example.emarketing_case.ui.theme.EMarketingCaseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EMarketingCaseTheme {
                EMarketingCaseApp()
            }
        }
    }
}
