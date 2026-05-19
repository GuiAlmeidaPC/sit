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
import com.sit.domain.ValidationState
import com.sit.domain.WorkoutConfig
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
