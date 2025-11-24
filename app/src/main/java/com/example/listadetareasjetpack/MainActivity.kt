package com.example.listadetareasjetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.listadetareasjetpack.models.Tarea
import com.example.listadetareasjetpack.viewmodels.AuthViewModel
import com.example.listadetareasjetpack.utils.ApiService
import com.example.listadetareasjetpack.viewmodels.TareasViewModel
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
                val viewModel: AuthViewModel = remember { AuthViewModel(apiService = apiService) }
                val tareasViewModel: TareasViewModel = remember { TareasViewModel(apiService = apiService) }

                LaunchedEffect(viewModel.isAuthenticated) {
                    if (viewModel.isAuthenticated) {
                        val token = viewModel.getCurrentToken()
                        tareasViewModel.setAuthToken(token)
                        navController.navigate("main") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }

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
                        MainScreenStyled(
                            navController = navController,
                            viewModel = viewModel,
                            tareasViewModel = tareasViewModel
                        )
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

@Composable
fun HomeScreen(navController: NavController) {
    val appName = stringResource(R.string.app_name)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 200.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Día bajo control",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        BottomPanel(navController = navController)
    }
}

@Composable
fun BottomPanel(navController: NavController) {
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 60.dp, vertical = 60.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
fun MainScreenStyled(
    navController: NavController,
    viewModel: AuthViewModel,
    tareasViewModel: TareasViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showFechaDialog by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    var nuevoTitulo by remember { mutableStateOf("") }
    var nuevaDescripcion by remember { mutableStateOf("") }

    var tituloTarea by remember { mutableStateOf("") }
    var descripcionTarea by remember { mutableStateOf("") }

    var fechaLimite: String? by remember { 
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) 
    }

    val datePickerState = rememberDatePickerState()
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var tareaAEliminar: Tarea? by remember { mutableStateOf(null) }
    var tareaAEditar: Tarea? by remember { mutableStateOf(null) }
    var tareaAFechar: Tarea? by remember { mutableStateOf(null) }

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
                        viewModel.cleanAllFields()
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
                onClick = { 
                    showAddDialog = true 
                    fechaLimite = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                },
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 20.dp)
                        ) {
                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "Cerrar sesión",
                                    tint = secundario,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "TO DO LIST",
                                    color = secundario,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                    textAlign = TextAlign.Center
                                )
                                if (viewModel.currentUserName.isNotBlank()) {
                                    Text(
                                        text = "Hola, ${viewModel.currentUserName}",
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
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
            val tareas = tareasViewModel.tareas
            if (tareas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes tareas agregadas", color = Color.Gray, fontSize = 18.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
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
                                nuevoTitulo = tarea.titulo
                                nuevaDescripcion = tarea.descripcion
                                fechaLimite = tarea.fecha_limite
                                showEditDialog = true
                            },
                            onFecha = {
                                tareaAFechar = tarea
                                fechaLimite = tarea.fecha_limite
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
                    tareaAEliminar?.let { t ->
                        tareasViewModel.deleteTarea(t.id)
                    }
                    tareaAEliminar = null
                    showDeleteDialog = false
                }
            )
        }

        if (showEditDialog && tareaAEditar != null) {
            EditTareaDialogStyled(
                nombreActual = nuevoTitulo,
                descripcionActual = nuevaDescripcion,
                fechaLimite = fechaLimite,
                onNombreChange = { nuevoTitulo = it },
                onDescripcionChange = { nuevaDescripcion = it },
                onFechaChange = { fechaLimite = it },
                onOpenDatePicker = { showDatePickerDialog = true },
                onDismiss = { showEditDialog = false },
                onConfirm = {
                    tareaAEditar?.let { t ->
                        tareasViewModel.updateTarea(
                            id = t.id,
                            titulo = nuevoTitulo,
                            descripcion = nuevaDescripcion,
                            fechaLimite = fechaLimite
                        )
                    }
                    tareaAEditar = null
                    showEditDialog = false
                }
            )
        }

        if (showAddDialog) {
            AddTareaDialogStyled(
                nombre = tituloTarea,
                descripcion = descripcionTarea,
                onNombreChange = { tituloTarea = it },
                onDescripcionChange = { descripcionTarea = it },
                onDismiss = {
                    showAddDialog = false
                    tituloTarea = ""
                    descripcionTarea = ""
                },
                onConfirm = {
                    if (tituloTarea.isNotBlank()) {
                        tareasViewModel.addTarea(
                            titulo = tituloTarea,
                            descripcion = descripcionTarea,
                            fechaLimite = fechaLimite ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        )
                        showAddDialog = false
                        tituloTarea = ""
                        descripcionTarea = ""
                        fechaLimite = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    }
                }
            )
        }

        if (showFechaDialog && tareaAFechar != null) {
            FechaDialogStyled(
                fechaFin = fechaLimite,
                onFechaFinChange = { fechaLimite = it },
                onDismiss = { showFechaDialog = false },
                onConfirm = {
                    tareaAFechar?.let { t ->
                        tareasViewModel.updateTarea(
                            id = t.id,
                            titulo = t.titulo,
                            descripcion = t.descripcion,
                            fechaLimite = fechaLimite
                        )
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
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                fechaLimite = sdf.format(millis)
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
    fechaFin: String?,
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
                if (fechaFin != null) {
                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = onFechaFinChange,
                        label = { Text("Fecha (ej: 2025-10-30)") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = onOpenDatePicker) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
        Column(modifier = Modifier.padding(horizontal=16.dp, vertical=10.dp)) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ){
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
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                tarea.titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorPrincipal,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            if (tarea.descripcion.isNotBlank()) {
                Text(
                    text = tarea.descripcion,
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (tarea.fecha_limite!!.isNotBlank()) {
                Text(
                    text = "Fecha límite: ${tarea.fecha_limite}",
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
    fechaLimite: String?,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onOpenDatePicker: () -> Unit,
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
                Spacer(modifier = Modifier.height(8.dp))
                if (fechaLimite != null) {
                    OutlinedTextField(
                        value = fechaLimite,
                        onValueChange = onFechaChange,
                        label = { Text("Fecha límite") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = onOpenDatePicker) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    format.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return format.format(millis)
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
                colors = topAppBarColors(containerColor = Color.Transparent),
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
                value = viewModel.usernameRegistro,
                onValueChange = viewModel::onUsernameRegistroChange,
                label = { Text("Nombre de usuario") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.nombreRegistro,
                onValueChange = viewModel::onNombreRegistroChange,
                label = { Text("Nombre") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.apellidosRegistro,
                onValueChange = viewModel::onApellidosRegistroChange,
                label = { Text("Apellidos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )
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
                    viewModel.register()
                    viewModel.cleanAllFields()
                          },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Crear cuenta")
                }
            }
            TextButton(
                onClick = {
                    navController.navigate("login")
                    viewModel.cleanAllFields()
                          },
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
                colors = topAppBarColors(containerColor = Color.Transparent),
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
                value = viewModel.usernameLogin,
                onValueChange = viewModel::onUsernameLoginChange,
                label = { Text("Nombre de usuario") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                    viewModel.login()
                          },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Iniciar sesión")
                }
            }
            TextButton(
                onClick = {
                    navController.navigate("registro")
                    viewModel.cleanAllFields()
                          },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}