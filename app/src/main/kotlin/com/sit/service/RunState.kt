package com.sit.service

import com.sit.domain.IntervalType

enum class RunPhase { IDLE, RUNNING, PAUSED, COMPLETED }

data class RunState(
    val phase: RunPhase,
    val intervalType: IntervalType,
    val currentIntervalIdx: Int,
    val totalIntervals: Int,
    val remainingInIntervalSec: Int,
    val intervalDurationSec: Int,
    val totalElapsedSec: Int,
    val totalWorkoutSec: Int,
    val currentCycle: Int,
    val totalCycles: Int,
) {
    companion object {
        val IDLE = RunState(
            phase = RunPhase.IDLE,
            intervalType = IntervalType.RUNNING,
            currentIntervalIdx = 0,
            totalIntervals = 0,
            remainingInIntervalSec = 0,
            intervalDurationSec = 0,
            totalElapsedSec = 0,
            totalWorkoutSec = 0,
            currentCycle = 0,
            totalCycles = 0,
        )
    }
}
