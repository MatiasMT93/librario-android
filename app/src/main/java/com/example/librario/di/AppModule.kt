package com.example.librario.di

import android.app.Application
import androidx.room.Room
import com.example.librario.data.local.BookDao
import com.example.librario.data.local.LibrarioDatabase
import com.example.librario.data.remote.GoogleBooksApi // <--- Importante
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 1. Proveer la Base de Datos (Seguro esta o la de abajo faltaban)
    @Provides
    @Singleton
    fun provideDatabase(app: Application): LibrarioDatabase {
        return Room.databaseBuilder(
            app,
            LibrarioDatabase::class.java,
            "librario_db"
        ).build()
    }

    // 2. Proveer el DAO (El ingrediente que Hilt te está reclamando)
    @Provides
    @Singleton
    fun provideBookDao(db: LibrarioDatabase): BookDao {
        return db.bookDao()
    }

    // 3. Proveer la API de Google (Internet)
    @Provides
    @Singleton
    fun provideGoogleBooksApi(): GoogleBooksApi {
        return retrofit2.Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(GoogleBooksApi::class.java)
    }

    // 4. Proveer el Repositorio (Junta el DAO y la API)
    @Provides
    @Singleton
    fun provideBookRepository(
        dao: BookDao,
        api: GoogleBooksApi
    ): com.example.librario.domain.repository.BookRepository {
        return com.example.librario.data.repository.BookRepositoryImpl(dao, api)
    }
}