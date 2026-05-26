package com.sit.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.sit.domain.AppLanguage
import com.sit.domain.ValidationMessage
import com.sit.domain.WorkoutConfig

data class AppStrings(
    val openMenuDescription: String,
    val totalWorkoutLabel: String,
    val basicModeLabel: String,
    val advancedModeLabel: String,
    val advancedPlusModeLabel: String,
    val warmupBlockLabel: String,
    val runBlockLabel: String,
    val walkBlockLabel: String,
    val restBlockLabel: String,
    val repeatBlockLabel: String,
    val addStepLabel: String,
    val addRepeatLabel: String,
    val removeBlockLabel: String,
    val moveUpLabel: String,
    val moveDownLabel: String,
    val repeatsLabel: String,
    val validationBlocksEmpty: String,
    val validationBlockDuration: String,
    val validationRepeatCount: String,
    val validationRepeatEmpty: String,
    val sprintsLabel: String,
    val sprintLabel: String,
    val sprintListLabel: String,
    val addSprintLabel: String,
    val removeSprintLabel: String,
    val restLabel: String,
    val soundLabel: String,
    val startWorkoutLabel: String,
    val themeLabel: String,
    val languageLabel: String,
    val aboutLabel: String,
    val aboutBodyPrimary: String,
    val aboutBodySecondary: String,
    val decreaseLabel: String,
    val increaseLabel: String,
    val dogLabel: String,
    val horrorLabel: String,
    val electroLabel: String,
    val noSoundLabel: String,
    val englishLabel: String,
    val portugueseLabel: String,
    val classicThemeLabel: String,
    val classicDarkThemeLabel: String,
    val neonThemeLabel: String,
    val forestThemeLabel: String,
    val forestDarkThemeLabel: String,
    val monoThemeLabel: String,
    val monoDarkThemeLabel: String,
    val glitterThemeLabel: String,
    val glitterDarkThemeLabel: String,
    val pauseLabel: String,
    val resumeLabel: String,
    val holdToStopLabel: String,
    val pausedStateLabel: String,
    val completeStateLabel: String,
    val runningStateLabel: String,
    val sprintingStateLabel: String,
    val restingStateLabel: String,
    val workoutCompleteLabel: String,
    val totalTimeLabel: String,
    val cyclesLabel: String,
    val doneLabel: String,
    val notificationChannelName: String,
    val notificationChannelDescription: String,
    val notificationStopLabel: String,
    val notificationPausedLabel: String,
    val notificationCompleteLabel: String,
    val notificationRunningLabel: String,
    val notificationSprintLabel: String,
    val notificationRestLabel: String,
    val validationTotalPositive: String,
    val validationSprintsPositive: String,
    val validationSprintPositive: String,
    val validationRestPositive: String,
    val validationRunningFits: String,
    private val workoutTemplate: (WorkoutConfig) -> String,
    private val cycleTemplate: (Int, Int) -> String,
    private val notificationTemplate: (String, Int, Int) -> String,
    private val expandTemplate: (String) -> String,
    private val collapseTemplate: (String) -> String,
) {
    fun workoutSummary(config: WorkoutConfig): String = workoutTemplate(config)

    fun cycleLabel(current: Int, total: Int): String = cycleTemplate(current, total)

    fun notificationText(remaining: String, current: Int, total: Int): String =
        notificationTemplate(remaining, current, total)

    fun expandLabel(title: String): String = expandTemplate(title)

    fun collapseLabel(title: String): String = collapseTemplate(title)

    fun validationReason(reason: ValidationMessage): String = when (reason) {
        ValidationMessage.TOTAL_TIME_MUST_BE_POSITIVE -> validationTotalPositive
        ValidationMessage.SPRINTS_MUST_BE_AT_LEAST_ONE -> validationSprintsPositive
        ValidationMessage.SPRINT_DURATION_MUST_BE_POSITIVE -> validationSprintPositive
        ValidationMessage.REST_DURATION_MUST_BE_POSITIVE -> validationRestPositive
        ValidationMessage.RUNNING_TIME_MUST_FIT -> validationRunningFits
        ValidationMessage.BLOCKS_MUST_NOT_BE_EMPTY -> validationBlocksEmpty
        ValidationMessage.BLOCK_DURATION_MUST_BE_POSITIVE -> validationBlockDuration
        ValidationMessage.REPEAT_COUNT_MUST_BE_POSITIVE -> validationRepeatCount
        ValidationMessage.REPEAT_MUST_HAVE_STEPS -> validationRepeatEmpty
    }

    fun blockTypeLabel(type: com.sit.domain.BlockType): String = when (type) {
        com.sit.domain.BlockType.WARMUP -> warmupBlockLabel
        com.sit.domain.BlockType.RUN -> runBlockLabel
        com.sit.domain.BlockType.WALK -> walkBlockLabel
        com.sit.domain.BlockType.REST -> restBlockLabel
        com.sit.domain.BlockType.REPEAT -> repeatBlockLabel
    }
}

