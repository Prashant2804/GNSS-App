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

    private data class PerProject(
        val selected: MutableStateFlow<List<String>>,
        val ui: StateFlow<PolygonsUiState>,
    )

    private val cache = LinkedHashMap<String, PerProject>()

    fun uiState(projectId: String): StateFlow<PolygonsUiState> {
        val existing = cache[projectId]
        if (existing != null) return existing.ui

        val selected = MutableStateFlow<List<String>>(emptyList())

        val ui = combine(
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

        val per = PerProject(selected = selected, ui = ui)
        cache[projectId] = per
        return ui
    }

    fun toggleVertex(projectId: String, id: String) {
        val sel = cache[projectId]?.selected ?: return
        val cur = sel.value.toMutableList()
        if (cur.contains(id)) cur.remove(id) else cur.add(id)
        sel.value = cur
    }

    fun clearSelection(projectId: String) {
        cache[projectId]?.selected?.value = emptyList()
    }

    fun savePolygon(projectId: String, name: String = "Polygon") {
        val ids = cache[projectId]?.selected?.value ?: return
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

    fun usePolygon(projectId: String, polygon: PolygonEntity) {
        val ids = polygon.vertexPointIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        cache[projectId]?.selected?.value = ids
    }
}

