package com.andres.pillremind.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.components.FormField


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                authViewModel.resetState()
                onLoginSuccess()
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FormField(label = "Email") {
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "tu@email.com",
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            FormField(label = "Contraseña") {
                AppTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Contraseña",
                    //isPassword = true,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            AppButton(
                text = "Iniciar sesión",
                onClick = { authViewModel.login(email, password) },
                size = ButtonSize.Large,
                isLoading = uiState is AuthUiState.Loading,
                enabled = email.isNotBlank() && password.isNotBlank()
            )

            Spacer(Modifier.height(16.dp))

            AppButton(
                text = "¿No tienes cuenta? Regístrate",
                onClick = onNavigateToRegister,
                size = ButtonSize.Large,
                variant = ButtonVariant.Ghost
            )
        }
        if (uiState is AuthUiState.ConflictoPerfiles) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Ya tienes datos guardados") },
                text = { Text("¿Qué quieres hacer con los datos de este dispositivo?") },
                confirmButton = {
                    TextButton(onClick = { authViewModel.resolverConflicto(mantenerLocales = true) }) {
                        Text("Subir mis datos")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { authViewModel.resolverConflicto(mantenerLocales = false) }) {
                        Text("Descargar de la nube")
                    }
                }
            )
        }
    }
}