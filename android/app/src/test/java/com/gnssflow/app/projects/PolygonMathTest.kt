package com.gnssflow.app.projects

import org.junit.Assert.assertTrue
import org.junit.Test

class PolygonMathTest {
    @Test
    fun areaPerimeter_triangleNonZero() {
        val pts = listOf(
            0.0 to 0.0,
            0.0 to 0.00001,
            0.00001 to 0.0,
        )
        val ap = PolygonMath.areaPerimeter(pts)
        assertTrue(ap.areaM2 > 0.0)
        assertTrue(ap.perimeterM > 0.0)
    }
}

