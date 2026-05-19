package com.sit.domain

object WorkoutPlanner {

    /**
     * Builds the ordered list of intervals for a workout: for each of the N cycles,
     * emits RUNNING -> SPRINTING -> RESTING. The per-cycle running duration is
     * rounded to whole seconds; any leftover seconds from the division are added
     * to (or subtracted from) the very first RUNNING interval so the sum of all
     * interval durations exactly equals [WorkoutConfig.totalSec].
     *
     * Requires a valid config (see [WorkoutConfig.validate]).
     */
    fun build(config: WorkoutConfig): List<Interval> {
        require(config.isValid()) { "Cannot plan an invalid workout: ${config.validate()}" }

        val baseRun = config.individualRunningSec()
        val remainder = config.totalRunningSec - baseRun * config.sprintCount
        val firstRun = baseRun + remainder

        val out = ArrayList<Interval>(config.sprintCount * 3)
        for ((i, sprintDurationSec) in config.sprintDurationsSec.withIndex()) {
            val runSec = if (i == 0) firstRun else baseRun
            out += Interval(IntervalType.RUNNING, runSec, i)
            out += Interval(IntervalType.SPRINTING, sprintDurationSec, i)
            out += Interval(IntervalType.RESTING, config.restSec, i)
        }
        return out
    }
}
