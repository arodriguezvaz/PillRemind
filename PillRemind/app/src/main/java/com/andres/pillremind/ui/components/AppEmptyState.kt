package com.andres.pillremind.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/** Tamaño de la flecha que señala el FAB. */
private val HintIconSize = 48.dp

/** Desplazamiento de la flecha para dirigir la mirada hacia el FAB. */
private val HintIconAnimation = AppSpacing.xxs

/**
 * Estado vacío para pantallas con FAB de añadir.
 *
 * Centra un mensaje con icono, título y descripción, y coloca debajo una
 * flecha grande que lleva la mirada hacia el FAB.
 *
 * @param icon Icono decorativo del estado vacío.
 * @param title Título del estado vacío.
 * @param description Descripción del estado vacío.
 * @param hint Texto que indica cómo crear el primer elemento.
 * @param modifier Modificador para el estado vacío.
 */
@Composable
fun AppEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    hint: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.md),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = AppElevation),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(AppSpacing.md)
                            .size(32.dp),
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = AppSpacing.xxs),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = hint,
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
            )
            Spacer(modifier = Modifier.width(AppSpacing.xs))
            ArrowToAddButton()
        }
    }
}

/**
 * Flecha hacia abajo que apunta al FAB con un pequeño movimiento repetido.
 */
@Composable
private fun ArrowToAddButton() {
    val transition = rememberInfiniteTransition(label = "flecha_perfil")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = HintIconAnimation.value,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "desplazamiento_flecha",
    )

    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_downward),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(HintIconSize)
            .offset(y = offset.dp),
    )
}

@Preview(showBackground = true, name = "Estado vacío con flecha")
@Composable
private fun AppEmptyStatePreview() {
    PillRemindTheme {
        AppEmptyState(
            icon = ImageVector.vectorResource(R.drawable.ic_person_add),
            title = stringResource(R.string.profiles_empty_title),
            description = stringResource(R.string.profiles_empty_body),
            hint = stringResource(R.string.profiles_empty_hint),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(showBackground = true, fontScale = 1.5f, name = "Estado vacío oscuro")
@Composable
private fun AppEmptyStateDarkPreview() {
    PillRemindTheme(darkTheme = true) {
        AppEmptyState(
            icon = ImageVector.vectorResource(R.drawable.ic_person_add),
            title = stringResource(R.string.profiles_empty_title),
            description = stringResource(R.string.profiles_empty_body),
            hint = stringResource(R.string.profiles_empty_hint),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
