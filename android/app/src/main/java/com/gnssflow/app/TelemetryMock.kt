package com.gnssflow.app

data class Telemetry(
    val fixQuality: String,
    val satellites: Int,
    val horizontalAccuracyM: Double,
)

object TelemetryMock {
    fun sample(): Telemetry {
        return Telemetry(
            fixQuality = "FIX",
            satellites = 12,
            horizontalAccuracyM = 0.8,
        )
    }
}
