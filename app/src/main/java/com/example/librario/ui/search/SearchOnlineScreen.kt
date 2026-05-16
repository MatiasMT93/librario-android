package com.example.librario.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.librario.domain.model.Book
import com.example.librario.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchOnlineScreen(
    viewModel: HomeViewModel,
    onBookSelected: (Book) -> Unit,
    onCreateManual: () -> Unit, // <--- NUEVO CALLBACK
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar en Google Books") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        // BOTÓN FLOTANTE PARA AGREGAR MANUALMENTE
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateManual,
                icon = { Icon(Icons.Default.Create, null) },
                text = { Text("¿No está? Agregar Manual") },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // BARRA DE BÚSQUEDA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("ISBN, Título o Autor") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        isSearching = true
                        viewModel.searchBooksManually(query) { foundBooks ->
                            results = foundBooks
                            isSearching = false
                        }
                    },
                    enabled = query.isNotBlank() && !isSearching
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            }

            // LISTA DE RESULTADOS
            if (results.isEmpty() && !isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Busca libros para agregar.\nSi no lo encontrás, usá el botón de abajo.",
                        color = Color.Gray,
                        modifier = Modifier.padding(32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn {
                    items(results) { book ->
                        OnlineBookItem(book = book, onClick = { onBookSelected(book) })
                    }
                }
            }
        }
    }
}

@Composable
fun OnlineBookItem(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp, 90.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(book.author, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (book.pageCount > 0) "${book.pageCount} pág." else "Páginas desconocidas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}