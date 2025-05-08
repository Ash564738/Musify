package com.example.musify.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _authenticationState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authenticationState: StateFlow<AuthState> = _authenticationState

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            Log.d(TAG, "Auth state changed: ${firebaseAuth.currentUser}")
        }
    }

    fun handleEmailLogin(email: String, password: String) {
        viewModelScope.launch {
            _authenticationState.value = AuthState.Loading("Signing in...")
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Login successful for email: $email")
                            _authenticationState.value = AuthState.Success("Login successful")
                        } else {
                            val errorMessage = task.exception?.message ?: "Authentication failed"
                            Log.e(TAG, "Login failed: $errorMessage")
                            handleError(errorMessage)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during login: ${e.message}", e)
                handleError(e.message ?: "Login failed")
            }
        }
    }

    fun handleEmailSignUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authenticationState.value = AuthState.Loading("Creating account...")
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Signup successful for email: $email")
                            updateUserProfile(username)
                        } else {
                            val errorMessage = task.exception?.message ?: "Signup failed"
                            Log.e(TAG, "Signup failed: $errorMessage")
                            handleError(errorMessage)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during signup: ${e.message}", e)
                handleError(e.message ?: "Signup failed")
            }
        }
    }

    private fun updateUserProfile(username: String) {
        val user = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()

        try {
            user?.updateProfile(profileUpdates)
                ?.addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Log.d(TAG, "Profile updated successfully for username: $username")
                        _authenticationState.value = AuthState.Success("Account created successfully")
                    } else {
                        val errorMessage = updateTask.exception?.message ?: "Profile update failed"
                        Log.e(TAG, "Profile update failed: $errorMessage")
                        handleError(errorMessage)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during profile update: ${e.message}", e)
            handleError(e.message ?: "Profile update failed")
        }
    }

    private fun handleError(message: String) {
        Log.e(TAG, "Error: $message")
        _authenticationState.value = AuthState.Error(message)
    }

    fun logout() {
        try {
            auth.signOut()
            Log.d(TAG, "User logged out successfully")
            _authenticationState.value = AuthState.Idle
        } catch (e: Exception) {
            Log.e(TAG, "Exception during logout: ${e.message}", e)
            handleError(e.message ?: "Logout failed")
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        data class Loading(val message: String) : AuthState()
        data class Success(val message: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}