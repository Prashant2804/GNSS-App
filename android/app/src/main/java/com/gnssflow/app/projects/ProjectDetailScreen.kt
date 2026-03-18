package com.gnssflow.app.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBack: () -> Unit,
    onStakeoutPoint: (String) -> Unit,
    onOpenLines: () -> Unit,
    onOpenPolygons: () -> Unit,
    vm: ProjectDetailViewModel = viewModel(),
) {
    val uiState by vm.uiState(projectId).collectAsState()
    var code by remember { mutableStateOf("PT") }
    var autoSeconds by remember { mutableStateOf("5") }
    var autoDistance by remember { mutableStateOf("1.0") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.autoCollectMinSeconds, uiState.autoCollectMinDistanceM) {
        autoSeconds = uiState.autoCollectMinSeconds.toString()
        autoDistance = uiState.autoCollectMinDistanceM.toString()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Project") },
            navigationIcon = {
                IconButton(onClick = onBack) { Text("Back") }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = onOpenLines, modifier = Modifier.fillMaxWidth()) {
                Text("Lines & offsets")
            }
            Button(onClick = onOpenPolygons, modifier = Modifier.fillMaxWidth()) {
                Text("Polygons (area/perimeter)")
            }

            Text(text = "Auto-collect", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = if (uiState.autoCollectEnabled) "Enabled" else "Disabled")
                Switch(
                    checked = uiState.autoCollectEnabled,
                    onCheckedChange = { checked ->
                        val s = autoSeconds.toIntOrNull() ?: uiState.autoCollectMinSeconds
                        val d = autoDistance.toDoubleOrNull() ?: uiState.autoCollectMinDistanceM
                        vm.setAutoCollect(projectId, checked, s, d)
                    },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = autoSeconds,
                    onValueChange = { autoSeconds = it },
                    label = { Text("Min seconds") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = autoDistance,
                    onValueChange = { autoDistance = it },
                    label = { Text("Min distance (m)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Button(
                onClick = {
                    val s = autoSeconds.toIntOrNull() ?: uiState.autoCollectMinSeconds
                    val d = autoDistance.toDoubleOrNull() ?: uiState.autoCollectMinDistanceM
                    vm.setAutoCollect(projectId, uiState.autoCollectEnabled, s, d)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save auto-collect settings")
            }

            HorizontalDivider()

            Text(text = "Collect point", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = { vm.collect(projectId, code) },
                    enabled = uiState.canCollect,
                ) {
                    Text("Collect")
                }
            }
            if (!uiState.canCollect) {
                Text(
                    text = "Connect to Pi to get position before collecting.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            HorizontalDivider()
            Text(text = "Points", style = MaterialTheme.typography.titleMedium)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(uiState.points, key = { it.id }) { p ->
                Surface(
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(text = p.code, style = MaterialTheme.typography.titleLarge)
                            Button(onClick = { onStakeoutPoint(p.id) }) {
                                Text("Navigate")
                            }
                        }
                        Text(text = "Lat: ${p.latitudeDeg}")
                        Text(text = "Lon: ${p.longitudeDeg}")
                        Text(text = "Alt: ${p.altitudeMSL} m")
                        if (p.imuRollDeg != null && p.imuPitchDeg != null && p.imuYawDeg != null) {
                            Text(text = "IMU: R ${"%.1f°".format(p.imuRollDeg)}  P ${"%.1f°".format(p.imuPitchDeg)}  Y ${"%.1f°".format(p.imuYawDeg)}")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

