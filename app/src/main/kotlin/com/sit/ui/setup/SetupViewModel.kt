package com.sit.ui.setup

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sit.data.UserPrefs
import com.sit.data.UserPrefsRepository
import com.sit.domain.AppLanguage
import com.sit.domain.AppTheme
import com.sit.domain.AudioTrack
import com.sit.domain.Block
import com.sit.domain.BlockType
import com.sit.domain.RepeatBlock
import com.sit.domain.SimpleBlock
import com.sit.domain.ValidationState
import com.sit.domain.WorkoutConfig
import com.sit.domain.WorkoutMode
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SetupUiState(
    val prefs: UserPrefs,
    val validation: ValidationState,
    val loaded: Boolean,
) {
    val config: WorkoutConfig get() = prefs.config
    val theme: AppTheme get() = prefs.theme
    val language: AppLanguage get() = prefs.language
    val mode: WorkoutMode get() = prefs.config.mode
    val canStart: Boolean get() = loaded && validation is ValidationState.Valid
}

class SetupViewModel(private val repo: UserPrefsRepository) : ViewModel() {

    private val _state = MutableStateFlow(
        SetupUiState(UserPrefs.DEFAULT, UserPrefs.DEFAULT.config.validate(), loaded = false)
    )
    val state: StateFlow<SetupUiState> = _state

    init {
        viewModelScope.launch {
            val first = repo.flow.first()
            _state.value = SetupUiState(first, first.config.validate(), loaded = true)
        }
    }

    fun setTotalSec(v: Int) = update { it.copy(config = it.config.copy(totalSec = v)) }
    fun setSprints(v: Int) = update { it.copy(config = it.config.copy(sprints = v)) }
    fun setSprintSec(v: Int) = update { it.copy(config = it.config.copy(sprintSec = v)) }
    fun setRestSec(v: Int) = update { it.copy(config = it.config.copy(restSec = v)) }
    fun setMode(mode: WorkoutMode) = update {
        it.copy(
            config = it.config.copy(
                mode = mode,
                advancedSprintSecs = if (it.config.advancedSprintSecs.isEmpty()) {
                    listOf(it.config.sprintSec.coerceAtLeast(1))
                } else {
                    it.config.advancedSprintSecs
                },
            )
        )
    }
    fun addAdvancedSprint() = update {
        val seed = it.config.advancedSprintSecs.lastOrNull() ?: it.config.sprintSec.coerceAtLeast(1)
        it.copy(
            config = it.config.copy(
                mode = WorkoutMode.ADVANCED,
                advancedSprintSecs = it.config.advancedSprintSecs + seed,
            )
        )
    }
    fun updateAdvancedSprint(index: Int, durationSec: Int) = update {
        if (index !in it.config.advancedSprintSecs.indices) return@update it
        it.copy(
            config = it.config.copy(
                advancedSprintSecs = it.config.advancedSprintSecs.mapIndexed { currentIndex, current ->
                    if (currentIndex == index) durationSec else current
                }
            )
        )
    }
    fun removeAdvancedSprint(index: Int) = update {
        if (it.config.advancedSprintSecs.size <= 1 || index !in it.config.advancedSprintSecs.indices) {
            return@update it
        }
        it.copy(
            config = it.config.copy(
                advancedSprintSecs = it.config.advancedSprintSecs.filterIndexed { currentIndex, _ ->
                    currentIndex != index
                }
            )
        )
    }
    fun addAdvancedPlusSimpleBlock(type: BlockType = BlockType.RUN, durationSec: Int = 30) = update {
        require(type != BlockType.REPEAT)
        it.copy(
            config = it.config.copy(
                advancedPlusBlocks = it.config.advancedPlusBlocks + SimpleBlock(
                    id = newId(),
                    type = type,
                    durationSec = durationSec,
                ),
            )
        )
    }

    fun addAdvancedPlusRepeatBlock() = update {
        it.copy(
            config = it.config.copy(
                advancedPlusBlocks = it.config.advancedPlusBlocks + RepeatBlock(
                    id = newId(),
                    repeats = 4,
                    steps = listOf(
                        SimpleBlock(id = newId(), type = BlockType.RUN, durationSec = 30),
                        SimpleBlock(id = newId(), type = BlockType.WALK, durationSec = 60),
                    ),
                ),
            )
        )
    }

