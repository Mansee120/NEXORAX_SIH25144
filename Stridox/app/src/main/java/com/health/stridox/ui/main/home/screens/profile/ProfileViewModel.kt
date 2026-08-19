package com.health.stridox.ui.main.home.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.stridox.domain.Preferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(private val prefs: Preferences) : ViewModel() {
    data class UiState(
        val name: String = "",
        val email: String = "",
        val emergency: String = "",
        val address: String = "",
        val photoUri: String? = null,
        val gender: String = "",
        val birthday: String = "",
        val height: String = "",
        val weight: String = "",
        val stepLength: String = "",
        val armpitToWrist: String = "",
        val wristToFoot: String = "",
        
        val isEditing: Boolean = false,

        val tempName: String = "",
        val tempEmail: String = "",
        val tempEmergency: String = "",
        val tempAddress: String = "",
        val tempPhotoUri: String? = null,
        val tempGender: String = "",
        val tempBirthday: String = "",
        val tempHeight: String = "",
        val tempWeight: String = "",
        val tempStepLength: String = "",
        val tempArmpitToWrist: String = "",
        val tempWristToFoot: String = ""
    )

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.getLoginResponseFlow().collect { user ->
                if (user != null) {
                    _state.update {
                        it.copy(
                            name = user.name,
                            email = user.email,
                            emergency = user.emergencyNumber,
                            photoUri = user.profileUrl,
                            address = user.address,
                            gender = user.gender,
                            birthday = user.birthday,
                            height = user.height,
                            weight = user.weight,
                            stepLength = user.stepLength,
                            armpitToWrist = user.armpitToWrist,
                            wristToFoot = user.wristToFoot,

                            tempName = user.name,
                            tempEmail = user.email,
                            tempEmergency = user.emergencyNumber,
                            tempAddress = user.address,
                            tempPhotoUri = user.profileUrl,
                            tempGender = user.gender,
                            tempBirthday = user.birthday,
                            tempHeight = user.height,
                            tempWeight = user.weight,
                            tempStepLength = user.stepLength,
                            tempArmpitToWrist = user.armpitToWrist,
                            tempWristToFoot = user.wristToFoot
                        )
                    }
                }
            }
        }
    }

    fun startEditing() = _state.update {
        it.copy(
            isEditing = true,
            tempPhotoUri = it.photoUri
        )
    }
    
    fun cancelEditing() = _state.update { cur ->
        cur.copy(
            isEditing = false,
            tempName = cur.name,
            tempEmail = cur.email,
            tempEmergency = cur.emergency,
            tempAddress = cur.address,
            tempPhotoUri = cur.photoUri,
            tempGender = cur.gender,
            tempBirthday = cur.birthday,
            tempHeight = cur.height,
            tempWeight = cur.weight,
            tempStepLength = cur.stepLength,
            tempArmpitToWrist = cur.armpitToWrist,
            tempWristToFoot = cur.wristToFoot
        )
    }

    fun onTempName(v: String) = _state.update { it.copy(tempName = v) }
    fun onTempEmail(v: String) = _state.update { it.copy(tempEmail = v) }
    fun onTempEmergency(v: String) = _state.update { it.copy(tempEmergency = v) }
    fun onImagePicked(uri: String) = _state.update { it.copy(tempPhotoUri = uri) }
    fun onTempAddress(v: String) = _state.update { it.copy(tempAddress = v) }

    fun onTempGender(v: String) = _state.update { it.copy(tempGender = v) }
    fun onTempBirthday(v: String) = _state.update { it.copy(tempBirthday = v) }
    fun onTempHeight(v: String) = _state.update { it.copy(tempHeight = v) }
    fun onTempWeight(v: String) = _state.update { it.copy(tempWeight = v) }
    fun onTempStepLength(v: String) = _state.update { it.copy(tempStepLength = v) }
    fun onTempArmpitToWrist(v: String) = _state.update { it.copy(tempArmpitToWrist = v) }
    fun onTempWristToFoot(v: String) = _state.update { it.copy(tempWristToFoot = v) }

    fun saveProfile() = viewModelScope.launch {
        val cur = state.value
        val user = prefs.getLoginResponse() ?: return@launch
        val updated = user.copy(
            name = cur.tempName,
            email = cur.tempEmail,
            emergencyNumber = cur.tempEmergency,
            profileUrl = cur.tempPhotoUri,
            address = cur.tempAddress,
            gender = cur.tempGender,
            birthday = cur.tempBirthday,
            height = cur.tempHeight,
            weight = cur.tempWeight,
            stepLength = cur.tempStepLength,
            armpitToWrist = cur.tempArmpitToWrist,
            wristToFoot = cur.tempWristToFoot
        )
        prefs.login(updated)
        _state.update {
            it.copy(
                isEditing = false,
                name = updated.name,
                email = updated.email,
                emergency = updated.emergencyNumber,
                photoUri = updated.profileUrl,
                address = updated.address,
                gender = updated.gender,
                birthday = updated.birthday,
                height = updated.height,
                weight = updated.weight,
                stepLength = updated.stepLength,
                armpitToWrist = updated.armpitToWrist,
                wristToFoot = updated.wristToFoot,
                tempPhotoUri = updated.profileUrl
            )
        }
    }

    fun logout(onLoggedOut: (() -> Unit)? = null) = viewModelScope.launch {
        prefs.logout()
        onLoggedOut?.invoke()
    }
}
