package com.iptvcoco.app.parser

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.iptvcoco.app.model.Category
import com.iptvcoco.app.model.Channel
import com.iptvcoco.app.model.ContentType
import com.iptvcoco.app.model.Episode
import com.iptvcoco.app.model.Movie
import com.iptvcoco.app.model.Season
import com.iptvcoco.app.model.Series
import java.net.HttpURLConnection
import java.net.URL

object XtreamParser {

    private val gson = Gson()

    fun parseXtreamPlaylist(baseUrl: String, username: String, password: String): M3UParser.ParsedPlaylist {
        val liveCats = fetchLiveCategories(baseUrl, username, password)
        val liveStreams = fetchLiveStreams(baseUrl, username, password)
        val vodCats = fetchVodCategories(baseUrl, username, password)
        val vodStreams = fetchVodStreams(baseUrl, username, password)
        val seriesCats = fetchSeriesCategories(baseUrl, username, password)
        val seriesList = fetchSeries(baseUrl, username, password)

        // Build category lookup maps
        val liveCatMap = liveCats.associateBy { it.id }
        val vodCatMap = vodCats.associateBy { it.id }
        val seriesCatMap = seriesCats.associateBy { it.id }

        // Map live streams to channels
        val channels = liveStreams.mapIndexed { index, stream ->
            val catId = stream.category_id ?: "0"
            val catName = liveCatMap[catId]?.name ?: "General"
            Channel(
                id = "ch_${stream.stream_id ?: index}",
                name = stream.name ?: "Unknown",
                logo = stream.stream_icon,
                streamUrl = "$baseUrl/live/$username/$password/${stream.stream_id}.${stream.container_extension ?: "ts"}",
                categoryId = "live_${catId.hashCode()}",
                categoryName = catName,
                epg = M3UParser.generateMockEPG(stream.name ?: "Unknown")
            )
        }

        // Map VOD streams to movies
        val movies = vodStreams.mapIndexed { index, stream ->
            val catId = stream.category_id ?: "0"
            val catName = vodCatMap[catId]?.name ?: "General"
            Movie(
                id = "mov_${stream.stream_id ?: index}",
                title = stream.name ?: "Unknown",
                poster = stream.stream_icon,
                banner = stream.stream_icon,
                streamUrl = "$baseUrl/movie/$username/$password/${stream.stream_id}.${stream.container_extension ?: "mp4"}",
                categoryId = "movie_${catId.hashCode()}",
                categoryName = catName,
                runtime = stream.info?.duration ?: "",
                rating = stream.rating ?: "",
                plot = stream.info?.plot ?: stream.plot ?: "",
                cast = stream.info?.cast ?: "",
                year = stream.info?.year ?: stream.year ?: ""
            )
        }

        // Map series
        val series = seriesList.mapIndexed { index, s ->
            val catId = s.category_id ?: "0"
            val catName = seriesCatMap[catId]?.name ?: "General"
            Series(
                id = "ser_${s.series_id ?: index}",
                title = s.name ?: "Unknown",
                poster = s.cover ?: s.stream_icon,
                banner = s.cover ?: s.stream_icon,
                categoryId = "series_${catId.hashCode()}",
                categoryName = catName,
                rating = s.rating ?: "",
                plot = s.plot ?: "",
                cast = "",
                year = s.year ?: "",
                seasons = emptyList() // Episodes fetched lazily or via separate call
            )
        }

        return M3UParser.ParsedPlaylist(
            liveCategories = liveCats.map { Category("live_${it.id.hashCode()}", it.name ?: "General", ContentType.LIVE) },
            movieCategories = vodCats.map { Category("movie_${it.id.hashCode()}", it.name ?: "General", ContentType.MOVIE) },
            seriesCategories = seriesCats.map { Category("series_${it.id.hashCode()}", it.name ?: "General", ContentType.SERIES) },
            channels = channels,
            movies = movies,
            series = series
        )
    }

    private inline fun <reified T> fetchJsonList(urlStr: String): List<T> {
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "IPTVCoco/1.0")
        connection.instanceFollowRedirects = true
        return try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw Exception("HTTP $responseCode for $urlStr")
            }
            val content = connection.inputStream.bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<T>>() {}.type
            gson.fromJson(content, type) ?: emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchLiveCategories(baseUrl: String, username: String, password: String): List<XtreamCategory> {
        return try {
            fetchJsonList("$baseUrl/player_api.php?username=$username&password=$password&action=get_live_categories")
        } catch (_: Exception) { emptyList() }
    }

    private fun fetchLiveStreams(baseUrl: String, username: String, password: String): List<XtreamStream> {
        return try {
            fetchJsonList("$baseUrl/player_api.php?username=$username&password=$password&action=get_live_streams")
        } catch (_: Exception) { emptyList() }
    }

    private fun fetchVodCategories(baseUrl: String, username: String, password: String): List<XtreamCategory> {
        return try {
            fetchJsonList("$baseUrl/player_api.php?username=$username&password=$password&action=get_vod_categories")
        } catch (_: Exception) { emptyList() }
    }

    private fun fetchVodStreams(baseUrl: String, username: String, password: String): List<XtreamStream> {
        return try {
            fetchJsonList("$baseUrl/player_api.php?username=$username&password=$password&action=get_vod_streams")
        } catch (_: Exception) { emptyList() }
    }

    private fun fetchSeriesCategories(baseUrl: String, username: String, password: String): List<XtreamCategory> {
        return try {
            fetchJsonList("$baseUrl/player_api.php?username=$username&password=$password&action=get_series_categories")
        } catch (_: Exception) { emptyList() }
    }

    private fun fetchSeries(baseUrl: String, username: String, password: String): List<XtreamSeries> {
        return try {
            fetchJsonList("$baseUrl/player_api.php?username=$username&password=$password&action=get_series")
        } catch (_: Exception) { emptyList() }
    }

    // Data classes for Xtream JSON responses
    private data class XtreamCategory(
        val category_id: String? = null,
        val category_name: String? = null,
        val parent_id: Int? = null
    ) {
        val id: String get() = category_id ?: "0"
        val name: String get() = category_name ?: "General"
    }

    private data class XtreamStream(
        val num: Int? = null,
        val name: String? = null,
        val stream_type: String? = null,
        val stream_id: Int? = null,
        val stream_icon: String? = null,
        val category_id: String? = null,
        val container_extension: String? = null,
        val rating: String? = null,
        val rating_5based: Double? = null,
        val plot: String? = null,
        val year: String? = null,
        val info: XtreamStreamInfo? = null
    )

    private data class XtreamStreamInfo(
        val duration: String? = null,
        val plot: String? = null,
        val cast: String? = null,
        val year: String? = null,
        val rating: String? = null
    )

    private data class XtreamSeries(
        val num: Int? = null,
        val name: String? = null,
        val series_id: Int? = null,
        val cover: String? = null,
        val stream_icon: String? = null,
        val category_id: String? = null,
        val plot: String? = null,
        val rating: String? = null,
        val rating_5based: Double? = null,
        val year: String? = null
    )
}
