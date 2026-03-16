package com.gnssflow.app.survey

object Exporters {
    fun exportCsv(points: List<SurveyPoint>): String {
        val header = "id,east,north,elevation,code"
        val rows = points.joinToString("\n") {
            "${it.id},${it.east},${it.north},${it.elevation},${it.code}"
        }
        return "$header\n$rows"
    }
}
