package com.example.datossinmvvm

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.datossinmvvm.ui.theme.BrightGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var db: UserDatabase
    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dataUser = remember { mutableStateOf("") }

    db = crearDatabase(context)

    val dao = db.userDao()

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {/* (Aqui va un titulo) */ },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrightGreen),
                actions = {
                    Button(
                        onClick = {
                            val user = User(0, firstName, lastName)
                            coroutineScope.launch {
                                AgregarUsuario(user = user, dao = dao)
                            }
                            firstName = ""
                            lastName = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightGreen)
                    ) {
                        Text("Agregar Usuario", fontSize = 16.sp)
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val data = getUsers(dao = dao)
                                dataUser.value = data
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightGreen)
                    ) {
                        Text("Listar Usuarios", fontSize = 16.sp)
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Spacer(Modifier.height(50.dp))
                TextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("ID (solo lectura)") },
                    readOnly = true,
                    singleLine = true
                )
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name: ") },
                    singleLine = true
                )
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name:") },
                    singleLine = true
                )
                Button(
                    onClick = {
                        coroutineScope.launch {
                            EliminarUltimoUsuario(dao = dao)
                            val data = getUsers(dao = dao)
                            dataUser.value = data
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightGreen)
                ) {
                    Text("Eliminar Último Usuario", fontSize = 16.sp)
                }
                Text(
                    text = dataUser.value, fontSize = 20.sp
                )
            }
        }
    )
}

@Composable
fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user_db"
    ).build()
}

suspend fun getUsers(dao: UserDao): String {
    var rpta = ""
    val users = dao.getAll()
    users.forEach { user ->
        val fila = user.firstName + " - " + user.lastName + "\n"
        rpta += fila
    }
    return rpta
}

suspend fun AgregarUsuario(user: User, dao: UserDao) {
    try {
        dao.insert(user)
    } catch (e: Exception) {
        Log.e("User", "Error: insert: ${e.message}")
    }
}

suspend fun EliminarUltimoUsuario(dao: UserDao) {
    try {
        val users = dao.getAll()
        if (users.isNotEmpty()) {
            val lastUser = users.last()
            dao.delete(lastUser)
        }
    } catch (e: Exception) {
        Log.e("User", "Error: delete: ${e.message}")
    }
}