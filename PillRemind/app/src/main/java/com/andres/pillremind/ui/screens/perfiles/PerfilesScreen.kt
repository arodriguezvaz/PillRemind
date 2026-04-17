package com.andres.pillremind.ui.screens.perfiles

import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.domain.model.Perfil
import com.andres.pillremind.ui.theme.PillRemindTheme

//Muestra los tres estados en los que puede estar la ui
private sealed class EditorState {
    data object Closed : EditorState()
    data object Add : EditorState()
    data class Edit(val perfil: Perfil) : EditorState()
}

@Composable
fun TaskListScreen(
    viewModel: PerfilesViewModel,
    modifier: Modifier = Modifier
) {
    val tasks = viewModel.perfiles.collectAsStateWithLifecycle()
    val editorState by remember { mutableStateOf<EditorState>(EditorState.Closed) }
    Column() {
        if (tasks.value.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("no hay tareas aun")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {

            }
        }
    }

}

@Preview
@Composable
fun TaskListEmptyScreen() {
    PillRemindTheme() {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Vista previa:conecta ViewMOdel a la app")
        }
    }
}