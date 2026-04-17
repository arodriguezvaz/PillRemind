package com.andres.pillremind.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.repository.PerfilRepositoryImpl
import com.andres.pillremind.ui.screens.crearperfil.CrearPerfilScreen
import com.andres.pillremind.ui.screens.crearperfil.CrearPerfilViewModel
import com.andres.pillremind.ui.screens.crearperfil.CrearPerfilViewModelFactory
import com.andres.pillremind.ui.screens.perfiles.PerfilesScreen
import com.andres.pillremind.ui.screens.perfiles.PerfilesViewModel
import com.andres.pillremind.ui.screens.perfiles.PerfilesViewModelFactory

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current

    // Instancia única de la base de datos
    val db = remember {
        AppDatabase.getInstance(context.applicationContext)
    }

    val perfilRepository = remember {
        PerfilRepositoryImpl(db.perfilDao())
    }

    val perfilesFactory = remember {
        PerfilesViewModelFactory(perfilRepository)
    }

    val crearPerfilFactory = remember {
        CrearPerfilViewModelFactory(perfilRepository)
    }

    NavHost(
        navController = navController,
        startDestination = AppScreens.Perfiles.route
    ) {

        composable(AppScreens.Perfiles.route) {

            val viewModel = viewModel<PerfilesViewModel>(
                factory = perfilesFactory
            )

            PerfilesScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(AppScreens.CrearPerfil.route) {

            val viewModel = viewModel<CrearPerfilViewModel>(
                factory = crearPerfilFactory
            )

            CrearPerfilScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}


