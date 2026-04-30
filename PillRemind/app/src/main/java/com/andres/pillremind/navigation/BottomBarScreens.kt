package com.andres.pillremind.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreens(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreens(
        route = "home",
        title = "Home",
        icon = Icons.Rounded.Home
    )

    object Medications : BottomBarScreens(
        route = "medications", "Medications", Icons.Rounded.Medication
    )

    object Profile : BottomBarScreens(
        route = "profile/see",
        title = "Profile",
        icon = Icons.Rounded.Person
    )

}