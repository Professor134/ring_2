// -----------------------------------------------------------------------------
// Project: Ring 2
// File: domain\src\test\java\com\example\ringapp\domain\engine\GamificationEngineTest.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test

// Handles the gamification engine test logic for this component without modifying runtime behavior.
// Represents the gamification engine test structure used by the application.
class GamificationEngineTest {
    @Test fun completedHabitRewardsFourPoints() = assertEquals(4, GamificationEngine.calculateHabitReward(true))
    @Test fun incompleteHabitHasNoReward() = assertEquals(0, GamificationEngine.calculateHabitReward(false))
    @Test fun completedTaskRewardsTwoPoints() = assertEquals(2, GamificationEngine.calculateTaskReward(true))
    @Test fun creationCostsArePredictable() {
        assertEquals(25, GamificationEngine.calculateCreationCost(GamificationEngine.CreationKind.HABIT))
        assertEquals(1, GamificationEngine.calculateCreationCost(GamificationEngine.CreationKind.TASK))
    }
}