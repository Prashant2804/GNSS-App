package com.gnssflow.app.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gnssflow.app.db.AppDatabase
import com.gnssflow.app.db.PointEntity
import com.gnssflow.app.telemetry.TelemetryStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AveragingUiState(
    val points: List<PointEntity> = emptyList(),
    val isAveraging: Boolean = false,
    val sampleCount: Int = 0,
    val result: AveragedPosition? = null,
    val savedMessage: String? = null,
)

class AveragingViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val pointDao = db.pointDao()
    private val repo = ProjectsRepository(db.projectDao(), pointDao)

    private val _isAveraging = MutableStateFlow(false)
    private val _sampleCount = MutableStateFlow(0)
    private val _result = MutableStateFlow<AveragedPosition?>(null)
    private val _savedMessage = MutableStateFlow<String?>(null)

    private val samples = mutableListOf<PointAverager.Sample>()
    private var collectJob: Job? = null

    private val cache = LinkedHashMap<String, StateFlow<AveragingUiState>>()

    fun uiState(projectId: String): StateFlow<AveragingUiState> {
        return cache.getOrPut(projectId) {
            combine(
                pointDao.observeByProject(projectId),
                _isAveraging, _sampleCount, _result, _savedMessage,
            ) { arr ->
                @Suppress("UNCHECKED_CAST")
                AveragingUiState(
                    points = arr[0] as List<PointEntity>,
                    isAveraging = arr[1] as Boolean,
                    sampleCount = arr[2] as Int,
                    result = arr[3] as AveragedPosition?,
                    savedMessage = arr[4] as String?,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AveragingUiState())
        }
    }

    fun startAveraging() {
        samples.clear()
        _sampleCount.value = 0
        _result.value = null
        _savedMessage.value = null
        _isAveraging.value = true

        collectJob = viewModelScope.launch {
            TelemetryStore.telemetry.filterNotNull().collect { t ->
                val lat = t.latitudeDeg ?: return@collect
                val lon = t.longitudeDeg ?: return@collect
                val alt = t.altitudeMSL ?: return@collect
                samples.add(PointAverager.Sample(lat, lon, alt, t.horizontalAccuracyM))
                _sampleCount.value = samples.size
            }
        }
    }

    fun stopAveraging() {
        collectJob?.cancel()
        collectJob = null
        _isAveraging.value = false
        _result.value = PointAverager.average(samples)
    }

    fun saveAsPoint(projectId: String, code: String) {
        val pos = _result.value ?: return
        viewModelScope.launch {
            val entity = PointEntity(
                id = java.util.UUID.randomUUID().toString(),
                projectId = projectId,
                code = code,
                latitudeDeg = pos.latitudeDeg,
                longitudeDeg = pos.longitudeDeg,
                altitudeMSL = pos.altitudeMSL,
                horizontalAccuracyM = pos.horizontalAccuracyM,
                imuRollDeg = null,
                imuPitchDeg = null,
                imuYawDeg = null,
                createdAtEpochMs = System.currentTimeMillis(),
            )
            pointDao.insert(entity)
            _savedMessage.value = "Saved \"$code\" (${pos.epochCount} epochs averaged)"
        }
    }

    override fun onCleared() {
        super.onCleared()
        collectJob?.cancel()
    }
}
