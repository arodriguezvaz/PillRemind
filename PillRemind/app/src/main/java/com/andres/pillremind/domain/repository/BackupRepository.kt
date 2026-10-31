package com.andres.pillremind.domain.repository

import java.io.InputStream
import java.io.OutputStream

/**
 * Operaciones de copia de seguridad local de la aplicación.
 *
 * Los flujos se abren desde la capa de UI mediante Storage Access Framework,
 * de modo que el dominio no depende de `android.net.Uri`.
 */
interface BackupRepository {

    /**
     * Comprueba si la copia requiere contraseña.
     *
     * Solo consume la cabecera del contenedor.
     */
    suspend fun isEncrypted(input: InputStream): Boolean

    /**
     * Escribe una copia completa en el flujo indicado.
     *
     * @param output Destino de la copia.
     * @param password Contraseña para cifrar la copia, o `null` para dejarla sin cifrar.
     */
    suspend fun exportTo(output: OutputStream, password: CharArray?)

    /**
     * Reemplaza todos los datos actuales por el contenido de la copia.
     *
     * @param input Origen de la copia.
     * @param password Contraseña de la copia, o `null` si no está cifrada.
     */
    suspend fun importFrom(input: InputStream, password: CharArray?)
}
