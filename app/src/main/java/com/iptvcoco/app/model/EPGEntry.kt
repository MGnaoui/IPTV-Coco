package com.iptvcoco.app.model

data class EPGEntry(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val description: String = ""
)
