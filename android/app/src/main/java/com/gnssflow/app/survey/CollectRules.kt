package com.gnssflow.app.survey

data class CollectRule(
    val minSeconds: Int,
    val minDistanceM: Double,
)

object CollectRulesEngine {
    fun shouldCollect(elapsedSeconds: Int, distanceMeters: Double, rule: CollectRule): Boolean {
        return elapsedSeconds >= rule.minSeconds || distanceMeters >= rule.minDistanceM
    }
}
