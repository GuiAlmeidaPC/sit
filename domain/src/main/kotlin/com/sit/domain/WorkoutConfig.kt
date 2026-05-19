package com.sit.domain

data class WorkoutConfig(
    val totalSec: Int,
    val sprints: Int,
    val sprintSec: Int,
    val restSec: Int,
    val audio: AudioTrack = AudioTrack.DOG_BARKING,
) {
    val totalSprintSec: Int get() = sprints * sprintSec
    val totalRestSec: Int get() = sprints * restSec
    val totalRunningSec: Int get() = totalSec - totalSprintSec - totalRestSec

    /** Per-cycle running duration, rounded to the nearest whole second.
     *  Any remainder is folded into the first running interval by the planner. */
    fun individualRunningSec(): Int =
        if (sprints <= 0) 0 else totalRunningSec / sprints

    fun validate(): ValidationState = when {
        totalSec <= 0 -> ValidationState.Invalid(ValidationMessage.TOTAL_TIME_MUST_BE_POSITIVE)
        sprints <= 0 -> ValidationState.Invalid(ValidationMessage.SPRINTS_MUST_BE_AT_LEAST_ONE)
        sprintSec <= 0 -> ValidationState.Invalid(ValidationMessage.SPRINT_DURATION_MUST_BE_POSITIVE)
        restSec <= 0 -> ValidationState.Invalid(ValidationMessage.REST_DURATION_MUST_BE_POSITIVE)
        totalRunningSec < sprints ->
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
