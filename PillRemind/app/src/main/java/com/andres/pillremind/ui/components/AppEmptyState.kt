package com.andres.pillremind.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Estado vacío con una card centrada, un icono, un título y una descripción.
 *
 * No añade margen horizontal: el llamador debe aplicar el espaciado lateral.
 *
 * @param icon Icono decorativo del estado vacío.
 * @param title Título del estado vacío.
 * @param description Descripción del estado vacío.
 * @param modifier Modificador para el estado vacío.
 */
@Composable
fun AppEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
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
    }
}

@Preview(showBackground = true, name = "Estado vacío")
@Composable
private fun AppEmptyStatePreview() {
    PillRemindTheme {
        AppEmptyState(
            icon = ImageVector.vectorResource(R.drawable.ic_person_add),
            title = stringResource(R.string.profiles_empty_title),
            description = stringResource(R.string.profiles_empty_body),
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.md),
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
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.md),
        )
    }
}
