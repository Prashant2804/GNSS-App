package com.gnssflow.app.projects

import com.gnssflow.app.db.PointEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {
    @Test
    fun emptyPointsReturnsHeaderOnly() {
        val csv = CsvExporter.exportPoints(emptyList())
        val lines = csv.trim().lines()
        assertEquals(1, lines.size)
        assertTrue(lines[0].startsWith("code,"))
    }

    @Test
    fun singlePointProducesCorrectRow() {
        val point = PointEntity(
            id = "p1",
            projectId = "proj1",
            code = "PT1",
            latitudeDeg = 51.5074,
            longitudeDeg = -0.1278,
            altitudeMSL = 10.0,
            imuRollDeg = 1.0,
            imuPitchDeg = 2.0,
            imuYawDeg = 3.0,
            horizontalAccuracyM = 0.02,
            createdAtEpochMs = 1000L,
        )
        val csv = CsvExporter.exportPoints(listOf(point))
        val lines = csv.trim().lines()
        assertEquals(2, lines.size)
        val row = lines[1].split(",")
        assertEquals("PT1", row[0])
        assertEquals("51.5074", row[1])
        assertEquals("-0.1278", row[2])
        assertEquals("10.0", row[3])
        assertEquals("0.02", row[4])
        assertEquals("1.0", row[5])
        assertEquals("2.0", row[6])
        assertEquals("3.0", row[7])
        assertEquals("1000", row[8])
    }

    @Test
    fun nullFieldsProduceEmptyColumns() {
        val point = PointEntity(
            id = "p2",
            projectId = "proj1",
            code = "PT2",
            latitudeDeg = 0.0,
            longitudeDeg = 0.0,
            altitudeMSL = 0.0,
            imuRollDeg = null,
            imuPitchDeg = null,
            imuYawDeg = null,
            horizontalAccuracyM = null,
            createdAtEpochMs = 2000L,
        )
        val csv = CsvExporter.exportPoints(listOf(point))
        val row = csv.trim().lines()[1].split(",")
        assertEquals("", row[4])
        assertEquals("", row[5])
        assertEquals("", row[6])
        assertEquals("", row[7])
    }

    @Test
    fun codeWithCommaIsEscaped() {
        val point = PointEntity(
            id = "p3",
            projectId = "proj1",
            code = "A,B",
            latitudeDeg = 0.0,
            longitudeDeg = 0.0,
            altitudeMSL = 0.0,
            imuRollDeg = null,
            imuPitchDeg = null,
            imuYawDeg = null,
            horizontalAccuracyM = null,
            createdAtEpochMs = 3000L,
        )
        val csv = CsvExporter.exportPoints(listOf(point))
        assertTrue(csv.contains("\"A,B\""))
    }
}
