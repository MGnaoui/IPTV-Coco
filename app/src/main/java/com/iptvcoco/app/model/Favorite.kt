package com.iptvcoco.app.model

sealed class Favorite {
    abstract val id: String
    abstract val name: String

    data class ChannelFavorite(
        override val id: String,
        override val name: String,
        val channel: Channel
    ) : Favorite()

    data class MovieFavorite(
        override val id: String,
        override val name: String,
        val movie: Movie
    ) : Favorite()

    data class SeriesFavorite(
        override val id: String,
        override val name: String,
        val series: Series
    ) : Favorite()
}
