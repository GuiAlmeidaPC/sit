package com.sit.domain

/** Logical block types the user can place into an Advanced+ workout. */
enum class BlockType {
    WARMUP,
    RUN,
    WALK,
    REST,
    /** Container that repeats a list of [SimpleBlock]s a given number of times. */
    REPEAT;

    /** Map a user-facing block type to the engine's coarse interval category. */
    fun toIntervalType(): IntervalType = when (this) {
        WARMUP -> IntervalType.RUNNING
        RUN -> IntervalType.SPRINTING
        WALK -> IntervalType.RESTING
        REST -> IntervalType.RESTING
        REPEAT -> error("REPEAT is a container, not a runnable interval")
    }
}

sealed class Block {
    abstract val id: String
    abstract val type: BlockType
}

data class SimpleBlock(
    override val id: String,
    override val type: BlockType,
    val durationSec: Int,
) : Block() {
    init {
        require(type != BlockType.REPEAT) { "SimpleBlock cannot have type REPEAT" }
    }
}

data class RepeatBlock(
    override val id: String,
    val repeats: Int,
    val steps: List<SimpleBlock>,
) : Block() {
    override val type: BlockType = BlockType.REPEAT
}

/** Recursive total duration of [blocks]. */
fun List<Block>.totalDurationSec(): Int = sumOf { block ->
    when (block) {
        is SimpleBlock -> block.durationSec
        is RepeatBlock -> block.repeats.coerceAtLeast(0) *
            block.steps.sumOf { it.durationSec }
    }
}

/** Recursive count of RUN-typed simple blocks (i.e. sprint count). */
fun List<Block>.totalRunCount(): Int = sumOf { block ->
    when (block) {
        is SimpleBlock -> if (block.type == BlockType.RUN) 1 else 0
        is RepeatBlock -> block.repeats.coerceAtLeast(0) *
            block.steps.count { it.type == BlockType.RUN }
    }
}

/** Recursive sum of durations across all blocks of [type]. */
fun List<Block>.totalDurationSecOf(type: BlockType): Int = sumOf { block ->
    when (block) {
        is SimpleBlock -> if (block.type == type) block.durationSec else 0
        is RepeatBlock -> block.repeats.coerceAtLeast(0) *
            block.steps.filter { it.type == type }.sumOf { it.durationSec }
    }
}
