package com.example.ringapp.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEngineTest {
    @Test
    fun profileProgressCreatesFourIncreasingTiersForEachAchievement() {
        val progress = AchievementEngine.buildProfileProgress(
            longestStreak = 21,
            habitsCreated = 20,
            completedTasks = 25,
            lifetimePoints = 1_500,
            level = 8,
            habitCompletions = 100
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
        assertEquals(21, progress.first().value)
        assertEquals(1f, progress[2].tiers[1].progress)
        assertEquals(1f, progress[3].tiers[1].progress)
        assertEquals(listOf(20, 50, 100, 200), progress[1].tiers.map { it.target })
        assertEquals(listOf(10, 25, 50, 100), progress[1].tiers.map { it.rewardPoints })
    }

    @Test
    fun laterTiersStayLockedUntilThePreviousThresholdIsReached() {
        val progress = AchievementEngine.buildProfileProgress(
            longestStreak = 0,
            habitsCreated = 19,
            completedTasks = 0,
            lifetimePoints = 0,
            level = 1,
            habitCompletions = 0
        ).first { it.id == "habit-builder" }

        assertTrue(progress.tiers[0].unlocked)
        assertTrue(progress.tiers[1].unlocked.not())
        assertTrue(progress.tiers[1].progress == 0f)

        val atFirstThreshold = AchievementEngine.buildProfileProgress(
            longestStreak = 0,
            habitsCreated = 20,
            completedTasks = 0,
            lifetimePoints = 0,
            level = 1,
            habitCompletions = 0
        ).first { it.id == "habit-builder" }
        assertTrue(atFirstThreshold.tiers[0].progress == 1f)
        assertTrue(atFirstThreshold.tiers[1].unlocked)
    }

    @Test
    fun earnedTiersStayEarnedWhenCurrentProgressDrops() {
        val earned = AchievementEngine.buildProfileProgress(
            longestStreak = 0,
            habitsCreated = 0,
            completedTasks = 0,
            lifetimePoints = 0,
            level = 1,
            habitCompletions = 0,
            earnedTierReferences = setOf("achievement-habit-builder-newbie")
        ).first { it.id == "habit-builder" }

        assertTrue(earned.tiers[0].earned)
        assertTrue(earned.tiers[0].unlocked)
        assertEquals(1f, earned.tiers[0].progress)
        assertTrue(earned.tiers[1].unlocked)
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
