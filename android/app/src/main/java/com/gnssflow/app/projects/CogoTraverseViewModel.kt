package com.gnssflow.app.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gnssflow.app.db.AppDatabase
import com.gnssflow.app.db.PointEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

data class CogoTraverseUiState(
    val points: List<PointEntity> = emptyList(),
    val startId: String? = null,
    val lastMessage: String? = null,
)

class CogoTraverseViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val pointDao = db.pointDao()

    private data class PerProject(
        val startId: MutableStateFlow<String?>,
        val message: MutableStateFlow<String?>,
        val ui: StateFlow<CogoTraverseUiState>,
    )

    private val cache = LinkedHashMap<String, PerProject>()

    fun uiState(projectId: String): StateFlow<CogoTraverseUiState> {
        val existing = cache[projectId]
        if (existing != null) return existing.ui

        val startId = MutableStateFlow<String?>(null)
        val message = MutableStateFlow<String?>(null)

        val ui = combine(
            pointDao.observeByProject(projectId),
            startId,
            message,
        ) { pts, sId, msg ->
            CogoTraverseUiState(points = pts, startId = sId, lastMessage = msg)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CogoTraverseUiState())

        cache[projectId] = PerProject(startId, message, ui)
        return ui
    }

    fun selectStart(projectId: String, id: String?) {
        cache[projectId]?.startId?.value = id
    }

    fun computeAndSave(
        projectId: String,
        distanceM: Double,
        azimuthDeg: Double,
        deltaZ: Double,
        code: String,
    ) {
        val per = cache[projectId] ?: return
        val startPointId = per.startId.value ?: run {
            per.message.value = "Select a start point first"
            return
        }

        viewModelScope.launch {
            val start = pointDao.getById(startPointId)
            if (start == null) {
                per.message.value = "Start point not found"
                return@launch
            }

            val azRad = Math.toRadians(azimuthDeg)
            val dN = distanceM * cos(azRad)
            val dE = distanceM * sin(azRad)

            val r = 6371000.0
            val newLat = start.latitudeDeg + Math.toDegrees(dN / r)
            val meanLatRad = Math.toRadians((start.latitudeDeg + newLat) / 2.0)
            val newLon = start.longitudeDeg + Math.toDegrees(dE / (r * cos(meanLatRad)))
            val newAlt = start.altitudeMSL + deltaZ

            val trimmedCode = code.trim().ifEmpty { "TRAV" }
            val id = UUID.randomUUID().toString()
            pointDao.insert(
                PointEntity(
                    id = id,
                    projectId = projectId,
                    code = trimmedCode,
                    latitudeDeg = newLat,
                    longitudeDeg = newLon,
                    altitudeMSL = newAlt,
                    imuRollDeg = null,
                    imuPitchDeg = null,
                    imuYawDeg = null,
                    horizontalAccuracyM = null,
                    createdAtEpochMs = System.currentTimeMillis(),
                ),
            )

            per.message.value = "Created \"$trimmedCode\" at (%.6f, %.6f, %.2f)".format(newLat, newLon, newAlt)
            per.startId.value = id
        }
    }
}
