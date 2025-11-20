package com.example.listadetareasjetpack

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel() {
    var emailLogin by mutableStateOf("")
        private set
    var passwordLogin by mutableStateOf("")
        private set
    var emailRegistro by mutableStateOf("")
        private set
    var passwordRegistro by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf("")
        private set
    var isAuthenticated by mutableStateOf(false)
        private set

    private val usuarios = mutableListOf<Usuario>()

    init {
        agregarUsuarioPredeterminado()
    }

    private fun agregarUsuarioPredeterminado() {
        if (usuarios.isEmpty()) {
            usuarios.add(
                Usuario(
                    email = "admin@todo.cl",
                    password = "admin123"
                )
            )
        }
    }

    fun onEmailLoginChange(email: String) { emailLogin = email }
    fun onPasswordLoginChange(password: String) { passwordLogin = password }
    fun onEmailRegistroChange(email: String) { emailRegistro = email }
    fun onPasswordRegistroChange(password: String) { passwordRegistro = password }
    fun onConfirmPasswordChange(password: String) { confirmPassword = password }

    private fun clearError() {
        errorMessage = ""
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun login(): Boolean {
        clearError()
        if (emailLogin.isBlank() || passwordLogin.isBlank()) {
            errorMessage = "Completa todos los campos"
            return false
        }
        if (!isValidEmail(emailLogin)) {
            errorMessage = "Email inválido"
            return false
        }

        val usuario = usuarios.find { it.email == emailLogin && it.password == passwordLogin }
        if (usuario != null) {
            isAuthenticated = true
            return true
        } else {
            errorMessage = "Credenciales incorrectas"
            return false
        }
    }

    fun register(): Boolean {
        clearError()
        if (emailRegistro.isBlank() || passwordRegistro.isBlank() || confirmPassword.isBlank()) {
            errorMessage = "Completa todos los campos"
            return false
        }
        if (!isValidEmail(emailRegistro)) {
            errorMessage = "Email inválido"
            return false
        }
        if (passwordRegistro.length < 6) {
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
            return false
        }
        if (passwordRegistro != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return false
        }
        if (usuarios.any { it.email == emailRegistro }) {
            errorMessage = "El email ya está registrado"
            return false
        }

        usuarios.add(Usuario(emailRegistro, passwordRegistro))
        isAuthenticated = true
        return true
    }

    fun logout() {
        isAuthenticated = false
        emailLogin = ""
        passwordLogin = ""
    }
}