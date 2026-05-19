package com.sit.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Process-wide state holder for the active workout. Written by TimerService,
 * observed by UI. Outside of the service's lifetime, [state] holds [RunState.IDLE].
 */
object RunStateHolder {
    private val _state = MutableStateFlow(RunState.IDLE)
    val state: StateFlow<RunState> = _state

    internal fun set(next: RunState) { _state.value = next }
    internal fun reset() { _state.value = RunState.IDLE }
}
