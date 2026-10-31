package com.andres.pillremind.data.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abre los flujos del archivo elegido por el usuario mediante Storage Access
 * Framework. No requiere permisos de almacenamiento.
 */
@Singleton
class BackupFileStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Abre el archivo seleccionado para lectura.
     *
     * @throws BackupException Si el proveedor no permite abrir el documento.
     */
    fun openInput(uri: Uri): InputStream =
        context.contentResolver.openInputStream(uri)
            ?: throw BackupException("No se pudo abrir el archivo seleccionado")

    /**
     * Crea o trunca el archivo de destino.
     *
     * @throws BackupException Si el proveedor no permite crear el documento.
     */
    fun openOutput(uri: Uri): OutputStream =
        context.contentResolver.openOutputStream(uri, "wt")
            ?: throw BackupException("No se pudo crear el archivo de copia")

    /**
     * Nombre sugerido para una copia nueva.
     */
    fun suggestedFileName(): String =
        "pillremind-backup-${LocalDate.now()}.pmbackup"

    /**
     * Intenta borrar un documento parcial tras un error de exportación.
     *
     * No todos los proveedores permiten eliminar documentos; el fallo se ignora.
     */
    fun delete(uri: Uri) {
        runCatching { context.contentResolver.delete(uri, null, null) }
    }
}
