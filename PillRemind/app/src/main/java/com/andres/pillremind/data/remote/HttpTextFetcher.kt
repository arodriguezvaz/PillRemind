package com.andres.pillremind.data.remote

/**
 * Obtiene el contenido de una URL como texto.
 *
 * Se mantiene como interfaz para poder sustituir la implementación real por
 * una falsa en las pruebas unitarias.
 */
interface HttpTextFetcher {

    /**
     * Descarga el contenido de [url].
     *
     * @param url Dirección HTTP o HTTPS que se quiere leer.
     * @return Cuerpo de la respuesta.
     */
    suspend fun fetch(url: String): String
}
