package com.example.librario.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.librario.domain.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    // Devuelve un flujo de datos. Si la tabla cambia, la UI se entera sola.
    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): Book?

    @Query("SELECT * FROM books WHERE isbn = :isbn")
    suspend fun getBookByIsbn(isbn: String): Book?

    // Si intento insertar un libro que ya existe (mismo ID), lo reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    // Contar total
    @Query("SELECT COUNT(*) FROM books")
    fun getBookCount(): Flow<Int>

    // Contar por estado (Ej: Cuántos "Leído")
    @Query("SELECT COUNT(*) FROM books WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>
}