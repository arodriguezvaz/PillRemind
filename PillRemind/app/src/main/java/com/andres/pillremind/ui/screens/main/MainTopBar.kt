package com.andres.pillremind.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.andres.pillremind.navigation.BottomBarScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    state: TopBarState,
    navController: NavHostController
) {
    navController.previousBackStackEntry != null
    val bottomBarRoutes = remember {
        setOf(
            BottomBarScreens.Home.route,
            BottomBarScreens.Medications.route,
            BottomBarScreens.Profile.route
        )
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val canGoBack = currentRoute !in bottomBarRoutes
            && navController.previousBackStackEntry != null
    TopAppBar(
        navigationIcon = {
            if (canGoBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
            }
        },
        title = {
            Column {
                if (state.showLogo) {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("PILL")
                            }

                            withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                                append("REMIND")
                            }
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(state.title)
            }
        },
        actions = {
            state.actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(action.icon, contentDescription = action.contentDescription)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}