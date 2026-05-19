package com.sit.domain

data class Interval(
    val type: IntervalType,
    val durationSec: Int,
    val cycleIndex: Int,
)
