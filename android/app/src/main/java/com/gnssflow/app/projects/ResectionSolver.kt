package com.gnssflow.app.projects

import kotlin.math.cos
import kotlin.math.sqrt

data class ResectionResult(
    val latDeg: Double,
    val lonDeg: Double,
    val altMSL: Double,
    val description: String,
)

object ResectionSolver {

    private const val R = 6371000.0

    data class KnownPoint(
        val latDeg: Double,
        val lonDeg: Double,
        val altMSL: Double,
        val distanceM: Double,
    )

    fun solve(p1: KnownPoint, p2: KnownPoint, p3: KnownPoint): ResectionResult? {
        val refLat = p1.latDeg
        val refLon = p1.lonDeg
        val cosRef = cos(Math.toRadians(refLat))

        fun toLocal(lat: Double, lon: Double): DoubleArray {
            val n = Math.toRadians(lat - refLat) * R
            val e = Math.toRadians(lon - refLon) * R * cosRef
            return doubleArrayOf(n, e)
        }

        val a = toLocal(p1.latDeg, p1.lonDeg)
        val b = toLocal(p2.latDeg, p2.lonDeg)
        val c = toLocal(p3.latDeg, p3.lonDeg)

        val d1 = p1.distanceM; val d2 = p2.distanceM; val d3 = p3.distanceM

        // Unit vector from A to B
        val abN = b[0] - a[0]; val abE = b[1] - a[1]
        val dAB = sqrt(abN * abN + abE * abE)
        if (dAB < 1e-9) return null
        val exN = abN / dAB; val exE = abE / dAB

        // Vector from A to C, project onto ex/ey
        val acN = c[0] - a[0]; val acE = c[1] - a[1]
        val i = exN * acN + exE * acE
        val eyN = acN - i * exN; val eyE = acE - i * exE
        val j = sqrt(eyN * eyN + eyE * eyE)
        if (j < 1e-9) return null // points are collinear
        val eyNu = eyN / j; val eyEu = eyE / j

        // 2D trilateration
        val x = (d1 * d1 - d2 * d2 + dAB * dAB) / (2.0 * dAB)
        val y = (d1 * d1 - d3 * d3 + i * i + j * j - 2.0 * i * x) / (2.0 * j)

        val localN = a[0] + x * exN + y * eyNu
        val localE = a[1] + x * exE + y * eyEu

        val solvedLat = refLat + Math.toDegrees(localN / R)
        val solvedLon = refLon + Math.toDegrees(localE / (R * cosRef))

        val avgAlt = (p1.altMSL + p2.altMSL + p3.altMSL) / 3.0

        return ResectionResult(
            latDeg = solvedLat,
            lonDeg = solvedLon,
            altMSL = avgAlt,
            description = "Trilateration from 3 known points",
        )
    }
}
