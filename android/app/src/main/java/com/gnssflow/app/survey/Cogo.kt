package com.gnssflow.app.survey

import kotlin.math.cos
import kotlin.math.sin

object Cogo {
    fun inverse(a: SurveyPoint, b: SurveyPoint): CogoResult {
        val distance = Geometry.distance(a, b)
        val azimuth = Geometry.azimuthRad(a, b)
        val dz = b.elevation - a.elevation
        return CogoResult(distance, azimuth, dz)
    }

    fun traverse(start: SurveyPoint, distance: Double, azimuthRad: Double, dz: Double): SurveyPoint {
        val east = start.east + distance * sin(azimuthRad)
        val north = start.north + distance * cos(azimuthRad)
        return start.copy(east = east, north = north, elevation = start.elevation + dz)
    }
}

data class CogoResult(
    val distance: Double,
    val azimuthRad: Double,
    val deltaZ: Double,
)
