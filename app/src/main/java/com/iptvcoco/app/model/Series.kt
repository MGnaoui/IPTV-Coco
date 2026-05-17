package com.iptvcoco.app.model

data class Series(
    val id: String,
    val title: String,
    val poster: String? = null,
    val banner: String? = null,
    val categoryId: String,
    val categoryName: String,
    val rating: String = "",
    val plot: String = "",
    val cast: String = "",
    val year: String = "",
    val seasons: List<Season> = emptyList()
)
