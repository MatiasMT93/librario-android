package com.example.librario.domain.repository

import com.example.librario.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    // Observar todos los libros (Flow actualiza la UI automáticamente si algo cambia)
    fun getAllBooks(): Flow<List<Book>>

    // Operaciones CRUD
    suspend fun getBookById(id: Long): Book?
    suspend fun insertBook(book: Book)
    suspend fun deleteBook(book: Book)
    // Devuelve un Libro (puede ser nulo si no existe en internet ni en local)
    // Usamos Result para manejar errores de red (sin internet, timeout, etc)
    suspend fun searchBookOnline(isbn: String): Result<Book?>

    suspend fun updateBook(book: Book)
    fun getBookCount(): Flow<Int>
    fun getCountByStatus(status: String): Flow<Int>

    suspend fun searchBooksByQuery(query: String): Result<List<Book>>

}