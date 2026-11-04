package com.andres.pillremind.ui.screens.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * TopAppBar principal de la aplicación.
 *
 * Es un componente presentacional: no conoce el `NavHostController` ni las
 * rutas. El logo y el título comparten una sola línea para respetar la altura
 * del small app bar; el título se recorta con elipsis y el logo es decorativo
 * para accesibilidad.
 *
 * Si [TopBarState.navigationAction] está definida, se muestra esa acción. En
 * caso contrario, se muestra el botón "atrás" cuando [canGoBack] es true.
 *
 * @param state Estado de la TopBar (título, logo, acciones y acción de navegación).
 * @param canGoBack Si es true, se muestra el botón de retroceso por defecto.
 * @param onBack Acción del botón de retroceso; normalmente
 *   `navController.popBackStack()`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    state: TopBarState,
    canGoBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    TopAppBar(
        navigationIcon = {
            val navigationAction = state.navigationAction
            when {
                navigationAction != null -> {
                    IconButton(onClick = navigationAction.onClick) {
                        Icon(
                            imageVector = navigationAction.icon,
                            contentDescription = navigationAction.contentDescription
                        )
                    }
                }

                canGoBack -> {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            }
        },
        title = {
            val logoFirstPart = stringResource(R.string.brand_pill)
            val logoSecondPart = stringResource(R.string.brand_remind)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.showLogo) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(logoFirstPart)
                            }

                            withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                                append(logoSecondPart)
                            }
                        },
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        // El logo es decorativo: el título de la página ya
                        // describe la pantalla para TalkBack.
                        modifier = Modifier.clearAndSetSemantics {}
                    )
                }
                if (state.title.isNotEmpty()) {
                    if (state.showLogo) {
                        Spacer(Modifier.width(AppSpacing.xs))
                    }
                    Text(
                        text = state.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        actions = {
            state.actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(action.icon, contentDescription = action.contentDescription)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}
