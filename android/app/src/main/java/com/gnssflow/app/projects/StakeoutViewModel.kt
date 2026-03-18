package com.gnssflow.app.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gnssflow.app.db.AppDatabase
import com.gnssflow.app.telemetry.TelemetryStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StakeoutUiState(
    val targetCode: String = "",
    val distanceM: Double? = null,
    val bearingDeg: Double? = null,
    val deltaNorthM: Double? = null,
    val deltaEastM: Double? = null,
    val deltaUpM: Double? = null,
    val horizontalAccuracyM: Double? = null,
)

class StakeoutViewModel(app: Application) : AndroidViewModel(app) {
    private val points = AppDatabase.get(app).pointDao()

    fun uiState(pointId: String): StateFlow<StakeoutUiState> {
        val targetFlow = points.observeById(pointId)
        return combine(targetFlow, TelemetryStore.telemetry) { target, telemetry ->
            if (target == null || telemetry == null) return@combine StakeoutUiState()
            val lat = telemetry.latitudeDeg ?: return@combine StakeoutUiState(targetCode = target.code)
            val lon = telemetry.longitudeDeg ?: return@combine StakeoutUiState(targetCode = target.code)
            val alt = telemetry.altitudeMSL ?: return@combine StakeoutUiState(targetCode = target.code)

            val dist = Geo.distanceMeters(lat, lon, target.latitudeDeg, target.longitudeDeg)
            val bearing = Geo.bearingDegrees(lat, lon, target.latitudeDeg, target.longitudeDeg)
            val (dn, de) = Geo.deltaNorthEastMeters(lat, lon, target.latitudeDeg, target.longitudeDeg)
            val du = (target.altitudeMSL - alt)

            StakeoutUiState(
                targetCode = target.code,
                distanceM = dist,
                bearingDeg = bearing,
                deltaNorthM = dn,
                deltaEastM = de,
                deltaUpM = du,
                horizontalAccuracyM = telemetry.horizontalAccuracyM,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StakeoutUiState())
    }
}

