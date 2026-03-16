package com.gnssflow.app.survey

data class StakeoutReport(
    val targetId: String,
    val measuredEast: Double,
    val measuredNorth: Double,
    val measuredElevation: Double,
    val deltaEast: Double,
    val deltaNorth: Double,
    val deltaElevation: Double,
)
