package com.gnssflow.app.projects

import com.gnssflow.app.db.PointEntity

object CsvExporter {
    private const val HEADER =
        "code,latitude_deg,longitude_deg,altitude_msl,horizontal_accuracy_m,imu_roll_deg,imu_pitch_deg,imu_yaw_deg,created_epoch_ms"

    fun exportPoints(points: List<PointEntity>): String {
        val sb = StringBuilder()
        sb.appendLine(HEADER)
        for (p in points) {
            sb.appendLine(
                listOf(
                    escapeCsv(p.code),
                    p.latitudeDeg.toString(),
                    p.longitudeDeg.toString(),
                    p.altitudeMSL.toString(),
                    (p.horizontalAccuracyM ?: "").toString(),
                    (p.imuRollDeg ?: "").toString(),
                    (p.imuPitchDeg ?: "").toString(),
                    (p.imuYawDeg ?: "").toString(),
                    p.createdAtEpochMs.toString(),
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
