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
    
    var currentUserName by mutableStateOf("")
        private set
        
    var isLoading by mutableStateOf(false)
        private set

    fun login() {
        viewModelScope.launch {
            isLoading = true
            try {
                if (usernameLogin.isEmpty() || passwordLogin.isEmpty()) {
                    errorMessage = "Por favor, llene todos los campos."
                    return@launch
                }

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
                        fetchUserProfile()
                        return@launch
                    }
                }
                errorMessage = "Credenciales incorrectas."
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error en login", e)
                errorMessage = "Error inesperado."
            } finally {
                isLoading = false
            }
        }
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val response = apiService.getUsuarios()
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    val user = usuarios?.find { it.username == usernameLogin }
                    if (user != null) {
                        currentUserName = "${user.nombre} ${user.apellidos}"
                        Log.d("AuthViewModel", "Usuario encontrado: $currentUserName")
                    } else {
                        Log.e("AuthViewModel", "Usuario no encontrado con email: $usernameLogin")
                    }
                } else {
                    Log.e("AuthViewModel", "Error al obtener usuarios: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al obtener perfil de usuario", e)
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
            isLoading = true
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
                    errorMessage = "Error al registrarse."
                    Log.e("AuthViewModel", "Error en register: ${response.code()} - ${response.message()}")
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error de red al registrar", e)
                errorMessage = "Error inesperado."
                isLoading = false
            }
        }
    }


    fun logout() {
        isAuthenticated = false
        currentToken = null
        currentUserName = ""
        Log.d("AuthViewModel", "Logout realizado.")
    }

    fun onEmailRegistroChange(email: String) { emailRegistro = email }
    fun onPasswordRegistroChange(password: String) { passwordRegistro = password }
    fun onConfirmPasswordChange(password: String) { confirmPassword = password }
    fun onNombreRegistroChange(nombre: String) { nombreRegistro = nombre }
    fun onApellidosRegistroChange(apellidos: String) { apellidosRegistro = apellidos }
    fun onUsernameRegistroChange(username: String) { usernameRegistro = username }
    fun onUsernameLoginChange(username: String) { usernameLogin = username }
    fun onPasswordLoginChange(password: String) { passwordLogin = password }
    fun getCurrentToken(): String? = currentToken

    fun cleanAllFields() {
        emailRegistro = ""
        passwordRegistro = ""
        confirmPassword = ""
        nombreRegistro = ""
        apellidosRegistro = ""
        usernameRegistro = ""
        usernameLogin = ""
        passwordLogin = ""
    }
}