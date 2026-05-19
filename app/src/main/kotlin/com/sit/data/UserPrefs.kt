package com.sit.data

import com.sit.domain.AppLanguage
import com.sit.domain.AppTheme
import com.sit.domain.AudioTrack
import com.sit.domain.WorkoutConfig

data class UserPrefs(
    val config: WorkoutConfig,
    val theme: AppTheme,
    val language: AppLanguage,
) {
    companion object {
        val DEFAULT = UserPrefs(
            config = WorkoutConfig(
                totalSec = 30 * 60,
                sprints = 5,
                sprintSec = 30,
                restSec = 90,
                audio = AudioTrack.DOG_BARKING,
            ),
            theme = AppTheme.CLASSIC,
            language = AppLanguage.ENGLISH,
        )
    }
}
