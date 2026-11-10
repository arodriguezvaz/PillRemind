package com.andres.pillremind.data.pdf

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abre el PDF elegido por el usuario mediante Storage Access Framework.
 *
 * No requiere permisos de almacenamiento y no conserva ninguna copia del
 * documento.
 */
@Singleton
class MedicationImportFileStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Abre el documento para lectura.
     *
     * @return Flujo del PDF, o `null` si el proveedor no lo permite.
     */
    fun openInput(uri: Uri): InputStream? =
        runCatching { context.contentResolver.openInputStream(uri) }.getOrNull()

    /**
     * Tamaño del documento en bytes, si el proveedor lo conoce.
     */
    fun size(uri: Uri): Long? = runCatching {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
            descriptor.length.takeIf { it >= 0 }
        }
    }.getOrNull()
}
