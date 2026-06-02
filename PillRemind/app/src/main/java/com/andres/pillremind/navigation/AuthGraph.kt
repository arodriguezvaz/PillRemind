package com.andres.pillremind.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.andres.pillremind.ui.screens.auth.LoginScreen
import com.andres.pillremind.ui.screens.auth.RegisterScreen

/**
 * Añade al grafo de navegación los destinos relacionados con la autenticación
 * de usuarios.
 *
 * Este subgrafo permite a los usuarios iniciar sesión o registrarse en la
 * aplicación. Tras una autenticación satisfactoria, se redirige al usuario
 * al flujo de selección de perfiles eliminando las pantallas de autenticación
 * de la pila de navegación.
 *
 * @param navController Controlador utilizado para gestionar la navegación
 * entre los destinos del flujo de autenticación.
 */
fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        route = Graph.AUTH,
        startDestination = AuthScreen.Login.route
    ) {
        //Pantalla de inicio de sesión
        composable(AuthScreen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Si el inicio de sesión es exitoso, se redirige al flujo de selección de perfiles
                    // y se elimina la pila de navegación
                    navController.navigate(Graph.PROFILE_SELECTION) {
                        popUpTo(Graph.ROOT) { inclusive = false }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AuthScreen.Register.route)
                },
                onBack = { navController.popBackStack() }
            )
        }
        //Pantalla de registro de usuario
        composable(AuthScreen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Si el registro es exitoso, se redirige al flujo de selección de perfiles y
                    // se elimina la pila de navegación
                    navController.navigate(Graph.PROFILE_SELECTION) {
                        popUpTo(Graph.ROOT) { inclusive = false }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Destinos de navegación pertenecientes al flujo de autenticación.
 *
 * Cada objeto representa una pantalla accesible dentro del grafo
 * [Graph.AUTH].
 *
 * @property route Ruta utilizada por Navigation Compose para identificar
 * el destino.
 */
sealed class AuthScreen(val route: String) {
    /**
     * Pantalla de inicio de sesión.
     */
    object Login : AuthScreen("login")

    /**
     * Pantalla de registro de usuario.
     */
    object Register : AuthScreen("register")
}