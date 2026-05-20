package com.sit.ui.setup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.sit.domain.AppLanguage
import com.sit.domain.AppTheme
import com.sit.domain.AudioTrack
import com.sit.domain.ValidationState
import com.sit.domain.WorkoutConfig
import com.sit.domain.WorkoutMode
import com.sit.i18n.LocalAppStrings
import com.sit.ui.theme.ThemeCatalog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SetupScreen(
    state: SetupUiState,
    onTotalSecChange: (Int) -> Unit,
    onModeChange: (WorkoutMode) -> Unit,
    onSprintsChange: (Int) -> Unit,
    onSprintSecChange: (Int) -> Unit,
    onAdvancedSprintChange: (Int, Int) -> Unit,
    onAddAdvancedSprint: () -> Unit,
    onRemoveAdvancedSprint: (Int) -> Unit,
    onRestSecChange: (Int) -> Unit,
    onAudioChange: (AudioTrack) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onStart: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val strings = LocalAppStrings.current
    val selectedTabIndex = if (state.mode == WorkoutMode.BASIC) 0 else 1
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { focusManager.clearFocus() })
                        },
                    state = listState,
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    stickyHeader {
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            TabRow(
                                selectedTabIndex = selectedTabIndex,
                                modifier = Modifier.padding(horizontal = 24.dp),
                            ) {
                                Tab(
                                    selected = selectedTabIndex == 0,
                                    onClick = { onModeChange(WorkoutMode.BASIC) },
                                    text = { Text(strings.basicModeLabel) },
                                )
                                Tab(
                                    selected = selectedTabIndex == 1,
                                    onClick = { onModeChange(WorkoutMode.ADVANCED) },
                                    text = { Text(strings.advancedModeLabel) },
                                )
                            }
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            TimePickerRow(
                                label = strings.totalWorkoutLabel,
                                valueSec = state.config.totalSec,
                                step = 60,
                                mode = TimeEditorMode.HOURS_MINUTES,
                                onChange = onTotalSecChange,
                            )

                            if (state.mode == WorkoutMode.BASIC) {
                                SprintCountRow(strings.sprintsLabel, state.config.sprints, onSprintsChange)
                                TimePickerRow(
                                    label = strings.sprintLabel,
                                    valueSec = state.config.sprintSec,
                                    step = 5,
                                    mode = TimeEditorMode.MINUTES_SECONDS,
                                    onChange = onSprintSecChange,
                                )
                            } else {
                                AdvancedSprintSection(
                                    sprintDurations = state.config.advancedSprintSecs,
                                    onSprintChange = onAdvancedSprintChange,
                                    onAddSprint = onAddAdvancedSprint,
                                    onRemoveSprint = onRemoveAdvancedSprint,
                                )
                            }

                            TimePickerRow(
                                label = strings.restLabel,
                                valueSec = state.config.restSec,
                                step = 5,
                                mode = TimeEditorMode.MINUTES_SECONDS,
                                onChange = onRestSecChange,
                            )

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
private fun TimePickerRow(
    label: String,
    valueSec: Int,
    step: Int,
    mode: TimeEditorMode,
    onChange: (Int) -> Unit,
) {
    val onTimeChange: (Int) -> Unit = { onChange(normalizeTimeValue(it, mode)) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(140.dp), style = MaterialTheme.typography.bodyLarge)
        TimeStepper(
            label = label,
            valueSec = valueSec,
            mode = mode,
            onValueSubmit = onTimeChange,
            onDec = { onTimeChange((valueSec - step).coerceAtLeast(0)) },
            onInc = { onTimeChange(valueSec + step) },
        )
    }
}

@Composable
private fun AdvancedSprintSection(
    sprintDurations: List<Int>,
    onSprintChange: (Int, Int) -> Unit,
    onAddSprint: () -> Unit,
    onRemoveSprint: (Int) -> Unit,
) {
    val strings = LocalAppStrings.current
    SectionLabel(strings.sprintListLabel)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sprintDurations.forEachIndexed { index, durationSec ->
            AdvancedSprintRow(
                label = "${strings.sprintLabel} ${index + 1}",
                valueSec = durationSec,
                canRemove = sprintDurations.size > 1,
                onValueSubmit = { onSprintChange(index, it) },
                onRemove = { onRemoveSprint(index) },
            )
        }
        Button(
            onClick = onAddSprint,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(strings.addSprintLabel)
        }
    }
}

