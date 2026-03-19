package com.gnssflow.app.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StakeoutScreen(
    pointId: String,
    onBack: () -> Unit,
    onExportReport: ((String) -> Unit)? = null,
    vm: StakeoutViewModel = viewModel(),
) {
    val state by vm.uiState(pointId).collectAsState()

    val distanceText = state.distanceM?.let { "%.2f m".format(it) } ?: "—"
    val bearingText = state.bearingDeg?.let { "%.1f°".format(it) } ?: "—"
    val dnText = state.deltaNorthM?.let { "%.2f m".format(it) } ?: "—"
    val deText = state.deltaEastM?.let { "%.2f m".format(it) } ?: "—"
    val duText = state.deltaUpM?.let { "%.2f m".format(it) } ?: "—"

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Navigate to Point") },
            navigationIcon = { IconButton(onClick = onBack) { Text("Back") } },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(text = "Target: ${state.targetCode}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Distance: $distanceText")
                    Text(text = "Bearing: $bearingText")
                }
            }

            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(text = "Deltas (target − current)", style = MaterialTheme.typography.titleMedium)
                    Text(text = "ΔN: $dnText")
                    Text(text = "ΔE: $deText")
                    Text(text = "ΔU: $duText")
                }
            }

            if (state.horizontalAccuracyM != null) {
                Text(text = "H Accuracy: ${"%.2f m".format(state.horizontalAccuracyM)}", style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { vm.saveReading(state) },
                    enabled = state.measuredLat != null,
                    modifier = Modifier.weight(1f),
                ) { Text("Save Reading") }
                OutlinedButton(
                    onClick = { vm.clearReport() },
                    enabled = state.reportEntries.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) { Text("Clear") }
            }

            if (state.savedMessage != null) {
                Text(state.savedMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }

            if (state.reportEntries.isNotEmpty()) {
                Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("Report (${state.reportEntries.size} readings)", style = MaterialTheme.typography.titleMedium)
                        for ((i, e) in state.reportEntries.withIndex()) {
                            Text("#${i + 1} ${e.targetCode}: dist=${"%.2f m".format(e.distanceM)}, " +
                                "ΔN=${"%.2f".format(e.deltaNorthM)}, ΔE=${"%.2f".format(e.deltaEastM)}, ΔU=${"%.2f".format(e.deltaUpM)}",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (onExportReport != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = { onExportReport(StakeoutReportExporter.exportCsv(state.reportEntries)) },
                            modifier = Modifier.weight(1f),
                        ) { Text("Export CSV") }
                        Button(
                            onClick = { onExportReport(StakeoutReportExporter.exportText(state.reportEntries)) },
                            modifier = Modifier.weight(1f),
                        ) { Text("Export Text") }
                    }
                }
            }
        }
    }
}
