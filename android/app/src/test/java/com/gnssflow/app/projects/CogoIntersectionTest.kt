package com.gnssflow.app.projects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CogoIntersectionTest {

    @Test
    fun bearingBearing_perpendicularBearingsMeetAtExpectedPoint() {
        val result = CogoIntersectionViewModel.bearingBearing(
            lat1 = 0.0, lon1 = 0.0, brng1Deg = 45.0,
            lat2 = 0.01, lon2 = 0.0, brng2Deg = 135.0,
        )
        assertNotNull(result)
        val midLon = result!!.second
        assert(midLon > 0.0) { "Intersection longitude should be positive, got $midLon" }
    }

    @Test
    fun bearingBearing_parallelBearingsReturnNull() {
        val result = CogoIntersectionViewModel.bearingBearing(
            lat1 = 0.0, lon1 = 0.0, brng1Deg = 90.0,
            lat2 = 0.001, lon2 = 0.0, brng2Deg = 90.0,
        )
        assertNull(result)
    }

    @Test
    fun distanceDistance_overlappingCirclesMeetCorrectly() {
        val result = CogoIntersectionViewModel.distanceDistance(
            lat1 = 0.0, lon1 = 0.0, d1 = 1000.0,
            lat2 = 0.0, lon2 = 0.01, d2 = 1000.0,
        )
        assertNotNull(result)
        val midLon = 0.005
        assertEquals(midLon, result!!.second, 0.002)
    }

    @Test
    fun distanceDistance_tooFarApartReturnsNull() {
        val result = CogoIntersectionViewModel.distanceDistance(
            lat1 = 0.0, lon1 = 0.0, d1 = 10.0,
            lat2 = 1.0, lon2 = 0.0, d2 = 10.0,
        )
        assertNull(result)
    }
}
