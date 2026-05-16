package com.example.librario.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.librario.domain.model.Book

@Database(
    entities = [Book::class], // Lista de todas las tablas
    version = 1 // Si cambiamos la estructura en el futuro, subimos este número
)
@TypeConverters(Converters::class) // Registramos el traductor de fechas
abstract class LibrarioDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao // Exponemos el DAO para que Hilt lo use
}