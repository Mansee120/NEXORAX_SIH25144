package com.health.stridox.ui.main.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.stridox.domain.Preferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val prefs: Preferences  // your domain.Preferences
) : ViewModel() {

    data class UiState(
        val username: String = "",
        val password: String = "",
        val usernameError: String? = null,
        val passwordError: String? = null,
        val loading: Boolean = false,
        val message: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    fun onUsernameChange(v: String) = _state.update {
        it.copy(username = v, usernameError = null, message = null)
    }

    fun onPasswordChange(v: String) = _state.update {
        it.copy(password = v, passwordError = null, message = null)
    }

    fun onLogin(onSuccess: () -> Unit) = viewModelScope.launch {
        _state.update { it.copy(loading = true, message = null) }
        val s = state.value

        var valid = true
        if (s.username.isBlank()) {
            _state.update { it.copy(usernameError = "Username is required") }
            valid = false
        }
        if (s.password.length < 6) {
            _state.update { it.copy(passwordError = "Password must be at least 6 characters") }
            valid = false
        }
        if (!valid) {
            _state.update { it.copy(loading = false) }
            return@launch
        }

        // Simple local auth using stored LoginUser
        val stored = prefs.getLoginResponse()

        val isMatch = stored != null && (stored.name.equals(
            s.username, ignoreCase = true
        ) || stored.email.equals(s.username, ignoreCase = true)) && stored.password == s.password

        if (isMatch) {
            // Optionally re-save to update anything
            prefs.login(stored)
            _state.update { it.copy(loading = false) }
            onSuccess()
        } else {
            _state.update {
                it.copy(
                    loading = false, message = "Invalid username or password"
                )
            }
        }
    }
}