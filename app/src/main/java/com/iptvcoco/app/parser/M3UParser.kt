package com.iptvcoco.app.parser

import com.iptvcoco.app.model.Category
import com.iptvcoco.app.model.Channel
import com.iptvcoco.app.model.ContentType
import com.iptvcoco.app.model.EPGEntry
import com.iptvcoco.app.model.Episode
import com.iptvcoco.app.model.Movie
import com.iptvcoco.app.model.Season
import com.iptvcoco.app.model.Series

object M3UParser {

    fun parsePlaylist(content: String): ParsedPlaylist {
        val lines = content.lines()
        val liveCategories = mutableMapOf<String, String>()
        val movieCategories = mutableMapOf<String, String>()
        val seriesCategories = mutableMapOf<String, String>()
        val channels = mutableListOf<Channel>()
        val movies = mutableListOf<Movie>()
        val seriesMap = mutableMapOf<String, MutableList<SeriesEpisodeRaw>>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF:")) {
                val infoLine = line
                val urlLine = if (i + 1 < lines.size) lines[i + 1].trim() else ""
                i += 2

                if (urlLine.isNotEmpty() && !urlLine.startsWith("#")) {
                    val attrs = parseAttributes(infoLine)
                    val name = parseName(infoLine)
                    val groupTitle = attrs["group-title"] ?: "General"
                    val logo = attrs["tvg-logo"] ?: attrs["logo"]
                    val type = detectType(attrs, urlLine, infoLine)

                    when (type) {
                        ContentType.LIVE -> {
                            val catId = "live_${groupTitle.hashCode()}"
                            liveCategories[catId] = groupTitle
                            channels.add(
                                Channel(
                                    id = "ch_${channels.size}",
                                    name = name,
                                    logo = logo,
                                    streamUrl = urlLine,
                                    categoryId = catId,
                                    categoryName = groupTitle,
                                    epg = generateMockEPG(name)
                                )
                            )
                        }
                        ContentType.MOVIE -> {
                            val catId = "movie_${groupTitle.hashCode()}"
                            movieCategories[catId] = groupTitle
                            movies.add(
                                Movie(
                                    id = "mov_${movies.size}",
                                    title = name,
                                    poster = logo,
                                    banner = logo,
                                    streamUrl = urlLine,
                                    categoryId = catId,
                                    categoryName = groupTitle,
                                    runtime = attrs["tvg-runtime"] ?: "",
                                    rating = attrs["tvg-rating"] ?: "",
                                    plot = attrs["tvg-plot"] ?: "",
                                    cast = attrs["tvg-cast"] ?: "",
                                    year = attrs["tvg-year"] ?: ""
                                )
                            )
                        }
                        ContentType.SERIES -> {
                            val catId = "series_${groupTitle.hashCode()}"
                            seriesCategories[catId] = groupTitle
                            val seriesName = attrs["series-name"] ?: name
                            val seasonNum = attrs["season"]?.toIntOrNull() ?: 1
                            val episodeNum = attrs["episode"]?.toIntOrNull() ?: 1

                            seriesMap.getOrPut(seriesName) { mutableListOf() }.add(
                                SeriesEpisodeRaw(
                                    seriesName = seriesName,
                                    poster = logo,
                                    categoryId = catId,
                                    categoryName = groupTitle,
                                    season = seasonNum,
                                    episode = episodeNum,
                                    episodeTitle = name,
                                    streamUrl = urlLine,
                                    rating = attrs["tvg-rating"] ?: "",
                                    plot = attrs["tvg-plot"] ?: "",
                                    cast = attrs["tvg-cast"] ?: "",
                                    year = attrs["tvg-year"] ?: ""
                                )
                            )
                        }
                    }
                }
            } else {
                i++
            }
        }

        val liveCats = liveCategories.map { (id, name) -> Category(id, name, ContentType.LIVE) }
        val movieCats = movieCategories.map { (id, name) -> Category(id, name, ContentType.MOVIE) }
        val seriesCats = seriesCategories.map { (id, name) -> Category(id, name, ContentType.SERIES) }

        val seriesList = seriesMap.map { (seriesName, episodes) ->
            val seasonsMap = episodes.groupBy { it.season }
                .map { (seasonNum, eps) ->
                    Season(
                        number = seasonNum,
                        episodes = eps.sortedBy { it.episode }.map { ep ->
                            Episode(
                                id = "ep_${ep.hashCode()}",
                                title = ep.episodeTitle,
                                number = ep.episode,
                                streamUrl = ep.streamUrl,
                                runtime = "",
                                plot = ep.plot
                            )
                        }
                    )
                }.sortedBy { it.number }

            val first = episodes.first()
            Series(
                id = "ser_${seriesName.hashCode()}",
                title = seriesName,
                poster = first.poster,
                banner = first.poster,
                categoryId = first.categoryId,
                categoryName = first.categoryName,
                rating = first.rating,
                plot = first.plot,
                cast = first.cast,
                year = first.year,
                seasons = seasonsMap
            )
        }

        return ParsedPlaylist(
            liveCategories = liveCats,
            movieCategories = movieCats,
            seriesCategories = seriesCats,
            channels = channels,
            movies = movies,
            series = seriesList
        )
    }

    private fun parseAttributes(line: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val regex = """([\w-]+)="([^"]*)")""".toRegex()
        regex.findAll(line).forEach { match ->
            map[match.groupValues[1]] = match.groupValues[2]
        }
        return map
    }

    private fun parseName(line: String): String {
        val commaIdx = line.lastIndexOf(",")
        return if (commaIdx != -1 && commaIdx < line.length - 1) {
            line.substring(commaIdx + 1).trim()
        } else {
            "Unknown"
        }
    }

    private fun detectType(attrs: Map<String, String>, url: String, infoLine: String): ContentType {
        if (attrs["series-name"] != null || attrs["season"] != null) return ContentType.SERIES
        val lower = infoLine.lowercase()
        if (lower.contains("movie") || lower.contains("film")) return ContentType.MOVIE
        val urlLower = url.lowercase()
        if (urlLower.contains("/movie/") || urlLower.contains("movie")) return ContentType.MOVIE
        if (urlLower.contains("/series/") || urlLower.contains("serie")) return ContentType.SERIES
        return ContentType.LIVE
    }

    private fun generateMockEPG(channelName: String): List<EPGEntry> {
        val now = System.currentTimeMillis()
        val hour = 3600000L
        return listOf(
            EPGEntry(
                title = "Currently Playing on $channelName",
                startTime = now - 30 * 60000,
                endTime = now + 30 * 60000,
                description = "Live broadcast"
            ),
            EPGEntry(
                title = "Up Next on $channelName",
                startTime = now + 30 * 60000,
                endTime = now + 90 * 60000,
                description = "Next program"
            ),
            EPGEntry(
                title = "Later on $channelName",
                startTime = now + 90 * 60000,
                endTime = now + 150 * 60000,
                description = "Future program"
            )
        )
    }

    data class ParsedPlaylist(
        val liveCategories: List<Category>,
        val movieCategories: List<Category>,
        val seriesCategories: List<Category>,
        val channels: List<Channel>,
        val movies: List<Movie>,
        val series: List<Series>
    )

    private data class SeriesEpisodeRaw(
        val seriesName: String,
        val poster: String?,
        val categoryId: String,
        val categoryName: String,
        val season: Int,
        val episode: Int,
        val episodeTitle: String,
        val streamUrl: String,
        val rating: String,
        val plot: String,
        val cast: String,
        val year: String
    )
}
