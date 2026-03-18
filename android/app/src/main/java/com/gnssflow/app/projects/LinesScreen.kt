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
fun LinesScreen(
    projectId: String,
    onBack: () -> Unit,
    vm: LinesViewModel = viewModel(),
) {
    val flow = remember(projectId) { vm.uiState(projectId) }
    val state by flow.collectAsState()

    val aPoint = state.selectedAId?.let { id -> state.points.firstOrNull { it.id == id } }
    val bPoint = state.selectedBId?.let { id -> state.points.firstOrNull { it.id == id } }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Lines") },
            navigationIcon = { IconButton(onClick = onBack) { Text("Back") } },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val so = state.stationOffset
            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Live station/offset", style = MaterialTheme.typography.titleMedium)
                    Text("A: ${aPoint?.code ?: "—"}")
                    Text("B: ${bPoint?.code ?: "—"}")
                    Text("Station: ${so?.let { "%.2f m".format(it.stationM) } ?: "—"}")
                    Text("Offset: ${so?.let { "%.2f m".format(it.offsetM) } ?: "—"}")
                }
            }

            Button(
                onClick = {
                    val a = state.selectedAId ?: return@Button
                    val b = state.selectedBId ?: return@Button
                    val aCode = aPoint?.code ?: "A"
                    val bCode = bPoint?.code ?: "B"
                    val name = "Line $aCode→$bCode (${state.lines.size + 1})"
                    vm.createLine(projectId, a, b, name)
                },
                enabled = state.selectedAId != null && state.selectedBId != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save line from A→B")
            }

            if (state.lines.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Saved lines", style = MaterialTheme.typography.titleMedium)
                for (ln in state.lines.take(5)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = ln.name)
                        Button(onClick = { vm.useLine(projectId, ln) }) { Text("Use") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Pick A and B from points", style = MaterialTheme.typography.titleMedium)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.points, key = { it.id }) { p ->
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
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = p.code, style = MaterialTheme.typography.titleLarge)
                        Text(text = "Lat: ${p.latitudeDeg}")
                        Text(text = "Lon: ${p.longitudeDeg}")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(onClick = { vm.selectA(projectId, p.id) }, modifier = Modifier.weight(1f)) { Text("Set A") }
                            Button(onClick = { vm.selectB(projectId, p.id) }, modifier = Modifier.weight(1f)) { Text("Set B") }
                        }
                    }
                }
            }
        }
    }
}

