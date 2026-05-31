package com.andres.pillremind.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.components.settings.SettingsCard
import com.andres.pillremind.ui.components.settings.SettingsInfoItem
import com.andres.pillremind.ui.components.settings.SettingsLinkItem
import com.andres.pillremind.ui.components.settings.SettingsToggleItem

@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {

    val usuario by settingsViewModel.usuario.collectAsStateWithLifecycle()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { /* tu topbar con título "Ajustes" y back */ }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    checked = usuario?.esPremium ?: false,
                    onCheckedChange = { settingsViewModel.togglePremium(it) }
                )
                if (usuario?.email != null) {
                    Spacer(Modifier.height(8.dp))
                    AppButton(
                        text = "Cerrar sesión",
                        onClick = { settingsViewModel.logout() },
                        variant = ButtonVariant.Danger,
                        size = ButtonSize.Large
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