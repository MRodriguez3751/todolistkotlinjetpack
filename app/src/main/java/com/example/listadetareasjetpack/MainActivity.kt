package com.example.listadetareasjetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.listadetareasjetpack.ui.theme.ListaDeTareasJetpackTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState

import com.example.listadetareasjetpack.viewmodels.AuthViewModel
import com.example.listadetareasjetpack.utils.ApiService

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initializeRetrofit()

        setContent {
            ListaDeTareasJetpackTheme {
                val navController = rememberNavController()
                val viewModel = remember { AuthViewModel(apiService = apiService) }

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(navController = navController)
                    }
                    composable("registro") {
                        RegistroScreen(navController = navController, viewModel = viewModel)
                    }
                    composable("login") {
                        LoginScreen(navController = navController, viewModel = viewModel)
                    }
                    composable("main") {
                        MainScreenStyled(navController = navController, viewModel = viewModel)
                    }
                }
            }
        }
    }

    private fun initializeRetrofit() {
        val baseUrl = "https://todolistkotlin.vercel.app/"
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }
}

data class Tarea(val id: Int, val nombre: String, val descripcion: String, val fechaFin: String = "")
data class Usuario(val email: String, val password: String)


@Composable
fun HomeScreen(navController: NavController) {
    val appName = stringResource(R.string.app_name)

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 200.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Text(
                text = "Bienvenido a $appName!",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(top = 16.dp)
            )
            Text(
                text = "Día bajo control",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(top = 8.dp)
            )
        }

        BottomPanel(navController = navController)
    }
}

