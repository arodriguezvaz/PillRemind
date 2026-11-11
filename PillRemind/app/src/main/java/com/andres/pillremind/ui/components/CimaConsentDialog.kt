package com.andres.pillremind.ui.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.andres.pillremind.R
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Diálogo de consentimiento para consultar CIMA.
 *
 * Se muestra antes de la primera consulta y desde la bienvenida. El texto es
 * desplazable para que quepa completo con fuentes grandes.
 *
 * @param onGrant Acción al aceptar la consulta.
 * @param onDecline Acción al rechazarla.
 */
@Composable
fun CimaConsentDialog(
    onGrant: () -> Unit,
    onDecline: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text(stringResource(R.string.scan_cn_consent_title)) },
        text = {
            Text(
                text = stringResource(R.string.scan_cn_consent_message),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        },
        confirmButton = {
            TextButton(onClick = onGrant) {
                Text(stringResource(R.string.scan_cn_consent_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(stringResource(R.string.scan_cn_consent_decline))
            }
        },
    )
}

@Preview(showBackground = true, name = "Consentimiento CIMA")
@Composable
private fun CimaConsentDialogPreview() {
    PillRemindTheme {
        CimaConsentDialog(onGrant = {}, onDecline = {})
    }
}
