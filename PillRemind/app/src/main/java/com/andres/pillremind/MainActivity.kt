package com.andres.pillremind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.ui.theme.PillRemindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PillRemindTheme {
                Surface(

                ) {

                }
            }
        }
    }
}


@Preview(
    name = "MyComposable Preview",
    widthDp = 360,
    heightDp = 640,
    fontScale = 1.2f,
    showBackground = true
)
@Composable
fun GreetingPreview() {
    PillRemindTheme {
        MainScreen()
    }
}