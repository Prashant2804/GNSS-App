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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
fun CogoIntersectionScreen(
    projectId: String,
    onBack: () -> Unit,
    vm: CogoIntersectionViewModel = viewModel(),
) {
    val flow = remember(projectId) { vm.uiState(projectId) }
    val state by flow.collectAsState()

    val pointA = state.pointAId?.let { id -> state.points.firstOrNull { it.id == id } }
    val pointB = state.pointBId?.let { id -> state.points.firstOrNull { it.id == id } }

    val isBearing = state.mode == IntersectionMode.BEARING_BEARING

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Intersection") },
            navigationIcon = { IconButton(onClick = onBack) { Text("Back") } },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IntersectionMode.entries.forEach { m ->
                    FilterChip(
                        selected = state.mode == m,
                        onClick = { vm.setMode(projectId, m) },
                        label = { Text(m.label) },
                    )
                }
            }

            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Result", style = MaterialTheme.typography.titleMedium)
                    Text("Point A: ${pointA?.code ?: "—"}")
                    Text("Point B: ${pointB?.code ?: "—"}")

                    val res = state.result
                    val err = state.error
                    when {
                        err != null -> Text(err, color = MaterialTheme.colorScheme.error)
                        res != null -> {
                            Text("Lat: ${"%.7f°".format(res.latDeg)}")
                            Text("Lon: ${"%.7f°".format(res.lonDeg)}")
                            Text(res.description, style = MaterialTheme.typography.bodySmall)
                        }
                        else -> Text("Select two points and enter values below")
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.input.valueA,
                    onValueChange = { vm.setInput(projectId, state.input.copy(valueA = it)) },
                    label = { Text(if (isBearing) "Bearing A (°)" else "Distance A (m)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.input.valueB,
                    onValueChange = { vm.setInput(projectId, state.input.copy(valueB = it)) },
                    label = { Text(if (isBearing) "Bearing B (°)" else "Distance B (m)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Button(
                onClick = { vm.compute(projectId) },
                enabled = state.pointAId != null && state.pointBId != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Compute") }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Select Point A and Point B", style = MaterialTheme.typography.titleMedium)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.points, key = { it.id }) { p ->
                val isA = state.pointAId == p.id
                val isB = state.pointBId == p.id
                Surface(
                    tonalElevation = if (isA || isB) 3.dp else 1.dp,
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
                                onClick = { vm.selectA(projectId, p.id) },
                                modifier = Modifier.weight(1f),
                            ) { Text(if (isA) "Point A *" else "Point A") }
                            Button(
                                onClick = { vm.selectB(projectId, p.id) },
                                modifier = Modifier.weight(1f),
                            ) { Text(if (isB) "Point B *" else "Point B") }
                        }
                    }
                }
            }
        }
    }
}
