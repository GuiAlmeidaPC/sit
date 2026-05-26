package com.sit.domain

data class Interval(
    val type: IntervalType,
    val durationSec: Int,
    val cycleIndex: Int,
    /** Original block type for Advanced+ planning. Null for Basic/Advanced modes. */
    val blockType: BlockType? = null,
)
