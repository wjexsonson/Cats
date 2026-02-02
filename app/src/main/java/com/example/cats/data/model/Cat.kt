package com.example.cats.data.model

import com.google.gson.annotations.SerializedName

data class Cat(
    @SerializedName("id") val id: String,
    @SerializedName("url") val url: String,
    var isFavorite: Boolean = false
)
