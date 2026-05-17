package com.iptvcoco.app.model

data class Episode(
    val id: String,
    val title: String,
    val number: Int,
    val streamUrl: String,
    val runtime: String = "",
    val plot: String = "",
    val resumePosition: Long = 0
)
