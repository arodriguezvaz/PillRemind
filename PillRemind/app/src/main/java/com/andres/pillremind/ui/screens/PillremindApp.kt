package com.andres.pillremind.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.andres.pillremind.navigation.AppNavigation
import com.andres.pillremind.ui.theme.PillRemindTheme

@Composable
fun PillRemind() {
    PillRemindTheme() {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavigation()
        }
    }
}