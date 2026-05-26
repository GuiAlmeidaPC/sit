package com.sit.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.sit.audio.AudioController
import com.sit.data.BlocksCodec
import com.sit.domain.AppLanguage
import com.sit.domain.AudioTrack
import com.sit.domain.IntervalType
import com.sit.domain.WorkoutConfig
import com.sit.domain.WorkoutMode
import com.sit.domain.WorkoutPlanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var engine: WorkoutEngine? = null
    private val binder = LocalBinder()
    private var audio: AudioController? = null
    private var appLanguage: AppLanguage = AppLanguage.ENGLISH
    private var audioTrack: AudioTrack = AudioTrack.DOG_BARKING
    private var lastIntervalType: IntervalType? = null

    inner class LocalBinder : Binder() {
        fun service(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        audio = AudioController(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startWorkout(intent)
            ACTION_STOP -> stopWorkout()
            ACTION_TOGGLE_PAUSE -> engine?.togglePause()
        }
        return START_NOT_STICKY
    }

    private fun startWorkout(intent: Intent) {
        if (engine != null) return
        appLanguage = runCatching {
            AppLanguage.valueOf(intent.getStringExtra(EXTRA_LANGUAGE) ?: AppLanguage.ENGLISH.name)
        }.getOrDefault(AppLanguage.ENGLISH)
        val config = WorkoutConfig(
            totalSec = intent.getIntExtra(EXTRA_TOTAL_SEC, 0),
            sprints = intent.getIntExtra(EXTRA_SPRINTS, 0),
            sprintSec = intent.getIntExtra(EXTRA_SPRINT_SEC, 0),
            restSec = intent.getIntExtra(EXTRA_REST_SEC, 0),
            audio = AudioTrack.valueOf(
                intent.getStringExtra(EXTRA_AUDIO) ?: AudioTrack.DOG_BARKING.name
            ),
            mode = runCatching {
                WorkoutMode.valueOf(intent.getStringExtra(EXTRA_MODE) ?: WorkoutMode.BASIC.name)
            }.getOrDefault(WorkoutMode.BASIC),
            advancedSprintSecs = intent.getIntArrayExtra(EXTRA_ADVANCED_SPRINTS)?.toList() ?: emptyList(),
            advancedPlusBlocks = BlocksCodec.decode(intent.getStringExtra(EXTRA_ADVANCED_PLUS_BLOCKS)),
        )
        if (!config.isValid()) {
            stopSelf()
            return
        }
        audioTrack = config.audio
        lastIntervalType = null
        val intervals = WorkoutPlanner.build(config)
        NotificationHelper.ensureChannel(this, appLanguage)

        val initial = RunState(
            phase = RunPhase.RUNNING,
            intervalType = intervals.first().type,
            currentIntervalIdx = 0,
            totalIntervals = intervals.size,
            remainingInIntervalSec = intervals.first().durationSec,
            intervalDurationSec = intervals.first().durationSec,
            totalElapsedSec = 0,
            totalWorkoutSec = intervals.sumOf { it.durationSec },
            currentCycle = 1,
            totalCycles = config.sprintCount,
        )
        RunStateHolder.set(initial)
        startForegroundCompat(NotificationHelper.build(this, initial, appLanguage))

        engine = WorkoutEngine(
            intervals = intervals,
            totalCycles = config.sprintCount,
            scope = scope,
            onTick = ::handleTick,
            onComplete = ::handleComplete,
        ).also { it.start() }
    }

    private fun handleTick(state: RunState) {
        applyAudioTransition(state)
        RunStateHolder.set(state)
        val mgr = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        mgr.notify(NotificationHelper.NOTIFICATION_ID, NotificationHelper.build(this, state, appLanguage))
    }

    private fun applyAudioTransition(state: RunState) {
        if (state.phase == RunPhase.PAUSED) {
            audio?.stopSprint()
            // Clear so a resume into the same sprint interval restarts audio.
            lastIntervalType = null
            return
        }
        val current = state.intervalType
        if (current == lastIntervalType) return
        lastIntervalType = current
        if (current == IntervalType.SPRINTING) {
            audio?.playSprint(audioTrack)
        } else {
            audio?.stopSprint()
        }
    }

    private fun handleComplete() {
        // Hold the COMPLETED state on the holder so UI can show the summary;
        // tear down the foreground service + notification.
        audio?.stopSprint()
        engine?.stop()
        engine = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopWorkout() {
        audio?.stopSprint()
        engine?.stop()
        engine = null
        RunStateHolder.reset()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundCompat(n: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.NOTIFICATION_ID,
                n,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID, n)
        }
    }

    override fun onDestroy() {
        // Defensive path: covers system-initiated destruction (low memory,
        // task removed) on top of the explicit teardown in stopWorkout /
        // handleComplete, so we never leak a ghost notification or audio
        // focus past the service's lifetime.
        engine?.stop()
        engine = null
        audio?.release()
        audio = null
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Throwable) {
            // Already stopped — ignore.
        }
        if (RunStateHolder.state.value.phase == RunPhase.RUNNING ||
            RunStateHolder.state.value.phase == RunPhase.PAUSED
        ) {
            RunStateHolder.reset()
        }
        scope.cancel()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // User swiped the app away — tear the workout down so we don't keep
        // running silently with no UI surface.
        stopWorkout()
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        const val ACTION_START = "com.sit.service.START"
        const val ACTION_STOP = "com.sit.service.STOP"
        const val ACTION_TOGGLE_PAUSE = "com.sit.service.TOGGLE_PAUSE"

        private const val EXTRA_TOTAL_SEC = "total_sec"
        private const val EXTRA_SPRINTS = "sprints"
        private const val EXTRA_SPRINT_SEC = "sprint_sec"
        private const val EXTRA_REST_SEC = "rest_sec"
        private const val EXTRA_AUDIO = "audio"
        private const val EXTRA_LANGUAGE = "language"
        private const val EXTRA_MODE = "mode"
        private const val EXTRA_ADVANCED_SPRINTS = "advanced_sprints"
        private const val EXTRA_ADVANCED_PLUS_BLOCKS = "advanced_plus_blocks"

        fun start(ctx: Context, config: WorkoutConfig, language: AppLanguage) {
            val i = Intent(ctx, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TOTAL_SEC, config.totalSec)
                putExtra(EXTRA_SPRINTS, config.sprints)
                putExtra(EXTRA_SPRINT_SEC, config.sprintSec)
                putExtra(EXTRA_REST_SEC, config.restSec)
                putExtra(EXTRA_AUDIO, config.audio.name)
                putExtra(EXTRA_LANGUAGE, language.name)
                putExtra(EXTRA_MODE, config.mode.name)
                putExtra(EXTRA_ADVANCED_SPRINTS, config.advancedSprintSecs.toIntArray())
                putExtra(EXTRA_ADVANCED_PLUS_BLOCKS, BlocksCodec.encode(config.advancedPlusBlocks))
            }
            ctx.startForegroundService(i)
        }

        fun stop(ctx: Context) {
            ctx.startService(
                Intent(ctx, TimerService::class.java).setAction(ACTION_STOP)
            )
        }

        fun togglePause(ctx: Context) {
            ctx.startService(
                Intent(ctx, TimerService::class.java).setAction(ACTION_TOGGLE_PAUSE)
            )
        }
    }
}
