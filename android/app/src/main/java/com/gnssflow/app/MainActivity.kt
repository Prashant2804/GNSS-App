package com.gnssflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConnectScreen()
                }
            }
        }
    }
}

@Composable
fun ConnectScreen(
    vm: ConnectViewModel = viewModel(),
) {
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
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
        } else {
            Text(text = "No telemetry yet")
        }

        Button(
            onClick = { vm.checkHealthAndConnect() },
            enabled = !state.isCheckingHealth,
        ) {
            Text(text = if (state.isTelemetryConnected) "Reconnect" else "Connect")
        }
    }
}
