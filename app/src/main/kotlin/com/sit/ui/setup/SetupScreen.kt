package com.sit.ui.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.sit.domain.AppLanguage
import com.sit.domain.AppTheme
import com.sit.domain.AudioTrack
import com.sit.domain.ValidationState
import com.sit.domain.WorkoutConfig
import com.sit.i18n.LocalAppStrings
import com.sit.ui.theme.ThemeCatalog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    state: SetupUiState,
    onTotalSecChange: (Int) -> Unit,
    onSprintsChange: (Int) -> Unit,
    onSprintSecChange: (Int) -> Unit,
    onRestSecChange: (Int) -> Unit,
    onAudioChange: (AudioTrack) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onStart: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val strings = LocalAppStrings.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SettingsDrawer(
                selectedTheme = state.theme,
                selectedLanguage = state.language,
                onThemeChange = onThemeChange,
                onLanguageChange = onLanguageChange,
            )
        },
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Filled.Menu,
                                    contentDescription = strings.openMenuDescription,
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "SIT",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                },
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    TimePickerRow(strings.totalWorkoutLabel, state.config.totalSec, step = 30, onTotalSecChange)
                    SprintCountRow(strings.sprintsLabel, state.config.sprints, onSprintsChange)
                    TimePickerRow(strings.sprintLabel, state.config.sprintSec, step = 5, onSprintSecChange)
                    TimePickerRow(strings.restLabel, state.config.restSec, step = 5, onRestSecChange)

                    InfoCard(state.config, state.validation)

                    SectionLabel(strings.soundLabel)
                    AudioSelector(state.config.audio, onAudioChange)

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onStart,
                        enabled = state.canStart,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) {
                        Text(strings.startWorkoutLabel, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsDrawer(
    selectedTheme: AppTheme,
    selectedLanguage: AppLanguage,
    onThemeChange: (AppTheme) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val strings = LocalAppStrings.current
    var themeExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }
    var aboutExpanded by remember { mutableStateOf(false) }

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "SIT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )

            ExpandableSection(
                title = strings.themeLabel,
                expanded = themeExpanded,
                onToggle = { themeExpanded = !themeExpanded },
            ) {
                ThemeSelector(selectedTheme, onThemeChange)
            }

            ExpandableSection(
                title = strings.languageLabel,
                expanded = languageExpanded,
                onToggle = { languageExpanded = !languageExpanded },
            ) {
                LanguageSelector(selectedLanguage, onLanguageChange)
            }

            ExpandableSection(
                title = strings.aboutLabel,
                expanded = aboutExpanded,
                onToggle = { aboutExpanded = !aboutExpanded },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = strings.aboutBodyPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    )
                    Text(
                        text = strings.aboutBodySecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    val strings = LocalAppStrings.current
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron-rotate",
    )
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onToggle)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = if (expanded) strings.collapseLabel(title) else strings.expandLabel(title),
                modifier = Modifier.rotate(chevronRotation),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 16.dp,
                ),
            ) {
                content()
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
            value = valueSec,
            inputMode = StepperInputMode.TIME,
            onValueSubmit = { onChange(it.coerceAtLeast(0)) },
            onDec = { onChange((valueSec - step).coerceAtLeast(0)) },
            onInc = { onChange(valueSec + step) },
        )
    }
}

@Composable
private fun SprintCountRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(140.dp), style = MaterialTheme.typography.bodyLarge)
        Stepper(
            value = value,
            inputMode = StepperInputMode.INTEGER,
            onValueSubmit = { onChange(it.coerceAtLeast(1)) },
            onDec = { onChange((value - 1).coerceAtLeast(1)) },
            onInc = { onChange(value + 1) },
        )
    }
}

