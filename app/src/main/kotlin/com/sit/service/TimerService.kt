package com.sit.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.sit.domain.AudioTrack
import com.sit.domain.WorkoutConfig
import com.sit.domain.WorkoutPlanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var engine: WorkoutEngine? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun service(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
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
        val config = WorkoutConfig(
            totalSec = intent.getIntExtra(EXTRA_TOTAL_SEC, 0),
            sprints = intent.getIntExtra(EXTRA_SPRINTS, 0),
            sprintSec = intent.getIntExtra(EXTRA_SPRINT_SEC, 0),
            restSec = intent.getIntExtra(EXTRA_REST_SEC, 0),
            audio = AudioTrack.valueOf(
                intent.getStringExtra(EXTRA_AUDIO) ?: AudioTrack.DOG_BARKING.name
            ),
        )
        if (!config.isValid()) {
            stopSelf()
            return
        }
        val intervals = WorkoutPlanner.build(config)

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
            totalCycles = config.sprints,
        )
        RunStateHolder.set(initial)
        startForegroundCompat(NotificationHelper.build(this, initial))

        engine = WorkoutEngine(
            intervals = intervals,
            totalCycles = config.sprints,
            scope = scope,
            onTick = ::handleTick,
            onComplete = ::handleComplete,
        ).also { it.start() }
    }

    private fun handleTick(state: RunState) {
        RunStateHolder.set(state)
        val mgr = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        mgr.notify(NotificationHelper.NOTIFICATION_ID, NotificationHelper.build(this, state))
    }

    private fun handleComplete() {
        // Hold the COMPLETED state on the holder so UI can show the summary;
        // tear down the foreground service + notification.
        engine?.stop()
        engine = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopWorkout() {
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
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID, n)
        }
    }

    override fun onDestroy() {
        engine?.stop()
        engine = null
        scope.cancel()
        super.onDestroy()
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

        fun start(ctx: Context, config: WorkoutConfig) {
            val i = Intent(ctx, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TOTAL_SEC, config.totalSec)
                putExtra(EXTRA_SPRINTS, config.sprints)
                putExtra(EXTRA_SPRINT_SEC, config.sprintSec)
                putExtra(EXTRA_REST_SEC, config.restSec)
                putExtra(EXTRA_AUDIO, config.audio.name)
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
