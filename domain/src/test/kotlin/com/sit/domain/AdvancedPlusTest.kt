package com.sit.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AdvancedPlusTest {

    private fun simple(id: String, type: BlockType, durationSec: Int) =
        SimpleBlock(id = id, type = type, durationSec = durationSec)

    @Test fun `totalSec sums simple blocks and unrolled repeat blocks`() {
        val blocks = listOf<Block>(
            simple("w", BlockType.WARMUP, 300),
            RepeatBlock(
                id = "r",
                repeats = 3,
                steps = listOf(
                    simple("s1", BlockType.RUN, 30),
                    simple("s2", BlockType.WALK, 60),
                ),
            ),
            simple("c", BlockType.REST, 120),
        )
        // 300 + 3*(30+60) + 120 = 300 + 270 + 120 = 690
        assertEquals(690, blocks.totalDurationSec())
        assertEquals(3, blocks.totalRunCount())
        assertEquals(90, blocks.totalDurationSecOf(BlockType.RUN))
        assertEquals(180, blocks.totalDurationSecOf(BlockType.WALK))
    }

    @Test fun `WorkoutConfig in ADVANCED_PLUS mode derives totals from blocks`() {
        val blocks = listOf<Block>(
            simple("w", BlockType.WARMUP, 300),
            RepeatBlock(
                id = "r",
                repeats = 5,
                steps = listOf(
                    simple("s1", BlockType.RUN, 30),
                    simple("s2", BlockType.WALK, 90),
                ),
            ),
        )
        val cfg = WorkoutConfig(
            totalSec = 0,
            sprints = 0,
            sprintSec = 0,
            restSec = 0,
            mode = WorkoutMode.ADVANCED_PLUS,
            advancedPlusBlocks = blocks,
        )
        assertTrue(cfg.isValid())
        assertEquals(5, cfg.sprintCount)
        assertEquals(900, cfg.effectiveTotalSec) // 300 + 5*(30+90)
        assertEquals(150, cfg.totalSprintSec)
        assertEquals(450, cfg.totalRestSec)
    }

    @Test fun `planner flattens repeat blocks in order`() {
        val cfg = WorkoutConfig(
            totalSec = 0, sprints = 0, sprintSec = 0, restSec = 0,
            mode = WorkoutMode.ADVANCED_PLUS,
            advancedPlusBlocks = listOf(
                simple("w", BlockType.WARMUP, 300),
                RepeatBlock(
                    id = "r",
                    repeats = 2,
                    steps = listOf(
                        simple("s1", BlockType.RUN, 30),
                        simple("s2", BlockType.WALK, 60),
                    ),
                ),
                simple("c", BlockType.REST, 120),
            ),
        )
        val plan = WorkoutPlanner.build(cfg)
        assertEquals(6, plan.size) // 1 + 2*(2) + 1
        assertEquals(420 + 180, plan.sumOf { it.durationSec }) // 300 + 2*(30+60) + 120 = 600

        // Block type tags preserved.
        assertEquals(BlockType.WARMUP, plan[0].blockType)
        assertEquals(BlockType.RUN, plan[1].blockType)
        assertEquals(BlockType.WALK, plan[2].blockType)
        assertEquals(BlockType.RUN, plan[3].blockType)
        assertEquals(BlockType.WALK, plan[4].blockType)
        assertEquals(BlockType.REST, plan[5].blockType)

        // Interval type mapping.
        assertEquals(IntervalType.RUNNING, plan[0].type)
        assertEquals(IntervalType.SPRINTING, plan[1].type)
        assertEquals(IntervalType.RESTING, plan[2].type)
        assertEquals(IntervalType.SPRINTING, plan[3].type)

        // Cycle indices: 0 before any RUN, then 0,0,1,1,1 (RUN bumps before emit).
        assertEquals(listOf(0, 0, 0, 1, 1, 1), plan.map { it.cycleIndex })
    }

    @Test fun `empty block list is invalid`() {
        val cfg = WorkoutConfig(
            totalSec = 0, sprints = 0, sprintSec = 0, restSec = 0,
            mode = WorkoutMode.ADVANCED_PLUS,
            advancedPlusBlocks = emptyList(),
        )
        assertFalse(cfg.isValid())
        assertIs<ValidationState.Invalid>(cfg.validate())
    }

    @Test fun `zero duration block is invalid`() {
        val cfg = WorkoutConfig(
            totalSec = 0, sprints = 0, sprintSec = 0, restSec = 0,
            mode = WorkoutMode.ADVANCED_PLUS,
            advancedPlusBlocks = listOf(simple("a", BlockType.RUN, 0)),
        )
        assertFalse(cfg.isValid())
    }

    @Test fun `repeat with zero repeats is invalid`() {
        val cfg = WorkoutConfig(
            totalSec = 0, sprints = 0, sprintSec = 0, restSec = 0,
            mode = WorkoutMode.ADVANCED_PLUS,
            advancedPlusBlocks = listOf(
                RepeatBlock(
                    id = "r", repeats = 0,
                    steps = listOf(simple("s", BlockType.RUN, 30)),
                ),
            ),
        )
        assertFalse(cfg.isValid())
    }

    @Test fun `repeat with empty steps is invalid`() {
        val cfg = WorkoutConfig(
            totalSec = 0, sprints = 0, sprintSec = 0, restSec = 0,
            mode = WorkoutMode.ADVANCED_PLUS,
            advancedPlusBlocks = listOf(
                RepeatBlock(id = "r", repeats = 3, steps = emptyList()),
            ),
        )
        assertFalse(cfg.isValid())
    }
}
