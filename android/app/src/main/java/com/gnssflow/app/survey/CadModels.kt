package com.gnssflow.app.survey

data class CadLayer(
    val name: String,
    val color: String,
)

data class DxfEntity(
    val id: String,
    val layer: String,
)
