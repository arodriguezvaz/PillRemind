package com.andres.pillremind

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.components.ButtonVariant
import com.andres.pillremind.ui.components.PersonCard
import com.andres.pillremind.ui.theme.PillRemindTheme

@Composable
fun MainScreen() {
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text("Estados", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
            }

            item {
                AppButton(
                    text = "Disabled",
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                AppButton(
                    text = if (isLoading) "" else "Toca para loading",
                    onClick = { isLoading = !isLoading },
                    isLoading = isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                PersonCard(
                    name = "Juan",
                    onClick = {},
                )
            }

            item {
                Text("Variantes", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
            }

            item {
                AppButton(
                    text = "Primary",
                    onClick = {},
                    variant = ButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                AppButton(
                    text = "Secondary",
                    onClick = {},
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                AppButton(
                    text = "Ghost",
                    onClick = {},
                    variant = ButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                AppButton(
                    text = "Danger",
                    onClick = {},
                    variant = ButtonVariant.Danger,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text("Tamaños", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
            }

            item {
                AppButton(text = "Small", onClick = {}, size = ButtonSize.Small)
            }

            item {
                AppButton(text = "Medium", onClick = {}, size = ButtonSize.Medium)
            }

            item {
                AppButton(text = "Large", onClick = {}, size = ButtonSize.Large)
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Preview(
    name = "Button Preview",
    widthDp = 360,
    heightDp = 780,
    showBackground = true,
)
@Composable
fun MainScreenPreview() {
    PillRemindTheme {
        MainScreen()
    }
}