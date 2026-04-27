package com.andres.pillremind.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreen(
        route = "home",
        title = "Home",
        icon = Icons.Rounded.Home
    )

    object Medications : BottomBarScreen(
        route = "medications", "Medications", Icons.Rounded.Medication
    )

    object Profile : BottomBarScreen(
        route = "profile/see",
        title = "Profile",
        icon = Icons.Rounded.Person
    )

}