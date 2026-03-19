package com.gnssflow.app.projects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ResectionSolverTest {

    @Test
    fun triangleWithKnownDistancesFindsCenter() {
        val midLat = 0.005
        val d1 = Geo.distanceMeters(midLat, 0.005, 0.0, 0.0)
        val d2 = Geo.distanceMeters(midLat, 0.005, 0.01, 0.0)
        val d3 = Geo.distanceMeters(midLat, 0.005, 0.0, 0.01)

        val result = ResectionSolver.solve(
            ResectionSolver.KnownPoint(0.0, 0.0, 100.0, d1),
            ResectionSolver.KnownPoint(0.01, 0.0, 100.0, d2),
            ResectionSolver.KnownPoint(0.0, 0.01, 100.0, d3),
        )
        assertNotNull(result)
        assertEquals(midLat, result!!.latDeg, 0.001)
        assertEquals(0.005, result.lonDeg, 0.001)
    }

    @Test
    fun collinearPointsHandledGracefully() {
        ResectionSolver.solve(
            ResectionSolver.KnownPoint(0.0, 0.0, 0.0, 100.0),
            ResectionSolver.KnownPoint(0.001, 0.0, 0.0, 100.0),
            ResectionSolver.KnownPoint(0.002, 0.0, 0.0, 100.0),
        )
    }

    @Test
    fun verySmallDistancesStillComputeResult() {
        val result = ResectionSolver.solve(
            ResectionSolver.KnownPoint(0.0, 0.0, 0.0, 1.0),
            ResectionSolver.KnownPoint(10.0, 0.0, 0.0, 1.0),
            ResectionSolver.KnownPoint(0.0, 10.0, 0.0, 1.0),
        )
        assertNotNull(result)
    }
}
