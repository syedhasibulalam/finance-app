package com.achievemeaalk.freedjf.ui.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _eventChannel = Channel<OnboardingEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    fun onOnboardingFinished() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
            _eventChannel.send(OnboardingEvent.NavigateToName)
        }
    }

    fun onNameSubmitted(name: String) {
        viewModelScope.launch {
            preferencesRepository.setUserName(name)
        }
    }

    fun onFirstAccountPromptCompleted() {
        viewModelScope.launch {
            preferencesRepository.setFirstAccountPromptCompleted(true)
        }
    }
}

sealed class OnboardingEvent {
    object NavigateToHome : OnboardingEvent()
    object NavigateToName : OnboardingEvent()
    object NavigateToAccounts : OnboardingEvent()
}
