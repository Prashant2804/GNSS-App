package com.gnssflow.app.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    vm: StakeoutViewModel = viewModel(),
) {
    val state by vm.uiState(pointId).collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Navigate to point") },
            navigationIcon = { IconButton(onClick = onBack) { Text("Back") } },
        )

        val distanceText = state.distanceM?.let { "%.2f m".format(it) } ?: "—"
        val bearingText = state.bearingDeg?.let { "%.1f°".format(it) } ?: "—"
        val dnText = state.deltaNorthM?.let { "%.2f m".format(it) } ?: "—"
        val deText = state.deltaEastM?.let { "%.2f m".format(it) } ?: "—"
        val duText = state.deltaUpM?.let { "%.2f m".format(it) } ?: "—"

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
                    Text(text = "Deltas (target - current)", style = MaterialTheme.typography.titleMedium)
                    Text(text = "ΔN: $dnText")
                    Text(text = "ΔE: $deText")
                    Text(text = "ΔU: $duText")
                }
            }

            if (state.horizontalAccuracyM != null) {
                val accText = "H Acc: %.2f m".format(state.horizontalAccuracyM)
                Text(text = accText, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

