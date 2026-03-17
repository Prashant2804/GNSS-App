package com.gnssflow.app.survey

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectRulesEngineTest {
    @Test
    fun shouldCollect_whenTimeOrDistanceThresholdMet() {
        val rule = CollectRule(minSeconds = 5, minDistanceM = 2.0)
        assertFalse(CollectRulesEngine.shouldCollect(elapsedSeconds = 1, distanceMeters = 0.5, rule = rule))
        assertTrue(CollectRulesEngine.shouldCollect(elapsedSeconds = 5, distanceMeters = 0.5, rule = rule))
        assertTrue(CollectRulesEngine.shouldCollect(elapsedSeconds = 1, distanceMeters = 2.0, rule = rule))
    }
}

