package com.gnssflow.app.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gnssflow.app.db.AppDatabase
import com.gnssflow.app.telemetry.TelemetryStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StakeoutUiState(
    val targetCode: String = "",
    val targetLat: Double? = null,
    val targetLon: Double? = null,
    val targetAlt: Double? = null,
    val measuredLat: Double? = null,
    val measuredLon: Double? = null,
    val measuredAlt: Double? = null,
    val distanceM: Double? = null,
    val bearingDeg: Double? = null,
    val deltaNorthM: Double? = null,
    val deltaEastM: Double? = null,
    val deltaUpM: Double? = null,
    val horizontalAccuracyM: Double? = null,
    val reportEntries: List<StakeoutReportEntry> = emptyList(),
    val savedMessage: String? = null,
)

class StakeoutViewModel(app: Application) : AndroidViewModel(app) {
    private val points = AppDatabase.get(app).pointDao()

    private val reportEntries = MutableStateFlow<List<StakeoutReportEntry>>(emptyList())
    private val savedMessage = MutableStateFlow<String?>(null)

    fun uiState(pointId: String): StateFlow<StakeoutUiState> {
        val targetFlow = points.observeById(pointId)
        return combine(targetFlow, TelemetryStore.telemetry, reportEntries, savedMessage) { target, telemetry, entries, msg ->
            if (target == null) return@combine StakeoutUiState()
            if (telemetry == null) return@combine StakeoutUiState(
                targetCode = target.code,
                targetLat = target.latitudeDeg, targetLon = target.longitudeDeg, targetAlt = target.altitudeMSL,
                reportEntries = entries, savedMessage = msg,
            )
            val lat = telemetry.latitudeDeg ?: return@combine StakeoutUiState(
                targetCode = target.code,
                targetLat = target.latitudeDeg, targetLon = target.longitudeDeg, targetAlt = target.altitudeMSL,
                reportEntries = entries, savedMessage = msg,
            )
            val lon = telemetry.longitudeDeg ?: return@combine StakeoutUiState(
                targetCode = target.code,
                targetLat = target.latitudeDeg, targetLon = target.longitudeDeg, targetAlt = target.altitudeMSL,
                reportEntries = entries, savedMessage = msg,
            )
            val alt = telemetry.altitudeMSL ?: return@combine StakeoutUiState(
                targetCode = target.code,
                targetLat = target.latitudeDeg, targetLon = target.longitudeDeg, targetAlt = target.altitudeMSL,
                reportEntries = entries, savedMessage = msg,
            )

            val dist = Geo.distanceMeters(lat, lon, target.latitudeDeg, target.longitudeDeg)
            val bearing = Geo.bearingDegrees(lat, lon, target.latitudeDeg, target.longitudeDeg)
            val (dn, de) = Geo.deltaNorthEastMeters(lat, lon, target.latitudeDeg, target.longitudeDeg)
            val du = target.altitudeMSL - alt

            StakeoutUiState(
                targetCode = target.code,
                targetLat = target.latitudeDeg, targetLon = target.longitudeDeg, targetAlt = target.altitudeMSL,
                measuredLat = lat, measuredLon = lon, measuredAlt = alt,
                distanceM = dist,
                bearingDeg = bearing,
                deltaNorthM = dn,
                deltaEastM = de,
                deltaUpM = du,
                horizontalAccuracyM = telemetry.horizontalAccuracyM,
                reportEntries = entries,
                savedMessage = msg,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StakeoutUiState())
    }

    fun saveReading(state: StakeoutUiState) {
        val lat = state.measuredLat ?: return
        val lon = state.measuredLon ?: return
        val alt = state.measuredAlt ?: return
        val tLat = state.targetLat ?: return
        val tLon = state.targetLon ?: return
        val tAlt = state.targetAlt ?: return

        val entry = StakeoutReportEntry(
            targetCode = state.targetCode,
            targetLat = tLat, targetLon = tLon, targetAlt = tAlt,
            measuredLat = lat, measuredLon = lon, measuredAlt = alt,
            deltaNorthM = state.deltaNorthM ?: 0.0,
            deltaEastM = state.deltaEastM ?: 0.0,
            deltaUpM = state.deltaUpM ?: 0.0,
            distanceM = state.distanceM ?: 0.0,
            horizontalAccuracyM = state.horizontalAccuracyM,
            timestampMs = System.currentTimeMillis(),
        )
        reportEntries.value = reportEntries.value + entry
        savedMessage.value = "Reading #${reportEntries.value.size} saved"
    }

    fun clearReport() {
        reportEntries.value = emptyList()
        savedMessage.value = null
    }
}
