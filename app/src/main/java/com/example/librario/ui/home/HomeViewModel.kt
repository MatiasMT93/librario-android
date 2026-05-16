package com.example.librario.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.librario.domain.model.Book
import com.example.librario.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val TAG = "LIBRARIO_DEBUG"

    // Estados de Carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 1. BUSCADOR DE TEXTO
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // 2. NUEVO: FILTRO DE ESTADO (Por defecto "Todos")
    private val _statusFilter = MutableStateFlow("Todos")
    val statusFilter = _statusFilter.asStateFlow()

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    // Función para cambiar el chip seleccionado
    fun onStatusFilterChange(status: String) { _statusFilter.value = status }

    // Obtenemos todos los libros
    private val allBooksFlow = repository.getAllBooks()

    // --- LÓGICA MAESTRA DE FILTRADO ---
    // Combinamos: (Lista Completa) + (Texto Escrito) + (Estado Seleccionado)
    val libraryBooks = combine(allBooksFlow, _searchQuery, _statusFilter) { list, query, status ->

        // A. Primero separamos los "Míos" de los "Deseados"
        var myBooks = list.filter { it.status != "Deseado" }

        // B. Aplicamos Filtro de Estado (Si no es "Todos")
        if (status != "Todos") {
            myBooks = myBooks.filter { it.status == status }
        }

        // C. Aplicamos Buscador de Texto (Si escribió algo)
        if (query.isNotBlank()) {
            myBooks = myBooks.filter { book ->
                book.title.contains(query, ignoreCase = true) ||
                        book.author.contains(query, ignoreCase = true) ||
                        (book.collectionName?.contains(query, ignoreCase = true) == true)
            }
        }

        myBooks // Devolvemos la lista ya filtrada y limpia
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Los deseados siguen igual (solo filtro por texto)
    val wishlistBooks = combine(allBooksFlow, _searchQuery) { list, query ->
        val wishBooks = list.filter { it.status == "Deseado" }
        if (query.isBlank()) wishBooks else wishBooks.filter {
            it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCollections = allBooksFlow.map { list ->
        list.mapNotNull { it.collectionName } // Tomamos solo los nombres
            .filter { it.isNotBlank() }       // Sacamos los vacíos
            .distinct()                       // Sacamos duplicados
            .sorted()                         // Ordenamos alfabéticamente
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ESTADÍSTICAS (Siempre muestran los totales reales, sin importar el filtro) ---
    val totalOwnedBooks = allBooksFlow.map { list -> list.count { it.status != "Deseado" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val readBooks = repository.getCountByStatus("Leído")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val readingBooks = repository.getCountByStatus("Leyendo")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- FUNCIONES ---
    // Ahora acepta un callback que devuelve TRUE (encontrado) o FALSE (no encontrado)
    fun fetchAndSaveBook(isbn: String, targetStatus: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.searchBookOnline(isbn)

                result.onSuccess { book ->
                    if (book != null) {
                        // ¡ÉXITO! Lo guardamos
                        val bookToSave = book.copy(status = targetStatus)
                        repository.insertBook(bookToSave)
                        onResult(true) // Avisamos que salió bien
                    } else {
                        // Google respondió, pero sin libros
                        onResult(false) // Avisamos que falló
                    }
                }

                result.onFailure {
                    // Error de conexión o API
                    onResult(false)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchBooksManually(query: String, onResult: (List<Book>) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.searchBooksByQuery(query)
            _isLoading.value = false
            result.onSuccess { list -> onResult(list) }
            result.onFailure { onResult(emptyList()) }
        }
    }

    fun saveBookManually(book: Book) = viewModelScope.launch { repository.insertBook(book) }
    fun updateBook(book: Book) = viewModelScope.launch { repository.updateBook(book) }
    fun deleteBook(book: Book) = viewModelScope.launch { repository.deleteBook(book) }
    // 1. EXPORTAR: Devuelve un String con todos los libros en formato JSON
    fun createBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            // Obtenemos la lista actual SIN filtros (la lista cruda)
            val allBooks = repository.getAllBooks().first() // .first() toma una instantánea actual

            // Convertimos a JSON usando GSON
            val gson = com.google.gson.Gson()
            val jsonString = gson.toJson(allBooks)

            onResult(jsonString)
        }
    }

    // 2. IMPORTAR: Recibe el texto JSON y guarda los libros
    fun restoreBackupJson(jsonString: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val gson = com.google.gson.Gson()
                // Convertimos el texto de vuelta a una lista de libros
                val typeToken = object : com.google.gson.reflect.TypeToken<List<Book>>() {}.type
                val books: List<Book> = gson.fromJson(jsonString, typeToken)

                // Guardamos uno por uno (o podrías hacer un insertAll en el repo)
                books.forEach { book ->
                    repository.insertBook(book)
                }
                onFinished(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onFinished(false)
            }
        }
    }

    // Funciones para las tarjetas de estadísticas de la Home
    fun getBookCount() = repository.getBookCount()

    fun getCountByStatus(status: String) = repository.getCountByStatus(status)

    fun getAllBooks() = repository.getAllBooks()

}