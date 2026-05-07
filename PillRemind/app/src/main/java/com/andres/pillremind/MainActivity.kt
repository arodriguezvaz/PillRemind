package com.andres.pillremind

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import com.andres.pillremind.navigation.RootNavigationGraph
import com.andres.pillremind.ui.theme.PillRemindTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal de la aplicación.
 *
 * Se encarga de inicializar la interfaz Compose y solicitar
 * los permisos necesarios para notificaciones y alarmas exactas.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()
        requestExactAlarmPermissionIfNeeded()

        setContent {
            PillRemindTheme {
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

    /**
     * Solicita permiso para programar alarmas exactas en Android 12 o superior.
     * Si no está concedido, redirige al usuario a la pantalla de configuración del sistema.
     */
    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:$packageName".toUri()
                }
                startActivity(intent)
            }
        }
    }
}