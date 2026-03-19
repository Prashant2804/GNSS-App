package com.gnssflow.app.projects

import com.gnssflow.app.db.PointEntity
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class KmlExporterTest {

    private fun point(code: String, lat: Double = 12.0, lon: Double = 77.0, alt: Double = 900.0) =
        PointEntity(
            id = "id-$code", projectId = "proj", code = code,
            latitudeDeg = lat, longitudeDeg = lon, altitudeMSL = alt,
            imuRollDeg = null, imuPitchDeg = null, imuYawDeg = null,
            horizontalAccuracyM = 0.5, createdAtEpochMs = 1000L,
        )

    @Test
    fun emptyListProducesValidKml() {
        val kml = KmlExporter.exportPoints(emptyList())
        assertTrue(kml.contains("<kml"))
        assertTrue(kml.contains("</kml>"))
        assertFalse(kml.contains("<Placemark>"))
    }

    @Test
    fun singlePointHasCoordinatesAndCode() {
        val kml = KmlExporter.exportPoints(listOf(point("PT1", 12.5, 77.3, 850.0)))
        assertTrue(kml.contains("<name>PT1</name>"))
        assertTrue(kml.contains("77.3,12.5,850.0"))
    }

    @Test
    fun specialCharactersAreEscaped() {
        val kml = KmlExporter.exportPoints(listOf(point("A<B&C")))
        assertTrue(kml.contains("A&lt;B&amp;C"))
        assertFalse(kml.contains("A<B&C"))
    }

    @Test
    fun multiplePointsAllPresent() {
        val kml = KmlExporter.exportPoints(listOf(point("A"), point("B"), point("C")))
        assertTrue(kml.contains("<name>A</name>"))
        assertTrue(kml.contains("<name>B</name>"))
        assertTrue(kml.contains("<name>C</name>"))
    }
}
