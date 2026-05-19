package com.sit.ui.active

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sit.domain.IntervalType
import com.sit.service.RunPhase
import com.sit.service.RunState
import com.sit.ui.theme.LocalSitPalette
import com.sit.ui.theme.ThemePalette

@Composable
fun ActiveRunScreen(
    state: RunState,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
) {
    val palette = LocalSitPalette.current

    val baseTarget = baseBackground(state, palette)
    val isSprint = state.intervalType == IntervalType.SPRINTING && state.phase == RunPhase.RUNNING

    val flashFraction = sprintFlashFraction(isSprint)
    val animatedBase by animateColorAsState(
        targetValue = baseTarget,
        animationSpec = tween(durationMillis = 450),
        label = "bg",
    )
    val bg = if (isSprint) {
        lerp(palette.sprint, palette.sprintFlashAlt, flashFraction)
    } else {
        animatedBase
    }

    val fg = onColorFor(bg)

    Box(
        modifier = Modifier.fillMaxSize().background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Text(
                text = stateLabel(state),
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = fg,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = fmt(state.remainingInIntervalSec),
                fontSize = 140.sp,
                fontWeight = FontWeight.Black,
                color = fg,
            )

            Text(
                text = "Cycle ${state.currentCycle} of ${state.totalCycles}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = fg.copy(alpha = 0.85f),
            )

            Spacer(Modifier.height(32.dp))

            val progress = if (state.totalWorkoutSec == 0) 0f
                else state.totalElapsedSec.toFloat() / state.totalWorkoutSec
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = fg,
                trackColor = fg.copy(alpha = 0.25f),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${fmt(state.totalElapsedSec)}  /  ${fmt(state.totalWorkoutSec)}",
                fontSize = 14.sp,
                color = fg.copy(alpha = 0.75f),
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onTogglePause,
                colors = ButtonDefaults.buttonColors(
                    containerColor = fg.copy(alpha = 0.18f),
                    contentColor = fg,
                ),
                modifier = Modifier.width(200.dp).height(56.dp),
            ) {
                Text(
                    text = if (state.phase == RunPhase.PAUSED) "Resume" else "Pause",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(16.dp))

            LongPressStopButton(fg = fg, onStop = onStop)
        }
    }
}

@Composable
private fun LongPressStopButton(fg: Color, onStop: () -> Unit) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onStop() },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(fg.copy(alpha = 0.08f)),
        )
        Text(
            text = "Hold to Stop",
            color = fg.copy(alpha = 0.9f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun sprintFlashFraction(active: Boolean): Float {
    if (!active) return 0f
    val t = rememberInfiniteTransition(label = "sprint-flash")
    val v by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sprint-flash-v",
    )
    return v
}

private fun baseBackground(state: RunState, palette: ThemePalette): Color {
    if (state.phase == RunPhase.PAUSED) {
        return paletteFor(state.intervalType, palette).copy(alpha = 1f).let {
            // Dim paused state a touch by mixing with black.
            lerp(it, Color.Black, 0.25f)
        }
    }
    if (state.phase == RunPhase.COMPLETED) return palette.rest
    return paletteFor(state.intervalType, palette)
}

private fun paletteFor(type: IntervalType, palette: ThemePalette): Color = when (type) {
    IntervalType.RUNNING -> palette.run
    IntervalType.SPRINTING -> palette.sprint
    IntervalType.RESTING -> palette.rest
}

/** Pick black or white text based on background luminance for contrast. */
private fun onColorFor(bg: Color): Color {
    val r = bg.red
    val g = bg.green
    val b = bg.blue
    val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
    return if (luminance < 0.55f) Color.White else Color(0xFF111111)
}

private fun stateLabel(s: RunState): String = when {
    s.phase == RunPhase.PAUSED -> "Paused"
    s.phase == RunPhase.COMPLETED -> "Complete!"
    s.intervalType == IntervalType.RUNNING -> "RUN"
    s.intervalType == IntervalType.SPRINTING -> "SPRINT!"
    s.intervalType == IntervalType.RESTING -> "REST"
    else -> ""
}

private fun fmt(sec: Int): String {
    val s = sec.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}
