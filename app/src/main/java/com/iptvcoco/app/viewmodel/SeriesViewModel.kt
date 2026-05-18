package com.iptvcoco.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iptvcoco.app.IPTVCocoApplication
import com.iptvcoco.app.model.Category
import com.iptvcoco.app.model.ContentType
import com.iptvcoco.app.model.Series

class SeriesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as IPTVCocoApplication).repository

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _series = MutableLiveData<List<Series>>()
    val series: LiveData<List<Series>> = _series

    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory

    private val _searchQueryCategories = MutableLiveData("")
    private val _searchQuerySeries = MutableLiveData("")

    init {
        loadCategories()
    }

    fun loadCategories() {
        val all = repository.getCategories(ContentType.SERIES)
        val categories = listOf(Category("all", "All", ContentType.SERIES)) + all
        _categories.value = categories
        if (categories.isNotEmpty() && _selectedCategory.value == null) {
            selectCategory(categories.first())
        }
    }

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        loadSeries()
    }

    fun loadSeries() {
        val cat = _selectedCategory.value
        val query = _searchQuerySeries.value ?: ""
        val categoryId = if (cat?.id == "all") null else cat?.id
        _series.value = if (query.isBlank()) {
            repository.getSeries(categoryId)
        } else {
            repository.searchSeries(query, categoryId)
        }
    }

    fun setCategorySearch(query: String) {
        _searchQueryCategories.value = query
        val all = repository.getCategories(ContentType.SERIES)
        val categories = listOf(Category("all", "All", ContentType.SERIES)) + all
        _categories.value = if (query.isBlank()) categories else categories.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    fun setSeriesSearch(query: String) {
        _searchQuerySeries.value = query
        loadSeries()
    }

    fun toggleFavorite(seriesId: String) {
        repository.toggleFavoriteSeries(seriesId)
    }

    fun isFavorite(seriesId: String): Boolean = repository.isFavoriteSeries(seriesId)
}
