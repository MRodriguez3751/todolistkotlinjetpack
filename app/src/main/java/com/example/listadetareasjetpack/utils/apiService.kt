package com.example.listadetareasjetpack.utils

import com.example.listadetareasjetpack.models.Usuario
import com.example.listadetareasjetpack.models.UsuarioCreate
import com.example.listadetareasjetpack.models.UsuarioUpdate
import com.example.listadetareasjetpack.models.Tarea
import com.example.listadetareasjetpack.models.TareaCreate
import com.example.listadetareasjetpack.models.TareaUpdate
import com.example.listadetareasjetpack.models.AuthResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("/auth")
    suspend fun authenticate(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @GET("/usuarios")
    suspend fun getUsuarios(
    ): Response<List<Usuario>>

    @GET("/usuarios/{id}")
    suspend fun getUsuarioById(
        @Path("id") id: Int,
    ): Response<List<Usuario>>

    @POST("/usuarios")
    suspend fun createUsuario(
        @Body usuarioCreate: UsuarioCreate,
    ): Response<List<Usuario>>

    @PUT("/usuarios/{id}")
    suspend fun updateUsuario(
        @Path("id") id: Int,
        @Query("contraseña_actual") currentPassword: String,
        @Body usuarioUpdate: UsuarioUpdate,
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    @DELETE("/usuarios/{id}")
    suspend fun deleteUsuario(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("/tareas")
    suspend fun getTareas(
        @Header("Authorization") token: String
    ): Response<List<Tarea>>

    @GET("/tareas/{id}")
    suspend fun getTarea(
        @Header("Authorization") token: String
    ): Response<Tarea>

    @POST("/tareas")
    suspend fun createTarea(
        @Body tareaCreate: TareaCreate,
        @Header("Authorization") token: String
    ): Response<List<Tarea>>

    @PUT("/tareas/{id}")
    suspend fun updateTarea(
        @Path("id") id: Int,
        @Body tareaUpdate: TareaUpdate,
        @Header("Authorization") token: String
    ): Response<List<Tarea>>

    @DELETE("/tareas/{id}")
    suspend fun deleteTarea(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>
}