package com.example.cats.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CatEntity::class], version = 1, exportSchema = false)
abstract class CatDatabase : RoomDatabase() {
    abstract fun catDao(): CatDao
}
