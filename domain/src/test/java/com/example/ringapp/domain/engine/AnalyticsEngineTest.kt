package com.example.ringapp.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsEngineTest {
    @Test fun dailyScoreUsesNormalizedWeights() = assertEquals(100, AnalyticsEngine.calculateDailyScore(100, 100, 100, 100))
    @Test fun completionRateHandlesEmptyInput() = assertEquals(0, AnalyticsEngine.calculateCompletionRate(0, 0))
    @Test fun growthComparesComparableScores() = assertEquals(12, AnalyticsEngine.calculateGrowth(72, 60))
}