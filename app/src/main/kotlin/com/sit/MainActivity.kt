package com.sit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sit.ui.setup.SetupScreen
import com.sit.ui.setup.SetupViewModel
import com.sit.ui.theme.SitTheme

class MainActivity : ComponentActivity() {

    private val setupViewModel: SetupViewModel by viewModels {
        SetupViewModel.factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by setupViewModel.state.collectAsStateWithLifecycle()
            SitTheme(theme = state.theme) {
                SetupScreen(
                    state = state,
                    onTotalSecChange = setupViewModel::setTotalSec,
                    onSprintsChange = setupViewModel::setSprints,
                    onSprintSecChange = setupViewModel::setSprintSec,
                    onRestSecChange = setupViewModel::setRestSec,
                    onAudioChange = setupViewModel::setAudio,
                    onThemeChange = setupViewModel::setTheme,
                    onStart = {
                        // TODO Phase 3: launch the TimerService + Active Run screen
                    },
                )
            }
        }
    }
}
