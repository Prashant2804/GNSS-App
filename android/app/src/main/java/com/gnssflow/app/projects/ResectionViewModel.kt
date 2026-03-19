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

data class ResectionUiState(
    val points: List<PointEntity> = emptyList(),
    val selectedIds: List<String> = emptyList(),
    val distances: List<String> = emptyList(),
    val result: ResectionResult? = null,
    val error: String? = null,
    val savedMessage: String? = null,
)

class ResectionViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val pointDao = db.pointDao()

    private val selectedIds = MutableStateFlow<List<String>>(emptyList())
    private val distances = MutableStateFlow<List<String>>(listOf("", "", ""))
    private val result = MutableStateFlow<ResectionResult?>(null)
    private val error = MutableStateFlow<String?>(null)
    private val savedMessage = MutableStateFlow<String?>(null)

    private val cache = LinkedHashMap<String, StateFlow<ResectionUiState>>()

    fun uiState(projectId: String): StateFlow<ResectionUiState> {
        return cache.getOrPut(projectId) {
            combine(
                pointDao.observeByProject(projectId),
                selectedIds, distances, result, error, savedMessage,
            ) { arr ->
                @Suppress("UNCHECKED_CAST")
                ResectionUiState(
                    points = arr[0] as List<PointEntity>,
                    selectedIds = arr[1] as List<String>,
                    distances = arr[2] as List<String>,
                    result = arr[3] as ResectionResult?,
                    error = arr[4] as String?,
                    savedMessage = arr[5] as String?,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResectionUiState())
        }
    }

    fun togglePoint(id: String) {
        val current = selectedIds.value.toMutableList()
        if (current.contains(id)) {
            val idx = current.indexOf(id)
            current.removeAt(idx)
            val d = distances.value.toMutableList()
            if (idx < d.size) d.removeAt(idx)
            distances.value = d
        } else if (current.size < 3) {
            current.add(id)
            val d = distances.value.toMutableList()
            while (d.size < current.size) d.add("")
            distances.value = d
        }
        selectedIds.value = current
        result.value = null
        error.value = null
        savedMessage.value = null
    }

    fun setDistance(index: Int, value: String) {
        val d = distances.value.toMutableList()
        while (d.size <= index) d.add("")
        d[index] = value
        distances.value = d
    }

    fun compute() {
        val ids = selectedIds.value
        if (ids.size != 3) {
            error.value = "Select exactly 3 points"
            result.value = null
            return
        }
        val state = cache.values.firstOrNull()?.value ?: return
        val pts = ids.mapNotNull { id -> state.points.firstOrNull { it.id == id } }
        if (pts.size != 3) {
            error.value = "Could not find selected points"
            result.value = null
            return
        }
        val dists = distances.value.take(3).map { it.toDoubleOrNull() }
        if (dists.any { it == null || it <= 0.0 }) {
            error.value = "Enter valid positive distances for all 3 points"
            result.value = null
            return
        }

        val known = pts.zip(dists).map { (p, d) ->
            ResectionSolver.KnownPoint(p.latitudeDeg, p.longitudeDeg, p.altitudeMSL, d!!)
        }

        val res = ResectionSolver.solve(known[0], known[1], known[2])
        if (res != null) {
            result.value = res
            error.value = null
        } else {
            error.value = "Points are too close together or collinear — pick 3 well-spaced points"
            result.value = null
        }
    }

    fun saveAsPoint(projectId: String, code: String) {
        val res = result.value ?: return
        viewModelScope.launch {
            val entity = PointEntity(
                id = java.util.UUID.randomUUID().toString(),
                projectId = projectId,
                code = code,
                latitudeDeg = res.latDeg,
                longitudeDeg = res.lonDeg,
                altitudeMSL = res.altMSL,
                horizontalAccuracyM = null,
                imuRollDeg = null, imuPitchDeg = null, imuYawDeg = null,
                createdAtEpochMs = System.currentTimeMillis(),
            )
            pointDao.insert(entity)
            savedMessage.value = "Saved \"$code\" from resection"
        }
    }
}
