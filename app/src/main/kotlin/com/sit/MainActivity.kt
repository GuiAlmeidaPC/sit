package com.sit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sit.i18n.LocalAppStrings
import com.sit.i18n.stringsFor
import com.sit.service.RunPhase
import com.sit.service.RunStateHolder
import com.sit.service.TimerService
import com.sit.ui.active.ActiveRunScreen
import com.sit.ui.setup.SetupScreen
import com.sit.ui.setup.SetupViewModel
import com.sit.ui.summary.SummaryScreen
import com.sit.ui.theme.SitTheme

class MainActivity : ComponentActivity() {

    private val setupViewModel: SetupViewModel by viewModels {
        SetupViewModel.factory(application)
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maybeRequestNotificationPermission()
        setContent {
            val setup by setupViewModel.state.collectAsStateWithLifecycle()
            val run by RunStateHolder.state.collectAsStateWithLifecycle()
            val strings = remember(setup.language) { stringsFor(setup.language) }
            CompositionLocalProvider(LocalAppStrings provides strings) {
                SitTheme(theme = setup.theme) {
                    when (run.phase) {
                        RunPhase.IDLE -> SetupScreen(
                            state = setup,
                            onTotalSecChange = setupViewModel::setTotalSec,
                            onSprintsChange = setupViewModel::setSprints,
                            onSprintSecChange = setupViewModel::setSprintSec,
                            onRestSecChange = setupViewModel::setRestSec,
                            onAudioChange = setupViewModel::setAudio,
                            onThemeChange = setupViewModel::setTheme,
                            onLanguageChange = setupViewModel::setLanguage,
                            onStart = { TimerService.start(this@MainActivity, setup.config, setup.language) },
                        )
                        RunPhase.COMPLETED -> SummaryScreen(
                            state = run,
                            onDone = { RunStateHolder.reset() },
                        )
                        RunPhase.RUNNING, RunPhase.PAUSED -> ActiveRunScreen(
                            state = run,
                            onTogglePause = { TimerService.togglePause(this@MainActivity) },
                            onStop = { TimerService.stop(this@MainActivity) },
                        )
                    }
                }
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
