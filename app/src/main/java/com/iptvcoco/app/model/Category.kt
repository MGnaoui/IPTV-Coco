package com.iptvcoco.app.model

data class Category(
    val id: String,
    val name: String,
    val type: ContentType
)

enum class ContentType {
    LIVE, MOVIE, SERIES
}