@Composable
private fun AdvancedSprintRow(
    label: String,
    valueSec: Int,
    canRemove: Boolean,
    onValueSubmit: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(140.dp), style = MaterialTheme.typography.bodyLarge)
        StepperValueArea {
            TimeEditorField(
                label = label,
                valueSec = valueSec,
                mode = TimeEditorMode.MINUTES_SECONDS,
                onValueSubmit = { onValueSubmit(it.coerceAtLeast(0)) },
            )
        }
        IconButton(
            onClick = onRemove,
            enabled = canRemove,
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            ),
        ) {
            Icon(Icons.Filled.Delete, contentDescription = strings.removeSprintLabel)
        }
    }
}

@Composable
private fun SprintCountRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(140.dp), style = MaterialTheme.typography.bodyLarge)
        Stepper(
            value = value,
            onValueSubmit = { onChange(it.coerceAtLeast(1)) },
            onDec = { onChange((value - 1).coerceAtLeast(1)) },
            onInc = { onChange(value + 1) },
        )
    }
}

@Composable
private fun TimeStepper(
    label: String,
    valueSec: Int,
    mode: TimeEditorMode,
    onValueSubmit: (Int) -> Unit,
    onDec: () -> Unit,
    onInc: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepperButton(icon = Icons.Filled.Remove, contentDescription = strings.decreaseLabel, onClick = onDec)
        StepperValueArea {
            TimeEditorField(
                label = label,
                valueSec = valueSec,
                mode = mode,
                onValueSubmit = onValueSubmit,
            )
        }
        StepperButton(icon = Icons.Filled.Add, contentDescription = strings.increaseLabel, onClick = onInc)
    }
}

