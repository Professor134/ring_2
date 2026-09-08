package com.example.ringapp.domain.engine

object GamificationEngine {
    fun calculateHabitReward(completed: Boolean): Int = if (completed) 4 else 0
    fun calculateTaskReward(completed: Boolean): Int = if (completed) 2 else 0
    fun calculateCreationCost(kind: CreationKind): Int = when (kind) {
        CreationKind.HABIT -> 25
        CreationKind.TASK -> 1
    }

    enum class CreationKind { HABIT, TASK }
}
