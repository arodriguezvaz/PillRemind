package com.andres.pillremind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.navigation.RootNavigationGraph
import com.andres.pillremind.ui.screens.onboarding.OnboardingRoute
import com.andres.pillremind.ui.theme.PillRemindTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Actividad principal de la aplicación.
 *
 * Muestra la bienvenida de primer arranque hasta que el usuario la completa y,
 * a partir de ahí, el grafo de navegación normal. Los permisos de
 * notificaciones y alarmas exactas se solicitan desde esa bienvenida y desde
 * Ajustes, nunca de forma automática en cada arranque.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by sessionManager.isDarkMode
                .collectAsStateWithLifecycle(initialValue = false)
            PillRemindTheme(darkTheme = isDarkMode) {
                val onboardingCompleted by sessionManager.onboardingCompleted
                    .collectAsStateWithLifecycle(initialValue = null)

                when (onboardingCompleted) {
                    // Mientras DataStore responde se evita mostrar la bienvenida
                    // a usuarios que ya la completaron.
                    null -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    )

                    false -> OnboardingRoute()
                    true -> RootNavigationGraph(navController = rememberNavController())
                }
            }
        }
    }
}