@Composable
private fun Stepper(
    value: Int,
    inputMode: StepperInputMode,
    onValueSubmit: (Int) -> Unit,
    onDec: () -> Unit,
    onInc: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isEditing by remember { mutableStateOf(false) }
    var draftDigits by remember { mutableStateOf("") }

    fun startEditing() {
        draftDigits = ""
        isEditing = true
    }

    fun stopEditing(submit: Boolean) {
        if (submit) {
            parseStepperValue(draftDigits, inputMode)?.let(onValueSubmit)
        }
        isEditing = false
        keyboardController?.hide()
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        StepperButton(icon = Icons.Filled.Remove, contentDescription = strings.decreaseLabel, onClick = onDec)
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .width(72.dp)
                .height(44.dp)
                .pointerInput(value, inputMode) {
                    detectTapGestures(
                        onDoubleTap = { startEditing() },
                        onLongPress = { startEditing() },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            if (isEditing) {
                BasicTextField(
                    value = formatStepperDraft(draftDigits, inputMode),
                    onValueChange = { updated ->
                        draftDigits = updated.filter(Char::isDigit).takeLast(maxDigitsFor(inputMode))
                        parseStepperValue(draftDigits, inputMode)?.let(onValueSubmit)
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { stopEditing(submit = true) }),
                    modifier = Modifier
                        .width(64.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && isEditing) {
                                stopEditing(submit = true)
                            }
                        },
                )
            } else {
                Text(
                    text = formatStepperValue(value, inputMode),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
        }
        StepperButton(icon = Icons.Filled.Add, contentDescription = strings.increaseLabel, onClick = onInc)
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
    val strings = LocalAppStrings.current
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
                    "⚠ ${strings.validationReason((validation as ValidationState.Invalid).reason)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            } else {
                Text(
                    strings.workoutSummary(config),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun AudioSelector(selected: AudioTrack, onChange: (AudioTrack) -> Unit) {
    val strings = LocalAppStrings.current
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AudioChip(AudioTrack.DOG_BARKING, strings.dogLabel, Icons.Filled.Pets, selected, onChange)
        AudioChip(AudioTrack.HORROR_CHASE, strings.horrorLabel, Icons.Filled.GraphicEq, selected, onChange)
        AudioChip(AudioTrack.ELECTRO_RUSH, strings.electroLabel, Icons.Filled.Bolt, selected, onChange)
        AudioChip(AudioTrack.STANDARD_BEEP, strings.beepLabel, Icons.Filled.MusicNote, selected, onChange)
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
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(selectorButtonWidth, selectorButtonHeight)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, border, RoundedCornerShape(12.dp))
            .clickable { onChange(track) }
            .padding(12.dp),
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun LanguageSelector(selected: AppLanguage, onChange: (AppLanguage) -> Unit) {
    val strings = LocalAppStrings.current
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LanguageChip(
            language = AppLanguage.ENGLISH,
            label = strings.englishLabel,
            selected = selected,
            onChange = onChange,
        )
        LanguageChip(
            language = AppLanguage.PORTUGUESE,
            label = strings.portugueseLabel,
            selected = selected,
            onChange = onChange,
        )
    }
}

@Composable
private fun LanguageChip(
    language: AppLanguage,
    label: String,
    selected: AppLanguage,
    onChange: (AppLanguage) -> Unit,
) {
    val border = if (language == selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(languageChipWidth)
            .height(selectorButtonHeight)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, border, RoundedCornerShape(12.dp))
            .clickable { onChange(language) }
            .padding(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ThemeSelector(selected: AppTheme, onChange: (AppTheme) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppTheme.entries.forEach { theme ->
            ThemeSwatch(theme, selected = theme == selected, onClick = { onChange(theme) })
        }
    }
}

@Composable
private fun ThemeSwatch(theme: AppTheme, selected: Boolean, onClick: () -> Unit) {
    val strings = LocalAppStrings.current
    val palette = ThemeCatalog.palette(theme)
    val border = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(selectorButtonWidth, selectorButtonHeight)
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
        Text(themeLabel(theme, strings), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
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

private fun themeLabel(t: AppTheme, strings: com.sit.i18n.AppStrings): String = when (t) {
    AppTheme.CLASSIC -> strings.classicThemeLabel
    AppTheme.CLASSIC_DARK -> strings.classicDarkThemeLabel
    AppTheme.NEON -> strings.neonThemeLabel
    AppTheme.FOREST -> strings.forestThemeLabel
    AppTheme.FOREST_DARK -> strings.forestDarkThemeLabel
    AppTheme.MONO -> strings.monoThemeLabel
    AppTheme.MONO_DARK -> strings.monoDarkThemeLabel
    AppTheme.GLITTER_POP -> strings.glitterThemeLabel
    AppTheme.GLITTER_POP_DARK -> strings.glitterDarkThemeLabel
}

private val languageChipWidth = 104.dp
private val selectorButtonWidth = 72.dp
private val selectorButtonHeight = 76.dp

private enum class StepperInputMode {
    TIME,
    INTEGER,
}

private fun formatStepperValue(value: Int, inputMode: StepperInputMode): String = when (inputMode) {
    StepperInputMode.TIME -> formatMmSs(value)
    StepperInputMode.INTEGER -> value.toString()
}

private fun formatStepperDraft(digits: String, inputMode: StepperInputMode): String = when (inputMode) {
    StepperInputMode.TIME -> formatTimeDigits(digits)
    StepperInputMode.INTEGER -> digits.ifEmpty { "0" }
}

private fun parseStepperValue(digits: String, inputMode: StepperInputMode): Int? = when (inputMode) {
    StepperInputMode.TIME -> parseTimeDigits(digits)
    StepperInputMode.INTEGER -> digits.toIntOrNull()
}

private fun maxDigitsFor(inputMode: StepperInputMode): Int = when (inputMode) {
    StepperInputMode.TIME -> 6
    StepperInputMode.INTEGER -> 3
}

private fun formatTimeDigits(digits: String): String {
    val sanitized = digits.filter(Char::isDigit).takeLast(maxDigitsFor(StepperInputMode.TIME))
    if (sanitized.isEmpty()) return "0:00"
    val secondsPart = sanitized.takeLast(2).padStart(2, '0')
    val minutesPart = sanitized.dropLast(2).ifEmpty { "0" }
    return "$minutesPart:$secondsPart"
}

private fun parseTimeDigits(digits: String): Int? {
    val sanitized = digits.filter(Char::isDigit).takeLast(maxDigitsFor(StepperInputMode.TIME))
    if (sanitized.isEmpty()) return null
    val seconds = sanitized.takeLast(2).toIntOrNull() ?: return null
    val minutes = sanitized.dropLast(2).ifEmpty { "0" }.toIntOrNull() ?: return null
    return (minutes * 60) + seconds
}

private fun formatMmSs(sec: Int): String {
    val s = sec.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}
