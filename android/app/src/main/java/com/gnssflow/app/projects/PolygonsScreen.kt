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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolygonsScreen(
    projectId: String,
    onBack: () -> Unit,
    vm: PolygonsViewModel = viewModel(),
) {
    val state by vm.uiState(projectId).collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Polygons") },
            navigationIcon = { IconButton(onClick = onBack) { Text("Back") } },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Live area/perimeter", style = MaterialTheme.typography.titleMedium)
                    Text("Vertices: ${state.selectedVertexIds.size}")
                    Text("Area: %.2f m²".format(state.areaM2))
                    Text("Perimeter: %.2f m".format(state.perimeterM))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { vm.clearSelection() }, modifier = Modifier.weight(1f)) { Text("Clear") }
                Button(
                    onClick = { vm.savePolygon(projectId) },
                    enabled = state.selectedVertexIds.size >= 3,
                    modifier = Modifier.weight(1f),
                ) { Text("Save") }
            }

            if (state.polygons.isNotEmpty()) {
                Text("Saved polygons: ${state.polygons.size}", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Tap points to add/remove vertices", style = MaterialTheme.typography.titleMedium)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.points, key = { it.id }) { p ->
                val selected = state.selectedVertexIds.contains(p.id)
                Surface(
                    tonalElevation = if (selected) 3.dp else 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = p.code, style = MaterialTheme.typography.titleLarge)
                            Text(text = "Lat: ${p.latitudeDeg}")
                            Text(text = "Lon: ${p.longitudeDeg}")
                        }
                        Button(onClick = { vm.toggleVertex(p.id) }) {
                            Text(if (selected) "Remove" else "Add")
                        }
                    }
                }
            }
        }
    }
}

