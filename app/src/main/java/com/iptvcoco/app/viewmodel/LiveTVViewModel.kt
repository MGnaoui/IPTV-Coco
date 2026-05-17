package com.iptvcoco.app.viewmodel

import com.iptvcoco.app.IPTVCocoApplication
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iptvcoco.app.model.Category
import com.iptvcoco.app.model.Channel
import com.iptvcoco.app.model.ContentType

class LiveTVViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IPTVCocoApplication.instance.repository

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
        _categories.value = all
        if (all.isNotEmpty() && _selectedCategory.value == null) {
            selectCategory(all.first())
        }
    }

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        loadChannels()
    }

    fun loadChannels() {
        val cat = _selectedCategory.value
        val query = _searchQueryChannels.value ?: ""
        _channels.value = if (query.isBlank()) {
            repository.getChannels(cat?.id)
        } else {
            repository.searchChannels(query, cat?.id)
        }
    }

    fun selectChannel(channel: Channel) {
        _selectedChannel.value = channel
    }

    fun setCategorySearch(query: String) {
        _searchQueryCategories.value = query
        val all = repository.getCategories(ContentType.LIVE)
        _categories.value = if (query.isBlank()) all else all.filter {
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
