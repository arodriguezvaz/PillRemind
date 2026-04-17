package com.andres.pillremind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.repository.PerfilRepositoryImpl
import com.andres.pillremind.ui.screens.PillRemind
import com.andres.pillremind.ui.screens.crearperfil.CrearPerfilScreen
import com.andres.pillremind.ui.screens.crearperfil.CrearPerfilViewModel
import com.andres.pillremind.ui.screens.perfiles.PerfilesViewModel
import com.andres.pillremind.ui.theme.PillRemindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PillRemindTheme {
                PillRemind(

                )
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