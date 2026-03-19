package com.gnssflow.app.projects

import com.gnssflow.app.db.PointEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StakeoutReportEntry(
    val targetCode: String,
    val targetLat: Double,
    val targetLon: Double,
    val targetAlt: Double,
    val measuredLat: Double,
    val measuredLon: Double,
    val measuredAlt: Double,
    val deltaNorthM: Double,
    val deltaEastM: Double,
    val deltaUpM: Double,
    val distanceM: Double,
    val horizontalAccuracyM: Double?,
    val timestampMs: Long,
)

object StakeoutReportExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun exportCsv(entries: List<StakeoutReportEntry>): String {
        val sb = StringBuilder()
        sb.appendLine("target_code,target_lat,target_lon,target_alt," +
            "measured_lat,measured_lon,measured_alt," +
            "delta_north_m,delta_east_m,delta_up_m,distance_m," +
            "horizontal_accuracy_m,timestamp")
        for (e in entries) {
            sb.appendLine(listOf(
                e.targetCode,
                e.targetLat, e.targetLon, e.targetAlt,
                e.measuredLat, e.measuredLon, e.measuredAlt,
                "%.3f".format(e.deltaNorthM),
                "%.3f".format(e.deltaEastM),
                "%.3f".format(e.deltaUpM),
                "%.3f".format(e.distanceM),
                e.horizontalAccuracyM?.let { "%.3f".format(it) } ?: "",
                dateFormat.format(Date(e.timestampMs)),
            ).joinToString(","))
        }
        return sb.toString()
    }

    fun exportText(entries: List<StakeoutReportEntry>): String {
        if (entries.isEmpty()) return "No stakeout records."
        val sb = StringBuilder()
        sb.appendLine("===== STAKEOUT REPORT =====")
        sb.appendLine("Generated: ${dateFormat.format(Date())}")
        sb.appendLine("Records: ${entries.size}")
        sb.appendLine()

        for ((i, e) in entries.withIndex()) {
            sb.appendLine("--- Record ${i + 1}: ${e.targetCode} ---")
            sb.appendLine("  Time:     ${dateFormat.format(Date(e.timestampMs))}")
            sb.appendLine("  Target:   Lat ${e.targetLat}  Lon ${e.targetLon}  Alt ${e.targetAlt} m")
            sb.appendLine("  Measured: Lat ${e.measuredLat}  Lon ${e.measuredLon}  Alt ${e.measuredAlt} m")
            sb.appendLine("  Delta N:  ${"%.3f".format(e.deltaNorthM)} m")
            sb.appendLine("  Delta E:  ${"%.3f".format(e.deltaEastM)} m")
            sb.appendLine("  Delta U:  ${"%.3f".format(e.deltaUpM)} m")
            sb.appendLine("  Distance: ${"%.3f".format(e.distanceM)} m")
            if (e.horizontalAccuracyM != null) {
                sb.appendLine("  H Acc:    ${"%.3f".format(e.horizontalAccuracyM)} m")
            }
            sb.appendLine()
        }
        return sb.toString()
    }
}
