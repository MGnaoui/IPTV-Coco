package com.iptvcoco.app.repository

import android.content.Context
import com.iptvcoco.app.model.Category
import com.iptvcoco.app.model.Channel
import com.iptvcoco.app.model.ContentType
import com.iptvcoco.app.model.M3UAccount
import com.iptvcoco.app.model.Movie
import com.iptvcoco.app.model.Season
import com.iptvcoco.app.model.Series
import com.iptvcoco.app.parser.M3UParser
import com.iptvcoco.app.util.AppLogger
import com.iptvcoco.app.util.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class IPTVRepository(context: Context) {
    private val prefs = PreferencesManager(context)
    private val cacheFile = File(context.cacheDir, "playlist.m3u")
    private val jsonCacheFile = File(context.cacheDir, "playlist.json")
    private val gson = com.google.gson.Gson()

    private var parsedPlaylist: M3UParser.ParsedPlaylist? = null

    init {
        cleanupOldCaches()
        if (isLoggedIn()) {
            // Try M3U cache first
            if (cacheFile.exists()) {
                try {
                    val content = cacheFile.readText()
                    parsedPlaylist = M3UParser.parsePlaylist(content)
                } catch (_: Exception) {
                    cacheFile.delete()
                }
            }
            // Fallback to JSON cache (Xtream)
            if (parsedPlaylist == null && jsonCacheFile.exists()) {
                try {
                    val json = jsonCacheFile.readText()
                    parsedPlaylist = gson.fromJson(json, M3UParser.ParsedPlaylist::class.java)
                } catch (_: Exception) {
                    jsonCacheFile.delete()
                }
            }
        }
    }

    private fun cleanupOldCaches() {
        val maxAgeMs = 7L * 24 * 60 * 60 * 1000 // 7 days
        val maxSizeBytes = 100L * 1024 * 1024 // 100 MB
        val now = System.currentTimeMillis()

        listOf(cacheFile, jsonCacheFile).forEach { file ->
            if (file.exists()) {
                val age = now - file.lastModified()
                val size = file.length()
                if (age > maxAgeMs || size > maxSizeBytes) {
                    file.delete()
                }
            }
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

    fun getLastRefresh(): Long = prefs.getLastRefresh()

    fun hasPlaylistData(): Boolean {
        return parsedPlaylist != null &&
                (parsedPlaylist?.channels?.isNotEmpty() == true ||
                        parsedPlaylist?.movies?.isNotEmpty() == true ||
                        parsedPlaylist?.series?.isNotEmpty() == true)
    }

    fun logout() {
        prefs.clearAll()
        parsedPlaylist = null
        cacheFile.delete()
        jsonCacheFile.delete()
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

    fun getSeriesInfo(seriesId: String): Series? {
        val series = getSeriesById(seriesId) ?: return null
        // M3U playlists already have seasons populated
        if (series.seasons.isNotEmpty()) return series

        val account = getAccount() ?: return series
        return if (account.type == M3UAccount.AccountType.XTREAM) {
            try {
                val xtreamSeriesId = seriesId.removePrefix("ser_")
                val seasons = com.iptvcoco.app.parser.XtreamParser.fetchSeriesInfo(
                    account.url.trim().trimEnd('/'),
                    account.username ?: "",
                    account.password ?: "",
                    xtreamSeriesId
                )
                series.copy(seasons = seasons)
            } catch (e: Exception) {
                AppLogger.logEvent("Failed to fetch series info for $seriesId: ${e.message}")
                series
            }
        } else {
            series
        }
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

    suspend fun login(account: M3UAccount): Result<M3UParser.ParsedPlaylist> = withContext(Dispatchers.IO) {
        val maxRetries = 3
        val errors = mutableListOf<String>()

        repeat(maxRetries) { attempt ->
            val result = when (account.type ?: M3UAccount.AccountType.M3U) {
                M3UAccount.AccountType.M3U -> loginM3U(account)
                M3UAccount.AccountType.XTREAM -> loginXtream(account)
            }

            if (result.isSuccess) {
                AppLogger.logEvent("Login succeeded on attempt ${attempt + 1} (${account.type})")
                return@withContext result
            }

            val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
            errors.add("Attempt ${attempt + 1}/$maxRetries: $errorMsg")
            AppLogger.logEvent("Login attempt ${attempt + 1}/$maxRetries failed (${account.type}): $errorMsg")

            if (attempt < maxRetries - 1) {
                delay(1500)
            }
        }

        val summary = errors.joinToString("\n")
        AppLogger.logEvent("Login failed after $maxRetries attempts.\n$summary")
        Result.failure(Exception("Not connected after $maxRetries attempts.\n$summary"))
    }

    private fun loginM3U(account: M3UAccount): Result<M3UParser.ParsedPlaylist> {
        val errors = mutableListOf<String>()
        val urlsToTry = buildUrls(account)
        for (urlStr in urlsToTry) {
            try {
                val content = fetchM3UContent(urlStr, 30000)
                cacheFile.writeText(content)
                val playlist = M3UParser.parsePlaylist(content)
                parsedPlaylist = playlist
                prefs.saveAccount(account)
                prefs.saveLastRefresh(System.currentTimeMillis())
                return Result.success(playlist)
            } catch (t: Throwable) {
                errors.add("• ${t.message}")
            }
        }
        val summary = errors.joinToString("\n")
        return Result.failure(Exception("Tried ${urlsToTry.size} URL(s):\n$summary"))
    }

    private fun loginXtream(account: M3UAccount): Result<M3UParser.ParsedPlaylist> {
        return try {
            var baseUrl = account.url.trim()
            if (baseUrl.endsWith("/")) baseUrl = baseUrl.dropLast(1)
            val playlist = com.iptvcoco.app.parser.XtreamParser.parseXtreamPlaylist(
                baseUrl,
                account.username ?: "",
                account.password ?: ""
            )
            parsedPlaylist = playlist
            // Save as JSON cache for instant restart
            try {
                jsonCacheFile.writeText(gson.toJson(playlist))
            } catch (_: Exception) { }
            prefs.saveAccount(account)
            prefs.saveLastRefresh(System.currentTimeMillis())
            Result.success(playlist)
        } catch (t: Throwable) {
            Result.failure(Exception("Xtream login failed: ${t.message}"))
        }
    }

    private fun fetchXtreamM3uUrl(account: M3UAccount): String? {
        var baseUrl = account.url.trim()
        if (baseUrl.endsWith("/")) baseUrl = baseUrl.dropLast(1)

        val apiUrl = "$baseUrl/player_api.php?username=${account.username}&password=${account.password}"
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "IPTVCoco/1.0")
        connection.instanceFollowRedirects = true
        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw Exception("HTTP $responseCode")
            }
            val content = connection.inputStream.bufferedReader().use { it.readText() }
            // Parse JSON to find m3u_url
            val gson = com.google.gson.Gson()
            val json = gson.fromJson(content, com.google.gson.JsonObject::class.java)
            val userInfo = json.getAsJsonObject("user_info")
            val m3uUrl = userInfo?.get("m3u_url")?.asString
                ?: userInfo?.get("m3u8_url")?.asString
                ?: userInfo?.get("output")?.asString
            return m3uUrl
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchM3UContent(urlStr: String, timeoutMs: Int = 15000): String {
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = timeoutMs
        connection.readTimeout = timeoutMs
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "IPTVCoco/1.0")
        connection.instanceFollowRedirects = true
        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw Exception("HTTP $responseCode for URL: $urlStr")
            }
            val content = connection.inputStream.bufferedReader().use { it.readText() }
            if (!content.trim().startsWith("#EXTM3U") && !content.trim().startsWith("#EXTINF")) {
                val preview = content.take(200).replace("\n", " ")
                throw Exception("Invalid M3U from: $urlStr | Preview: $preview")
            }
            return content
        } finally {
            connection.disconnect()
        }
    }

    private fun buildUrls(account: M3UAccount): List<String> {
        var baseUrl = account.url.trim()
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.dropLast(1)
        }

        // If URL already looks like a direct M3U or API URL, use it as-is
        val looksLikeM3u = baseUrl.contains(".m3u") || baseUrl.contains(".m3u8") || baseUrl.contains("get.php") || baseUrl.contains("player_api.php")

        if (looksLikeM3u) {
            // Just replace placeholders
            var directUrl = baseUrl
            if (!account.username.isNullOrBlank()) {
                directUrl = directUrl.replace("USERNAME", account.username).replace("{username}", account.username)
            }
            if (!account.password.isNullOrBlank()) {
                directUrl = directUrl.replace("PASSWORD", account.password).replace("{password}", account.password)
            }
            return listOf(directUrl)
        }

        // Otherwise, construct Xtream Codes style URLs
        val urls = mutableListOf<String>()
        if (!account.username.isNullOrBlank() && !account.password.isNullOrBlank()) {
            urls.add("$baseUrl/get.php?username=${account.username}&password=${account.password}&type=m3u_plus&output=ts")
            urls.add("$baseUrl/get.php?username=${account.username}&password=${account.password}&type=m3u&output=ts")
            urls.add("$baseUrl/get.php?username=${account.username}&password=${account.password}&type=m3u_plus")
            urls.add("$baseUrl/get.php?username=${account.username}&password=${account.password}&type=m3u")
            urls.add("$baseUrl/player_api.php?username=${account.username}&password=${account.password}&action=get_series")
        }
        // Fallback: try base URL with credentials appended as generic query params
        urls.add("$baseUrl?username=${account.username}&password=${account.password}")
        return urls
    }
}
