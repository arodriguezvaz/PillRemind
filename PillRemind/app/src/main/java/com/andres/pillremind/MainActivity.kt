package com.andres.pillremind

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.navigation.RootNavigationGraph
import com.andres.pillremind.ui.theme.PillRemindTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Actividad principal de la aplicación.
 *
 * Se encarga de inicializar la interfaz Compose y solicitar el permiso de
 * notificaciones. El permiso de alarmas exactas se gestiona desde Ajustes para
 * no interrumpir al usuario con una redirección automática.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

        setContent {
            val isDarkMode by sessionManager.isDarkMode.collectAsState(initial = false)
            PillRemindTheme(darkTheme = isDarkMode) {
                RootNavigationGraph(navController = rememberNavController())
            }
        }
    }

    // Lanzador para el permiso de notificaciones (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* isGranted -> puedes reaccionar aquí si quieres */ }

    /**
     * Solicita el permiso de notificaciones en Android 13 o superior si no está concedido.
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
