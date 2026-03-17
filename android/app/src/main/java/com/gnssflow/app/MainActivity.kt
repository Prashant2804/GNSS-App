package com.gnssflow.app

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gnssflow.app.projects.ProjectDetailScreen
import com.gnssflow.app.projects.ProjectsScreen

class MainActivity : ComponentActivity() {
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
