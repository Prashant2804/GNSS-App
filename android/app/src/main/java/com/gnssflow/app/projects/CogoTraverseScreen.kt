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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
fun CogoTraverseScreen(
    projectId: String,
    onBack: () -> Unit,
    vm: CogoTraverseViewModel = viewModel(),
) {
    val flow = remember(projectId) { vm.uiState(projectId) }
    val state by flow.collectAsState()

    var distance by remember { mutableStateOf("") }
    var azimuth by remember { mutableStateOf("") }
    var deltaZ by remember { mutableStateOf("0.0") }
    var code by remember { mutableStateOf("TRAV") }

    val startPoint = state.startId?.let { id -> state.points.firstOrNull { it.id == id } }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("COGO Traverse") },
            navigationIcon = { IconButton(onClick = onBack) { Text("Back") } },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Traverse from point", style = MaterialTheme.typography.titleMedium)
                    Text("Start: ${startPoint?.code ?: "— (select below)"}")
                    if (state.lastMessage != null) {
                        Text(state.lastMessage!!, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    label = { Text("Distance (m)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = azimuth,
                    onValueChange = { azimuth = it },
                    label = { Text("Azimuth (°)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = deltaZ,
                    onValueChange = { deltaZ = it },
                    label = { Text("\u0394Z (m)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Button(
                onClick = {
                    val d = distance.toDoubleOrNull() ?: return@Button
                    val az = azimuth.toDoubleOrNull() ?: return@Button
                    val dz = deltaZ.toDoubleOrNull() ?: 0.0
                    vm.computeAndSave(projectId, d, az, dz, code)
                },
                enabled = state.startId != null && distance.toDoubleOrNull() != null && azimuth.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Compute & Save Point")
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Select start point", style = MaterialTheme.typography.titleMedium)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.points, key = { it.id }) { p ->
                val isStart = state.startId == p.id
                Surface(
                    tonalElevation = if (isStart) 3.dp else 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = p.code, style = MaterialTheme.typography.titleLarge)
                            Text(text = "Lat: ${p.latitudeDeg}  Lon: ${p.longitudeDeg}")
                        }
                        Button(onClick = { vm.selectStart(projectId, p.id) }) {
                            Text(if (isStart) "Start *" else "Start")
                        }
                    }
                }
            }
        }
    }
}
