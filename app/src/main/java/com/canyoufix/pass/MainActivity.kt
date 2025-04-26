package com.canyoufix.pass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.canyoufix.pass.ui.theme.PassTheme
import com.canyoufix.ui.navigation.AuthNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PassTheme {
                AuthNavigation()
            }
        }
    }
}