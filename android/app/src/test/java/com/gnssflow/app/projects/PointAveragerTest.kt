package com.gnssflow.app.projects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PointAveragerTest {

    @Test
    fun emptyListReturnsNull() {
        assertNull(PointAverager.average(emptyList()))
    }

    @Test
    fun singleSampleReturnsItself() {
        val s = PointAverager.Sample(12.0, 77.0, 900.0, 0.5)
        val r = PointAverager.average(listOf(s))
        assertNotNull(r)
        assertEquals(12.0, r!!.latitudeDeg, 1e-9)
        assertEquals(77.0, r.longitudeDeg, 1e-9)
        assertEquals(900.0, r.altitudeMSL, 1e-9)
        assertEquals(0.5, r.horizontalAccuracyM!!, 1e-9)
        assertEquals(1, r.epochCount)
    }

    @Test
    fun averageOfTwoSamples() {
        val samples = listOf(
            PointAverager.Sample(10.0, 20.0, 100.0, 1.0),
            PointAverager.Sample(12.0, 22.0, 200.0, 3.0),
        )
        val r = PointAverager.average(samples)!!
        assertEquals(11.0, r.latitudeDeg, 1e-9)
        assertEquals(21.0, r.longitudeDeg, 1e-9)
        assertEquals(150.0, r.altitudeMSL, 1e-9)
        assertEquals(2.0, r.horizontalAccuracyM!!, 1e-9)
        assertEquals(2, r.epochCount)
    }

    @Test
    fun nullAccuraciesHandled() {
        val samples = listOf(
            PointAverager.Sample(10.0, 20.0, 100.0, null),
            PointAverager.Sample(12.0, 22.0, 200.0, null),
        )
        val r = PointAverager.average(samples)!!
        assertNull(r.horizontalAccuracyM)
    }
}
