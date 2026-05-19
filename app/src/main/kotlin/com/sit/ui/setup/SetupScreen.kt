package com.sit.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sit.domain.AppTheme
import com.sit.domain.AudioTrack
import com.sit.domain.ValidationState
import com.sit.domain.WorkoutConfig
import com.sit.ui.theme.ThemeCatalog
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    state: SetupUiState,
    onTotalSecChange: (Int) -> Unit,
    onSprintsChange: (Int) -> Unit,
    onSprintSecChange: (Int) -> Unit,
    onRestSecChange: (Int) -> Unit,
    onAudioChange: (AudioTrack) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onStart: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SettingsDrawer(
                selectedTheme = state.theme,
                onThemeChange = onThemeChange,
            )
        },
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "Open menu",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "SIT",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                TimePickerRow("Total workout", state.config.totalSec, step = 30, onTotalSecChange)
                SprintCountRow(state.config.sprints, onSprintsChange)
                TimePickerRow("Sprint", state.config.sprintSec, step = 5, onSprintSecChange)
                TimePickerRow("Rest", state.config.restSec, step = 5, onRestSecChange)

                InfoCard(state.config, state.validation)

                SectionLabel("Sound")
                AudioSelector(state.config.audio, onAudioChange)

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onStart,
                    enabled = state.canStart,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text("START WORKOUT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingsDrawer(
    selectedTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "SIT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )

            SectionLabel("Theme")
            ThemeSelector(selectedTheme, onThemeChange)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SectionLabel("About")
            Text(
                text = "SIT is a sprint interval training timer. Set your total workout " +
                    "time, the number of sprints, and your sprint/rest durations — the app " +
                    "calculates the steady-pace running interval between sets so the math " +
                    "fits your time budget exactly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            )
            Text(
                text = "During each sprint a chase sound plays and other audio (music, " +
                    "podcasts) ducks to push you to keep up. The workout keeps running " +
                    "with the screen locked via a foreground notification.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
    )
}

@Composable
private fun TimePickerRow(label: String, valueSec: Int, step: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(140.dp), style = MaterialTheme.typography.bodyLarge)
        Stepper(
            display = formatMmSs(valueSec),
            onDec = { onChange((valueSec - step).coerceAtLeast(0)) },
            onInc = { onChange(valueSec + step) },
        )
    }
}

@Composable
private fun SprintCountRow(value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Sprints", modifier = Modifier.width(140.dp), style = MaterialTheme.typography.bodyLarge)
        Stepper(
            display = value.toString(),
            onDec = { onChange((value - 1).coerceAtLeast(1)) },
            onInc = { onChange(value + 1) },
        )
    }
}

@Composable
private fun Stepper(display: String, onDec: () -> Unit, onInc: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepperButton(icon = Icons.Filled.Remove, contentDescription = "Decrease", onClick = onDec)
        Text(
            text = display,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 12.dp).width(72.dp),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        StepperButton(icon = Icons.Filled.Add, contentDescription = "Increase", onClick = onInc)
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        colors = IconButtonDefaults.outlinedIconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}

@Composable
private fun InfoCard(config: WorkoutConfig, validation: ValidationState) {
    val isInvalid = validation is ValidationState.Invalid
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isInvalid) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            if (isInvalid) {
                Text(
                    "⚠ ${(validation as ValidationState.Invalid).reason}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            } else {
                Text(
                    buildString {
                        append("Workout: ")
                        append(config.sprints)
                        append(" cycles of ")
                        append(formatMmSs(config.individualRunningSec()))
                        append(" Run ➔ ")
                        append(formatMmSs(config.sprintSec))
                        append(" Sprint ➔ ")
                        append(formatMmSs(config.restSec))
                        append(" Rest.")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun AudioSelector(selected: AudioTrack, onChange: (AudioTrack) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AudioChip(AudioTrack.DOG_BARKING, "Dog", Icons.Filled.Pets, selected, onChange)
        AudioChip(AudioTrack.HORROR_CHASE, "Horror", Icons.Filled.GraphicEq, selected, onChange)
        AudioChip(AudioTrack.STANDARD_BEEP, "Beep", Icons.Filled.MusicNote, selected, onChange)
    }
}

@Composable
private fun AudioChip(
    track: AudioTrack,
    label: String,
    icon: ImageVector,
    selected: AudioTrack,
    onChange: (AudioTrack) -> Unit,
) {
    val isSelected = track == selected
    val border = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, border, RoundedCornerShape(12.dp))
            .clickable { onChange(track) }
            .padding(12.dp),
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ThemeSelector(selected: AppTheme, onChange: (AppTheme) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppTheme.entries.forEach { theme ->
            ThemeSwatch(theme, selected = theme == selected, onClick = { onChange(theme) })
        }
    }
}

@Composable
private fun ThemeSwatch(theme: AppTheme, selected: Boolean, onClick: () -> Unit) {
    val palette = ThemeCatalog.palette(theme)
    val border = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Row {
            Dot(palette.rest)
            Spacer(Modifier.width(2.dp))
            Dot(palette.run)
            Spacer(Modifier.width(2.dp))
            Dot(palette.sprint)
        }
        Spacer(Modifier.height(4.dp))
        Text(themeLabel(theme), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color)
    )
}

private fun themeLabel(t: AppTheme): String = when (t) {
    AppTheme.CLASSIC -> "Classic"
    AppTheme.NEON -> "Neon"
    AppTheme.FOREST -> "Forest"
    AppTheme.MONO -> "Mono"
    AppTheme.GLITTER_POP -> "Glitter"
}

private fun formatMmSs(sec: Int): String {
    val s = sec.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}
