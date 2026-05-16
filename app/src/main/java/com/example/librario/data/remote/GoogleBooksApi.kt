package com.example.librario.data.remote

import com.example.librario.data.remote.dto.GoogleBooksResponse
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApi {

    // 1. Búsqueda en Google Books (AHORA CON TU API KEY)
    @GET("volumes")
    suspend fun getBookByIsbn(
        @Query("q") query: String,
        // NUEVO: Agregamos la llave acá abajo. Pegá tu código entre las comillas.
        @Query("key") apiKey: String = "AIzaSyD8XYMV-CB4ozW-PI2UnW6bKpipN4-Nbv0"
    ): Response<GoogleBooksResponse>

    // 2. Búsqueda en Open Library (Este queda igual, no necesita llave)
    @GET("https://openlibrary.org/api/books")
    suspend fun searchOpenLibrary(
        @Query("bibkeys") isbn: String,
        @Query("jscmd") mode: String = "data",
        @Query("format") format: String = "json"
    ): JsonObject
}