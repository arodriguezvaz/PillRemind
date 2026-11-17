package com.andres.pillremind.data.pdf

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.io.OutputStream
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abre el archivo PDF elegido por el usuario mediante Storage Access
 * Framework y propone un nombre para el informe.
 */
@Singleton
class AdherencePdfFileStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Crea o trunca el documento de destino.
     *
     * @throws IOException Si el proveedor no permite crear el archivo.
     */
    fun openOutput(uri: Uri): OutputStream =
        context.contentResolver.openOutputStream(uri, "wt")
            ?: throw IOException("No se pudo abrir el informe para escritura")

    /**
     * Nombre sugerido para un informe nuevo.
     *
     * @param date Fecha del informe.
     */
    fun suggestedFileName(date: LocalDate): String =
        "pillremind-adherencia-$date.pdf"

    /**
     * Intenta borrar un documento parcial tras un error de exportación.
     *
     * No todos los proveedores permiten eliminar documentos; el fallo se ignora.
     */
    fun delete(uri: Uri) {
        runCatching { context.contentResolver.delete(uri, null, null) }
    }
}
