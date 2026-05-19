package com.sit.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sit.MainActivity
import com.sit.R
import com.sit.domain.AppLanguage
import com.sit.domain.IntervalType
import com.sit.i18n.stringsFor

object NotificationHelper {
    const val CHANNEL_ID = "sit_workout"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(ctx: Context, language: AppLanguage) {
        val strings = stringsFor(language)
        val mgr = ctx.getSystemService(NotificationManager::class.java) ?: return
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                strings.notificationChannelName,
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = strings.notificationChannelDescription
                setShowBadge(false)
            }
            mgr.createNotificationChannel(chan)
        }
    }

    fun build(ctx: Context, state: RunState, language: AppLanguage): android.app.Notification {
        val strings = stringsFor(language)
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

        val title = stateLabel(state, strings)
        val text = strings.notificationText(
            remaining = fmt(state.remainingInIntervalSec),
            current = state.currentCycle,
            total = state.totalCycles,
        )

        return NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sit_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openIntent)
            .addAction(0, strings.notificationStopLabel, stopIntent)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .build()
    }

    private fun stateLabel(s: RunState, strings: com.sit.i18n.AppStrings): String {
        if (s.phase == RunPhase.PAUSED) return strings.notificationPausedLabel
        if (s.phase == RunPhase.COMPLETED) return strings.notificationCompleteLabel
        return when (s.intervalType) {
            IntervalType.RUNNING -> strings.notificationRunningLabel
            IntervalType.SPRINTING -> strings.notificationSprintLabel
            IntervalType.RESTING -> strings.notificationRestLabel
        }
    }

    private fun fmt(sec: Int): String {
        val s = sec.coerceAtLeast(0)
        return "%d:%02d".format(s / 60, s % 60)
    }
}
