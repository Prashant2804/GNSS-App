package com.gnssflow.app.projects

import com.gnssflow.app.db.PointEntity

object DxfExporter {

    fun exportPoints(points: List<PointEntity>): String {
        val sb = StringBuilder()

        section(sb, "HEADER") {
            appendLine("9"); appendLine("\$ACADVER"); appendLine("1"); appendLine("AC1015")
            appendLine("9"); appendLine("\$INSUNITS"); appendLine("70"); appendLine("6")
        }

        section(sb, "TABLES") {
            appendLine("0"); appendLine("TABLE"); appendLine("2"); appendLine("LAYER")
            appendLine("70"); appendLine("1")
            appendLine("0"); appendLine("LAYER")
            appendLine("2"); appendLine("GNSS_POINTS")
            appendLine("70"); appendLine("0")
            appendLine("62"); appendLine("7")
            appendLine("6"); appendLine("CONTINUOUS")
            appendLine("0"); appendLine("ENDTAB")
        }

        section(sb, "ENTITIES") {
            for (p in points) {
                appendLine("0"); appendLine("POINT")
                appendLine("8"); appendLine("GNSS_POINTS")
                appendLine("10"); appendLine(p.longitudeDeg.toString())
                appendLine("20"); appendLine(p.latitudeDeg.toString())
                appendLine("30"); appendLine(p.altitudeMSL.toString())

                appendLine("0"); appendLine("TEXT")
                appendLine("8"); appendLine("GNSS_POINTS")
                appendLine("10"); appendLine(p.longitudeDeg.toString())
                appendLine("20"); appendLine(p.latitudeDeg.toString())
                appendLine("30"); appendLine(p.altitudeMSL.toString())
                appendLine("40"); appendLine("0.0001")
                appendLine("1"); appendLine(p.code)
            }
        }

        sb.appendLine("0"); sb.appendLine("EOF")
        return sb.toString()
    }

    private inline fun section(sb: StringBuilder, name: String, block: StringBuilder.() -> Unit) {
        sb.appendLine("0"); sb.appendLine("SECTION")
        sb.appendLine("2"); sb.appendLine(name)
        sb.block()
        sb.appendLine("0"); sb.appendLine("ENDSEC")
    }
}
