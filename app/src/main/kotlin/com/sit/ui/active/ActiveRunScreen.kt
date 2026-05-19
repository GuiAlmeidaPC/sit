package com.sit.ui.active

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sit.domain.IntervalType
import com.sit.service.RunPhase
import com.sit.service.RunState
import com.sit.ui.theme.LocalSitPalette

/**
 * Placeholder Active Run UI for Phase 3. Will be styled with animateColorAsState,
 * giant countdown, and long-press stop in Phase 5.
 */
@Composable
fun ActiveRunScreen(
    state: RunState,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
) {
    val palette = LocalSitPalette.current
    val bg: Color = when (state.intervalType) {
        IntervalType.RUNNING -> palette.run
        IntervalType.SPRINTING -> palette.sprint
        IntervalType.RESTING -> palette.rest
    }

    Box(
        modifier = Modifier.fillMaxSize().background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = stateLabel(state),
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = fmt(state.remainingInIntervalSec),
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Cycle ${state.currentCycle} of ${state.totalCycles}",
                fontSize = 16.sp,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Total ${fmt(state.totalElapsedSec)} / ${fmt(state.totalWorkoutSec)}",
                fontSize = 14.sp,
                color = Color.White,
            )
            Spacer(Modifier.height(16.dp))
            val progress = if (state.totalWorkoutSec == 0) 0f
                else state.totalElapsedSec.toFloat() / state.totalWorkoutSec
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.width(280.dp).height(6.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
            )
            Spacer(Modifier.height(32.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = onTogglePause) {
                    Text(if (state.phase == RunPhase.PAUSED) "Resume" else "Pause")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onStop) {
                    Text("Stop")
                }
            }
        }
    }
}

private fun stateLabel(s: RunState): String = when {
    s.phase == RunPhase.PAUSED -> "Paused"
    s.phase == RunPhase.COMPLETED -> "Complete!"
    s.intervalType == IntervalType.RUNNING -> "Run"
    s.intervalType == IntervalType.SPRINTING -> "Sprint!"
    s.intervalType == IntervalType.RESTING -> "Rest"
    else -> ""
}

private fun fmt(sec: Int): String {
    val s = sec.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}
