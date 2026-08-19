package com.health.stridox.ui.main.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.stridox.data.LoginUser
import com.health.stridox.domain.Preferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val prefs: Preferences
) : ViewModel() {

    data class UiState(
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val emergencyNumber: String = "",
        val profileUri: String? = null,
        val address: String = "",
        val gender: String = "",
        val birthday: String = "",
        val height: String = "",
        val weight: String = "",
        val stepLength: String = "",
        val armpitToWrist: String = "",
        val wristToFoot: String = "",
        val nameError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val emergencyError: String? = null,
        val addressError: String? = null,
        val loading: Boolean = false,
        val message: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    fun onName(v: String) = _state.update {
        it.copy(name = v, nameError = null, message = null)
    }

    fun onEmail(v: String) = _state.update {
        it.copy(email = v, emailError = null, message = null)
    }

    fun onPassword(v: String) = _state.update {
        it.copy(password = v, passwordError = null, message = null)
    }

    fun onEmergency(v: String) = _state.update {
        it.copy(emergencyNumber = v, emergencyError = null, message = null)
    }

    fun onProfileUri(u: String?) = _state.update {
        it.copy(profileUri = u)
    }
    fun onAddress(v: String) = _state.update {
        it.copy(address = v, addressError = null, message = null)
    }

    fun onGender(v: String) = _state.update { it.copy(gender = v) }
    fun onBirthday(v: String) = _state.update { it.copy(birthday = v) }
    fun onHeight(v: String) = _state.update { it.copy(height = v) }
    fun onWeight(v: String) = _state.update { it.copy(weight = v) }
    fun onStepLength(v: String) = _state.update { it.copy(stepLength = v) }
    fun onArmpitToWrist(v: String) = _state.update { it.copy(armpitToWrist = v) }
    fun onWristToFoot(v: String) = _state.update { it.copy(wristToFoot = v) }


    fun onRegister(onSuccess: () -> Unit) = viewModelScope.launch {
        _state.update { it.copy(loading = true, message = null) }
        val s = state.value

        var valid = true

        if (s.name.isBlank()) {
            _state.update { it.copy(nameError = "Required") }
            valid = false
        }

        if (s.email.isBlank() || !s.email.contains("@")) {
            _state.update { it.copy(emailError = "Invalid email") }
            valid = false
        }

        if (s.password.length < 6) {
            _state.update { it.copy(passwordError = "Min 6 characters") }
            valid = false
        }

        if (s.emergencyNumber.length < 6) {
            _state.update { it.copy(emergencyError = "Invalid emergency number") }
            valid = false
        }

        if (s.address.trim().length < 3) {
            _state.update { it.copy(addressError = "Enter a valid address") }
            valid = false
        }

        if (!valid) {
            _state.update { it.copy(loading = false) }
            return@launch
        }

        val user = LoginUser(
            name = s.name.trim(),
            password = s.password.trim(),
            email = s.email.trim(),
            emergencyNumber = s.emergencyNumber.trim(),
            profileUrl = s.profileUri?.trim() ?: "",
            address = s.address.trim(),
            gender = s.gender.trim(),
            birthday = s.birthday.trim(),
            height = s.height.trim(),
            weight = s.weight.trim(),
            stepLength = s.stepLength.trim(),
            armpitToWrist = s.armpitToWrist.trim(),
            wristToFoot = s.wristToFoot.trim()
        )

        prefs.login(user) // persist user

        _state.update { it.copy(loading = false) }
        onSuccess()
    }
}
