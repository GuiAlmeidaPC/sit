package com.sit.data

import com.sit.domain.AppLanguage
import com.sit.domain.AppTheme
import com.sit.domain.AudioTrack
import com.sit.domain.Block
import com.sit.domain.BlockType
import com.sit.domain.RepeatBlock
import com.sit.domain.SimpleBlock
import com.sit.domain.WorkoutConfig
import com.sit.domain.WorkoutMode

data class UserPrefs(
    val config: WorkoutConfig,
    val theme: AppTheme,
    val language: AppLanguage,
) {
    companion object {
        val DEFAULT_ADVANCED_PLUS_BLOCKS: List<Block> = listOf(
            SimpleBlock(id = "default-warmup", type = BlockType.WARMUP, durationSec = 300),
            RepeatBlock(
                id = "default-repeat",
                repeats = 5,
                steps = listOf(
                    SimpleBlock(id = "default-repeat-run", type = BlockType.RUN, durationSec = 30),
                    SimpleBlock(id = "default-repeat-walk", type = BlockType.WALK, durationSec = 90),
                ),
            ),
            SimpleBlock(id = "default-cooldown", type = BlockType.REST, durationSec = 180),
        )

        val DEFAULT = UserPrefs(
            config = WorkoutConfig(
                totalSec = 30 * 60,
                sprints = 5,
                sprintSec = 30,
                restSec = 90,
                audio = AudioTrack.DOG_BARKING,
                mode = WorkoutMode.BASIC,
                advancedSprintSecs = List(5) { 30 },
                advancedPlusBlocks = DEFAULT_ADVANCED_PLUS_BLOCKS,
            ),
            theme = AppTheme.CLASSIC,
            language = AppLanguage.ENGLISH,
        )
    }
}
