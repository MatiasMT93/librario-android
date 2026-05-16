package com.example.librario.data.remote.dto

import com.google.gson.annotations.SerializedName

// 1. La respuesta raíz (La caja grande)
data class GoogleBooksResponse(
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("items") val items: List<GoogleBookItem>? = null
)

// 2. Cada ítem de la lista
data class GoogleBookItem(
    @SerializedName("id") val id: String,
    @SerializedName("volumeInfo") val volumeInfo: VolumeInfo
)

// 3. La información real del libro (Título, Autores, etc)
data class VolumeInfo(
    @SerializedName("title") val title: String,
    @SerializedName("authors") val authors: List<String>? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("pageCount") val pageCount: Int? = 0,
    @SerializedName("imageLinks") val imageLinks: ImageLinks? = null
)

// 4. Las portadas
data class ImageLinks(
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("smallThumbnail") val smallThumbnail: String?
)