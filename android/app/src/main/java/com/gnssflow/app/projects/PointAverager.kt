package com.gnssflow.app.projects

data class AveragedPosition(
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val altitudeMSL: Double,
    val horizontalAccuracyM: Double?,
    val epochCount: Int,
)

object PointAverager {

    data class Sample(
        val latitudeDeg: Double,
        val longitudeDeg: Double,
        val altitudeMSL: Double,
        val horizontalAccuracyM: Double?,
    )

    fun average(samples: List<Sample>): AveragedPosition? {
        if (samples.isEmpty()) return null

        val n = samples.size
        val avgLat = samples.sumOf { it.latitudeDeg } / n
        val avgLon = samples.sumOf { it.longitudeDeg } / n
        val avgAlt = samples.sumOf { it.altitudeMSL } / n

        val accSamples = samples.mapNotNull { it.horizontalAccuracyM }
        val avgAcc = if (accSamples.isNotEmpty()) accSamples.average() else null

        return AveragedPosition(
            latitudeDeg = avgLat,
            longitudeDeg = avgLon,
            altitudeMSL = avgAlt,
            horizontalAccuracyM = avgAcc,
            epochCount = n,
        )
    }
}
