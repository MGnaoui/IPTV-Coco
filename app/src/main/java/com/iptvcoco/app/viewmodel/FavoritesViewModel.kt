package com.iptvcoco.app.viewmodel

import com.iptvcoco.app.IPTVCocoApplication
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iptvcoco.app.model.Channel
import com.iptvcoco.app.model.Movie
import com.iptvcoco.app.model.Series

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IPTVCocoApplication.instance.repository

    private val _favoriteChannels = MutableLiveData<List<Channel>>()
    val favoriteChannels: LiveData<List<Channel>> = _favoriteChannels

    private val _favoriteMovies = MutableLiveData<List<Movie>>()
    val favoriteMovies: LiveData<List<Movie>> = _favoriteMovies

    private val _favoriteSeries = MutableLiveData<List<Series>>()
    val favoriteSeries: LiveData<List<Series>> = _favoriteSeries

    fun loadFavorites() {
        _favoriteChannels.value = repository.getFavoriteChannels()
        _favoriteMovies.value = repository.getFavoriteMovies()
        _favoriteSeries.value = repository.getFavoriteSeries()
    }
}
