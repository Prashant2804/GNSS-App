package com.gnssflow.app.projects

import com.gnssflow.app.db.PointEntity
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class DxfExporterTest {

    private fun point(code: String, lat: Double = 12.0, lon: Double = 77.0, alt: Double = 900.0) =
        PointEntity(
            id = "id-$code", projectId = "proj", code = code,
            latitudeDeg = lat, longitudeDeg = lon, altitudeMSL = alt,
            imuRollDeg = null, imuPitchDeg = null, imuYawDeg = null,
            horizontalAccuracyM = null, createdAtEpochMs = 1000L,
        )

    @Test
    fun emptyListProducesValidDxf() {
        val dxf = DxfExporter.exportPoints(emptyList())
        assertTrue(dxf.contains("HEADER"))
        assertTrue(dxf.contains("EOF"))
        val entitySection = dxf.substringAfter("ENTITIES").substringBefore("ENDSEC")
        assertFalse("ENTITIES section should be empty", entitySection.contains("POINT"))
    }

    @Test
    fun singlePointHasCoordinatesAndLabel() {
        val dxf = DxfExporter.exportPoints(listOf(point("STA1", 12.5, 77.3, 850.0)))
        assertTrue(dxf.contains("POINT"))
        assertTrue(dxf.contains("77.3"))
        assertTrue(dxf.contains("12.5"))
        assertTrue(dxf.contains("850.0"))
        assertTrue(dxf.contains("STA1"))
    }

    @Test
    fun multiplePointsAllPresent() {
        val dxf = DxfExporter.exportPoints(listOf(point("A"), point("B")))
        assertTrue(dxf.contains("A"))
        assertTrue(dxf.contains("B"))
    }

    @Test
    fun layerNameIsGnssPoints() {
        val dxf = DxfExporter.exportPoints(listOf(point("X")))
        assertTrue(dxf.contains("GNSS_POINTS"))
    }
}