    fun removeAdvancedPlusBlock(blockId: String) = updateBlocks { blocks ->
        blocks.filter { it.id != blockId }
    }

    fun moveAdvancedPlusBlock(blockId: String, delta: Int) = updateBlocks { blocks ->
        val index = blocks.indexOfFirst { it.id == blockId }
        val target = index + delta
        if (index < 0 || target !in blocks.indices) blocks else blocks.toMutableList().apply {
            add(target, removeAt(index))
        }
    }

    fun updateAdvancedPlusSimpleType(blockId: String, type: BlockType) = updateBlocks { blocks ->
        require(type != BlockType.REPEAT)
        blocks.map { block ->
            if (block is SimpleBlock && block.id == blockId) block.copy(type = type) else block
        }
    }

    fun updateAdvancedPlusSimpleDuration(blockId: String, durationSec: Int) = updateBlocks { blocks ->
        blocks.map { block ->
            if (block is SimpleBlock && block.id == blockId) {
                block.copy(durationSec = durationSec.coerceAtLeast(0))
            } else block
        }
    }

    fun updateAdvancedPlusRepeatCount(blockId: String, repeats: Int) = updateBlocks { blocks ->
        blocks.map { block ->
            if (block is RepeatBlock && block.id == blockId) {
                block.copy(repeats = repeats.coerceAtLeast(1))
            } else block
        }
    }

    fun addAdvancedPlusRepeatStep(
        blockId: String,
        type: BlockType = BlockType.RUN,
        durationSec: Int = 30,
    ) = updateBlocks { blocks ->
        require(type != BlockType.REPEAT)
        blocks.map { block ->
            if (block is RepeatBlock && block.id == blockId) {
                block.copy(steps = block.steps + SimpleBlock(newId(), type, durationSec))
            } else block
        }
    }

    fun removeAdvancedPlusRepeatStep(blockId: String, stepId: String) = updateBlocks { blocks ->
        blocks.map { block ->
            if (block is RepeatBlock && block.id == blockId) {
                block.copy(steps = block.steps.filter { it.id != stepId })
            } else block
        }
    }

    fun updateAdvancedPlusRepeatStepType(blockId: String, stepId: String, type: BlockType) =
        updateBlocks { blocks ->
            require(type != BlockType.REPEAT)
            blocks.map { block ->
                if (block is RepeatBlock && block.id == blockId) {
                    block.copy(
                        steps = block.steps.map { s -> if (s.id == stepId) s.copy(type = type) else s },
                    )
                } else block
            }
        }

    fun updateAdvancedPlusRepeatStepDuration(blockId: String, stepId: String, durationSec: Int) =
        updateBlocks { blocks ->
            blocks.map { block ->
                if (block is RepeatBlock && block.id == blockId) {
                    block.copy(
                        steps = block.steps.map { s ->
                            if (s.id == stepId) s.copy(durationSec = durationSec.coerceAtLeast(0)) else s
                        },
                    )
                } else block
            }
        }

    private fun updateBlocks(transform: (List<Block>) -> List<Block>) = update {
        it.copy(config = it.config.copy(advancedPlusBlocks = transform(it.config.advancedPlusBlocks)))
    }

    private fun newId(): String = UUID.randomUUID().toString()

    fun setAudio(a: AudioTrack) = update { it.copy(config = it.config.copy(audio = a)) }
    fun setTheme(t: AppTheme) = update { it.copy(theme = t) }
    fun setLanguage(l: AppLanguage) = update { it.copy(language = l) }

    private fun update(transform: (UserPrefs) -> UserPrefs) {
        val next = transform(_state.value.prefs)
        _state.value = _state.value.copy(prefs = next, validation = next.config.validate())
        viewModelScope.launch { repo.save(next) }
    }

    companion object {
        fun factory(app: Application) = viewModelFactory {
            initializer { SetupViewModel(UserPrefsRepository(app)) }
        }
    }
}
