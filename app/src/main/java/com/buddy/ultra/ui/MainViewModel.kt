package com.buddy.ultra.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buddy.ultra.actions.ActionExecutionEngine
import com.buddy.ultra.backend.BuddyRepository
import com.buddy.ultra.database.BuddyDao
import com.buddy.ultra.database.MessageEntity
import com.buddy.ultra.security.SecurityManager
import com.buddy.ultra.voice.OrbState
import com.buddy.ultra.voice.SpeechEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: BuddyRepository,
    private val actionEngine: ActionExecutionEngine,
    val dao: BuddyDao,
    val speechEngine: SpeechEngine,
    val securityManager: SecurityManager
) : ViewModel() {

    private val _isServerUp = MutableStateFlow(false)
    val isServerUp: StateFlow<Boolean> = _isServerUp.asStateFlow()

    val conversationHistory = dao.getRecentHistory()

    init {
        checkBackendHealth()
        speechEngine.onSpeechCaptured = { spoken ->
            submitCommand(spoken)
        }
    }

    fun checkBackendHealth() {
        viewModelScope.launch {
            _isServerUp.value = repository.pingHealth()
        }
    }

    fun submitCommand(cmd: String) {
        if (cmd.isBlank()) return
        viewModelScope.launch {
            dao.insertMessage(MessageEntity(role = "USER", text = cmd))
            speechEngine.setOrbState(OrbState.THINKING)

            val res = repository.sendQuery(cmd)
            res.onSuccess { response ->
                var out = response.message
                var actType: String? = null

                if (!response.actions.isNullOrEmpty()) {
                    speechEngine.setOrbState(OrbState.EXECUTING)
                    for (action in response.actions) {
                        out = actionEngine.execute(action)
                        actType = action.type
                    }
                }

                dao.insertMessage(MessageEntity(role = "BUDDY", text = out, actionType = actType))
                speechEngine.speak(out)
            }.onFailure {
                speechEngine.setOrbState(OrbState.OFFLINE)
                val fail = "Offline Mode: Connection error to Buddy Core."
                dao.insertMessage(MessageEntity(role = "BUDDY", text = fail))
                speechEngine.speak(fail)
            }
        }
    }

    fun toggleVoiceOrb() {
        if (speechEngine.orbState.value == OrbState.LISTENING) {
            speechEngine.stopListening()
        } else {
            speechEngine.startListening()
        }
    }
}
