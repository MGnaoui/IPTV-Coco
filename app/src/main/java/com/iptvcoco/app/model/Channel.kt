package com.iptvcoco.app.model

data class Channel(
    val id: String,
    val name: String,
    val logo: String? = null,
    val streamUrl: String,
    val categoryId: String,
    val categoryName: String,
    val epg: List<EPGEntry> = emptyList()
)
