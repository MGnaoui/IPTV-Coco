package com.iptvcoco.app.model

data class Movie(
    val id: String,
    val title: String,
    val poster: String? = null,
    val banner: String? = null,
    val streamUrl: String,
    val categoryId: String,
    val categoryName: String,
    val runtime: String = "",
    val rating: String = "",
    val plot: String = "",
    val cast: String = "",
    val year: String = "",
    val resumePosition: Long = 0
)
