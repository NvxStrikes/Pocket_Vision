package com.pocketvision.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pocketvision.app.ui.CameraScreen
import com.pocketvision.app.ui.theme.PocketVisionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketVisionTheme {
                CameraScreen()
            }
        }
    }
}
