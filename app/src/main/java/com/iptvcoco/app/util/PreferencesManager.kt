package com.iptvcoco.app.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iptvcoco.app.model.M3UAccount

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "iptv_prefs"
        private const val KEY_ACCOUNT = "account"
        private const val KEY_FAVORITE_CHANNELS = "favorite_channels"
        private const val KEY_FAVORITE_MOVIES = "favorite_movies"
        private const val KEY_FAVORITE_SERIES = "favorite_series"
        private const val KEY_RESUME_POSITIONS = "resume_positions"
        private const val KEY_LAST_REFRESH = "last_refresh"
    }

    fun saveAccount(account: M3UAccount) {
        prefs.edit().putString(KEY_ACCOUNT, gson.toJson(account)).apply()
    }

    fun getAccount(): M3UAccount? {
        val json = prefs.getString(KEY_ACCOUNT, null) ?: return null
        return try {
            gson.fromJson(json, M3UAccount::class.java)
        } catch (_: Exception) {
            // Clear corrupted account data
            prefs.edit().remove(KEY_ACCOUNT).apply()
            null
        }
    }

    fun clearAccount() {
        prefs.edit().remove(KEY_ACCOUNT).apply()
    }

    fun addFavoriteChannel(channelId: String) {
        val set = getFavoriteChannels().toMutableSet()
        set.add(channelId)
        prefs.edit().putStringSet(KEY_FAVORITE_CHANNELS, set).apply()
    }

    fun removeFavoriteChannel(channelId: String) {
        val set = getFavoriteChannels().toMutableSet()
        set.remove(channelId)
        prefs.edit().putStringSet(KEY_FAVORITE_CHANNELS, set).apply()
    }

    fun isChannelFavorite(channelId: String): Boolean {
        return getFavoriteChannels().contains(channelId)
    }

    fun getFavoriteChannels(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITE_CHANNELS, emptySet()) ?: emptySet()
    }

    fun addFavoriteMovie(movieId: String) {
        val set = getFavoriteMovies().toMutableSet()
        set.add(movieId)
        prefs.edit().putStringSet(KEY_FAVORITE_MOVIES, set).apply()
    }

    fun removeFavoriteMovie(movieId: String) {
        val set = getFavoriteMovies().toMutableSet()
        set.remove(movieId)
        prefs.edit().putStringSet(KEY_FAVORITE_MOVIES, set).apply()
    }

    fun isMovieFavorite(movieId: String): Boolean {
        return getFavoriteMovies().contains(movieId)
    }

    fun getFavoriteMovies(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITE_MOVIES, emptySet()) ?: emptySet()
    }

    fun addFavoriteSeries(seriesId: String) {
        val set = getFavoriteSeries().toMutableSet()
        set.add(seriesId)
        prefs.edit().putStringSet(KEY_FAVORITE_SERIES, set).apply()
    }

    fun removeFavoriteSeries(seriesId: String) {
        val set = getFavoriteSeries().toMutableSet()
        set.remove(seriesId)
        prefs.edit().putStringSet(KEY_FAVORITE_SERIES, set).apply()
    }

    fun isSeriesFavorite(seriesId: String): Boolean {
        return getFavoriteSeries().contains(seriesId)
    }

    fun getFavoriteSeries(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITE_SERIES, emptySet()) ?: emptySet()
    }

    fun saveResumePosition(contentId: String, position: Long) {
        val map = getResumePositions().toMutableMap()
        map[contentId] = position
        prefs.edit().putString(KEY_RESUME_POSITIONS, gson.toJson(map)).apply()
    }

    fun getResumePosition(contentId: String): Long {
        return getResumePositions()[contentId] ?: 0L
    }

    fun getResumePositions(): Map<String, Long> {
        val json = prefs.getString(KEY_RESUME_POSITIONS, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Long>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    fun saveLastRefresh(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_REFRESH, timestamp).apply()
    }

    fun getLastRefresh(): Long {
        return prefs.getLong(KEY_LAST_REFRESH, 0L)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
