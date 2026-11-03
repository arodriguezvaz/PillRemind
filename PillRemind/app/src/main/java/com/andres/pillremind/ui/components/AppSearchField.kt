package com.andres.pillremind.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.andres.pillremind.R

/**
 * Campo de búsqueda con lupa y acción para limpiar el texto.
 *
 * @param value Texto actual.
 * @param onValueChange Callback al cambiar el texto.
 * @param placeholder Texto de ayuda.
 * @param modifier Modificador para el campo.
 */
@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = Icons.Rounded.Search,
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.search_clear),
                    )
                }
            }
        } else {
            null
        },
        imeAction = ImeAction.Search,
    )
}
