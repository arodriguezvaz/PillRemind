package com.andres.pillremind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.andres.pillremind.navigation.RootNavigationGraph
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Actividad principal de la aplicación
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PillRemindTheme {
                RootNavigationGraph(navController = rememberNavController())
            }
        }
    }
}