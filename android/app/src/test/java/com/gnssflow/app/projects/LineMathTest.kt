package com.gnssflow.app.projects

import org.junit.Assert.assertEquals
import org.junit.Test

class LineMathTest {
    @Test
    fun stationOffset_onXAxis() {
        // A(0,0) to B(0,10) in N/E: increasing East
        val r1 = LineMath.stationOffset(
            aNorth = 0.0, aEast = 0.0,
            bNorth = 0.0, bEast = 10.0,
            pNorth = 0.0, pEast = 5.0,
        )
        assertEquals(5.0, r1.stationM, 1e-6)
        assertEquals(0.0, r1.offsetM, 1e-6)

        val r2 = LineMath.stationOffset(
            aNorth = 0.0, aEast = 0.0,
            bNorth = 0.0, bEast = 10.0,
            pNorth = 2.0, pEast = 5.0,
        )
        assertEquals(5.0, r2.stationM, 1e-6)
        assertEquals(2.0, r2.offsetM, 1e-6)
    }
}

