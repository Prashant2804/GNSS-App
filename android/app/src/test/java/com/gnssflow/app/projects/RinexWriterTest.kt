package com.gnssflow.app.projects

import com.gnssflow.app.db.ObservationEpochEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RinexWriterTest {

    private fun sampleSatJson(): String {
        return """[
            {"gnss_id":"GPS","sv_id":1,"signal":"L1C","pseudorange_m":21000000.0,"carrier_phase_cycles":110000000.0,"doppler_hz":-500.0,"cno_dbhz":42.0},
            {"gnss_id":"GPS","sv_id":1,"signal":"L2W","pseudorange_m":16350000.0,"carrier_phase_cycles":86000000.0,"doppler_hz":-389.0,"cno_dbhz":39.0},
            {"gnss_id":"GAL","sv_id":2,"signal":"E1C","pseudorange_m":22800000.0,"carrier_phase_cycles":120000000.0,"doppler_hz":-300.0,"cno_dbhz":40.0}
        ]"""
    }

    @Test
    fun emptyEpochsReturnsEmpty() {
        assertEquals("", RinexWriter.generate(emptyList()))
    }

    @Test
    fun singleEpochProducesValidRinex() {
        val epoch = ObservationEpochEntity(
            projectId = "p1",
            timestampUtc = "2024-06-15T10:30:45.000Z",
            gpsWeek = 2318,
            gpsTowS = 382245.0,
            receiverClockBiasS = 1.5e-8,
            satellitesJson = sampleSatJson(),
        )
        val rinex = RinexWriter.generate(listOf(epoch))

        assertTrue("Should have version header", rinex.contains("RINEX VERSION / TYPE"))
        assertTrue("Should have END OF HEADER", rinex.contains("END OF HEADER"))
        assertTrue("Should have epoch line", rinex.contains("> 2024 06 15 10 30"))
        assertTrue("Should have GPS satellite", rinex.contains("G01"))
        assertTrue("Should have Galileo satellite", rinex.contains("E02"))
    }

    @Test
    fun multipleEpochsAllAppear() {
        val epochs = (1..5).map { i ->
            ObservationEpochEntity(
                projectId = "p1",
                timestampUtc = "2024-06-15T10:30:%02d.000Z".format(i),
                gpsWeek = 2318,
                gpsTowS = 382245.0 + i,
                receiverClockBiasS = 1.5e-8,
                satellitesJson = sampleSatJson(),
            )
        }
        val rinex = RinexWriter.generate(epochs)
        val epochLines = rinex.lines().filter { it.startsWith(">") }
        assertEquals(5, epochLines.size)
    }

    @Test
    fun headerContainsApproxPosition() {
        val epoch = ObservationEpochEntity(
            projectId = "p1",
            timestampUtc = "2024-06-15T10:30:45.000Z",
            gpsWeek = 2318,
            gpsTowS = 382245.0,
            receiverClockBiasS = 0.0,
            satellitesJson = sampleSatJson(),
        )
        val rinex = RinexWriter.generate(listOf(epoch), approxLat = 12.9716, approxLon = 77.5946, approxAlt = 900.0)
        assertTrue("Should contain APPROX POSITION XYZ", rinex.contains("APPROX POSITION XYZ"))
    }
}
