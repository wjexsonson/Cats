package com.example.cats.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_cats")
data class CatEntity(
    @PrimaryKey
    val id: String,
    val url: String
)
