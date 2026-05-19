package com.sit.service

import android.os.SystemClock
import com.sit.domain.Interval
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Drives the workout timer. Uses [SystemClock.elapsedRealtime] deltas (not tick
 * counts) so drift, doze, and brief coroutine pauses can't accumulate error.
 *
 * Lifecycle: [start] -> ([pause]/[resume])* -> [stop]/auto-complete.
 */
class WorkoutEngine(
    private val intervals: List<Interval>,
    private val totalCycles: Int,
    private val scope: CoroutineScope,
    private val onTick: (RunState) -> Unit,
    private val onComplete: () -> Unit,
) {
    private val totalSec: Int = intervals.sumOf { it.durationSec }

    private var startElapsedMs: Long = 0L
    private var pausedAccumMs: Long = 0L
    private var pausedAtMs: Long? = null
    private var tickJob: Job? = null
    private var completed = false

    val totalWorkoutSec: Int get() = totalSec

    fun start() {
        startElapsedMs = SystemClock.elapsedRealtime()
        pausedAccumMs = 0L
        pausedAtMs = null
        completed = false
        emit()
        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive && !completed) {
                delay(250L)
                if (pausedAtMs == null) emit()
            }
        }
    }

    fun pause() {
        if (pausedAtMs != null || completed) return
        pausedAtMs = SystemClock.elapsedRealtime()
        emit()
    }

    fun resume() {
        val pa = pausedAtMs ?: return
        pausedAccumMs += SystemClock.elapsedRealtime() - pa
        pausedAtMs = null
        emit()
    }

    fun togglePause() {
        if (pausedAtMs == null) pause() else resume()
    }

    fun stop() {
        tickJob?.cancel()
        tickJob = null
    }

    private fun effectiveElapsedSec(): Int {
        val now = SystemClock.elapsedRealtime()
        val activePausedMs = pausedAtMs?.let { now - it } ?: 0L
        val ms = (now - startElapsedMs) - pausedAccumMs - activePausedMs
        return (ms / 1000L).toInt().coerceAtLeast(0)
    }

    private fun emit() {
        val elapsed = effectiveElapsedSec()
        if (elapsed >= totalSec) {
            completed = true
            onTick(
                RunState(
                    phase = RunPhase.COMPLETED,
                    intervalType = intervals.last().type,
                    currentIntervalIdx = intervals.lastIndex,
                    totalIntervals = intervals.size,
                    remainingInIntervalSec = 0,
                    intervalDurationSec = intervals.last().durationSec,
                    totalElapsedSec = totalSec,
                    totalWorkoutSec = totalSec,
                    currentCycle = totalCycles,
                    totalCycles = totalCycles,
                )
            )
            onComplete()
            return
        }
        var acc = 0
        for ((idx, iv) in intervals.withIndex()) {
            val end = acc + iv.durationSec
            if (elapsed < end) {
                val intoInterval = elapsed - acc
                val remaining = iv.durationSec - intoInterval
                onTick(
                    RunState(
                        phase = if (pausedAtMs != null) RunPhase.PAUSED else RunPhase.RUNNING,
                        intervalType = iv.type,
                        currentIntervalIdx = idx,
                        totalIntervals = intervals.size,
                        remainingInIntervalSec = remaining,
                        intervalDurationSec = iv.durationSec,
                        totalElapsedSec = elapsed,
                        totalWorkoutSec = totalSec,
                        currentCycle = iv.cycleIndex + 1,
                        totalCycles = totalCycles,
                    )
                )
                return
            }
            acc = end
        }
    }
}
