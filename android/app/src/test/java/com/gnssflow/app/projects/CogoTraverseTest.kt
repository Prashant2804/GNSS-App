package com.gnssflow.app.projects

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class CogoTraverseTest {
    @Test
    fun traverseNorthIncreasesLatitude() {
        val startLat = 51.5074
        val startLon = -0.1278
        val distM = 100.0
        val azDeg = 0.0
        val azRad = Math.toRadians(azDeg)
        val dN = distM * cos(azRad)
        val dE = distM * sin(azRad)

        val r = 6371000.0
        val newLat = startLat + Math.toDegrees(dN / r)
        val meanLatRad = Math.toRadians((startLat + newLat) / 2.0)
        val newLon = startLon + Math.toDegrees(dE / (r * cos(meanLatRad)))

        assert(newLat > startLat) { "Traversing north should increase lat" }
        assertEquals(startLon, newLon, 0.0001)
    }

    @Test
    fun traverseEastIncreasesLongitude() {
        val startLat = 0.0
        val startLon = 0.0
        val distM = 100.0
        val azDeg = 90.0
        val azRad = Math.toRadians(azDeg)
        val dN = distM * cos(azRad)
        val dE = distM * sin(azRad)

        val r = 6371000.0
        val newLat = startLat + Math.toDegrees(dN / r)
        val meanLatRad = Math.toRadians((startLat + newLat) / 2.0)
        val newLon = startLon + Math.toDegrees(dE / (r * cos(meanLatRad)))

        assertEquals(0.0, newLat, 0.0001)
        assert(newLon > startLon) { "Traversing east should increase lon" }
    }

    @Test
    fun traverseRoundTripConsistentWithInverse() {
        val startLat = 48.8566
        val startLon = 2.3522
        val distM = 500.0
        val azDeg = 45.0
        val azRad = Math.toRadians(azDeg)
        val dN = distM * cos(azRad)
        val dE = distM * sin(azRad)

        val r = 6371000.0
        val newLat = startLat + Math.toDegrees(dN / r)
        val meanLatRad = Math.toRadians((startLat + newLat) / 2.0)
        val newLon = startLon + Math.toDegrees(dE / (r * cos(meanLatRad)))

        val inverseD = Geo.distanceMeters(startLat, startLon, newLat, newLon)
        assertEquals(distM, inverseD, 1.0)

        val inverseAz = Geo.bearingDegrees(startLat, startLon, newLat, newLon)
        assertEquals(azDeg, inverseAz, 1.0)
    }
}
