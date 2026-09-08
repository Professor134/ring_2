package com.example.ringapp.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class GamificationEngineTest {
    @Test fun completedHabitRewardsFourPoints() = assertEquals(4, GamificationEngine.calculateHabitReward(true))
    @Test fun incompleteHabitHasNoReward() = assertEquals(0, GamificationEngine.calculateHabitReward(false))
    @Test fun completedTaskRewardsTwoPoints() = assertEquals(2, GamificationEngine.calculateTaskReward(true))
    @Test fun creationCostsArePredictable() {
        assertEquals(25, GamificationEngine.calculateCreationCost(GamificationEngine.CreationKind.HABIT))
        assertEquals(1, GamificationEngine.calculateCreationCost(GamificationEngine.CreationKind.TASK))
    }
}