package com.iptvcoco.app.repository

import android.content.Context
import com.iptvcoco.app.model.Category
import com.iptvcoco.app.model.Channel
import com.iptvcoco.app.model.ContentType
import com.iptvcoco.app.model.M3UAccount
import com.iptvcoco.app.model.Movie
import com.iptvcoco.app.model.Series
import com.iptvcoco.app.parser.M3UParser
import com.iptvcoco.app.util.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class IPTVRepository(context: Context) {
    private val prefs = PreferencesManager(context)
    private val cacheFile = File(context.cacheDir, "playlist.m3u")

    private var parsedPlaylist: M3UParser.ParsedPlaylist? = null

    init {
        if (isLoggedIn() && cacheFile.exists()) {
            try {
                val content = cacheFile.readText()
                parsedPlaylist = M3UParser.parsePlaylist(content)
            } catch (_: Exception) {
                // ignore parse errors on init
            }
        }
    }

    suspend fun login(account: M3UAccount): Result<M3UParser.ParsedPlaylist> = withContext(Dispatchers.IO) {
        try {
            val content = fetchM3UContent(account)
            cacheFile.writeText(content)
            val playlist = M3UParser.parsePlaylist(content)
            parsedPlaylist = playlist
            prefs.saveAccount(account)
            Result.success(playlist)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reload(): Result<M3UParser.ParsedPlaylist> = withContext(Dispatchers.IO) {
        val account = prefs.getAccount()
        if (account != null) {
            login(account)
        } else {
            Result.failure(Exception("Not logged in"))
        }
    }

    fun isLoggedIn(): Boolean = prefs.getAccount() != null

    fun getAccount(): M3UAccount? = prefs.getAccount()

    fun logout() {
        prefs.clearAll()
        parsedPlaylist = null
        cacheFile.delete()
    }

    fun getCategories(type: ContentType): List<Category> {
        return when (type) {
            ContentType.LIVE -> parsedPlaylist?.liveCategories ?: emptyList()
            ContentType.MOVIE -> parsedPlaylist?.movieCategories ?: emptyList()
            ContentType.SERIES -> parsedPlaylist?.seriesCategories ?: emptyList()
        }
    }

    fun getChannels(categoryId: String? = null): List<Channel> {
        val all = parsedPlaylist?.channels ?: emptyList()
        return if (categoryId != null) all.filter { it.categoryId == categoryId } else all
    }

    fun getMovies(categoryId: String? = null): List<Movie> {
        val all = parsedPlaylist?.movies ?: emptyList()
        return if (categoryId != null) all.filter { it.categoryId == categoryId } else all
    }

    fun getSeries(categoryId: String? = null): List<Series> {
        val all = parsedPlaylist?.series ?: emptyList()
        return if (categoryId != null) all.filter { it.categoryId == categoryId } else all
    }

    fun getChannelById(id: String): Channel? {
        return parsedPlaylist?.channels?.find { it.id == id }
    }

    fun getMovieById(id: String): Movie? {
        return parsedPlaylist?.movies?.find { it.id == id }
    }

    fun getSeriesById(id: String): Series? {
        return parsedPlaylist?.series?.find { it.id == id }
    }

    fun searchChannels(query: String, categoryId: String? = null): List<Channel> {
        return getChannels(categoryId).filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    fun searchMovies(query: String, categoryId: String? = null): List<Movie> {
        return getMovies(categoryId).filter {
            it.title.contains(query, ignoreCase = true)
        }
    }

    fun searchSeries(query: String, categoryId: String? = null): List<Series> {
        return getSeries(categoryId).filter {
            it.title.contains(query, ignoreCase = true)
        }
    }

    fun searchCategories(query: String, type: ContentType): List<Category> {
        return getCategories(type).filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    fun getFavoriteChannels(): List<Channel> {
        val favIds = prefs.getFavoriteChannels()
        return parsedPlaylist?.channels?.filter { favIds.contains(it.id) } ?: emptyList()
    }

    fun getFavoriteMovies(): List<Movie> {
        val favIds = prefs.getFavoriteMovies()
        return parsedPlaylist?.movies?.filter { favIds.contains(it.id) } ?: emptyList()
    }

    fun getFavoriteSeries(): List<Series> {
        val favIds = prefs.getFavoriteSeries()
        return parsedPlaylist?.series?.filter { favIds.contains(it.id) } ?: emptyList()
    }

    fun toggleFavoriteChannel(channelId: String) {
        if (prefs.isChannelFavorite(channelId)) {
            prefs.removeFavoriteChannel(channelId)
        } else {
            prefs.addFavoriteChannel(channelId)
        }
    }

    fun toggleFavoriteMovie(movieId: String) {
        if (prefs.isMovieFavorite(movieId)) {
            prefs.removeFavoriteMovie(movieId)
        } else {
            prefs.addFavoriteMovie(movieId)
        }
    }

    fun toggleFavoriteSeries(seriesId: String) {
        if (prefs.isSeriesFavorite(seriesId)) {
            prefs.removeFavoriteSeries(seriesId)
        } else {
            prefs.addFavoriteSeries(seriesId)
        }
    }

    fun isFavoriteChannel(channelId: String): Boolean = prefs.isChannelFavorite(channelId)
    fun isFavoriteMovie(movieId: String): Boolean = prefs.isMovieFavorite(movieId)
    fun isFavoriteSeries(seriesId: String): Boolean = prefs.isSeriesFavorite(seriesId)

    fun saveResumePosition(contentId: String, position: Long) {
        prefs.saveResumePosition(contentId, position)
    }

    fun getResumePosition(contentId: String): Long {
        return prefs.getResumePosition(contentId)
    }

    private fun fetchM3UContent(account: M3UAccount): String {
        var urlStr = account.url
        if (!account.username.isNullOrBlank()) {
            urlStr = urlStr.replace("USERNAME", account.username)
                .replace("{username}", account.username)
        }
        if (!account.password.isNullOrBlank()) {
            urlStr = urlStr.replace("PASSWORD", account.password)
                .replace("{password}", account.password)
        }
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.requestMethod = "GET"
        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw Exception("Server returned HTTP $responseCode")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
