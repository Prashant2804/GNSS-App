package com.gnssflow.app.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gnssflow.app.db.AppDatabase
import com.gnssflow.app.db.PointEntity
import com.gnssflow.app.telemetry.TelemetryStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProjectDetailUiState(
    val points: List<PointEntity> = emptyList(),
    val lastError: String? = null,
    val canCollect: Boolean = false,
    val autoCollectEnabled: Boolean = false,
    val autoCollectMinSeconds: Int = 5,
    val autoCollectMinDistanceM: Double = 1.0,
    val lastAutoCollectAtEpochMs: Long? = null,
)

class ProjectDetailViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: ProjectsRepository =
        ProjectsRepository(
            AppDatabase.get(app).projectDao(),
            AppDatabase.get(app).pointDao(),
        )

    private var autoJob: Job? = null
    private var lastAutoAt: Long? = null
    private var lastAutoLat: Double? = null
    private var lastAutoLon: Double? = null

    private val uiStateCache = LinkedHashMap<String, StateFlow<ProjectDetailUiState>>()

    fun uiState(projectId: String): StateFlow<ProjectDetailUiState> {
        return uiStateCache.getOrPut(projectId) {
            combine(
                repo.observePoints(projectId),
                repo.observeProject(projectId),
                TelemetryStore.telemetry,
            ) { points, project, telemetry ->
                ProjectDetailUiState(
                    points = points,
                    canCollect = telemetry?.latitudeDeg != null && telemetry.longitudeDeg != null && telemetry.altitudeMSL != null,
                    autoCollectEnabled = project?.autoCollectEnabled == true,
                    autoCollectMinSeconds = project?.autoCollectMinSeconds ?: 5,
                    autoCollectMinDistanceM = project?.autoCollectMinDistanceM ?: 1.0,
                    lastAutoCollectAtEpochMs = lastAutoAt,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProjectDetailUiState())
        }
    }

    fun collect(projectId: String, code: String) {
        val telemetry = TelemetryStore.telemetry.value ?: run {
            return
        }
        viewModelScope.launch {
            runCatching {
                repo.collectPoint(projectId = projectId, code = code, telemetry = telemetry)
            }
        }
    }

    fun setAutoCollect(
        projectId: String,
        enabled: Boolean,
        minSeconds: Int,
        minDistanceM: Double,
    ) {
        viewModelScope.launch {
            repo.updateAutoCollect(projectId, enabled, minSeconds, minDistanceM)
        }
        if (enabled) {
            startAutoCollect(projectId)
        } else {
            stopAutoCollect()
        }
    }

    private fun startAutoCollect(projectId: String) {
        if (autoJob?.isActive == true) return
        autoJob = viewModelScope.launch {
            val latest = repo.latestPoint(projectId)
            if (latest != null) {
                lastAutoAt = latest.createdAtEpochMs
                lastAutoLat = latest.latitudeDeg
                lastAutoLon = latest.longitudeDeg
            } else {
                lastAutoAt = null
                lastAutoLat = null
                lastAutoLon = null
            }

            val telemetryFlow = TelemetryStore.telemetry.filterNotNull()
            telemetryFlow.collect { t ->
                val proj = repo.getProject(projectId)
                if (proj?.autoCollectEnabled != true) {
                    stopAutoCollect()
                    return@collect
                }
                if (t.fixQuality.lowercase() != "fix") return@collect

                val now = System.currentTimeMillis()
                val lat = t.latitudeDeg ?: return@collect
                val lon = t.longitudeDeg ?: return@collect

                val elapsedSec = ((now - (lastAutoAt ?: now)) / 1000.0).toInt()
                val distM = if (lastAutoLat != null && lastAutoLon != null) {
                    Geo.distanceMeters(lastAutoLat!!, lastAutoLon!!, lat, lon)
                } else {
                    Double.POSITIVE_INFINITY
                }

                if (elapsedSec >= proj.autoCollectMinSeconds || distM >= proj.autoCollectMinDistanceM) {
                    runCatching {
                        repo.collectPoint(projectId, "AUTO", t, nowEpochMs = now)
                        lastAutoAt = now
                        lastAutoLat = lat
                        lastAutoLon = lon
                    }
                }
            }
        }
    }

    private fun stopAutoCollect() {
        autoJob?.cancel()
        autoJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoCollect()
    }
}

