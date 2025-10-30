// main/java/com/achievemeaalk/freedjf/ui/accounts/AccountsViewModel.kt
package com.achievemeaalk.freedjf.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achievemeaalk.freedjf.data.model.Account
import com.achievemeaalk.freedjf.domain.repository.AccountsRepository
import com.achievemeaalk.freedjf.ui.ads.AdViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    val adViewModel: AdViewModel,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val accounts: StateFlow<List<Account>> =
        accountsRepository.getAllAccounts()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val totalBalance: StateFlow<Double> = accounts.map { accs ->
        accs.sumOf { it.balance }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0.0
    )

    val account = accountsRepository.getAccount(savedStateHandle.get<String>("accountId")?.toIntOrNull() ?: 0)

    private val _accountCreatedEvent = MutableSharedFlow<Unit>()
    val accountCreatedEvent = _accountCreatedEvent.asSharedFlow()

    fun addAccount(account: Account, isFromOnboarding: Boolean = false, onAccountAdded: (String) -> Unit) = viewModelScope.launch {
        val newAccountId = accountsRepository.insertAccount(account)
        if (!isFromOnboarding) {
            _accountCreatedEvent.emit(Unit)
        }
        onAccountAdded(newAccountId.toString())
    }

    fun updateAccount(account: Account, onAccountUpdated: (String) -> Unit) = viewModelScope.launch {
        accountsRepository.updateAccount(account)
        onAccountUpdated(account.accountId.toString())
    }

    fun deleteAccount(account: Account) = viewModelScope.launch {
        accountsRepository.deleteAccount(account)
    }
}
