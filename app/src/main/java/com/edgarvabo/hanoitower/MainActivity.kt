package com.edgarvabo.hanoitower

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.edgarvabo.hanoitower.presentation.ui.HanoiScreen
import com.edgarvabo.hanoitower.ui.theme.HanoiTowerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // ¡No olvides esta anotación!
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HanoiTowerTheme {
                HanoiScreen()
            }
        }
    }
}