package com.iptvcoco.app.viewmodel

import com.iptvcoco.app.IPTVCocoApplication
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iptvcoco.app.model.Category
import com.iptvcoco.app.model.ContentType
import com.iptvcoco.app.model.Movie

class MoviesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IPTVCocoApplication.instance.repository

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> = _movies

    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory

    private val _searchQueryCategories = MutableLiveData("")
    private val _searchQueryMovies = MutableLiveData("")

    init {
        loadCategories()
    }

    fun loadCategories() {
        val all = repository.getCategories(ContentType.MOVIE)
        _categories.value = all
        if (all.isNotEmpty() && _selectedCategory.value == null) {
            selectCategory(all.first())
        }
    }

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        loadMovies()
    }

    fun loadMovies() {
        val cat = _selectedCategory.value
        val query = _searchQueryMovies.value ?: ""
        _movies.value = if (query.isBlank()) {
            repository.getMovies(cat?.id)
        } else {
            repository.searchMovies(query, cat?.id)
        }
    }

    fun setCategorySearch(query: String) {
        _searchQueryCategories.value = query
        val all = repository.getCategories(ContentType.MOVIE)
        _categories.value = if (query.isBlank()) all else all.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    fun setMovieSearch(query: String) {
        _searchQueryMovies.value = query
        loadMovies()
    }

    fun toggleFavorite(movieId: String) {
        repository.toggleFavoriteMovie(movieId)
    }

    fun isFavorite(movieId: String): Boolean = repository.isFavoriteMovie(movieId)
}
