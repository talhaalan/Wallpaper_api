package com.tknsoftwarestudio.wallpaperapi.models

data class Photo (
    val id: String,
    val created_at: String,
    val width: Int,
    val height: Int,
    val color: String? = "#000000",
    val likes: Int,
    val description: String?,
    val alt_description: String?,
    val urls: Urls,
    //val links: Links,
    val user: User)
