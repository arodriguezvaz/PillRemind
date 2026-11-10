package com.andres.pillremind.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Contenido desplazable con acciones al final.
 *
 * Si el contenido no llena la pantalla, el bloque de acciones se ancla al fondo;
 * si es más alto que el espacio disponible, viaja con el scroll. El alto del
 * footer se mide en tiempo de ejecución para que funcione también con texto
 * grande.
 *
 * @param modifier Modificador para el contenedor.
 * @param scrollState Estado del scroll, reutilizable por la pantalla.
 * @param footer Acciones que cierran la ficha.
 * @param content Contenido de la ficha.
 */
@Composable
fun AppScrollWithFooter(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    footer: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        var footerHeightPx by remember { mutableIntStateOf(0) }
        val footerHeight = with(density) { footerHeightPx.toDp() }
        val verticalPadding = AppSpacing.md
        val minContentHeight = (maxHeight - footerHeight - verticalPadding - verticalPadding)
            .coerceAtLeast(0.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = AppSpacing.md, vertical = verticalPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minContentHeight),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                content = content,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { footerHeightPx = it.height },
            ) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Spacer(modifier = Modifier.height(AppSpacing.md))
                footer()
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 640, name = "Ficha con acciones al final")
@Composable
private fun AppScrollWithFooterPreview() {
    PillRemindTheme {
        AppScrollWithFooter(
            footer = {
                AppFormActions(
                    primaryText = "Editar",
                    onPrimaryClick = {},
                    secondaryIcon = ImageVector.vectorResource(R.drawable.ic_delete),
                    secondaryContentDescription = "Eliminar",
                    onSecondaryClick = {},
                )
            },
        ) {
            Text(
                text = "Contenido corto",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Las acciones se quedan abajo cuando la ficha no llena la pantalla.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