val LocalAppStrings = staticCompositionLocalOf { stringsFor(AppLanguage.ENGLISH) }

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.ENGLISH -> AppStrings(
        openMenuDescription = "Open menu",
        totalWorkoutLabel = "Total workout",
        basicModeLabel = "Basic",
        advancedModeLabel = "Advanced",
        advancedPlusModeLabel = "Advanced+",
        warmupBlockLabel = "Warmup",
        runBlockLabel = "Run",
        walkBlockLabel = "Walk",
        restBlockLabel = "Rest",
        repeatBlockLabel = "Repeat",
        addStepLabel = "Add step",
        addRepeatLabel = "Add repeat",
        removeBlockLabel = "Remove block",
        moveUpLabel = "Move up",
        moveDownLabel = "Move down",
        repeatsLabel = "Repeats",
        validationBlocksEmpty = "Add at least one block to build your workout",
        validationBlockDuration = "Each block must have a positive duration",
        validationRepeatCount = "Repeat count must be at least 1",
        validationRepeatEmpty = "Repeat blocks must contain at least one step",
        sprintsLabel = "Sprints",
        sprintLabel = "Sprint",
        sprintListLabel = "Sprint list",
        addSprintLabel = "Add sprint",
        removeSprintLabel = "Remove sprint",
        restLabel = "Rest",
        soundLabel = "Sound",
        startWorkoutLabel = "START WORKOUT",
        themeLabel = "Theme",
        languageLabel = "Language",
        aboutLabel = "About",
        aboutBodyPrimary = "SIT is a sprint interval training timer. Set your total " +
            "workout time, the number of sprints, and your sprint/rest durations — " +
            "the app calculates the steady-pace running interval between sets so the math " +
            "fits your time budget exactly.",
        aboutBodySecondary = "During each sprint a chase sound plays and other audio " +
            "(music, podcasts) ducks to push you to keep up. The workout keeps running " +
            "with the screen locked via a foreground notification.",
        decreaseLabel = "Decrease",
        increaseLabel = "Increase",
        dogLabel = "Dog",
        horrorLabel = "Horror",
        electroLabel = "Electro",
        noSoundLabel = "No Sound",
        englishLabel = "English",
        portugueseLabel = "Portuguese",
        classicThemeLabel = "Classic",
        classicDarkThemeLabel = "Classic Dark",
        neonThemeLabel = "Neon",
        forestThemeLabel = "Forest",
        forestDarkThemeLabel = "Forest Dark",
        monoThemeLabel = "Mono",
        monoDarkThemeLabel = "Mono Dark",
        glitterThemeLabel = "Glitter",
        glitterDarkThemeLabel = "Glitter Dark",
        pauseLabel = "Pause",
        resumeLabel = "Resume",
        holdToStopLabel = "Hold to Stop",
        pausedStateLabel = "Paused",
        completeStateLabel = "Complete!",
        runningStateLabel = "RUN",
        sprintingStateLabel = "SPRINT!",
        restingStateLabel = "REST",
        workoutCompleteLabel = "Workout Complete",
        totalTimeLabel = "Total time",
        cyclesLabel = "Cycles",
        doneLabel = "Done",
        notificationChannelName = "Workout Timer",
        notificationChannelDescription = "Active interval workout",
        notificationStopLabel = "Stop",
        notificationPausedLabel = "Paused",
        notificationCompleteLabel = "Workout complete",
        notificationRunningLabel = "Running",
        notificationSprintLabel = "Sprint!",
        notificationRestLabel = "Rest",
        validationTotalPositive = "Total workout time must be positive",
        validationSprintsPositive = "Number of sprints must be at least 1",
        validationSprintPositive = "Sprint duration must be positive",
        validationRestPositive = "Rest duration must be positive",
        validationRunningFits = "Sprint + rest time exceeds (or leaves <1s/cycle for) running",
        workoutTemplate = { config ->
            when (config.mode.name) {
                "ADVANCED_PLUS" ->
                    "Workout: ${formatMmSs(config.effectiveTotalSec)} total, " +
                        "${config.sprintCount} sprint(s)."
                "ADVANCED" ->
                    "Workout: ${config.sprintCount} cycles of ${formatMmSs(config.individualRunningSec())} " +
                        "Run -> ${formatSprintList(config)} Sprint -> ${formatMmSs(config.restSec)} Rest."
                else ->
                    "Workout: ${config.sprintCount} cycles of ${formatMmSs(config.individualRunningSec())} " +
                        "Run -> ${formatMmSs(config.sprintSec)} Sprint -> ${formatMmSs(config.restSec)} Rest."
            }
        },
        cycleTemplate = { current, total -> "Cycle $current of $total" },
        notificationTemplate = { remaining, current, total -> "$remaining • cycle $current/$total" },
        expandTemplate = { title -> "Expand $title" },
        collapseTemplate = { title -> "Collapse $title" },
    )
    AppLanguage.PORTUGUESE -> AppStrings(
        openMenuDescription = "Abrir menu",
        totalWorkoutLabel = "Treino total",
        basicModeLabel = "Basico",
        advancedModeLabel = "Avancado",
        advancedPlusModeLabel = "Avancado+",
        warmupBlockLabel = "Aquecimento",
        runBlockLabel = "Sprint",
        walkBlockLabel = "Caminhada",
        restBlockLabel = "Descanso",
        repeatBlockLabel = "Repetir",
        addStepLabel = "Adicionar passo",
        addRepeatLabel = "Adicionar repeticao",
        removeBlockLabel = "Remover bloco",
        moveUpLabel = "Mover para cima",
        moveDownLabel = "Mover para baixo",
        repeatsLabel = "Repeticoes",
        validationBlocksEmpty = "Adicione pelo menos um bloco para montar o treino",
        validationBlockDuration = "Cada bloco deve ter uma duracao positiva",
        validationRepeatCount = "O numero de repeticoes deve ser pelo menos 1",
        validationRepeatEmpty = "Blocos de repeticao precisam ter ao menos um passo",
        sprintsLabel = "Sprints",
        sprintLabel = "Sprint",
        sprintListLabel = "Lista de sprints",
        addSprintLabel = "Adicionar sprint",
        removeSprintLabel = "Remover sprint",
        restLabel = "Descanso",
        soundLabel = "Som",
        startWorkoutLabel = "INICIAR TREINO",
        themeLabel = "Tema",
        languageLabel = "Idioma",
        aboutLabel = "Sobre",
        aboutBodyPrimary = "SIT e um temporizador de treino intervalado de sprint. Defina " +
            "o tempo total do treino, o numero de sprints e as duracoes de sprint/descanso — " +
            "o app calcula o intervalo de corrida em ritmo constante entre as series para que " +
            "tudo caiba exatamente no tempo disponivel.",
        aboutBodySecondary = "Durante cada sprint, um som de perseguicao toca e outros audios " +
            "(musicas, podcasts) abaixam para incentivar voce a continuar. O treino continua " +
            "com a tela bloqueada por meio de uma notificacao em primeiro plano.",
        decreaseLabel = "Diminuir",
        increaseLabel = "Aumentar",
        dogLabel = "Cao",
        horrorLabel = "Terror",
        electroLabel = "Eletro",
        noSoundLabel = "Sem Som",
        englishLabel = "English",
        portugueseLabel = "Português",
        classicThemeLabel = "Classico",
        classicDarkThemeLabel = "Classico Escuro",
        neonThemeLabel = "Neon",
        forestThemeLabel = "Floresta",
        forestDarkThemeLabel = "Floresta Escura",
        monoThemeLabel = "Mono",
        monoDarkThemeLabel = "Mono Escuro",
        glitterThemeLabel = "Glitter",
        glitterDarkThemeLabel = "Glitter Escuro",
        pauseLabel = "Pausar",
        resumeLabel = "Retomar",
        holdToStopLabel = "Segure para parar",
        pausedStateLabel = "Pausado",
        completeStateLabel = "Concluido!",
        runningStateLabel = "CORRA",
        sprintingStateLabel = "SPRINT!",
        restingStateLabel = "DESCANSE",
        workoutCompleteLabel = "Treino concluido",
        totalTimeLabel = "Tempo total",
        cyclesLabel = "Ciclos",
        doneLabel = "Concluir",
        notificationChannelName = "Temporizador de treino",
        notificationChannelDescription = "Treino intervalado ativo",
        notificationStopLabel = "Parar",
        notificationPausedLabel = "Pausado",
        notificationCompleteLabel = "Treino concluido",
        notificationRunningLabel = "Corrida",
        notificationSprintLabel = "Sprint!",
        notificationRestLabel = "Descanso",
        validationTotalPositive = "O tempo total do treino deve ser positivo",
        validationSprintsPositive = "O numero de sprints deve ser pelo menos 1",
        validationSprintPositive = "A duracao do sprint deve ser positiva",
        validationRestPositive = "A duracao do descanso deve ser positiva",
        validationRunningFits = "Sprint + descanso excedem o treino (ou deixam <1s/ciclo para corrida)",
        workoutTemplate = { config ->
            when (config.mode.name) {
                "ADVANCED_PLUS" ->
                    "Treino: ${formatMmSs(config.effectiveTotalSec)} no total, " +
                        "${config.sprintCount} sprint(s)."
                "ADVANCED" ->
                    "Treino: ${config.sprintCount} ciclos de ${formatMmSs(config.individualRunningSec())} " +
                        "Corrida -> ${formatSprintList(config)} Sprint -> ${formatMmSs(config.restSec)} Descanso."
                else ->
                    "Treino: ${config.sprintCount} ciclos de ${formatMmSs(config.individualRunningSec())} " +
                        "Corrida -> ${formatMmSs(config.sprintSec)} Sprint -> ${formatMmSs(config.restSec)} Descanso."
            }
        },
        cycleTemplate = { current, total -> "Ciclo $current de $total" },
        notificationTemplate = { remaining, current, total -> "$remaining • ciclo $current/$total" },
        expandTemplate = { title -> "Expandir $title" },
        collapseTemplate = { title -> "Recolher $title" },
    )
}

private fun formatMmSs(sec: Int): String {
    val s = sec.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}

private fun formatSprintList(config: WorkoutConfig): String =
    config.sprintDurationsSec.joinToString(", ") { formatMmSs(it) }
