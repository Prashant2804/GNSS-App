package com.gnssflow.app.projects

import kotlin.math.*

object Geo {
    fun distanceMeters(lat1Deg: Double, lon1Deg: Double, lat2Deg: Double, lon2Deg: Double): Double {
        val r = 6371000.0
        val lat1 = Math.toRadians(lat1Deg)
        val lat2 = Math.toRadians(lat2Deg)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(lon2Deg - lon1Deg)
        val a = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun bearingDegrees(latFromDeg: Double, lonFromDeg: Double, latToDeg: Double, lonToDeg: Double): Double {
        val latFrom = Math.toRadians(latFromDeg)
        val latTo = Math.toRadians(latToDeg)
        val dLon = Math.toRadians(lonToDeg - lonFromDeg)
        val y = sin(dLon) * cos(latTo)
        val x = cos(latFrom) * sin(latTo) - sin(latFrom) * cos(latTo) * cos(dLon)
        val brng = atan2(y, x)
        val deg = Math.toDegrees(brng)
        return ((deg % 360.0) + 360.0) % 360.0
    }

    /**
     * Approximate local tangent-plane deltas (North/East) in meters.
     * Good enough for short distances and mock telemetry.
     */
    fun deltaNorthEastMeters(
        latFromDeg: Double,
        lonFromDeg: Double,
        latToDeg: Double,
        lonToDeg: Double,
    ): Pair<Double, Double> {
        val r = 6371000.0
        val latFrom = Math.toRadians(latFromDeg)
        val latTo = Math.toRadians(latToDeg)
        val dLat = latTo - latFrom
        val dLon = Math.toRadians(lonToDeg - lonFromDeg)
        val meanLat = (latFrom + latTo) / 2.0
        val north = dLat * r
        val east = dLon * r * cos(meanLat)
        return north to east
    }
}

