package com.sit.domain

data class WorkoutConfig(
    val totalSec: Int,
    val sprints: Int,
    val sprintSec: Int,
    val restSec: Int,
    val audio: AudioTrack = AudioTrack.DOG_BARKING,
    val mode: WorkoutMode = WorkoutMode.BASIC,
    val advancedSprintSecs: List<Int> = List(sprints.coerceAtLeast(0)) { sprintSec.coerceAtLeast(0) },
    val advancedPlusBlocks: List<Block> = emptyList(),
) {
    /** Number of sprint cycles (RUN intervals) the engine should count down. */
    val sprintCount: Int get() = when (mode) {
        WorkoutMode.BASIC -> sprints
        WorkoutMode.ADVANCED -> advancedSprintSecs.size
        WorkoutMode.ADVANCED_PLUS -> advancedPlusBlocks.totalRunCount()
    }

    /** Per-sprint durations in order, for the modes that have them. Empty for ADVANCED_PLUS. */
    val sprintDurationsSec: List<Int> get() = when (mode) {
        WorkoutMode.BASIC -> List(sprints.coerceAtLeast(0)) { sprintSec }
        WorkoutMode.ADVANCED -> advancedSprintSecs
        WorkoutMode.ADVANCED_PLUS -> emptyList()
    }

    /** Effective total workout duration. For ADVANCED_PLUS this is derived from blocks. */
    val effectiveTotalSec: Int get() = when (mode) {
        WorkoutMode.BASIC, WorkoutMode.ADVANCED -> totalSec
        WorkoutMode.ADVANCED_PLUS -> advancedPlusBlocks.totalDurationSec()
    }

    val totalSprintSec: Int get() = when (mode) {
        WorkoutMode.ADVANCED_PLUS -> advancedPlusBlocks.totalDurationSecOf(BlockType.RUN)
        else -> sprintDurationsSec.sum()
    }

    val totalRestSec: Int get() = when (mode) {
        WorkoutMode.ADVANCED_PLUS ->
            advancedPlusBlocks.totalDurationSecOf(BlockType.WALK) +
                advancedPlusBlocks.totalDurationSecOf(BlockType.REST)
        else -> sprintCount * restSec
    }

    val totalRunningSec: Int get() = when (mode) {
        WorkoutMode.ADVANCED_PLUS -> advancedPlusBlocks.totalDurationSecOf(BlockType.WARMUP)
        else -> totalSec - totalSprintSec - totalRestSec
    }

    /** Per-cycle running duration, rounded to the nearest whole second.
     *  Any remainder is folded into the first running interval by the planner.
     *  N/A for ADVANCED_PLUS. */
    fun individualRunningSec(): Int =
        if (sprintCount <= 0) 0 else totalRunningSec / sprintCount

    fun validate(): ValidationState = when (mode) {
        WorkoutMode.ADVANCED_PLUS -> validateAdvancedPlus()
        else -> validateTopDown()
    }

    private fun validateTopDown(): ValidationState = when {
        totalSec <= 0 -> ValidationState.Invalid(ValidationMessage.TOTAL_TIME_MUST_BE_POSITIVE)
        sprintCount <= 0 -> ValidationState.Invalid(ValidationMessage.SPRINTS_MUST_BE_AT_LEAST_ONE)
        sprintDurationsSec.any { it <= 0 } ->
            ValidationState.Invalid(ValidationMessage.SPRINT_DURATION_MUST_BE_POSITIVE)
        restSec <= 0 -> ValidationState.Invalid(ValidationMessage.REST_DURATION_MUST_BE_POSITIVE)
        totalRunningSec < sprintCount ->
            ValidationState.Invalid(ValidationMessage.RUNNING_TIME_MUST_FIT)
        else -> ValidationState.Valid
    }

    private fun validateAdvancedPlus(): ValidationState {
        if (advancedPlusBlocks.isEmpty()) {
            return ValidationState.Invalid(ValidationMessage.BLOCKS_MUST_NOT_BE_EMPTY)
        }
        advancedPlusBlocks.forEach { block ->
            when (block) {
                is SimpleBlock -> if (block.durationSec <= 0) {
                    return ValidationState.Invalid(ValidationMessage.BLOCK_DURATION_MUST_BE_POSITIVE)
                }
                is RepeatBlock -> {
                    if (block.repeats <= 0) {
                        return ValidationState.Invalid(ValidationMessage.REPEAT_COUNT_MUST_BE_POSITIVE)
                    }
                    if (block.steps.isEmpty()) {
                        return ValidationState.Invalid(ValidationMessage.REPEAT_MUST_HAVE_STEPS)
                    }
                    if (block.steps.any { it.durationSec <= 0 }) {
                        return ValidationState.Invalid(ValidationMessage.BLOCK_DURATION_MUST_BE_POSITIVE)
                    }
                }
            }
        }
        return ValidationState.Valid
    }

    fun isValid(): Boolean = validate() is ValidationState.Valid
}

sealed interface ValidationState {
    data object Valid : ValidationState
    data class Invalid(val reason: ValidationMessage) : ValidationState
}

enum class ValidationMessage {
    TOTAL_TIME_MUST_BE_POSITIVE,
    SPRINTS_MUST_BE_AT_LEAST_ONE,
    SPRINT_DURATION_MUST_BE_POSITIVE,
    REST_DURATION_MUST_BE_POSITIVE,
    RUNNING_TIME_MUST_FIT,
    BLOCKS_MUST_NOT_BE_EMPTY,
    BLOCK_DURATION_MUST_BE_POSITIVE,
    REPEAT_COUNT_MUST_BE_POSITIVE,
    REPEAT_MUST_HAVE_STEPS,
}
