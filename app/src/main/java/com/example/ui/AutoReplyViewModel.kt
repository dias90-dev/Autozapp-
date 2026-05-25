package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MatchType
import com.example.data.PreferenceManager
import com.example.data.ReplyRule
import com.example.data.ReplyRuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AutoReplyViewModel(
    private val repository: ReplyRuleRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val rules: StateFlow<List<ReplyRule>> = repository.allRules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isPro = MutableStateFlow(preferenceManager.isPro)
    val isPro = _isPro.asStateFlow()

    private val _remainingReplies = MutableStateFlow(preferenceManager.getDailyRemaining())
    val remainingReplies = _remainingReplies.asStateFlow()

    fun updateUsage() {
        _remainingReplies.value = preferenceManager.getDailyRemaining()
    }

    fun upgrade() {
        preferenceManager.isPro = true
        _isPro.value = true
        updateUsage()
    }

    fun addRule(pattern: String, reply: String, matchType: MatchType): Boolean {
        val currentRulesCount = rules.value.size
        if (!preferenceManager.isPro && currentRulesCount >= PreferenceManager.FREE_RULE_LIMIT) {
            return false
        }
        
        viewModelScope.launch {
            repository.insert(ReplyRule(pattern = pattern, reply = reply, matchType = matchType))
        }
        return true
    }

    fun updateRule(rule: ReplyRule) {
        viewModelScope.launch {
            repository.update(rule)
        }
    }

    fun deleteRule(rule: ReplyRule) {
        viewModelScope.launch {
            repository.delete(rule)
        }
    }

    fun toggleRule(rule: ReplyRule) {
        viewModelScope.launch {
            repository.update(rule.copy(isEnabled = !rule.isEnabled))
        }
    }
}

class AutoReplyViewModelFactory(
    private val repository: ReplyRuleRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AutoReplyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AutoReplyViewModel(repository, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
