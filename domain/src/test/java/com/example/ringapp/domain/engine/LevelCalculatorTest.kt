package com.example.ringapp.domain.engine

import com.example.ringapp.domain.usecase.LevelCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class LevelCalculatorTest {
    @Test fun levelStartsAtOneBeforeSecondThreshold() = assertEquals(1, LevelCalculator.levelForLifetimePoints(1499))
    @Test fun levelAdvancesAtThreshold() = assertEquals(2, LevelCalculator.levelForLifetimePoints(1500))
    @Test fun levelNeverDecreasesForHigherXp() = assertEquals(3, LevelCalculator.levelForLifetimePoints(2250))
}