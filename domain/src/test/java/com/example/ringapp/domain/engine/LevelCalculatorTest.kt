// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\test\java\com\example\ringapp\domain\engine\LevelCalculatorTest.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.engine

import com.example.ringapp.domain.usecase.LevelCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

// Handles the level calculator test logic for this component without modifying runtime behavior.
// Represents the level calculator test structure used by the application.
class LevelCalculatorTest {
    @Test fun levelStartsAtOneBeforeSecondThreshold() = assertEquals(1, LevelCalculator.levelForLifetimePoints(99))
    @Test fun levelAdvancesAtThreshold() = assertEquals(2, LevelCalculator.levelForLifetimePoints(100))
    @Test fun levelNeverDecreasesForHigherXp() = assertEquals(3, LevelCalculator.levelForLifetimePoints(225))
}