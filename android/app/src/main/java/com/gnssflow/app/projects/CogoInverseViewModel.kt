package com.gnssflow.app.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gnssflow.app.db.AppDatabase
import com.gnssflow.app.db.PointEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

data class InverseResult(
    val distanceM: Double,
    val azimuthDeg: Double,
    val deltaZ: Double,
    val deltaN: Double,
    val deltaE: Double,
)

data class CogoInverseUiState(
    val points: List<PointEntity> = emptyList(),
    val fromId: String? = null,
    val toId: String? = null,
    val result: InverseResult? = null,
)

class CogoInverseViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val pointDao = db.pointDao()

    private data class PerProject(
        val fromId: MutableStateFlow<String?>,
        val toId: MutableStateFlow<String?>,
        val ui: StateFlow<CogoInverseUiState>,
    )

    private val cache = LinkedHashMap<String, PerProject>()

    fun uiState(projectId: String): StateFlow<CogoInverseUiState> {
        val existing = cache[projectId]
        if (existing != null) return existing.ui

        val fromId = MutableStateFlow<String?>(null)
        val toId = MutableStateFlow<String?>(null)

        val ui = combine(
            pointDao.observeByProject(projectId),
            fromId,
            toId,
        ) { pts, fId, tId ->
            val from = pts.firstOrNull { it.id == fId }
            val to = pts.firstOrNull { it.id == tId }

            val result = if (from != null && to != null) {
                val dist = Geo.distanceMeters(from.latitudeDeg, from.longitudeDeg, to.latitudeDeg, to.longitudeDeg)
                val azimuth = Geo.bearingDegrees(from.latitudeDeg, from.longitudeDeg, to.latitudeDeg, to.longitudeDeg)
                val (dN, dE) = Geo.deltaNorthEastMeters(from.latitudeDeg, from.longitudeDeg, to.latitudeDeg, to.longitudeDeg)
                val dz = to.altitudeMSL - from.altitudeMSL
                InverseResult(distanceM = dist, azimuthDeg = azimuth, deltaZ = dz, deltaN = dN, deltaE = dE)
            } else null

            CogoInverseUiState(points = pts, fromId = fId, toId = tId, result = result)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CogoInverseUiState())

        cache[projectId] = PerProject(fromId, toId, ui)
        return ui
    }

    fun selectFrom(projectId: String, id: String?) {
        cache[projectId]?.fromId?.value = id
    }

    fun selectTo(projectId: String, id: String?) {
        cache[projectId]?.toId?.value = id
    }
}
