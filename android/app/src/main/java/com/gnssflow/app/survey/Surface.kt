package com.gnssflow.app.survey

data class SurfaceTin(val id: String, val triangles: List<Triangle>)

data class Triangle(
    val a: SurveyPoint,
    val b: SurveyPoint,
    val c: SurveyPoint,
)
