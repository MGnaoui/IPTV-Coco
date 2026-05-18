package com.iptvcoco.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iptvcoco.app.IPTVCocoApplication
import com.iptvcoco.app.model.Category
import com.iptvcoco.app.model.Channel
import com.iptvcoco.app.model.ContentType

class LiveTVViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as IPTVCocoApplication).repository

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _channels = MutableLiveData<List<Channel>>()
    val channels: LiveData<List<Channel>> = _channels

    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory

    private val _selectedChannel = MutableLiveData<Channel?>()
    val selectedChannel: LiveData<Channel?> = _selectedChannel

    private val _searchQueryCategories = MutableLiveData("")
    val searchQueryCategories: LiveData<String> = _searchQueryCategories

    private val _searchQueryChannels = MutableLiveData("")
    val searchQueryChannels: LiveData<String> = _searchQueryChannels

    init {
        loadCategories()
    }

    fun loadCategories() {
        val all = repository.getCategories(ContentType.LIVE)
        val categories = listOf(Category("favorites", "Favorites", ContentType.LIVE)) + all
        _categories.value = categories
        if (categories.isNotEmpty() && _selectedCategory.value == null) {
            // Select first real category by default, not Favorites
            selectCategory(if (all.isNotEmpty()) all.first() else categories.first())
        }
    }

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        loadChannels()
    }

    fun loadChannels() {
        val cat = _selectedCategory.value
        val query = _searchQueryChannels.value ?: ""
        _channels.value = when {
            cat?.id == "favorites" -> {
                val favs = repository.getFavoriteChannels()
                if (query.isBlank()) favs else favs.filter { it.name.contains(query, ignoreCase = true) }
            }
            query.isBlank() -> repository.getChannels(cat?.id)
            else -> repository.searchChannels(query, cat?.id)
        }
    }

    fun selectChannel(channel: Channel) {
        _selectedChannel.value = channel
    }

    fun setCategorySearch(query: String) {
        _searchQueryCategories.value = query
        val all = repository.getCategories(ContentType.LIVE)
        val categories = listOf(Category("favorites", "Favorites", ContentType.LIVE)) + all
        _categories.value = if (query.isBlank()) categories else categories.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    fun setChannelSearch(query: String) {
        _searchQueryChannels.value = query
        loadChannels()
    }

    fun toggleFavorite(channelId: String) {
        repository.toggleFavoriteChannel(channelId)
    }

    fun isFavorite(channelId: String): Boolean = repository.isFavoriteChannel(channelId)
}
