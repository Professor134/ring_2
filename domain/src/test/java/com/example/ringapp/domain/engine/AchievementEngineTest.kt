package com.example.ringapp.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEngineTest {
    @Test
    fun profileProgressCreatesFourIncreasingTiersForEachAchievement() {
        val progress = AchievementEngine.buildProfileProgress(
            longestStreak = 7,
            habitsCreated = 5,
            completedTasks = 10,
            lifetimePoints = 500,
            level = 5,
            habitCompletions = 20
        )

        assertEquals(6, progress.size)
        progress.forEach { achievement ->
            assertEquals(
                listOf(AchievementTier.NEWBIE, AchievementTier.EXPERIENCED, AchievementTier.EXPERT, AchievementTier.ELITE),
                achievement.tiers.map { it.tier }
            )
            assertTrue(achievement.tiers.zipWithNext().all { (lower, higher) -> higher.target > lower.target })
        }
        assertEquals(1f, progress.first().tiers[2].progress)
        assertEquals(7, progress.first().value)
        assertEquals(1f, progress[2].tiers[1].progress)
        assertEquals(0.25f, progress[3].tiers[2].progress, 0.001f)
    }

    @Test
    fun negativeMetricsAreDisplayedAsZero() {
        val progress = AchievementEngine.buildProfileProgress(
            longestStreak = -1,
            habitsCreated = -1,
            completedTasks = -1,
            lifetimePoints = -1,
            level = -1,
            habitCompletions = -1
        )

        assertTrue(progress.all { achievement ->
            achievement.value == 0 && achievement.tiers.all { it.current == 0 && it.progress == 0f }
        })
    }
}