@Composable
private fun TimeEditorField(
    label: String,
    valueSec: Int,
    mode: TimeEditorMode,
    onValueSubmit: (Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val minuteFocusRequester = remember { FocusRequester() }
    val secondFocusRequester = remember { FocusRequester() }
    var activePart by remember { mutableStateOf<TimePart?>(null) }
    var primaryField by remember { mutableStateOf(TextFieldValue("00")) }
    var secondaryField by remember { mutableStateOf(TextFieldValue("00")) }

    LaunchedEffect(valueSec, activePart) {
        val (primaryValue, secondaryValue) = splitTimeValue(normalizeTimeValue(valueSec, mode), mode)
        val formattedPrimary = formatTwoDigits(primaryValue)
        val formattedSecondary = formatTwoDigits(secondaryValue)
        if (activePart != TimePart.MINUTES) {
            primaryField = TextFieldValue(formattedPrimary, TextRange(formattedPrimary.length))
        }
        if (activePart != TimePart.SECONDS) {
            secondaryField = TextFieldValue(formattedSecondary, TextRange(formattedSecondary.length))
        }
    }

    TimeSegmentField(
        label = "$label ${mode.primaryLabel.accessibilityName}",
        unitLabel = mode.primaryLabel.shortLabel,
        value = primaryField,
        isFocused = activePart == TimePart.MINUTES,
        focusRequester = minuteFocusRequester,
        imeAction = ImeAction.Next,
        onFocused = {
            activePart = TimePart.MINUTES
            primaryField = primaryField.copy(selection = TextRange(0, primaryField.text.length))
        },
        onBlurred = {
            activePart = null
            val normalized = formatTwoDigits(parsePrimaryPart(primaryField.text))
            primaryField = TextFieldValue(normalized, TextRange(normalized.length))
        },
        onValueChange = { updated ->
            val oldText = primaryField.text
            val sanitized = sanitizePrimaryInput(updated.text)
            val textChanged = oldText != sanitized
            
            primaryField = if (textChanged) {
                TextFieldValue(sanitized, TextRange(sanitized.length))
            } else {
                TextFieldValue(sanitized, updated.selection)
            }
            
            if (textChanged) {
                onValueSubmit(combineTimeParts(sanitized, secondaryField.text, mode))
                if (sanitized.length == 2 && activePart == TimePart.MINUTES) {
                    secondFocusRequester.requestFocus()
                }
            }
        },
        keyboardActions = KeyboardActions(
            onNext = { secondFocusRequester.requestFocus() },
            onDone = { focusManager.clearFocus() },
        ),
    )
    Text(
        text = ":",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
    )
    TimeSegmentField(
        label = "$label ${mode.secondaryLabel.accessibilityName}",
        unitLabel = mode.secondaryLabel.shortLabel,
        value = secondaryField,
        isFocused = activePart == TimePart.SECONDS,
        focusRequester = secondFocusRequester,
        imeAction = ImeAction.Done,
        onFocused = {
            activePart = TimePart.SECONDS
            secondaryField = secondaryField.copy(selection = TextRange(0, secondaryField.text.length))
        },
        onBlurred = {
            activePart = null
            val normalized = formatTwoDigits(parseSecondaryPart(secondaryField.text))
            secondaryField = TextFieldValue(normalized, TextRange(normalized.length))
        },
        onValueChange = { updated ->
            val oldText = secondaryField.text
            val sanitized = sanitizeSecondaryInput(updated.text)
            val textChanged = oldText != sanitized
            
            secondaryField = if (textChanged) {
                TextFieldValue(sanitized, TextRange(sanitized.length))
            } else {
                TextFieldValue(sanitized, updated.selection)
            }
            
            if (textChanged) {
                onValueSubmit(combineTimeParts(primaryField.text, sanitized, mode))
            }
        },
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
    )
}

@Composable
private fun TimeSegmentField(
    label: String,
    unitLabel: String,
    value: TextFieldValue,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    imeAction: ImeAction,
    onFocused: () -> Unit,
    onBlurred: () -> Unit,
    onValueChange: (TextFieldValue) -> Unit,
    keyboardActions: KeyboardActions,
) {
    val activeUnderlineColor = MaterialTheme.colorScheme.primary
    val inactiveUnderlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val selectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    )
    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction,
            ),
            keyboardActions = keyboardActions,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        onFocused()
                    } else if (isFocused) {
                        onBlurred()
                    }
                },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .width(timeSegmentWidth)
                        .height(stepperEditorHeight)
                        .clickable { focusRequester.requestFocus() }
                        .drawBehind {
                            val strokeColor = if (isFocused) activeUnderlineColor else inactiveUnderlineColor
                            drawLine(
                                color = strokeColor,
                                start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                                strokeWidth = 2.dp.toPx(),
                            )
                        }
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        innerTextField()
                        Text(
                            text = unitLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun Stepper(
    value: Int,
    onValueSubmit: (Int) -> Unit,
    onDec: () -> Unit,
    onInc: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value.toString())) }

    LaunchedEffect(value, isFocused) {
        if (!isFocused) {
            val normalized = value.coerceAtLeast(1).toString()
            fieldValue = TextFieldValue(normalized, TextRange(normalized.length))
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        StepperButton(icon = Icons.Filled.Remove, contentDescription = strings.decreaseLabel, onClick = onDec)
        StepperValueArea {
            NumberSegmentField(
                label = strings.sprintsLabel,
                value = fieldValue,
                isFocused = isFocused,
                focusRequester = focusRequester,
                onFocused = {
                    isFocused = true
                    fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
                },
                onBlurred = {
                    isFocused = false
                    val normalized = value.coerceAtLeast(1).toString()
                    fieldValue = TextFieldValue(normalized, TextRange(normalized.length))
                },
                onValueChange = { updated ->
                    val oldText = fieldValue.text
                    val sanitized = updated.text.filter(Char::isDigit).take(3)
                    val textChanged = oldText != sanitized
                    
                    fieldValue = if (textChanged) {
                        TextFieldValue(sanitized, TextRange(sanitized.length))
                    } else {
                        TextFieldValue(sanitized, updated.selection)
                    }
                    
                    if (textChanged) {
                        sanitized.toIntOrNull()?.let { onValueSubmit(it.coerceAtLeast(1)) }
                    }
                },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
        }
        StepperButton(icon = Icons.Filled.Add, contentDescription = strings.increaseLabel, onClick = onInc)
    }
}

@Composable
private fun StepperValueArea(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .width(stepperEditorWidth)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun NumberSegmentField(
    label: String,
    value: TextFieldValue,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onBlurred: () -> Unit,
    onValueChange: (TextFieldValue) -> Unit,
    keyboardActions: KeyboardActions,
) {
    val activeUnderlineColor = MaterialTheme.colorScheme.primary
    val inactiveUnderlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val selectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    )
    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
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
            keyboardActions = keyboardActions,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        onFocused()
                    } else if (isFocused) {
                        onBlurred()
                    }
                },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .width(numberSegmentWidth)
                        .height(stepperEditorHeight)
                        .clickable { focusRequester.requestFocus() }
                        .drawBehind {
                            val strokeColor = if (isFocused) activeUnderlineColor else inactiveUnderlineColor
                            drawLine(
                                color = strokeColor,
                                start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                                strokeWidth = 2.dp.toPx(),
                            )
                        }
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    innerTextField()
                }
            },
        )
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
private val stepperEditorWidth = 120.dp
private val stepperEditorHeight = 58.dp
private val timeSegmentWidth = 48.dp
private val numberSegmentWidth = 64.dp

