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
    var usernameLogin by mutableStateOf("")
    var passwordLogin by mutableStateOf("")
    var isAuthenticated by mutableStateOf(false)
        private set
    private var currentToken: String? = null

    fun login() {
        viewModelScope.launch {
            try {
                val response: Response<AuthResponse> = apiService.authenticate(
                    username = usernameLogin,
                    password = passwordLogin
                )
                if (response.isSuccessful) {
                    val authData = response.body()
                    if (authData != null) {
                        currentToken = "Bearer ${authData.access_token}"
                        isAuthenticated = true
                        errorMessage = ""
                        Log.d("AuthViewModel", "Login exitoso, token guardado.")
                        cleanAllFields()
                        return@launch
                    }
                }
                errorMessage = "Error de autenticación: ${response.code()} - ${response.message()}"
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error en login", e)
                errorMessage = "Error de red: ${e.message}"
            }
        }
    }

    fun register() {
        if (passwordRegistro != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }
        if (passwordRegistro.length < 6) {
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        viewModelScope.launch {
            try {
                val usuarioCreate = UsuarioCreate(
                    username = usernameRegistro,
                    nombre = nombreRegistro,
                    apellidos = apellidosRegistro,
                    email = emailRegistro,
                    contraseña = passwordRegistro,
                    activo = true
                )
                val response = apiService.createUsuario(usuarioCreate)
                if (response.isSuccessful) {
                    Log.d("AuthViewModel", "Registro exitoso.")
                    usernameLogin = usernameRegistro
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
    fun onEmailLoginChange(email: String) { usernameLogin = email }
    fun onPasswordLoginChange(password: String) { passwordLogin = password }
    fun getCurrentToken(): String? = currentToken

    fun cleanAllFields() {
        emailRegistro = ""
        passwordRegistro = ""
        confirmPassword = ""
        errorMessage = ""
        nombreRegistro = ""
        apellidosRegistro = ""
        usernameRegistro = ""
        usernameLogin = ""
        passwordLogin = ""
    }
}