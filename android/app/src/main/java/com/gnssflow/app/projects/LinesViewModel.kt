package com.gnssflow.app.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gnssflow.app.db.AppDatabase
import com.gnssflow.app.db.LineEntity
import com.gnssflow.app.db.PointEntity
import com.gnssflow.app.telemetry.TelemetryStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class LinesUiState(
    val points: List<PointEntity> = emptyList(),
    val lines: List<LineEntity> = emptyList(),
    val selectedAId: String? = null,
    val selectedBId: String? = null,
    val stationOffset: StationOffset? = null,
)

class LinesViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val points = db.pointDao()
    private val lines = db.lineDao()

    private data class PerProject(
        val selectedA: MutableStateFlow<String?>,
        val selectedB: MutableStateFlow<String?>,
        val ui: StateFlow<LinesUiState>,
    )

    private val cache = LinkedHashMap<String, PerProject>()

    fun uiState(projectId: String): StateFlow<LinesUiState> {
        val existing = cache[projectId]
        if (existing != null) return existing.ui

        val selectedA = MutableStateFlow<String?>(null)
        val selectedB = MutableStateFlow<String?>(null)

        val ui = combine(
            points.observeByProject(projectId),
            lines.observeByProject(projectId),
            selectedA,
            selectedB,
            TelemetryStore.telemetry,
        ) { pts, ln, aId, bId, telemetry ->
            val a = pts.firstOrNull { it.id == aId }
            val b = pts.firstOrNull { it.id == bId }
            val lat = telemetry?.latitudeDeg
            val lon = telemetry?.longitudeDeg

            val so = if (a != null && b != null && lat != null && lon != null) {
                val aN = 0.0
                val aE = 0.0
                val (bN, bE) = Geo.deltaNorthEastMeters(a.latitudeDeg, a.longitudeDeg, b.latitudeDeg, b.longitudeDeg)
                val (pN, pE) = Geo.deltaNorthEastMeters(a.latitudeDeg, a.longitudeDeg, lat, lon)
                LineMath.stationOffset(aN, aE, bN, bE, pN, pE)
            } else null

            LinesUiState(
                points = pts,
                lines = ln,
                selectedAId = aId,
                selectedBId = bId,
                stationOffset = so,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LinesUiState())

        val per = PerProject(selectedA, selectedB, ui)
        cache[projectId] = per
        return ui
    }

    fun selectA(projectId: String, id: String?) {
        cache[projectId]?.selectedA?.value = id
    }

    fun selectB(projectId: String, id: String?) {
        cache[projectId]?.selectedB?.value = id
    }

    fun useLine(projectId: String, line: LineEntity) {
        cache[projectId]?.selectedA?.value = line.aPointId
        cache[projectId]?.selectedB?.value = line.bPointId
    }

    fun createLine(projectId: String, aId: String, bId: String, name: String) {
        viewModelScope.launch {
            lines.insert(
                LineEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = projectId,
                    name = name,
                    aPointId = aId,
                    bPointId = bId,
                    createdAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }
    }
}

