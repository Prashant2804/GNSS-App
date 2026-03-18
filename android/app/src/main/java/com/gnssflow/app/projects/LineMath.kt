package com.gnssflow.app.projects

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class StationOffset(
    val stationM: Double,
    val offsetM: Double,
)

object LineMath {
    /**
     * Compute station (along AB from A) and signed offset (left + / right -) for point P.
     * Inputs are in local meters in a North/East coordinate frame.
     */
    fun stationOffset(
        aNorth: Double,
        aEast: Double,
        bNorth: Double,
        bEast: Double,
        pNorth: Double,
        pEast: Double,
        clampToSegment: Boolean = true,
    ): StationOffset {
        val abN = bNorth - aNorth
        val abE = bEast - aEast
        val apN = pNorth - aNorth
        val apE = pEast - aEast
        val ab2 = abN * abN + abE * abE
        if (ab2 == 0.0) return StationOffset(0.0, 0.0)

        var t = (apN * abN + apE * abE) / ab2
        if (clampToSegment) t = max(0.0, min(1.0, t))

        val projN = aNorth + t * abN
        val projE = aEast + t * abE
        val dxN = pNorth - projN
        val dxE = pEast - projE
        val offset = sqrt(dxN * dxN + dxE * dxE)

        // Signed offset using 2D cross product sign (AB x AP) in (E,N) plane.
        val cross = abE * apN - abN * apE
        val signedOffset = if (cross >= 0) offset else -offset

        val station = t * sqrt(ab2)
        return StationOffset(stationM = station, offsetM = signedOffset)
    }
}

