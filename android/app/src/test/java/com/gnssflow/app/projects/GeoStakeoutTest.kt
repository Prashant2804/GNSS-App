package com.gnssflow.app.projects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoStakeoutTest {
    @Test
    fun bearingDegrees_isNorthEastSouthWest() {
        val fromLat = 0.0
        val fromLon = 0.0
        assertEquals(0.0, Geo.bearingDegrees(fromLat, fromLon, 1.0, 0.0), 2.0)
        assertEquals(90.0, Geo.bearingDegrees(fromLat, fromLon, 0.0, 1.0), 2.0)
        assertEquals(180.0, Geo.bearingDegrees(fromLat, fromLon, -1.0, 0.0), 2.0)
        assertEquals(270.0, Geo.bearingDegrees(fromLat, fromLon, 0.0, -1.0), 2.0)
    }

    @Test
    fun deltaNorthEastMeters_signsMatchDirection() {
        val (dn1, de1) = Geo.deltaNorthEastMeters(0.0, 0.0, 0.001, 0.0)
        assertTrue(dn1 > 0)
        assertEquals(0.0, de1, 1.0)

        val (dn2, de2) = Geo.deltaNorthEastMeters(0.0, 0.0, 0.0, 0.001)
        assertTrue(de2 > 0)
        assertEquals(0.0, dn2, 1.0)
    }
}

