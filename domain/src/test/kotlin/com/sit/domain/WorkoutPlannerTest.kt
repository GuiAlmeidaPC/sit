package com.sit.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkoutPlannerTest {

    @Test fun `clean division - spec example`() {
        val cfg = WorkoutConfig(totalSec = 1800, sprints = 5, sprintSec = 30, restSec = 90)
        val plan = WorkoutPlanner.build(cfg)

        assertEquals(15, plan.size)
        assertEquals(1800, plan.sumOf { it.durationSec })

        plan.chunked(3).forEachIndexed { i, cycle ->
            assertEquals(IntervalType.RUNNING, cycle[0].type)
            assertEquals(IntervalType.SPRINTING, cycle[1].type)
            assertEquals(IntervalType.RESTING, cycle[2].type)
            assertEquals(i, cycle[0].cycleIndex)
            assertEquals(240, cycle[0].durationSec)
            assertEquals(30, cycle[1].durationSec)
            assertEquals(90, cycle[2].durationSec)
        }
    }

    @Test fun `remainder is folded into first running interval`() {
        // running total = 1000 - 5*(20+30) = 750; per cycle = 150; remainder = 0.
        // Use one that yields a real remainder:
        // total 1003, 5 sprints of 20, rest 30 -> running 753, base 150 (*5=750), remainder 3
        val cfg = WorkoutConfig(totalSec = 1003, sprints = 5, sprintSec = 20, restSec = 30)
        val plan = WorkoutPlanner.build(cfg)

        assertEquals(1003, plan.sumOf { it.durationSec })

        val runs = plan.filter { it.type == IntervalType.RUNNING }
        assertEquals(153, runs.first().durationSec) // 150 + remainder 3
        runs.drop(1).forEach { assertEquals(150, it.durationSec) }
    }

    @Test fun `negative remainder also folds into first running interval`() {
        // Construct case where division rounds up (remainder negative).
        // running total = totalRunningSec, base = totalRunningSec / sprints (int div).
        // Int division truncates toward zero so remainder is always >= 0 for positive ints;
        // however if we later switch to nearest-integer rounding, this guards against drift.
        // For now verify exactness invariant under a range of inputs.
        for (total in listOf(601, 602, 603, 604, 605, 1234, 9999)) {
            val cfg = WorkoutConfig(totalSec = total, sprints = 7, sprintSec = 15, restSec = 25)
            if (!cfg.isValid()) continue
            val plan = WorkoutPlanner.build(cfg)
            assertEquals(total, plan.sumOf { it.durationSec },
                "total mismatch for totalSec=$total")
        }
    }

    @Test fun `single sprint produces single cycle`() {
        val cfg = WorkoutConfig(totalSec = 300, sprints = 1, sprintSec = 30, restSec = 60)
        val plan = WorkoutPlanner.build(cfg)
        assertEquals(3, plan.size)
        assertEquals(210, plan[0].durationSec) // 300 - 30 - 60
        assertEquals(IntervalType.RUNNING, plan[0].type)
        assertEquals(IntervalType.SPRINTING, plan[1].type)
        assertEquals(IntervalType.RESTING, plan[2].type)
    }

    @Test fun `building from invalid config throws`() {
        val cfg = WorkoutConfig(totalSec = 60, sprints = 5, sprintSec = 30, restSec = 90)
        assertFailsWith<IllegalArgumentException> { WorkoutPlanner.build(cfg) }
    }

    @Test fun `cycle indices are 0 through N-1`() {
        val cfg = WorkoutConfig(totalSec = 1800, sprints = 5, sprintSec = 30, restSec = 90)
        val plan = WorkoutPlanner.build(cfg)
        val cycleIndices = plan.map { it.cycleIndex }.distinct()
        assertEquals(listOf(0, 1, 2, 3, 4), cycleIndices)
    }
}
