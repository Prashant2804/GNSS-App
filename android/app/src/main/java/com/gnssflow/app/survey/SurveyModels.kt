package com.gnssflow.app.survey

data class SurveyPoint(
    val id: String,
    val east: Double,
    val north: Double,
    val elevation: Double,
    val code: String,
)

data class SurveyLine(
    val id: String,
    val vertices: List<SurveyPoint>,
)

data class SurveyPolygon(
    val id: String,
    val vertices: List<SurveyPoint>,
)
