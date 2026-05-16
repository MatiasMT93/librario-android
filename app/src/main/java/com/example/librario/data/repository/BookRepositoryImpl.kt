package com.example.librario.data.repository

import com.example.librario.data.local.BookDao
import com.example.librario.data.remote.GoogleBooksApi
import com.example.librario.data.remote.dto.VolumeInfo // <--- ¡ESTE IMPORT TIENE QUE ESTAR ACÁ ARRIBA!
import com.example.librario.domain.model.Book
import com.example.librario.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val dao: BookDao,
    private val api: GoogleBooksApi
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> = dao.getAllBooks()
    override suspend fun getBookById(id: Long): Book? = dao.getBookById(id)
    override suspend fun insertBook(book: Book) = dao.insertBook(book)
    override suspend fun deleteBook(book: Book) = dao.deleteBook(book)
    override suspend fun updateBook(book: Book) = dao.updateBook(book)
    override fun getBookCount(): Flow<Int> = dao.getBookCount()
    override fun getCountByStatus(status: String): Flow<Int> = dao.getCountByStatus(status)

    // Agregamos 'override' porque esta función está en la Interfaz
    override suspend fun searchBookOnline(isbn: String): Result<Book?> {
        return try {
            // 1. INTENTO CON GOOGLE BOOKS
            val response = api.getBookByIsbn(isbn)

            if (response.isSuccessful && response.body() != null) {
                val googleData = response.body()!!

                if (!googleData.items.isNullOrEmpty()) {
                    // ¡Encontrado en Google!
                    val item = googleData.items[0]
                    // Ahora sí reconoce 'toBook' porque importamos VolumeInfo arriba
                    val book = item.volumeInfo.toBook(isbn)
                    Result.success(book)
                } else {
                    // Google lista vacía -> Open Library
                    searchOpenLibraryFallback(isbn)
                }
            } else {
                // Error Google -> Open Library
                searchOpenLibraryFallback(isbn)
            }
        } catch (e: Exception) {
            // Error Red -> Open Library
            searchOpenLibraryFallback(isbn)
        }
    }

    // --- Función auxiliar para buscar en Open Library ---
    private suspend fun searchOpenLibraryFallback(isbn: String): Result<Book?> {
        return try {
            val key = "ISBN:$isbn"
            // Llamamos a la API
            val response = api.searchOpenLibrary(key)

            if (response.has(key)) {
                val data = response.getAsJsonObject(key)

                val title = if (data.has("title")) data.get("title").asString else "Sin título"

                var author = "Desconocido"
                if (data.has("authors")) {
                    val authorsArray = data.getAsJsonArray("authors")
                    if (authorsArray.size() > 0) {
                        author = authorsArray[0].asJsonObject.get("name").asString
                    }
                }

                var coverUrl: String? = null
                if (data.has("cover")) {
                    val coverObj = data.getAsJsonObject("cover")
                    if (coverObj.has("large")) coverUrl = coverObj.get("large").asString
                    else if (coverObj.has("medium")) coverUrl = coverObj.get("medium").asString
                }

                val pages = if (data.has("number_of_pages")) data.get("number_of_pages").asInt else 0

                val book = Book(
                    isbn = isbn,
                    title = title,
                    author = author,
                    description = "Importado desde Open Library",
                    coverUrl = coverUrl,
                    pageCount = pages,
                    status = "Por Leer"
                )
                Result.success(book)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.success(null)
        }
    }

    override suspend fun searchBooksByQuery(query: String): Result<List<Book>> {
        return try {
            val response = api.getBookByIsbn(query)

            if (response.isSuccessful) {
                val googleResult = response.body()
                if (googleResult != null && !googleResult.items.isNullOrEmpty()) {

                    val books = googleResult.items.map { item ->
                        // Reutilizamos la función toBook para no repetir código
                        // Usamos item.id como ISBN provisional si no viene en la query
                        item.volumeInfo.toBook(item.id ?: "SIN_ID")
                    }
                    Result.success(books)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("Error Google: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// --- EXTENSIONES DE MAPEO ---
// Como ya importamos VolumeInfo arriba del todo, esto funciona perfecto aquí abajo.
fun VolumeInfo.toBook(isbn: String): Book {
    return Book(
        isbn = isbn,
        title = this.title ?: "Sin título",
        author = if (!this.authors.isNullOrEmpty()) this.authors[0] else "Desconocido",
        description = this.description ?: "Sin descripción",
        coverUrl = this.imageLinks?.thumbnail?.replace("http:", "https:"),
        pageCount = this.pageCount ?: 0,
        status = "Por Leer"
    )
}