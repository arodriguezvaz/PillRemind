package com.andres.pillremind.ui.screens.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.components.settings.SettingsCard
import com.andres.pillremind.ui.components.settings.SettingsInfoItem
import com.andres.pillremind.ui.components.settings.SettingsLinkItem
import com.andres.pillremind.ui.components.settings.SettingsToggleItem
import com.andres.pillremind.ui.session.SessionViewModel

/**
 * Pantalla de ajustes de la aplicación.
 *
 * Permite gestionar la configuración del usuario, incluyendo modo oscuro,
 * estado de la sesión y acciones como cierre de sesión o navegación.
 *
 * Observa el estado del usuario y reacciona a eventos como logout.
 *
 * @param onNavigateToLogin Callback para navegar a la pantalla de login.
 * @param onBack Callback para volver a la pantalla anterior.
 * @param settingsViewModel ViewModel encargado de la lógica de ajustes.
 * @param onLogout Callback ejecutado cuando el usuario cierra sesión.
 * @param sessionViewModel ViewModel encargado de la gestión de sesión activa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {

    val usuario by settingsViewModel.usuario.collectAsStateWithLifecycle()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    var esPremium by remember(usuario) { mutableStateOf(usuario?.esPremium ?: false) }

    val context = LocalContext.current
    var exactAlarmsAllowed by remember { mutableStateOf(canScheduleExactAlarms(context)) }

    // Al volver de los ajustes del sistema se refresca el estado del permiso.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        exactAlarmsAllowed = canScheduleExactAlarms(context)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is SettingsUiState.LoggedOut -> {
                settingsViewModel.resetState()
                sessionViewModel.reiniciarSesion()
                onLogout()
            }

            else -> {}
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Tarjeta de sesión
            SettingsCard {
                usuario?.email?.let {
                    SettingsInfoItem(label = "Sesión iniciada como:", value = it)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                SettingsToggleItem(
                    label = "Modo premium",
                    checked = esPremium,
                    onCheckedChange = {
                        esPremium = it
                        settingsViewModel.togglePremium(it)
                    },
                    enabled = usuario?.email != null
                )
                if (usuario?.email != null) {
                    Spacer(Modifier.height(8.dp))
                    AppButton(
                        text = "Cerrar sesión",
                        onClick = { settingsViewModel.logout() },
                        variant = ButtonVariant.Danger,
                        size = ButtonSize.Large,
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                    AppButton(
                        text = "Iniciar sesión",
                        onClick = onNavigateToLogin,
                        variant = ButtonVariant.Primary,
                        size = ButtonSize.Large
                    )
                }
            }

            // Tarjeta de apariencia
            SettingsCard {
                SettingsToggleItem(
                    label = "Modo oscuro",
                    checked = isDarkMode,
                    onCheckedChange = { settingsViewModel.toggleDarkMode(it) }
                )
            }

            // Aviso de alarmas exactas: no se abre Ajustes automáticamente.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAlarmsAllowed) {
                SettingsCard(title = "Recordatorios") {
                    Text(
                        text = "Las alarmas exactas están desactivadas. Sin este permiso " +
                            "los recordatorios pueden retrasarse.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    AppButton(
                        text = "Permitir alarmas exactas",
                        onClick = { openExactAlarmSettings(context) },
                        size = ButtonSize.Large
                    )
                }
            }

            // Tarjeta legal
            SettingsCard(title = "Legal") {
                SettingsLinkItem("Política de Privacidad", { /* abrir url */ })
                SettingsLinkItem("Términos y condiciones de uso", { /* abrir url */ })
                SettingsLinkItem("Política de Suscripciones / Pagos", { /* abrir url */ })
                SettingsLinkItem("Aviso Legal", { /* abrir url */ })
            }
        }
    }
}

/**
 * Comprueba si la app puede programar alarmas exactas.
 *
 * En Android 11 y anteriores el permiso no existe, así que se considera concedido.
 */
private fun canScheduleExactAlarms(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
}

/**
 * Abre la pantalla del sistema para conceder el permiso de alarmas exactas.
 */
@RequiresApi(Build.VERSION_CODES.S)
private fun openExactAlarmSettings(context: Context) {
    val packageUri = "package:${context.packageName}".toUri()
    val exactAlarmIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri)

    runCatching { context.startActivity(exactAlarmIntent) }
        .onFailure {
            // Algunos fabricantes no exponen la pantalla anterior; se usa la ficha
            // de la app como alternativa.
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
                )
            }
        }
}
