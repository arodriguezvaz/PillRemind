package com.andres.pillremind.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.andres.pillremind.data.repository.ProfileRepositoryImpl
import com.andres.pillremind.ui.screens.BottomBarScreen
import com.andres.pillremind.ui.screens.home.HomeScreen
import com.andres.pillremind.ui.screens.home.HomeViewModel
import com.andres.pillremind.ui.screens.home.HomeViewModelFactory
import com.andres.pillremind.ui.screens.profile.ProfileMode
import com.andres.pillremind.ui.screens.profile.ProfileScreen
import com.andres.pillremind.ui.screens.profile.ProfileViewModel
import com.andres.pillremind.ui.screens.profile.ProfileViewModelFactory
import com.andres.pillremind.ui.session.SessionViewModel

@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    repo: ProfileRepositoryImpl
) {
    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(BottomBarScreen.Home.route) {
            val profileId by sessionViewModel.activeProfileId.collectAsState()
            //TODO revisar esto
            if (profileId == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@composable
            }
            val vm = viewModel<HomeViewModel>(
                key = "profile_${profileId}",
                factory = HomeViewModelFactory(repo, profileId!!),
            )
            HomeScreen(
                homeViewModel = vm
            )
        }


        composable("profile/see") {
            val profileId by sessionViewModel.activeProfileId.collectAsState()

            val vm = viewModel<ProfileViewModel>(
                factory = ProfileViewModelFactory(repo, profileId)
            )

            ProfileScreen(
                profileViewModel = vm,
                profileMode = ProfileMode.View
            )
        }
    }
}

