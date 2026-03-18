package com.gnssflow.app.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gnssflow.app.db.AppDatabase
import com.gnssflow.app.db.PointEntity
import com.gnssflow.app.db.PolygonEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class PolygonsUiState(
    val points: List<PointEntity> = emptyList(),
    val polygons: List<PolygonEntity> = emptyList(),
    val selectedVertexIds: List<String> = emptyList(),
    val areaM2: Double = 0.0,
    val perimeterM: Double = 0.0,
)

class PolygonsViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val points = db.pointDao()
    private val polygons = db.polygonDao()

    private val selected = MutableStateFlow<List<String>>(emptyList())

    fun uiState(projectId: String): StateFlow<PolygonsUiState> {
        return combine(
            points.observeByProject(projectId),
            polygons.observeByProject(projectId),
            selected,
        ) { pts, polys, sel ->
            val ordered = sel.mapNotNull { id -> pts.firstOrNull { it.id == id } }
            val latLon = ordered.map { it.latitudeDeg to it.longitudeDeg }
            val ap = PolygonMath.areaPerimeter(latLon)
            PolygonsUiState(
                points = pts,
                polygons = polys,
                selectedVertexIds = sel,
                areaM2 = ap.areaM2,
                perimeterM = ap.perimeterM,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PolygonsUiState())
    }

    fun toggleVertex(id: String) {
        val cur = selected.value.toMutableList()
        if (cur.contains(id)) cur.remove(id) else cur.add(id)
        selected.value = cur
    }

    fun clearSelection() {
        selected.value = emptyList()
    }

    fun savePolygon(projectId: String, name: String = "Polygon") {
        val ids = selected.value
        if (ids.size < 3) return
        viewModelScope.launch {
            polygons.insert(
                PolygonEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = projectId,
                    name = name,
                    vertexPointIdsCsv = ids.joinToString(","),
                    createdAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }
    }
}

