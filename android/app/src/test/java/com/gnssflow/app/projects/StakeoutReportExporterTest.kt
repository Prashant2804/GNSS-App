package com.gnssflow.app.projects

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class StakeoutReportExporterTest {

    private fun entry(code: String = "PT1") = StakeoutReportEntry(
        targetCode = code,
        targetLat = 12.0, targetLon = 77.0, targetAlt = 900.0,
        measuredLat = 12.0001, measuredLon = 77.0001, measuredAlt = 899.5,
        deltaNorthM = 11.1, deltaEastM = 9.4, deltaUpM = -0.5,
        distanceM = 14.6,
        horizontalAccuracyM = 0.02,
        timestampMs = 1700000000000L,
    )

    @Test
    fun csvHasHeaderAndRow() {
        val csv = StakeoutReportExporter.exportCsv(listOf(entry()))
        assertTrue(csv.contains("target_code"))
        assertTrue(csv.contains("PT1"))
        assertTrue(csv.contains("11.100"))
    }

    @Test
    fun csvEmptyListHasOnlyHeader() {
        val csv = StakeoutReportExporter.exportCsv(emptyList())
        assertTrue(csv.contains("target_code"))
        assertFalse(csv.contains("PT1"))
    }

    @Test
    fun textReportContainsDeltas() {
        val text = StakeoutReportExporter.exportText(listOf(entry("STA1"), entry("STA2")))
        assertTrue(text.contains("STAKEOUT REPORT"))
        assertTrue(text.contains("STA1"))
        assertTrue(text.contains("STA2"))
        assertTrue(text.contains("Delta N"))
        assertTrue(text.contains("Records: 2"))
    }

    @Test
    fun textReportEmptyList() {
        val text = StakeoutReportExporter.exportText(emptyList())
        assertTrue(text.contains("No stakeout records"))
    }
}
