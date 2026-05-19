package com.sit.domain

data class WorkoutConfig(
    val totalSec: Int,
    val sprints: Int,
    val sprintSec: Int,
    val restSec: Int,
    val audio: AudioTrack = AudioTrack.DOG_BARKING,
    val mode: WorkoutMode = WorkoutMode.BASIC,
    val advancedSprintSecs: List<Int> = List(sprints.coerceAtLeast(0)) { sprintSec.coerceAtLeast(0) },
) {
    val sprintCount: Int get() = when (mode) {
        WorkoutMode.BASIC -> sprints
        WorkoutMode.ADVANCED -> advancedSprintSecs.size
    }

    val sprintDurationsSec: List<Int> get() = when (mode) {
        WorkoutMode.BASIC -> List(sprints.coerceAtLeast(0)) { sprintSec }
        WorkoutMode.ADVANCED -> advancedSprintSecs
    }

    val totalSprintSec: Int get() = sprintDurationsSec.sum()
    val totalRestSec: Int get() = sprintCount * restSec
    val totalRunningSec: Int get() = totalSec - totalSprintSec - totalRestSec

    /** Per-cycle running duration, rounded to the nearest whole second.
     *  Any remainder is folded into the first running interval by the planner. */
    fun individualRunningSec(): Int =
        if (sprintCount <= 0) 0 else totalRunningSec / sprintCount

    fun validate(): ValidationState = when {
        totalSec <= 0 -> ValidationState.Invalid(ValidationMessage.TOTAL_TIME_MUST_BE_POSITIVE)
        sprintCount <= 0 -> ValidationState.Invalid(ValidationMessage.SPRINTS_MUST_BE_AT_LEAST_ONE)
        sprintDurationsSec.any { it <= 0 } ->
            ValidationState.Invalid(ValidationMessage.SPRINT_DURATION_MUST_BE_POSITIVE)
        restSec <= 0 -> ValidationState.Invalid(ValidationMessage.REST_DURATION_MUST_BE_POSITIVE)
        totalRunningSec < sprintCount ->
            ValidationState.Invalid(ValidationMessage.RUNNING_TIME_MUST_FIT)
        else -> ValidationState.Valid
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
}
