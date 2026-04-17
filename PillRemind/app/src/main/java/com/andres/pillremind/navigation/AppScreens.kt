package com.andres.pillremind.navigation

/**
 * Clase sellada quE permitE tipar las pantallas de la app
 * @param route Ruta de la pantalla
 */
sealed class AppScreens(val route: String) {
    object Perfiles : AppScreens("perfiles")
    object CrearPerfil : AppScreens("crearPerfil")
}