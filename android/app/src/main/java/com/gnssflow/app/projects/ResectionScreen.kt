package com.gnssflow.app.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResectionScreen(
    projectId: String,
    onBack: () -> Unit,
    vm: ResectionViewModel = viewModel(),
) {
    val flow = remember(projectId) { vm.uiState(projectId) }
    val state by flow.collectAsState()
    var code by remember { mutableStateOf("RESECT") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Resection") },
            navigationIcon = { IconButton(onClick = onBack) { Text("Back") } },
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp, vertical = 8.dp,
            ),
        ) {
            // --- Result card ---
            item {
                Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("Result", style = MaterialTheme.typography.titleMedium)
                        val err = state.error
                        val res = state.result
                        when {
                            err != null -> Text(err, color = MaterialTheme.colorScheme.error)
                            res != null -> {
                                Text("Lat: ${"%.7f°".format(res.latDeg)}")
                                Text("Lon: ${"%.7f°".format(res.lonDeg)}")
                                Text("Alt: ${"%.3f m".format(res.altMSL)}")
                                Text(res.description, style = MaterialTheme.typography.bodySmall)
                            }
                            else -> Text("Select 3 points and enter measured distances")
                        }
                    }
                }
            }

            // --- Selected points with distance inputs ---
            items(state.selectedIds.size) { i ->
                val pt = state.points.firstOrNull { it.id == state.selectedIds[i] }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${i + 1}. ${pt?.code ?: "?"}", modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = state.distances.getOrElse(i) { "" },
                        onValueChange = { vm.setDistance(i, it) },
                        label = { Text("Dist (m)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(onClick = { vm.togglePoint(state.selectedIds[i]) }) {
                        Text("Remove")
                    }
                }
            }

            // --- Compute button ---
            item {
                Button(
                    onClick = { vm.compute() },
                    enabled = state.selectedIds.size == 3,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Compute") }
            }

            // --- Save result ---
            if (state.result != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        Button(onClick = { vm.saveAsPoint(projectId, code) }) {
                            Text("Save Point")
                        }
                    }
                }
            }

            if (state.savedMessage != null) {
                item {
                    Text(
                        state.savedMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // --- Divider + point list header ---
            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Select points (tap to toggle, max 3)",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // --- All project points ---
            if (state.points.isEmpty()) {
                item {
                    Text(
                        "No points in this project. Collect points first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(state.points, key = { it.id }) { p ->
                val idx = state.selectedIds.indexOf(p.id)
                val isSelected = idx >= 0
                Surface(
                    tonalElevation = if (isSelected) 3.dp else 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(text = p.code, style = MaterialTheme.typography.titleLarge)
                            Text(text = "Lat: ${p.latitudeDeg}  Lon: ${p.longitudeDeg}")
                        }
                        Button(
                            onClick = { vm.togglePoint(p.id) },
                            enabled = isSelected || state.selectedIds.size < 3,
                        ) {
                            Text(if (isSelected) "#${idx + 1} ✓" else "Select")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
