package com.example.educloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.educloud.theme.EduCloudTheme
import com.example.educloud.ui.EduCloudNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduCloudTheme {
                EduCloudNavigation()
            }
        }
    }
}
