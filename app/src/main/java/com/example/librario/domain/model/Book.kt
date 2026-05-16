package com.example.librario.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "books") // 1. Definimos el nombre de la tabla
data class Book(
    @PrimaryKey(autoGenerate = true) // 2. El ID se autogenera (1, 2, 3...)
    val id: Long = 0,

    val isbn: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val description: String = "",
    val pageCount: Int = 0,

    val isRead: Boolean = false,
    val isReading: Boolean = false,
    val currentPage: Int = 0,

    val dateAdded: Date = Date(),

    val status: String = "Por Leer", // Valores: "Por Leer", "Leyendo", "Leído"
    val collectionName: String? = null // Ej: "Harry Potter", "El Señor de los Anillos"
)