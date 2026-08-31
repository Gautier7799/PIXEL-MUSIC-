package com.example.pixelmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pixelmusic.ui.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            // الآن التطبيق يبدأ من خريطة التوجيه بدلاً من شاشة واحدة ثابتة
            AppNavigation() 
        }
    }
}
