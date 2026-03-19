package com.gnssflow.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gnssflow.app.projects.AveragingScreen
import com.gnssflow.app.projects.CogoIntersectionScreen
import com.gnssflow.app.projects.CogoInverseScreen
import com.gnssflow.app.projects.CogoTraverseScreen
import com.gnssflow.app.projects.ProjectDetailScreen
import com.gnssflow.app.projects.ProjectsScreen
import com.gnssflow.app.projects.LinesScreen
import com.gnssflow.app.projects.PolygonsScreen
import com.gnssflow.app.projects.ResectionScreen
import com.gnssflow.app.projects.StakeoutScreen
import java.io.File

class MainActivity : ComponentActivity() {

    /**
     * Write content to a temp file in cache/exports/, then open the system
     * share sheet with the file URI. This gives the user both "Save to
     * Downloads / Files" and "Share with app" options.
     */
    private fun exportFile(content: String, fileName: String, mimeType: String) {
        val dir = File(cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeText(content)

        val uri = FileProvider.getUriForFile(
            this,
            "${applicationInfo.packageName}.fileprovider",
            file,
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Export $fileName"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var route by remember { mutableStateOf("connect") }
                    when {
                        route == "connect" -> ConnectScreen(onGoToProjects = { route = "projects" })
                        route == "projects" -> ProjectsScreen(
                            onBack = { route = "connect" },
                            onOpenProject = { projectId -> route = "project:$projectId" },
                        )
                        route.startsWith("project:") -> {
                            val projectId = route.removePrefix("project:")
                            ProjectDetailScreen(
                                projectId = projectId,
                                onBack = { route = "projects" },
                                onStakeoutPoint = { pointId -> route = "stakeout:$pointId" },
                                onOpenLines = { route = "lines:$projectId" },
                                onOpenPolygons = { route = "polygons:$projectId" },
                                onOpenCogoInverse = { route = "cogo-inverse:$projectId" },
                                onOpenCogoTraverse = { route = "cogo-traverse:$projectId" },
                                onOpenCogoIntersection = { route = "cogo-intersection:$projectId" },
                                onOpenAveraging = { route = "averaging:$projectId" },
                                onOpenResection = { route = "resection:$projectId" },
                                onExportCsv = { csv -> exportFile(csv, "gnss_flow_points.csv", "text/csv") },
                                onExportKml = { kml -> exportFile(kml, "gnss_flow_points.kml", "application/vnd.google-earth.kml+xml") },
                                onExportDxf = { dxf -> exportFile(dxf, "gnss_flow_points.dxf", "application/dxf") },
                                onExportRinex = { rinex -> exportFile(rinex, "gnss_flow_observations.obs", "application/octet-stream") },
                            )
                        }
                        route.startsWith("lines:") -> {
                            val projectId = route.removePrefix("lines:")
                            LinesScreen(
                                projectId = projectId,
                                onBack = { route = "project:$projectId" },
                            )
                        }
                        route.startsWith("polygons:") -> {
                            val projectId = route.removePrefix("polygons:")
                            PolygonsScreen(
                                projectId = projectId,
                                onBack = { route = "project:$projectId" },
                            )
                        }
                        route.startsWith("cogo-inverse:") -> {
                            val projectId = route.removePrefix("cogo-inverse:")
                            CogoInverseScreen(
                                projectId = projectId,
                                onBack = { route = "project:$projectId" },
                            )
                        }
                        route.startsWith("cogo-traverse:") -> {
                            val projectId = route.removePrefix("cogo-traverse:")
                            CogoTraverseScreen(
                                projectId = projectId,
                                onBack = { route = "project:$projectId" },
                            )
                        }
                        route.startsWith("cogo-intersection:") -> {
                            val projectId = route.removePrefix("cogo-intersection:")
                            CogoIntersectionScreen(
                                projectId = projectId,
                                onBack = { route = "project:$projectId" },
                            )
                        }
                        route.startsWith("averaging:") -> {
                            val projectId = route.removePrefix("averaging:")
                            AveragingScreen(
                                projectId = projectId,
                                onBack = { route = "project:$projectId" },
                            )
                        }
                        route.startsWith("resection:") -> {
                            val projectId = route.removePrefix("resection:")
                            ResectionScreen(
                                projectId = projectId,
                                onBack = { route = "project:$projectId" },
                            )
                        }
                        route.startsWith("stakeout:") -> {
                            val pointId = route.removePrefix("stakeout:")
                            StakeoutScreen(
                                pointId = pointId,
                                onBack = { route = "projects" },
                                onExportReport = { content ->
                                    val iscsv = content.startsWith("target_code")
                                    val ext = if (iscsv) "csv" else "txt"
                                    val mime = if (iscsv) "text/csv" else "text/plain"
                                    exportFile(content, "stakeout_report.$ext", mime)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectScreen(
    vm: ConnectViewModel = viewModel(),
    onGoToProjects: () -> Unit,
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onGoToProjects,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Projects")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "IMU (roll/pitch/yaw)")
                        Switch(
                            checked = state.imuEnabled,
                            onCheckedChange = { vm.setImuEnabled(it) },
                        )
                    }
                    if (state.imuStatus != null) {
                        Text(text = state.imuStatus!!)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { vm.saveNtripConfig() },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(text = "Save NTRIP")
                        }
                        Button(
                            onClick = { vm.toggleNtripConnection() },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(text = "Toggle NTRIP")
                        }
                    }

                    Button(
                        onClick = { vm.checkHealthAndConnect() },
                        enabled = !state.isCheckingHealth,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = if (state.isTelemetryConnected) "Reconnect" else "Connect")
                    }

                    if (state.ntripStatus != null) {
                        Text(text = state.ntripStatus!!)
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(text = "GNSS Flow", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = { vm.onBaseUrlChanged(it) },
                label = { Text("Pi base URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            val statusText = when {
                state.isCheckingHealth -> "Checking /health..."
                state.health != null -> "Pi: ${state.health!!.status}"
                state.healthError != null -> "Error: ${state.healthError}"
                else -> "Pi: Not checked"
            }
            Text(text = statusText)

            val telemetry = state.telemetry
            if (telemetry != null) {
                Text(text = "Fix: ${telemetry.fixQuality}")
                Text(text = "Sats: ${telemetry.satellites}")
                Text(text = "H Acc: ${telemetry.horizontalAccuracyM} m")
                val corr = telemetry.corrections
                if (corr != null) {
                    Text(text = "Corrections: ${if (corr.connected) "Connected" else "Disconnected"}")
                    Text(text = "RTCM: ${corr.bytesPerSec} B/s")
                }
                val imu = telemetry.imu
                if (imu != null) {
                    Text(text = "Roll: ${"%.1f°".format(imu.rollDeg)}")
                    Text(text = "Pitch: ${"%.1f°".format(imu.pitchDeg)}")
                    Text(text = "Yaw: ${"%.1f°".format(imu.yawDeg)}")
                }
            } else {
                Text(text = "No telemetry yet")
            }

            HorizontalDivider()
            Text(text = "NTRIP", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.ntripCasterHost,
                onValueChange = { vm.onNtripCasterHostChanged(it) },
                label = { Text("Caster host") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.ntripCasterPort.toString(),
                onValueChange = { vm.onNtripCasterPortChanged(it) },
                label = { Text("Caster port") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.ntripMountPoint,
                onValueChange = { vm.onNtripMountPointChanged(it) },
                label = { Text("Mount point") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.ntripUsername,
                onValueChange = { vm.onNtripUsernameChanged(it) },
                label = { Text("Username (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.ntripPassword,
                onValueChange = { vm.onNtripPasswordChanged(it) },
                label = { Text("Password (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
