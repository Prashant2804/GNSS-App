package com.gnssflow.app.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun AveragingScreen(
    projectId: String,
    onBack: () -> Unit,
    vm: AveragingViewModel = viewModel(),
) {
    val flow = remember(projectId) { vm.uiState(projectId) }
    val state by flow.collectAsState()
    var code by remember { mutableStateOf("AVG") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Point Averaging") },
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
                    Text("How it works", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tap Start to collect telemetry samples. " +
                            "Tap Stop when enough samples are collected. " +
                            "The averaged position is computed and can be saved as a new point.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (state.isAveraging) {
                        Text("Collecting...", style = MaterialTheme.typography.titleMedium)
                        Text("Samples: ${state.sampleCount}")
                    } else if (state.result != null) {
                        val r = state.result!!
                        Text("Averaged (${r.epochCount} samples)", style = MaterialTheme.typography.titleMedium)
                        Text("Lat: ${"%.8f°".format(r.latitudeDeg)}")
                        Text("Lon: ${"%.8f°".format(r.longitudeDeg)}")
                        Text("Alt: ${"%.3f m".format(r.altitudeMSL)}")
                        if (r.horizontalAccuracyM != null) {
                            Text("Avg H Acc: ${"%.3f m".format(r.horizontalAccuracyM)}")
                        }
                    } else {
                        Text("No data yet", style = MaterialTheme.typography.titleMedium)
                        Text("Connect to Pi and tap Start to begin collecting")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { vm.startAveraging() },
                    enabled = !state.isAveraging,
                    modifier = Modifier.weight(1f),
                ) { Text("Start") }
                Button(
                    onClick = { vm.stopAveraging() },
                    enabled = state.isAveraging,
                    modifier = Modifier.weight(1f),
                ) { Text("Stop") }
            }

            if (state.result != null) {
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

            if (state.savedMessage != null) {
                Text(
                    state.savedMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
