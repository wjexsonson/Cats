package com.example.cats.di

import android.content.Context
import androidx.room.Room
import com.example.cats.data.db.CatDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideAppDatabase(): CatDatabase {
        return Room.databaseBuilder(
            context,
            CatDatabase::class.java,
            "cats_database"
        ).build()
    }

    @Provides
    fun provideCatDao(database: CatDatabase) = database.catDao()
}
