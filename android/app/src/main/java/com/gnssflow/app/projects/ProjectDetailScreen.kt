package com.gnssflow.app.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBack: () -> Unit,
    onStakeoutPoint: (String) -> Unit,
    onOpenLines: () -> Unit,
    onOpenPolygons: () -> Unit,
    onOpenCogoInverse: () -> Unit,
    onOpenCogoTraverse: () -> Unit,
    onOpenCogoIntersection: () -> Unit,
    onOpenAveraging: () -> Unit,
    onOpenResection: () -> Unit,
    onExportCsv: (String) -> Unit,
    onExportKml: (String) -> Unit,
    onExportDxf: (String) -> Unit,
    onExportRinex: (String) -> Unit,
    vm: ProjectDetailViewModel = viewModel(),
) {
    val uiState by vm.uiState(projectId).collectAsState()
    var code by remember { mutableStateOf("PT") }
    var autoSeconds by remember { mutableStateOf("5") }
    var autoDistance by remember { mutableStateOf("1.0") }

    LaunchedEffect(uiState.autoCollectMinSeconds, uiState.autoCollectMinDistanceM) {
        autoSeconds = uiState.autoCollectMinSeconds.toString()
        autoDistance = uiState.autoCollectMinDistanceM.toString()
    }

    var splitFraction by remember { mutableFloatStateOf(0.6f) }
    var totalHeight by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { totalHeight = it.height.toFloat() },
    ) {
        TopAppBar(
            title = { Text("Project") },
            navigationIcon = {
                IconButton(onClick = onBack) { Text("Back") }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(splitFraction)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // --- Navigation tools ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onOpenLines, modifier = Modifier.weight(1f)) {
                    Text("Lines")
                }
                Button(onClick = onOpenPolygons, modifier = Modifier.weight(1f)) {
                    Text("Polygons")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onOpenCogoInverse, modifier = Modifier.weight(1f)) {
                    Text("Inverse")
                }
                Button(onClick = onOpenCogoTraverse, modifier = Modifier.weight(1f)) {
                    Text("Traverse")
                }
                Button(onClick = onOpenCogoIntersection, modifier = Modifier.weight(1f)) {
                    Text("Intersect")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onOpenAveraging, modifier = Modifier.weight(1f)) {
                    Text("Averaging")
                }
                Button(onClick = onOpenResection, modifier = Modifier.weight(1f)) {
                    Text("Resection")
                }
            }

            // --- Export ---
            Text("Export", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onExportCsv(CsvExporter.exportPoints(uiState.points)) },
                    enabled = uiState.points.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) { Text("CSV") }
                Button(
                    onClick = { onExportKml(KmlExporter.exportPoints(uiState.points)) },
                    enabled = uiState.points.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) { Text("KML") }
                Button(
                    onClick = { onExportDxf(DxfExporter.exportPoints(uiState.points)) },
                    enabled = uiState.points.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) { Text("DXF") }
                Button(
                    onClick = {
                        vm.generateRinex(projectId) { rinex ->
                            if (rinex.isNotEmpty()) onExportRinex(rinex)
                        }
                    },
                    enabled = uiState.obsEpochCount > 0,
                    modifier = Modifier.weight(1f),
                ) { Text("RINEX") }
            }

            // --- Raw Observations ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Raw Observations", style = MaterialTheme.typography.titleSmall)
                    Text(
                        if (uiState.isRecordingObs) "Recording (${uiState.obsEpochCount} epochs)"
                        else "Stopped (${uiState.obsEpochCount} epochs)",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Button(onClick = { vm.toggleObsRecording(projectId) }) {
                    Text(if (uiState.isRecordingObs) "Stop" else "Record")
                }
            }

            HorizontalDivider()

            // --- Auto-collect ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Auto-collect", style = MaterialTheme.typography.titleSmall)
                Switch(
                    checked = uiState.autoCollectEnabled,
                    onCheckedChange = { checked ->
                        val s = autoSeconds.toIntOrNull() ?: uiState.autoCollectMinSeconds
                        val d = autoDistance.toDoubleOrNull() ?: uiState.autoCollectMinDistanceM
                        vm.setAutoCollect(projectId, checked, s, d)
                    },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = autoSeconds,
                    onValueChange = { autoSeconds = it },
                    label = { Text("Sec") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = autoDistance,
                    onValueChange = { autoDistance = it },
                    label = { Text("Dist (m)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        val s = autoSeconds.toIntOrNull() ?: uiState.autoCollectMinSeconds
                        val d = autoDistance.toDoubleOrNull() ?: uiState.autoCollectMinDistanceM
                        vm.setAutoCollect(projectId, uiState.autoCollectEnabled, s, d)
                    },
                ) { Text("Save") }
            }

            HorizontalDivider()

            // --- Collect point ---
            Row(
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
                Button(
                    onClick = { vm.collect(projectId, code) },
                    enabled = uiState.canCollect,
                ) { Text("Collect") }
            }
            if (!uiState.canCollect) {
                Text(
                    text = "Connect to Pi to get position before collecting.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        // --- Draggable divider ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (totalHeight > 0f) {
                            splitFraction = (splitFraction + dragAmount / totalHeight)
                                .coerceIn(0.25f, 0.85f)
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = MaterialTheme.shapes.small,
                    ),
            )
        }

        Text(
            text = "Points (${uiState.points.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f - splitFraction),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        ) {
            items(uiState.points, key = { it.id }) { p ->
                Surface(
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
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

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
