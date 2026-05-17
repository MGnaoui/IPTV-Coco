package com.iptvcoco.app.ui.main

import com.iptvcoco.app.IPTVCocoApplication
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.iptvcoco.app.R
import com.iptvcoco.app.databinding.ActivityMainBinding
import com.iptvcoco.app.repository.IPTVRepository
import com.iptvcoco.app.ui.favorites.FavoritesFragment
import com.iptvcoco.app.ui.live.LiveTVFragment
import com.iptvcoco.app.ui.movies.MoviesFragment
import com.iptvcoco.app.ui.series.SeriesFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = IPTVCocoApplication.instance.repository
        if (repository.isLoggedIn() && repository.getCategories(com.iptvcoco.app.model.ContentType.LIVE).isEmpty()) {
            lifecycleScope.launch {
                repository.reload()
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LiveTVFragment())
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_live -> LiveTVFragment()
                R.id.nav_movies -> MoviesFragment()
                R.id.nav_series -> SeriesFragment()
                R.id.nav_favorites -> FavoritesFragment()
                else -> null
            }
            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, it)
                    .commit()
                true
            } ?: false
        }
    }
}
