package com.android.example.eventpop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.example.eventpop.ui.navigation.EventPopNavGraph
import com.android.example.eventpop.ui.theme.EventPopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventPopTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EventPopNavGraph()
                }
            }
        }
    }
}