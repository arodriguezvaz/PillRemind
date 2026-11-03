package com.andres.pillremind.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.andres.pillremind.ui.theme.AppSpacing

/**
 * Estructura común de los formularios con una barra de acciones fija al pie.
 *
 * El contenido se desplaza en el espacio que queda por encima de [actionBar],
 * de modo que las acciones principales (guardar, editar, archivar...) siguen
 * visibles aunque el formulario sea largo.
 *
 * @param modifier Modificador aplicado al contenedor completo.
 * @param scrollState Estado de desplazamiento del contenido.
 * @param verticalArrangement Separación vertical entre los hijos del contenido.
 * @param horizontalAlignment Alineación horizontal del contenido.
 * @param actionBar Barra fija que se muestra al pie de la pantalla.
 * @param content Campos y secciones del formulario.
 */
@Composable
fun AppFormScaffold(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    actionBar: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(AppSpacing.md)
                .verticalScroll(scrollState),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
        actionBar()
    }
}
