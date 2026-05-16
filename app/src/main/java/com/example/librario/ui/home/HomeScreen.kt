package com.example.librario.ui.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.librario.domain.model.Book
import com.example.librario.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    onOpenScanner: () -> Unit,
    onSearchClick: () -> Unit,
    onBookClick: (Book) -> Unit
) {
    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    // --- LÓGICA DE BACKUP ---
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.createBackupJson { json ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Backup guardado con éxito", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al guardar el archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                    val json = reader.readText()
                    viewModel.restoreBackupJson(json) { success ->
                        if (success) Toast.makeText(context, "Biblioteca restaurada", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(context, "Formato de archivo inválido", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al abrir el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- ESTADOS REACTIVOS ---
    val bookCount by viewModel.totalOwnedBooks.collectAsState()
    val readingCount by viewModel.readingBooks.collectAsState()
    val readCount by viewModel.readBooks.collectAsState()
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.statusFilter.collectAsState()

    val booksToDisplay by (if (currentTab == 0) viewModel.libraryBooks else viewModel.wishlistBooks)
        .collectAsState(initial = emptyList())

    var showQuickActions by remember { mutableStateOf(false) }
    val filters = listOf("Todos", "Por Leer", "Leyendo", "Leído")

    // --- DIÁLOGO DE ELIMINACIÓN ---
    if (bookToDelete != null) {
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            containerColor = CardColor,
            title = { Text("¿Eliminar libro?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que quieres eliminar \"${bookToDelete?.title}\"? Esta acción no se puede deshacer.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    bookToDelete?.let { viewModel.deleteBook(it) }
                    bookToDelete = null
                    Toast.makeText(context, "Libro eliminado", Toast.LENGTH_SHORT).show()
                }) {
                    Text("ELIMINAR", color = StatRead, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) {
                    Text("CANCELAR", color = TextSecondary)
                }
            }
        )
    }

    // --- CUADRO FLOTANTE DE INFORMACIÓN ---
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = CardColor,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("LIBRARIO", color = TextPrimary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            },
            text = {
                Column {
                    Text("Desarrollado por Matias Tassi", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            showInfoDialog = false
                            createDocumentLauncher.launch("librario_backup.json")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SearchBarBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Exportar Biblioteca")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            showInfoDialog = false
                            openDocumentLauncher.launch(arrayOf("application/json"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp), tint = TextPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Restaurar Backup", color = TextPrimary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("CERRAR", color = GradientPrimaryStart, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- BOTTOM SHEET ---
    if (showQuickActions) {
        ModalBottomSheet(
            onDismissRequest = { showQuickActions = false },
            containerColor = CardColor,
            scrimColor = Color.Black.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ACCIONES RÁPIDAS", color = TextPrimary, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(24.dp))
                QuickActionButton(
                    icon = Icons.Default.Search,
                    title = "Buscar Manual",
                    subtitle = "Introduce el título o autor",
                    onClick = { showQuickActions = false; onSearchClick() }
                )
                Spacer(modifier = Modifier.height(16.dp))
                QuickActionButton(
                    icon = Icons.Default.Add,
                    title = "Escanear",
                    subtitle = "Escanea el código de barras",
                    onClick = { showQuickActions = false; onOpenScanner() }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundGradient)) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showQuickActions = true },
                    shape = CircleShape,
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            brush = Brush.linearGradient(listOf(GradientPrimaryStart, GradientPrimaryEnd)),
                            shape = CircleShape
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = SearchBarBg,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { onTabChange(0) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("MIS LIBROS", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextPrimary,
                            selectedTextColor = TextPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = CardBorder
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { onTabChange(1) },
                        icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null) },
                        label = { Text("DESEADOS", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextPrimary,
                            selectedTextColor = TextPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = CardBorder
                        )
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Mi Biblioteca", style = TitleLargeSerif, color = TextPrimary)
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { showInfoDialog = true }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("TOTAL", bookCount.toString(), StatTotal, Modifier.weight(1f))
                    StatCard("LEYENDO", readingCount.toString(), StatReading, Modifier.weight(1f))
                    StatCard("LEÍDOS", readCount.toString(), StatRead, Modifier.weight(1f))
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar en mi lista...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SearchBarBg,
                        unfocusedContainerColor = SearchBarBg,
                        focusedBorderColor = CardBorder,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (currentTab == 0) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    ) {
                        items(filters) { filter ->
                            val isSelected = selectedFilter == filter
                            FilterChipCustom(
                                text = filter,
                                isSelected = isSelected,
                                onClick = { viewModel.onStatusFilterChange(filter) }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (booksToDisplay.isEmpty()) {
                    EmptyStateView(currentTab)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(booksToDisplay) { book ->
                            BookListItem(
                                book = book, 
                                onClick = { onBookClick(book) },
                                onDelete = { bookToDelete = book }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookListItem(book: Book, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(width = 50.dp, height = 75.dp),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                if (book.coverUrl.isNullOrEmpty()) {
                    Box(Modifier.fillMaxSize().background(SearchBarBg), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = TextSecondary)
                    }
                } else {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (book.status != "Deseado") {
                    Text(
                        text = book.status,
                        color = when(book.status) {
                            "Leído" -> StatRead
                            "Leyendo" -> StatReading
                            else -> TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = StatRead.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FilterChipCustom(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) GradientPrimaryStart.copy(alpha = 0.3f) else SearchBarBg)
            .border(1.dp, if (isSelected) GradientPrimaryStart else Color.Transparent, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (isSelected) TextPrimary else TextSecondary, fontSize = 14.sp)
    }
}

@Composable
fun EmptyStateView(currentTab: Int) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.List, null, modifier = Modifier.size(80.dp), tint = TextSecondary.copy(alpha = 0.5f))
        Text(
            if (currentTab == 0) "Biblioteca vacía" else "Sin deseados",
            fontWeight = FontWeight.Bold, color = TextPrimary
        )
    }
}

@Composable
fun StatCard(title: String, value: String, textColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 24.sp, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SearchBarBg)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = TextPrimary)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}
