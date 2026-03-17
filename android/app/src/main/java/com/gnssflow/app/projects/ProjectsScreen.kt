package com.gnssflow.app.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onBack: () -> Unit,
    onOpenProject: (String) -> Unit,
    vm: ProjectsViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsState()
    var newName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = { Text("Projects") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("Back")
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Create a project",
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Project name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        vm.create(newName)
                        newName = ""
                    },
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "All projects", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.projects, key = { it.id }) { p ->
                    ProjectRow(
                        name = p.name,
                        onOpen = { onOpenProject(p.id) },
                        onRename = { vm.rename(p.id, it) },
                        onDelete = { vm.delete(p.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectRow(
    name: String,
    onOpen: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember(name) { mutableStateOf(name) }

    Surface(tonalElevation = 1.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (editing) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { editing = false }) { Text("Cancel") }
                    Button(
                        onClick = {
                            onRename(draft)
                            editing = false
                        },
                    ) {
                        Text("Save")
                    }
                }
            } else {
                Text(text = name, style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onOpen) { Text("Open") }
                    Button(onClick = { editing = true }) { Text("Rename") }
                    Button(onClick = onDelete) { Text("Delete") }
                }
            }
        }
    }
}

