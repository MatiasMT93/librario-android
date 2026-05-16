package com.example.librario

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.librario.domain.model.Book
import com.example.librario.ui.camera.CameraScreen
import com.example.librario.ui.edit.EditBookScreen
import com.example.librario.ui.home.HomeScreen
import com.example.librario.ui.home.HomeViewModel
import com.example.librario.ui.search.SearchOnlineScreen
import com.example.librario.ui.theme.LibrarioTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LibrarioTheme {
                val viewModel: HomeViewModel = hiltViewModel()
                val context = LocalContext.current

                // --- DATOS ---
                val collections by viewModel.availableCollections.collectAsState()

                // --- ESTADOS DE NAVEGACIÓN ---
                var currentTab by remember { mutableStateOf(0) }
                var showCamera by remember { mutableStateOf(false) }
                var showSearch by remember { mutableStateOf(false) }
                var editingBook by remember { mutableStateOf<Book?>(null) }

                // --- ESTADOS PARA "LIBRO NO ENCONTRADO" ---
                var showNotFoundDialog by remember { mutableStateOf(false) }
                var pendingIsbn by remember { mutableStateOf("") } // Guardamos el código que falló

                // --- NAVEGACIÓN ---

                if (showNotFoundDialog) {
                    // CARTEL DE ALERTA
                    // CARTEL DE ALERTA
                    AlertDialog(
                        onDismissRequest = { showNotFoundDialog = false },
                        title = { Text("Libro no encontrado") },
                        text = { Text("No pudimos encontrar información de este libro online.\n\n¿Querés agregarlo manualmente?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showNotFoundDialog = false

                                val initialStatus = if (currentTab == 1) "Deseado" else "Por Leer"

                                // --- AQUÍ ESTABA EL ERROR ---
                                val newEmptyBook = Book(
                                    isbn = pendingIsbn,
                                    title = "",
                                    author = "",
                                    description = "Agregado manualmente",
                                    status = initialStatus,

                                    // Agregamos los campos que faltaban:
                                    coverUrl = null,  // No tiene tapa todavía
                                    pageCount = 0     // No sabemos las páginas
                                )

                                editingBook = newEmptyBook
                            }) {
                                Text("Sí, agregar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showNotFoundDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                if (showSearch) {
                    // PANTALLA 1: BÚSQUEDA ONLINE
                    BackHandler { showSearch = false }
                    SearchOnlineScreen(
                        viewModel = viewModel,
                        onBack = { showSearch = false },

                        // CASO A: ENCONTRÓ EL LIBRO Y LO SELECCIONÓ
                        onBookSelected = { newBook ->
                            val statusToSave = if (currentTab == 1) "Deseado" else "Por Leer"
                            val bookToSave = newBook.copy(status = statusToSave)
                            viewModel.saveBookManually(bookToSave)

                            val mensaje = if (currentTab == 1) "¡Agregado a Deseados!" else "¡Agregado a Biblioteca!"
                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                            showSearch = false
                        },

                        // CASO B: BOTÓN "AGREGAR MANUALMENTE" (NUEVO)
                        onCreateManual = {
                            // 1. Cerramos búsqueda
                            showSearch = false

                            // 2. Definimos estado inicial
                            val initialStatus = if (currentTab == 1) "Deseado" else "Por Leer"

                            // 3. Creamos libro vacío (con ID temporal único)
                            val newEmptyBook = Book(
                                isbn = "MANUAL_${System.currentTimeMillis()}", // ID único temporal
                                title = "",
                                author = "",
                                description = "",
                                status = initialStatus,
                                coverUrl = null,
                                pageCount = 0
                            )

                            // 4. Abrimos editor
                            editingBook = newEmptyBook
                        }
                    )

                } else if (showCamera) {
                    // PANTALLA 2: CÁMARA
                    BackHandler { showCamera = false }
                    CameraScreen(
                        onCodeScanned = { code ->
                            Toast.makeText(context, "Buscando...", Toast.LENGTH_SHORT).show()

                            val statusDestino = if (currentTab == 1) "Deseado" else "Por Leer"

                            // Llamamos a la función con el callback de resultado
                            viewModel.fetchAndSaveBook(code, statusDestino) { success ->
                                if (success) {
                                    // Si lo encontró, cerramos cámara y listo
                                    showCamera = false
                                } else {
                                    // SI FALLÓ:
                                    // 1. Cerramos la cámara
                                    showCamera = false
                                    // 2. Guardamos el código para usarlo después
                                    pendingIsbn = code
                                    // 3. Mostramos el diálogo de error
                                    showNotFoundDialog = true
                                }
                            }
                        }
                    )

                } else if (editingBook != null) {
                    // PANTALLA 3: EDITOR / DETALLE
                    BackHandler { editingBook = null }
                    EditBookScreen(
                        book = editingBook!!,
                        existingCollections = collections,
                        onCancel = { editingBook = null },
                        onSave = { updatedBook ->
                            // Aquí detectamos si es un libro nuevo (insert) o viejo (update)
                            // Como Room usa 'OnConflictStrategy.REPLACE', insertBook funciona para ambos casos si el ID (ISBN) es el mismo.
                            // Pero para ser prolijos, usamos updateBook si ya existía.
                            // En tu caso simple, viewModel.updateBook o saveBookManually hacen lo mismo.

                            viewModel.saveBookManually(updatedBook) // Usamos save para asegurar que se cree si no existe
                            Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
                            editingBook = null
                        }
                    )

                } else {
                    // PANTALLA 4: HOME
                    if (currentTab == 1) {
                        BackHandler { currentTab = 0 }
                    }
                    HomeScreen(
                        viewModel = viewModel,
                        currentTab = currentTab,
                        onTabChange = { currentTab = it },
                        onOpenScanner = { showCamera = true },
                        onSearchClick = { showSearch = true },
                        onBookClick = { book -> editingBook = book }
                    )
                }
            }
        }
    }
}