package com.example.listadetareasjetpack.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listadetareasjetpack.utils.ApiService
import com.example.listadetareasjetpack.models.AuthResponse
import com.example.listadetareasjetpack.models.UsuarioCreate
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthViewModel(private val apiService: ApiService) : ViewModel() {

    var emailRegistro by mutableStateOf("")
    var passwordRegistro by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var errorMessage by mutableStateOf("")
    var nombreRegistro by mutableStateOf("")
    var apellidosRegistro by mutableStateOf("")
    var usernameRegistro by mutableStateOf("")
    var emailLogin by mutableStateOf("")
    var passwordLogin by mutableStateOf("")
    var isAuthenticated by mutableStateOf(false)
        private set
    private var currentToken: String? = null

    fun login(): Boolean {
        viewModelScope.launch {
            try {
                val response: Response<AuthResponse> = apiService.authenticate(
                    username = emailLogin,
                    password = passwordLogin
                )

                if (response.isSuccessful) {
                    val authData = response.body()
                    if (authData != null) {
                        currentToken = "Bearer ${authData.access_token}"
                        isAuthenticated = true
                        errorMessage = ""
                        Log.d("AuthViewModel", "Login exitoso, token guardado.")
                        return@launch
                    }
                } else {
                    errorMessage = "Error de autenticación: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error en login", e)
                errorMessage = "Error de red: ${e.message}"
            }
        }
        return false
    }

    fun register(): Boolean {
        if (passwordRegistro != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return false
        }
        if (passwordRegistro.length < 6) {
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
            return false
        }
        viewModelScope.launch {
            try {
                val usuarioCreate = UsuarioCreate(
                    username = usernameRegistro,
                    nombre = nombreRegistro,
                    apellidos = apellidosRegistro,
                    email = emailRegistro,
                    contraseña = passwordRegistro
                )

                errorMessage = ""
                val response = apiService.createUsuario(usuarioCreate)
                if(response.isSuccessful) {
                    errorMessage = ""
                    Log.d("AuthViewModel", "Registro exitoso.")
                    emailLogin = emailRegistro
                    passwordLogin = passwordRegistro
                    login()
                } else {
                    errorMessage = "Error al registrar: ${response.code()} - ${response.message()}"
                    Log.e("AuthViewModel", "Error en register: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error de red al registrar", e)
                errorMessage = "Error de red al registrar: ${e.message}"
            }
        }
        return errorMessage.isEmpty()
    }


    fun logout() {
        isAuthenticated = false
        currentToken = null
        Log.d("AuthViewModel", "Logout realizado.")
    }

    fun onEmailRegistroChange(email: String) { emailRegistro = email }
    fun onPasswordRegistroChange(password: String) { passwordRegistro = password }
    fun onConfirmPasswordChange(password: String) { confirmPassword = password }
    fun onNombreRegistroChange(nombre: String) { nombreRegistro = nombre }
    fun onApellidosRegistroChange(apellidos: String) { apellidosRegistro = apellidos }
    fun onUsernameRegistroChange(username: String) { usernameRegistro = username }
    fun onEmailLoginChange(email: String) { emailLogin = email }
    fun onPasswordLoginChange(password: String) { passwordLogin = password }
    fun getCurrentToken(): String? = currentToken
}