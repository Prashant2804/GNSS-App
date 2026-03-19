package com.gnssflow.app.projects

import org.junit.Assert.assertEquals
import org.junit.Test

class CogoInverseTest {
    @Test
    fun distanceBetweenSamePointIsZero() {
        val d = Geo.distanceMeters(51.5074, -0.1278, 51.5074, -0.1278)
        assertEquals(0.0, d, 0.001)
    }

    @Test
    fun distanceReturnsPositive() {
        val d = Geo.distanceMeters(51.5074, -0.1278, 48.8566, 2.3522)
        assert(d > 300_000) { "London to Paris should be > 300 km, got $d" }
        assert(d < 400_000) { "London to Paris should be < 400 km, got $d" }
    }

    @Test
    fun bearingNorthIsZero() {
        val b = Geo.bearingDegrees(0.0, 0.0, 1.0, 0.0)
        assertEquals(0.0, b, 0.01)
    }

    @Test
    fun bearingEastIs90() {
        val b = Geo.bearingDegrees(0.0, 0.0, 0.0, 1.0)
        assertEquals(90.0, b, 0.5)
    }

    @Test
    fun deltaNorthEastConsistent() {
        val (dN, dE) = Geo.deltaNorthEastMeters(0.0, 0.0, 0.001, 0.0)
        assert(dN > 100) { "0.001 deg north should be > 100 m, got $dN" }
        assertEquals(0.0, dE, 1.0)
    }
}
