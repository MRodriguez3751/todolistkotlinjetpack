package com.example.listadetareasjetpack.models

data class UsuarioCreate(
    val username: String,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val contraseña: String,
    val activo: Boolean
)

data class Usuario(
    val id: Int,
    val username: String,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val contraseña: String,
    val fecha_registro: String,
    val activo: Boolean
)

data class AuthResponse(
    val access_token: String,
    val token_type: String
)

data class TareaCreate(
    val titulo: String,
    val descripcion: String,
    val fecha_limite: String?,
    val completada: Boolean
)

data class TareaUpdate(
    val titulo: String? = null,
    val descripcion: String? = null,
    val fecha_limite: String? = null,
    val completada: Boolean? = null
)

data class Tarea(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val fecha_limite: String?,
    val fecha_creacion: String,
    val fecha_actualizacion: String,
    val completada: Boolean,
    val usuario_id: Int
)