package com.gnssflow.app.telemetry

import com.gnssflow.app.network.TelemetryDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TelemetryStore {
    private val _telemetry = MutableStateFlow<TelemetryDto?>(null)
    val telemetry: StateFlow<TelemetryDto?> = _telemetry.asStateFlow()

    fun update(dto: TelemetryDto) {
        _telemetry.value = dto
    }
}

