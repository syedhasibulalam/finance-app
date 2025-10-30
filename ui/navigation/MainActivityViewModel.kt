package com.achievemeaalk.freedjf.ui.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor() : ViewModel() {
    private val _navigationEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigationEvents: SharedFlow<String> = _navigationEvents.asSharedFlow()

    fun postRoute(route: String) {
        _navigationEvents.tryEmit(route)
    }
}




