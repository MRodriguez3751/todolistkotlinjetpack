package com.example.listadetareasjetpack.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listadetareasjetpack.models.Tarea
import com.example.listadetareasjetpack.models.TareaCreate
import com.example.listadetareasjetpack.models.TareaUpdate
import com.example.listadetareasjetpack.utils.ApiService
import kotlinx.coroutines.launch
import retrofit2.Response

class TareasViewModel(private val apiService: ApiService) : ViewModel() {

    var tareas by mutableStateOf<List<Tarea>>(emptyList())
        private set

    var errorMessage by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
        if (token != null) {
            fetchTareas()
        }
    }

    private fun fetchTareas() {
        if (authToken == null) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val response: Response<List<Tarea>> = apiService.getTareas(authToken!!)
                if (response.isSuccessful) {
                    tareas = response.body() ?: emptyList()
                } else {
                    errorMessage = "Error al cargar tareas: ${response.code()} ${response.message()}"
                    Log.e("TareasViewModel", errorMessage)
                }
            } catch (e: Exception) {
                errorMessage = "Error de red: ${e.message}"
                Log.e("TareasViewModel", "Error fetching tareas", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun addTarea(titulo: String, descripcion: String = "", fechaLimite: String) {
        if (authToken == null) return
        viewModelScope.launch {
            try {
                val nuevaTarea = TareaCreate(
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha_limite = fechaLimite,
                    completada = false
                )
                val response = apiService.createTarea(nuevaTarea, authToken!!)
                if (response.isSuccessful) {
                    fetchTareas()
                } else {
                    errorMessage = "Error al crear tarea: ${response.code()} ${response.message()}"
                    Log.e("TareasViewModel", errorMessage)
                }
            } catch (e: Exception) {
                errorMessage = "Error al crear tarea: ${e.message}"
                Log.e("TareasViewModel", "Error creating tarea", e)
            }
        }
    }

    fun updateTarea(id: Int, titulo: String, descripcion: String, fechaLimite: String?) {
        if (authToken == null) return
        viewModelScope.launch {
            try {
                val update = TareaUpdate(
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha_limite = fechaLimite
                )
                val response = apiService.updateTarea(id, update, authToken!!)
                if (response.isSuccessful) {
                    fetchTareas()
                } else {
                    errorMessage = "Error al actualizar tarea: ${response.code()} ${response.message()}"
                    Log.e("TareasViewModel", errorMessage)
                }
            } catch (e: Exception) {
                errorMessage = "Error al actualizar tarea: ${e.message}"
                Log.e("TareasViewModel", "Error updating tarea", e)
            }
        }
    }

    fun deleteTarea(id: Int) {
        if (authToken == null) return
        viewModelScope.launch {
            try {
                val response = apiService.deleteTarea(id, authToken!!)
                if (response.isSuccessful) {
                    fetchTareas()
                } else {
                    errorMessage = "Error al eliminar tarea: ${response.code()} ${response.message()}"
                    Log.e("TareasViewModel", errorMessage)
                }
            } catch (e: Exception) {
                errorMessage = "Error al eliminar tarea: ${e.message}"
                Log.e("TareasViewModel", "Error deleting tarea", e)
            }
        }
    }
}