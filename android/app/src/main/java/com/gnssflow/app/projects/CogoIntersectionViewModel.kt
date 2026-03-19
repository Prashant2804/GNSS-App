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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class IntersectionMode(val label: String) {
    BEARING_BEARING("Bearing + Bearing"),
    DISTANCE_DISTANCE("Distance + Distance"),
}

data class IntersectionInput(
    val valueA: String = "",
    val valueB: String = "",
)

data class IntersectionResultData(
    val latDeg: Double,
    val lonDeg: Double,
    val description: String,
)

data class CogoIntersectionUiState(
    val points: List<PointEntity> = emptyList(),
    val pointAId: String? = null,
    val pointBId: String? = null,
    val mode: IntersectionMode = IntersectionMode.BEARING_BEARING,
    val input: IntersectionInput = IntersectionInput(),
    val result: IntersectionResultData? = null,
    val error: String? = null,
)

class CogoIntersectionViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val pointDao = db.pointDao()

    private data class PerProject(
        val pointAId: MutableStateFlow<String?>,
        val pointBId: MutableStateFlow<String?>,
        val mode: MutableStateFlow<IntersectionMode>,
        val input: MutableStateFlow<IntersectionInput>,
        val result: MutableStateFlow<IntersectionResultData?>,
        val error: MutableStateFlow<String?>,
        val ui: StateFlow<CogoIntersectionUiState>,
    )

    private val cache = LinkedHashMap<String, PerProject>()

    fun uiState(projectId: String): StateFlow<CogoIntersectionUiState> {
        val existing = cache[projectId]
        if (existing != null) return existing.ui

        val pointAId = MutableStateFlow<String?>(null)
        val pointBId = MutableStateFlow<String?>(null)
        val mode = MutableStateFlow(IntersectionMode.BEARING_BEARING)
        val input = MutableStateFlow(IntersectionInput())
        val result = MutableStateFlow<IntersectionResultData?>(null)
        val error = MutableStateFlow<String?>(null)

        val ui = combine(
            pointDao.observeByProject(projectId),
            pointAId, pointBId, mode, input, result, error,
        ) { arr ->
            @Suppress("UNCHECKED_CAST")
            CogoIntersectionUiState(
                points = arr[0] as List<PointEntity>,
                pointAId = arr[1] as String?,
                pointBId = arr[2] as String?,
                mode = arr[3] as IntersectionMode,
                input = arr[4] as IntersectionInput,
                result = arr[5] as IntersectionResultData?,
                error = arr[6] as String?,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CogoIntersectionUiState())

        cache[projectId] = PerProject(pointAId, pointBId, mode, input, result, error, ui)
        return ui
    }

    fun selectA(projectId: String, id: String?) { cache[projectId]?.pointAId?.value = id }
    fun selectB(projectId: String, id: String?) { cache[projectId]?.pointBId?.value = id }
    fun setMode(projectId: String, m: IntersectionMode) { cache[projectId]?.mode?.value = m }
    fun setInput(projectId: String, inp: IntersectionInput) { cache[projectId]?.input?.value = inp }

    fun compute(projectId: String) {
        val per = cache[projectId] ?: return
        val state = per.ui.value
        val a = state.points.firstOrNull { it.id == state.pointAId }
        val b = state.points.firstOrNull { it.id == state.pointBId }
        if (a == null || b == null) {
            per.error.value = "Select both Point A and Point B"
            per.result.value = null
            return
        }

        val vA = state.input.valueA.toDoubleOrNull()
        val vB = state.input.valueB.toDoubleOrNull()
        if (vA == null || vB == null) {
            per.error.value = "Enter valid numbers for both values"
            per.result.value = null
            return
        }

        when (state.mode) {
            IntersectionMode.BEARING_BEARING -> {
                val res = bearingBearing(a.latitudeDeg, a.longitudeDeg, vA, b.latitudeDeg, b.longitudeDeg, vB)
                if (res != null) {
                    per.result.value = IntersectionResultData(res.first, res.second,
                        "Bearing %.1f° from %s meets Bearing %.1f° from %s".format(vA, a.code, vB, b.code))
                    per.error.value = null
                } else {
                    per.error.value = "Bearings are parallel — no intersection"
                    per.result.value = null
                }
            }
            IntersectionMode.DISTANCE_DISTANCE -> {
                val res = distanceDistance(a.latitudeDeg, a.longitudeDeg, vA, b.latitudeDeg, b.longitudeDeg, vB)
                if (res != null) {
                    per.result.value = IntersectionResultData(res.first, res.second,
                        "%.1f m from %s meets %.1f m from %s".format(vA, a.code, vB, b.code))
                    per.error.value = null
                } else {
                    per.error.value = "Circles don't intersect — adjust distances"
                    per.result.value = null
                }
            }
        }
    }

    companion object {
        private const val R = 6371000.0

        fun bearingBearing(
            lat1: Double, lon1: Double, brng1Deg: Double,
            lat2: Double, lon2: Double, brng2Deg: Double,
        ): Pair<Double, Double>? {
            val (n2, e2) = Geo.deltaNorthEastMeters(lat1, lon1, lat2, lon2)

            val a1 = Math.toRadians(brng1Deg)
            val a2 = Math.toRadians(brng2Deg)

            val d1N = cos(a1); val d1E = sin(a1)
            val d2N = cos(a2); val d2E = sin(a2)

            val denom = d1E * d2N - d1N * d2E
            if (kotlin.math.abs(denom) < 1e-12) return null

            val t = (e2 * d2N - n2 * d2E) / denom

            val intN = t * d1N
            val intE = t * d1E

            val newLat = lat1 + Math.toDegrees(intN / R)
            val meanLatRad = Math.toRadians((lat1 + newLat) / 2.0)
            val newLon = lon1 + Math.toDegrees(intE / (R * cos(meanLatRad)))

            return newLat to newLon
        }

        fun distanceDistance(
            lat1: Double, lon1: Double, d1: Double,
            lat2: Double, lon2: Double, d2: Double,
        ): Pair<Double, Double>? {
            val (n2, e2) = Geo.deltaNorthEastMeters(lat1, lon1, lat2, lon2)
            val d = sqrt(n2 * n2 + e2 * e2)

            if (d > d1 + d2 || d < kotlin.math.abs(d1 - d2) || d == 0.0) return null

            val a = (d1 * d1 - d2 * d2 + d * d) / (2 * d)
            val h = sqrt(d1 * d1 - a * a)

            val uN = n2 / d
            val uE = e2 / d

            val intN = a * uN + h * uE
            val intE = a * uE - h * uN

            val newLat = lat1 + Math.toDegrees(intN / R)
            val meanLatRad = Math.toRadians((lat1 + newLat) / 2.0)
            val newLon = lon1 + Math.toDegrees(intE / (R * cos(meanLatRad)))

            return newLat to newLon
        }
    }
}
