package com.example.librario.ui.edit

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.librario.domain.model.Book
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookScreen(
    book: Book,
    existingCollections: List<String>,
    onSave: (Book) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    // ESTADOS DE DATOS
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var coverUrl by remember { mutableStateOf(book.coverUrl) }
    var status by remember { mutableStateOf(book.status) }
    var currentPageStr by remember { mutableStateOf(book.currentPage.toString()) }
    var totalPagesStr by remember { mutableStateOf(book.pageCount.toString()) }
    var collection by remember { mutableStateOf(book.collectionName ?: "") }
    var isCollectionExpanded by remember { mutableStateOf(false) }

    // ESTADOS DE UI
    var showFullImageDialog by remember { mutableStateOf(false) } // <--- NUEVO ESTADO PARA ZOOM

    val currentPage = currentPageStr.toIntOrNull() ?: 0
    val totalPages = totalPagesStr.toIntOrNull() ?: 0
    val progress = if (totalPages > 0) currentPage.toFloat() / totalPages else 0f
    val isWishlist = status == "Deseado"

    // --- LÓGICA DE CÁMARA ---
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempPhotoUri != null) {
                coverUrl = tempPhotoUri.toString()
            }
        }
    )

    // --- DIALOG DE IMAGEN PANTALLA COMPLETA ---
    if (showFullImageDialog && coverUrl != null) {
        Dialog(
            onDismissRequest = { showFullImageDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false) // Para que ocupe toda la pantalla
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullImageDialog = false } // Tocar en cualquier lado cierra
            ) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "Portada Pantalla Completa",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit // Que se vea entera sin recortes
                )
                // Botón X para cerrar explícitamente
                IconButton(
                    onClick = { showFullImageDialog = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isWishlist) "Detalle de Deseado" else "Editar Libro") },
                navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.Default.ArrowBack, "Volver") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val updatedBook = book.copy(
                        title = title, author = author, coverUrl = coverUrl, status = status,
                        currentPage = currentPage, pageCount = totalPages, collectionName = collection.ifBlank { null }
                    )
                    onSave(updatedBook)
                },
                icon = { Icon(Icons.Default.Check, "Guardar") }, text = { Text("Guardar") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. ÁREA DE IMAGEN (Clic para agrandar) ---
            Box(
                modifier = Modifier
                    .height(200.dp)
                    // Quitamos el ancho fijo para que se adapte mejor si la imagen es ancha
                    .clip(RoundedCornerShape(8.dp))
                    // SOLO es clickeable para zoom si existe una imagen
                    .clickable(enabled = coverUrl != null) { showFullImageDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (coverUrl != null) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit // Fit para verla entera en el editor
                    )
                    // ¡YA NO HAY CAPA OSCURA NI ÍCONO ACÁ!
                } else {
                    // Placeholder gris si no hay imagen
                    Box(
                        modifier = Modifier
                            .size(130.dp, 200.dp) // Tamaño de libro estándar para el placeholder
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    }
                }
            }

            // --- 2. BOTÓN PARA CAMBIAR/AGREGAR FOTO (Debajo de la imagen) ---
            TextButton(
                onClick = {
                    // La misma lógica de cámara que teníamos antes
                    val uri = createImageFile(context)
                    tempPhotoUri = uri
                    cameraLauncher.launch(uri)
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Cambiamos el ícono y texto según si ya hay foto o no
                Icon(if (coverUrl == null) Icons.Default.Add else Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (coverUrl == null) "Agregar Portada" else "Cambiar Portada")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. CAMPOS DE TEXTO (Igual que antes) ---
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            if (isWishlist) {
                // VISTA DESEADO
                Text("Este libro está en tu lista de deseos.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 24.dp))
                Button(
                    onClick = {
                        val boughtBook = book.copy(title = title, author = author, coverUrl = coverUrl, status = "Por Leer", collectionName = collection.ifBlank { null })
                        onSave(boughtBook)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.ShoppingCart, null)
                    Spacer(Modifier.width(8.dp))
                    Text("¡YA LO COMPRÉ!", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
                CollectionAutocompleteField(collection, { collection = it; isCollectionExpanded = true }, filteredCollections(existingCollections, collection), isCollectionExpanded, { isCollectionExpanded = it }, { collection = it; isCollectionExpanded = false })

            } else {
                // VISTA BIBLIOTECA
                Text("Estado", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FilterChip(selected = status == "Por Leer", onClick = { status = "Por Leer" }, label = { Text("Por Leer") })
                    FilterChip(selected = status == "Leyendo", onClick = { status = "Leyendo" }, label = { Text("Leyendo") })
                    FilterChip(selected = status == "Leído", onClick = { status = "Leído"; currentPageStr = totalPagesStr }, label = { Text("Leído") })
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text("Progreso", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                if (status != "Por Leer") LinearProgressIndicator({ progress }, Modifier.fillMaxWidth().padding(vertical = 16.dp).height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(value = currentPageStr, onValueChange = { if (it.all { c -> c.isDigit() }) currentPageStr = it }, label = { Text("Pág. Actual") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = totalPagesStr, onValueChange = { if (it.all { c -> c.isDigit() }) totalPagesStr = it }, label = { Text("Total") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(24.dp))
                CollectionAutocompleteField(collection, { collection = it; isCollectionExpanded = true }, filteredCollections(existingCollections, collection), isCollectionExpanded, { isCollectionExpanded = it }, { collection = it; isCollectionExpanded = false })
            }
        }
    }
}

// --- HELPERS (Igual que antes) ---
fun createImageFile(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(imageFileName, ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", image)
}

fun filteredCollections(all: List<String>, current: String): List<String> {
    return all.filter { it.contains(current, ignoreCase = true) && it != current }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionAutocompleteField(value: String, onValueChange: (String) -> Unit, suggestions: List<String>, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, onOptionSelected: (String) -> Unit) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text("Colección") }, placeholder = { Text("Ej: Harry Potter") }, modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors())
        if (suggestions.isNotEmpty()) {
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                suggestions.forEach { selectionOption -> DropdownMenuItem(text = { Text(selectionOption) }, onClick = { onOptionSelected(selectionOption) }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding) }
            }
        }
    }
}