package com.sit.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertIs

class WorkoutConfigTest {

    @Test fun `spec example - 30min, 5 sprints of 30s, 90s rest`() {
        val cfg = WorkoutConfig(totalSec = 30 * 60, sprints = 5, sprintSec = 30, restSec = 90)
        // total sprint = 150, total rest = 450, running total = 1800-600 = 1200, per cycle = 240
        assertEquals(150, cfg.totalSprintSec)
        assertEquals(450, cfg.totalRestSec)
        assertEquals(1200, cfg.totalRunningSec)
        assertEquals(240, cfg.individualRunningSec())
        assertTrue(cfg.isValid())
    }

    @Test fun `invalid when sprint plus rest exceeds total`() {
        val cfg = WorkoutConfig(totalSec = 60, sprints = 5, sprintSec = 30, restSec = 90)
        assertFalse(cfg.isValid())
        assertIs<ValidationState.Invalid>(cfg.validate())
    }

    @Test fun `invalid when no room for at least 1s of running per cycle`() {
        val cfg = WorkoutConfig(totalSec = 124, sprints = 5, sprintSec = 10, restSec = 14)
        // total = 124, sprint+rest = 5*(10+14) = 120, running = 4 < sprints
        assertFalse(cfg.isValid())
    }

    @Test fun `invalid for non-positive inputs`() {
        assertFalse(WorkoutConfig(0, 5, 30, 90).isValid())
        assertFalse(WorkoutConfig(600, 0, 30, 90).isValid())
        assertFalse(WorkoutConfig(600, 5, 0, 90).isValid())
        assertFalse(WorkoutConfig(600, 5, 30, 0).isValid())
    }
}
