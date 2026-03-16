package com.gnssflow.app

import com.gnssflow.app.survey.Cogo
import com.gnssflow.app.survey.SurveyPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class CogoTest {
    @Test
    fun traverseCreatesPoint() {
        val start = SurveyPoint("s", 0.0, 0.0, 0.0, "S")
        val result = Cogo.traverse(start, 10.0, 0.0, 1.0)
        assertEquals(0.0, result.east, 0.0001)
        assertEquals(10.0, result.north, 0.0001)
        assertEquals(1.0, result.elevation, 0.0001)
    }
}
