package com.gnssflow.app.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gnssflow.app.db.AppDatabase
import com.gnssflow.app.db.PointEntity
import com.gnssflow.app.telemetry.ObservationRecorder
import com.gnssflow.app.telemetry.TelemetryStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
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
    val isRecordingObs: Boolean = false,
    val obsEpochCount: Int = 0,
)

class ProjectDetailViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val repo: ProjectsRepository =
        ProjectsRepository(db.projectDao(), db.pointDao())
    private val obsDao = db.observationDao()
    val obsRecorder = ObservationRecorder(obsDao)

    private var autoJob: Job? = null
    private var lastAutoAt: Long? = null
    private var lastAutoLat: Double? = null
    private var lastAutoLon: Double? = null

    private val _obsRecording = MutableStateFlow(false)
    private val _obsCount = MutableStateFlow(0)

    private val uiStateCache = LinkedHashMap<String, StateFlow<ProjectDetailUiState>>()

    fun uiState(projectId: String): StateFlow<ProjectDetailUiState> {
        return uiStateCache.getOrPut(projectId) {
            combine(
                repo.observePoints(projectId),
                repo.observeProject(projectId),
                TelemetryStore.telemetry,
                _obsRecording,
                _obsCount,
            ) { arr ->
                @Suppress("UNCHECKED_CAST")
                val points = arr[0] as List<PointEntity>
                val project = arr[1] as com.gnssflow.app.db.ProjectEntity?
                val telemetry = arr[2] as com.gnssflow.app.network.TelemetryDto?
                val recording = arr[3] as Boolean
                val count = arr[4] as Int
                ProjectDetailUiState(
                    points = points,
                    canCollect = telemetry?.latitudeDeg != null && telemetry.longitudeDeg != null && telemetry.altitudeMSL != null,
                    autoCollectEnabled = project?.autoCollectEnabled == true,
                    autoCollectMinSeconds = project?.autoCollectMinSeconds ?: 5,
                    autoCollectMinDistanceM = project?.autoCollectMinDistanceM ?: 1.0,
                    lastAutoCollectAtEpochMs = lastAutoAt,
                    isRecordingObs = recording,
                    obsEpochCount = count,
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

    private var obsJob: Job? = null

    fun toggleObsRecording(projectId: String) {
        if (obsRecorder.isRecording(projectId)) {
            obsRecorder.stopRecording(projectId)
            _obsRecording.value = false
            obsJob?.cancel()
            obsJob = null
        } else {
            obsRecorder.startRecording(projectId)
            _obsRecording.value = true
            obsJob = viewModelScope.launch {
                TelemetryStore.telemetry.filterNotNull().collect { t ->
                    val raw = t.rawObservation ?: return@collect
                    obsRecorder.onEpoch(projectId, raw)
                    _obsCount.value = obsDao.countByProject(projectId)
                }
            }
        }
    }

    fun generateRinex(projectId: String, callback: (String) -> Unit) {
        viewModelScope.launch {
            val epochs = obsDao.getByProject(projectId)
            if (epochs.isEmpty()) {
                callback("")
                return@launch
            }
            val telemetry = TelemetryStore.telemetry.value
            val rinex = RinexWriter.generate(
                epochs = epochs,
                markerName = "GNSSFLOW",
                approxLat = telemetry?.latitudeDeg ?: 0.0,
                approxLon = telemetry?.longitudeDeg ?: 0.0,
                approxAlt = telemetry?.altitudeMSL ?: 0.0,
            )
            callback(rinex)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoCollect()
        obsJob?.cancel()
    }
}

