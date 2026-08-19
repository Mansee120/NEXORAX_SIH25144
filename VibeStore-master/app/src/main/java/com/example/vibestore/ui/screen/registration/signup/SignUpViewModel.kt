package com.example.vibestore.ui.screen.registration.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibestore.data.repository.VibeStoreRepository
import com.example.vibestore.model.Address
import com.example.vibestore.model.Geolocation
import com.example.vibestore.model.Name
import com.example.vibestore.model.UserResponse
import com.example.vibestore.ui.common.UiState
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val repository: VibeStoreRepository
) : ViewModel() {

    private val _uiState: MutableLiveData<UiState<UserResponse>?> = MutableLiveData(null)
    val uiState: LiveData<UiState<UserResponse>?> get() = _uiState

    fun register(
        username: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
//                val user = repository.register(username, email, password)
                val fakeUser = UserResponse(
                    id = 101,
                    username = username,
                    email = email,
                    password = password,
                    phone = "+91-9876543210",

                    name = Name(
                        firstname = "Krupal",
                        lastname = "Vekariya"
                    ),
                    address = Address(
                        zipcode = "411014",
                        number = 27,
                        city = "Pune",
                        street = "Viman Nagar Road",
                        geolocation = Geolocation(
                            lat = "18.5679",
                            jsonMemberLong = "73.9143"
                        )
                    )
                )
                repository.saveLoginData(username, "")
                _uiState.value = UiState.Success(fakeUser)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = null
    }
}