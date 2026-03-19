package com.gnssflow.app.projects

import com.gnssflow.app.db.PointEntity

object KmlExporter {

    fun exportPoints(points: List<PointEntity>, documentName: String = "GNSS Flow Points"): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<kml xmlns="http://www.opengis.net/kml/2.2">""")
        sb.appendLine("<Document>")
        sb.appendLine("  <name>${esc(documentName)}</name>")

        sb.appendLine("""  <Style id="gnss-point">""")
        sb.appendLine("    <IconStyle>")
        sb.appendLine("      <scale>1.0</scale>")
        sb.appendLine("      <Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-circle.png</href></Icon>")
        sb.appendLine("    </IconStyle>")
        sb.appendLine("  </Style>")

        for (p in points) {
            sb.appendLine("  <Placemark>")
            sb.appendLine("    <name>${esc(p.code)}</name>")
            sb.appendLine("    <styleUrl>#gnss-point</styleUrl>")

            val desc = buildString {
                append("Accuracy: ${p.horizontalAccuracyM ?: "N/A"} m")
                if (p.imuRollDeg != null) {
                    append("\nIMU: R=%.1f° P=%.1f° Y=%.1f°".format(p.imuRollDeg, p.imuPitchDeg, p.imuYawDeg))
                }
            }
            sb.appendLine("    <description>${esc(desc)}</description>")

            sb.appendLine("    <Point>")
            sb.appendLine("      <altitudeMode>absolute</altitudeMode>")
            sb.appendLine("      <coordinates>${p.longitudeDeg},${p.latitudeDeg},${p.altitudeMSL}</coordinates>")
            sb.appendLine("    </Point>")
            sb.appendLine("  </Placemark>")
        }

        sb.appendLine("</Document>")
        sb.appendLine("</kml>")
        return sb.toString()
    }

    private fun esc(s: String): String =
        s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}
