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
        totalSec <= 0 -> ValidationState.Invalid("Total workout time must be positive")
        sprints <= 0 -> ValidationState.Invalid("Number of sprints must be at least 1")
        sprintSec <= 0 -> ValidationState.Invalid("Sprint duration must be positive")
        restSec <= 0 -> ValidationState.Invalid("Rest duration must be positive")
        totalRunningSec < sprints ->
            ValidationState.Invalid("Sprint + rest time exceeds (or leaves <1s/cycle for) running")
        else -> ValidationState.Valid
    }

    fun isValid(): Boolean = validate() is ValidationState.Valid
}

sealed interface ValidationState {
    data object Valid : ValidationState
    data class Invalid(val reason: String) : ValidationState
}
