package com.android.example.eventpop

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        val initialEventDetailId = intent.getStringExtra(EXTRA_EVENT_DETAIL_ID)
        setContent {
            EventPopTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EventPopNavGraph(initialEventDetailId = initialEventDetailId)
                }
            }
        }
    }

    companion object {
        const val EXTRA_EVENT_DETAIL_ID = "com.android.example.eventpop.EXTRA_EVENT_DETAIL_ID"
    }
}