private const val maxMinutesSecondsValueSec = (99 * 60) + 59
private const val maxHoursMinutesValueSec = (99 * 3600) + (59 * 60)

private enum class TimePart {
    MINUTES,
    SECONDS,
}

private enum class TimeLabel(val shortLabel: String, val accessibilityName: String) {
    HOURS("hr", "hours"),
    MINUTES("min", "minutes"),
    SECONDS("sec", "seconds"),
}

private enum class TimeEditorMode(
    val primaryLabel: TimeLabel,
    val secondaryLabel: TimeLabel,
) {
    HOURS_MINUTES(TimeLabel.HOURS, TimeLabel.MINUTES),
    MINUTES_SECONDS(TimeLabel.MINUTES, TimeLabel.SECONDS),
}

private fun sanitizePrimaryInput(text: String): String {
    return text.filter(Char::isDigit).take(2)
}

private fun sanitizeSecondaryInput(text: String): String {
    return text.filter(Char::isDigit).take(2)
}

private fun parsePrimaryPart(text: String): Int = text.filter(Char::isDigit).toIntOrNull()?.coerceIn(0, 99) ?: 0

private fun parseSecondaryPart(text: String): Int = text.filter(Char::isDigit).toIntOrNull()?.coerceIn(0, 59) ?: 0

private fun combineTimeParts(primaryText: String, secondaryText: String, mode: TimeEditorMode): Int {
    val primary = parsePrimaryPart(primaryText)
    val secondary = parseSecondaryPart(secondaryText)
    val valueSec = when (mode) {
        TimeEditorMode.HOURS_MINUTES -> (primary * 3600) + (secondary * 60)
        TimeEditorMode.MINUTES_SECONDS -> (primary * 60) + secondary
    }
    return normalizeTimeValue(valueSec, mode)
}

private fun splitTimeValue(valueSec: Int, mode: TimeEditorMode): Pair<Int, Int> {
    val normalizedValue = normalizeTimeValue(valueSec, mode)
    return when (mode) {
        TimeEditorMode.HOURS_MINUTES -> (normalizedValue / 3600) to ((normalizedValue % 3600) / 60)
        TimeEditorMode.MINUTES_SECONDS -> (normalizedValue / 60) to (normalizedValue % 60)
    }
}

private fun normalizeTimeValue(valueSec: Int, mode: TimeEditorMode): Int {
    val clamped = valueSec.coerceAtLeast(0).coerceAtMost(
        when (mode) {
            TimeEditorMode.HOURS_MINUTES -> maxHoursMinutesValueSec
            TimeEditorMode.MINUTES_SECONDS -> maxMinutesSecondsValueSec
        }
    )
    return when (mode) {
        TimeEditorMode.HOURS_MINUTES -> (clamped / 60) * 60
        TimeEditorMode.MINUTES_SECONDS -> clamped
    }
}

private fun formatTwoDigits(value: Int): String = value.coerceIn(0, 99).toString().padStart(2, '0')

private fun formatMmSs(sec: Int): String {
    val s = sec.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}
