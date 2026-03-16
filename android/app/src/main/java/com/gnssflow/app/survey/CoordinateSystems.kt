package com.gnssflow.app.survey

data class CoordinateSystem(
    val code: String,
    val name: String,
)

data class BaseShift(
    val dx: Double,
    val dy: Double,
    val scale: Double,
    val rotationRad: Double,
)
