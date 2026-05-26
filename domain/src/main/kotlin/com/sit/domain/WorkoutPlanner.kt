package com.sit.domain

object WorkoutPlanner {

    /**
     * Builds the ordered list of intervals for a workout.
     *
     * For [WorkoutMode.BASIC] and [WorkoutMode.ADVANCED] this emits the classic
     * `RUNNING -> SPRINTING -> RESTING` cycles with the per-cycle running
     * duration rounded to whole seconds; any leftover seconds from the division
     * are folded into the very first RUNNING interval so the sum of all
     * interval durations exactly equals [WorkoutConfig.totalSec].
     *
     * For [WorkoutMode.ADVANCED_PLUS] this flattens the user-defined block list
     * (expanding [RepeatBlock]s) into the linear timeline the engine will play.
     *
     * Requires a valid config (see [WorkoutConfig.validate]).
     */
    fun build(config: WorkoutConfig): List<Interval> {
        require(config.isValid()) { "Cannot plan an invalid workout: ${config.validate()}" }
        return when (config.mode) {
            WorkoutMode.BASIC, WorkoutMode.ADVANCED -> buildTopDown(config)
            WorkoutMode.ADVANCED_PLUS -> buildAdvancedPlus(config)
        }
    }

    private fun buildTopDown(config: WorkoutConfig): List<Interval> {
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

    private fun buildAdvancedPlus(config: WorkoutConfig): List<Interval> {
        val out = ArrayList<Interval>()
        var runsSeen = 0

        fun emit(step: SimpleBlock) {
            if (step.type == BlockType.RUN) runsSeen++
            // cycleIndex points at the "current" sprint cycle (1-based -> 0-based).
            // Before any RUN has been seen we clamp to 0 so the UI shows "Cycle 1 of N".
            val cycleIndex = (runsSeen - 1).coerceAtLeast(0)
            out += Interval(
                type = step.type.toIntervalType(),
                durationSec = step.durationSec,
                cycleIndex = cycleIndex,
                blockType = step.type,
            )
        }

        for (block in config.advancedPlusBlocks) {
            when (block) {
                is SimpleBlock -> emit(block)
                is RepeatBlock -> repeat(block.repeats.coerceAtLeast(0)) {
                    block.steps.forEach(::emit)
                }
            }
        }
        return out
    }
}