@Composable
fun BottomPanel(navController: NavController){
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFB6D9FF),
            Color(0xFF8CB4F0),
            Color(0xFF5C96E5)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                gradient,
                shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp)
            )
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 60.dp,
                    vertical = 60.dp
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Button(
                onClick = { navController.navigate("registro") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Crea tu cuenta", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Iniciar Sesión", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenStyled(navController: NavController, viewModel: AuthViewModel) {
    var tareas by remember { mutableStateOf(listOf<Tarea>()) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var tareaAEliminar by remember { mutableStateOf<Tarea?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var tareaAEditar by remember { mutableStateOf<Tarea?>(null) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaDescripcion by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var nombreTarea by remember { mutableStateOf("") }
    var descripcionTarea by remember { mutableStateOf("") }

    var showFechaDialog by remember { mutableStateOf(false) }
    var tareaAFechar by remember { mutableStateOf<Tarea?>(null) }
    var fechaFin by remember { mutableStateOf("") }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    val fondo = Color(0xFFF5F7FA)
    val secundario = Color(0xFF4A90E2)
    val tercero = Color(0xFF27AE60)

    var showLogoutDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = viewModel.isAuthenticated) {
        showLogoutDialog = true
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                        showLogoutDialog = false
                    }
                ) {
                    Text("Sí", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = secundario,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(65.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar tarea",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            ) {
                TopAppBar(
                    colors = topAppBarColors(containerColor = Color.Transparent),
                    modifier = Modifier.padding(0.dp),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                                .padding(end = 20.dp)
                        ) {
                            IconButton(onClick = {
                                showLogoutDialog = true
                            }) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "Cerrar sesión",
                                    tint = secundario,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Text(
                                text = "TO DO LIST",
                                color = secundario,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 40.dp)
                            )
                        }
                    }
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(fondo, Color.White)))
                .padding(padding)
                .padding(20.dp)
        ) {
            if (tareas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes tareas agregadas", color = Color.Gray, fontSize = 18.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tareas) { tarea ->
                        TareaCardStyled(
                            tarea = tarea,
                            colorPrincipal = tercero,
                            onEliminar = {
                                tareaAEliminar = tarea
                                showDeleteDialog = true
                            },
                            onEditar = {
                                tareaAEditar = tarea
                                nuevoNombre = tarea.nombre
                                nuevaDescripcion = tarea.descripcion
                                showEditDialog = true
                            },
                            onFecha = {
                                tareaAFechar = tarea
                                fechaFin = tarea.fechaFin
                                showFechaDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showDeleteDialog && tareaAEliminar != null) {
            DeleteConfirmationDialogStyled(
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    tareas = tareas.filter { it.id != tareaAEliminar!!.id }
                    tareaAEliminar = null
                    showDeleteDialog = false
                }
            )
        }

        if (showEditDialog && tareaAEditar != null) {
            EditTareaDialogStyled(
                nombreActual = nuevoNombre,
                descripcionActual = nuevaDescripcion,
                onNombreChange = { nuevoNombre = it },
                onDescripcionChange = { nuevaDescripcion = it },
                onDismiss = { showEditDialog = false },
                onConfirm = {
                    tareas = tareas.map {
                        if (it.id == tareaAEditar!!.id)
                            it.copy(nombre = nuevoNombre, descripcion = nuevaDescripcion)
                        else it
                    }
                    tareaAEditar = null
                    showEditDialog = false
                }
            )
        }

        if (showAddDialog) {
            AddTareaDialogStyled(
                nombre = nombreTarea,
                descripcion = descripcionTarea,
                onNombreChange = { nombreTarea = it },
                onDescripcionChange = { descripcionTarea = it },
                onDismiss = {
                    showAddDialog = false
                    nombreTarea = ""
                    descripcionTarea = ""
                },
                onConfirm = {
                    if (nombreTarea.isNotBlank()) {
                        val nuevaTarea = Tarea(
                            id = (tareas.maxOfOrNull { it.id } ?: 0) + 1,
                            nombre = nombreTarea,
                            descripcion = descripcionTarea
                        )
                        tareas = tareas + nuevaTarea
                        showAddDialog = false
                        nombreTarea = ""
                        descripcionTarea = ""
                    }
                }
            )
        }

        if (showFechaDialog && tareaAFechar != null) {
            FechaDialogStyled(
                fechaFin = fechaFin,
                onFechaFinChange = { fechaFin = it },
                onDismiss = { showFechaDialog = false },
                onConfirm = {
                    tareas = tareas.map {
                        if (it.id == tareaAFechar!!.id) it.copy(fechaFin = fechaFin) else it
                    }
                    showFechaDialog = false
                    tareaAFechar = null
                },
                onOpenDatePicker = { showDatePickerDialog = true },
                selectedDateMillis = selectedDateMillis
            )
        }

        if (showDatePickerDialog) {
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedDateMillis = datePickerState.selectedDateMillis
                            selectedDateMillis?.let { millis ->
                                fechaFin = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(millis)
                            }
                            showDatePickerDialog = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePickerDialog = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FechaDialogStyled(
    fechaFin: String,
    onFechaFinChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onOpenDatePicker: () -> Unit,
    selectedDateMillis: Long?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar fecha límite", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = fechaFin,
                    onValueChange = onFechaFinChange,
                    label = { Text("Fecha (ej: 30/10/2025)") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = onOpenDatePicker) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (selectedDateMillis != null) {
                    Text(
                        text = "Fecha seleccionada: ${convertMillisToDate(selectedDateMillis)}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFB6D9FF),
            Color(0xFF8CB4F0),
            Color(0xFF5C96E5)
        )
    )

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                colors = topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(gradient)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.emailRegistro,
                onValueChange = viewModel::onEmailRegistroChange,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.passwordRegistro,
                onValueChange = viewModel::onPasswordRegistroChange,
                label = { Text("Contraseña") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text("Confirmar contraseña") },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (viewModel.errorMessage.isNotBlank()) {
                Text(
                    text = viewModel.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (viewModel.register()) {
                        navController.navigate("main") { popUpTo("start") { inclusive = true } }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Crear cuenta")
            }

            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFB6D9FF),
            Color(0xFF8CB4F0),
            Color(0xFF5C96E5)
        )
    )

    var showPassword by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") },
                colors = topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(gradient)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.emailLogin,
                onValueChange = viewModel::onEmailLoginChange,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.passwordLogin,
                onValueChange = viewModel::onPasswordLoginChange,
                label = { Text("Contraseña") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (viewModel.errorMessage.isNotBlank()) {
                Text(
                    text = viewModel.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (viewModel.login()) {
                        navController.navigate("main") { popUpTo("start") { inclusive = true } }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Iniciar sesión")
            }

            TextButton(
                onClick = { navController.navigate("registro") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}

@Composable
fun TareaCardStyled(
    tarea: Tarea,
    colorPrincipal: Color,
    onEliminar: () -> Unit,
    onEditar: () -> Unit,
    onFecha: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    tarea.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorPrincipal
                )
                Row {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF4FC3F7))
                    }
                    IconButton(onClick = onFecha) {
                        Icon(Icons.Default.DateRange, contentDescription = "Añadir fecha", tint = Color(0xFFFFC107))
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFE57373))
                    }
                }
            }
            if (tarea.descripcion.isNotBlank()) {
                Text(
                    text = tarea.descripcion,
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (tarea.fechaFin.isNotBlank()) {
                Text(
                    text = "Fecha límite: ${tarea.fechaFin}",
                    color = Color(0xFF757575),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun AddTareaDialogStyled(
    nombre: String,
    descripcion: String,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar nueva tarea", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                TextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre de la tarea") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = descripcion,
                    onValueChange = onDescripcionChange,
                    label = { Text("Descripción (opcional)") },
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Agregar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun EditTareaDialogStyled(
    nombreActual: String,
    descripcionActual: String,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar tarea", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                TextField(
                    value = nombreActual,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre de la tarea") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = descripcionActual,
                    onValueChange = onDescripcionChange,
                    label = { Text("Descripción") },
                    maxLines = 3
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun DeleteConfirmationDialogStyled(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = "Advertencia", tint = Color(0xFFE57373)) },
        title = { Text("¡Cuidado!", fontWeight = FontWeight.Bold, color = Color(0xFFE57373)) },
        text = { Text("Tu tarea se eliminará para siempre. ¿Deseas continuar?", color = Color.DarkGray) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Eliminar", color = Color(0xFFE57373)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) } }
    )
}

fun convertMillisToDate(millis: Long): String {
    val date = Date(millis)
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return format.format(date)
}