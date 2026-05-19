package com.sit.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sit.MainActivity
import com.sit.R
import com.sit.domain.IntervalType

object NotificationHelper {
    const val CHANNEL_ID = "sit_workout"
    private const val CHANNEL_NAME = "Workout Timer"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(ctx: Context) {
        val mgr = ctx.getSystemService(NotificationManager::class.java) ?: return
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Active interval workout"
                setShowBadge(false)
            }
            mgr.createNotificationChannel(chan)
        }
    }

    fun build(ctx: Context, state: RunState): android.app.Notification {
        val openIntent = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stopIntent = PendingIntent.getService(
            ctx, 1,
            Intent(ctx, TimerService::class.java).setAction(TimerService.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val title = stateLabel(state)
        val text = "${fmt(state.remainingInIntervalSec)} • cycle ${state.currentCycle}/${state.totalCycles}"

        return NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sit_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openIntent)
            .addAction(0, "Stop", stopIntent)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .build()
    }

    private fun stateLabel(s: RunState): String {
        if (s.phase == RunPhase.PAUSED) return "Paused"
        if (s.phase == RunPhase.COMPLETED) return "Workout complete"
        return when (s.intervalType) {
            IntervalType.RUNNING -> "Running"
            IntervalType.SPRINTING -> "Sprint!"
            IntervalType.RESTING -> "Rest"
        }
    }

    private fun fmt(sec: Int): String {
        val s = sec.coerceAtLeast(0)
        return "%d:%02d".format(s / 60, s % 60)
    }
}
