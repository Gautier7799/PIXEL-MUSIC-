package com.example.pixelmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pixelmusic.ui.AppNavigation
import com.example.pixelmusic.ui.theme.PixelMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PixelMusicTheme {
                AppNavigation()
            }
        }
    }
}
