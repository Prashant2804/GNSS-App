package com.gnssflow.app

import com.gnssflow.app.survey.Geometry
import com.gnssflow.app.survey.SurveyPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class GeometryTest {
    @Test
    fun distanceComputesExpected() {
        val a = SurveyPoint("a", 0.0, 0.0, 0.0, "A")
        val b = SurveyPoint("b", 3.0, 4.0, 0.0, "B")
        assertEquals(5.0, Geometry.distance(a, b), 0.0001)
    }
}
