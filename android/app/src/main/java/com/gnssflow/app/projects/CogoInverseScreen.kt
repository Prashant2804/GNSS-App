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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CogoInverseScreen(
    projectId: String,
    onBack: () -> Unit,
    vm: CogoInverseViewModel = viewModel(),
) {
    val flow = remember(projectId) { vm.uiState(projectId) }
    val state by flow.collectAsState()

    val fromPoint = state.fromId?.let { id -> state.points.firstOrNull { it.id == id } }
    val toPoint = state.toId?.let { id -> state.points.firstOrNull { it.id == id } }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("COGO Inverse") },
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
                    Text("Result", style = MaterialTheme.typography.titleMedium)
                    Text("From: ${fromPoint?.code ?: "—"}")
                    Text("To: ${toPoint?.code ?: "—"}")
                    val r = state.result
                    if (r != null) {
                        Text("Distance: ${"%.3f m".format(r.distanceM)}")
                        Text("Azimuth: ${"%.4f°".format(r.azimuthDeg)}")
                        Text("\u0394N: ${"%.3f m".format(r.deltaN)}")
                        Text("\u0394E: ${"%.3f m".format(r.deltaE)}")
                        Text("\u0394Z: ${"%.3f m".format(r.deltaZ)}")
                    } else {
                        Text("Select two points below")
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Pick From and To points", style = MaterialTheme.typography.titleMedium)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.points, key = { it.id }) { p ->
                val isFrom = state.fromId == p.id
                val isTo = state.toId == p.id
                Surface(
                    tonalElevation = if (isFrom || isTo) 3.dp else 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(text = p.code, style = MaterialTheme.typography.titleLarge)
                                Text(text = "Lat: ${p.latitudeDeg}  Lon: ${p.longitudeDeg}")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = { vm.selectFrom(projectId, p.id) },
                                modifier = Modifier.weight(1f),
                            ) { Text(if (isFrom) "From *" else "From") }
                            Button(
                                onClick = { vm.selectTo(projectId, p.id) },
                                modifier = Modifier.weight(1f),
                            ) { Text(if (isTo) "To *" else "To") }
                        }
                    }
                }
            }
        }
    }
}
