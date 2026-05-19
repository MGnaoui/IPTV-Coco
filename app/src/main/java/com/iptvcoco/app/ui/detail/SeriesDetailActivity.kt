package com.iptvcoco.app.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.tabs.TabLayout
import com.iptvcoco.app.IPTVCocoApplication
import com.iptvcoco.app.R
import com.iptvcoco.app.adapter.EpisodeAdapter
import com.iptvcoco.app.databinding.ActivitySeriesDetailBinding
import com.iptvcoco.app.model.Season
import com.iptvcoco.app.model.Series
import com.iptvcoco.app.ui.player.PlayerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeriesDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeriesDetailBinding
    private val repository = IPTVCocoApplication.instance.repository
    private var seriesId: String? = null
    private var series: Series? = null
    private lateinit var episodeAdapter: EpisodeAdapter

    companion object {
        const val EXTRA_SERIES_ID = "series_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        seriesId = intent.getStringExtra(EXTRA_SERIES_ID)

        // Show basic info instantly from cached data
        seriesId?.let { id ->
            val cachedSeries = repository.getSeriesById(id)
            cachedSeries?.let {
                series = it
                displaySeriesInfo(it)
            }

            // Fetch episodes in background (Xtream on-demand)
            lifecycleScope.launch {
                val loadedSeries = withContext(Dispatchers.IO) {
                    repository.getSeriesInfo(id)
                }
                loadedSeries?.let {
                    series = it
                    displayEpisodes(it.seasons)
                }
            }
        }
    }

    /** Display basic series info immediately without waiting for network */
    private fun displaySeriesInfo(series: Series) {
        binding.tvTitle.text = series.title
        binding.tvRating.text = series.rating.ifBlank { "N/A" }
        binding.tvYear.text = series.year.ifBlank { "" }
        binding.tvPlot.text = series.plot.ifBlank { "No description available." }
        binding.tvCast.text = series.cast.ifBlank { "N/A" }

        if (!series.banner.isNullOrBlank()) {
            Glide.with(this)
                .load(series.banner)
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_tv)
                .error(R.drawable.ic_tv)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(binding.ivBanner)
        } else {
            binding.ivBanner.setImageResource(R.drawable.ic_tv)
        }

        updateFavoriteButton()
        binding.btnBack.setOnClickListener { finish() }
        binding.btnFavorite.setOnClickListener {
            repository.toggleFavoriteSeries(series.id)
            updateFavoriteButton()
        }

        // Setup episode adapter even if empty; will be updated when data arrives
        setupEpisodeAdapter(series)
    }

    private fun setupEpisodeAdapter(series: Series) {
        if (::episodeAdapter.isInitialized) return

        episodeAdapter = EpisodeAdapter { episode ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_STREAM_URL, episode.streamUrl)
                putExtra(PlayerActivity.EXTRA_TITLE, "${series.title} - ${episode.title}")
                putExtra(PlayerActivity.EXTRA_TYPE, PlayerActivity.TYPE_SERIES)
                putExtra(PlayerActivity.EXTRA_ID, episode.id)
            }
            startActivity(intent)
        }
        binding.rvEpisodes.apply {
            layoutManager = LinearLayoutManager(this@SeriesDetailActivity)
            adapter = episodeAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(20)
        }
    }

    /** Update episodes when background fetch completes */
    private fun displayEpisodes(seasons: List<Season>) {
        series?.let { s ->
            setupEpisodeAdapter(s)
        }

        binding.tabSeasons.removeAllTabs()
        if (seasons.isEmpty()) {
            binding.tabSeasons.visibility = View.GONE
            episodeAdapter.submitList(emptyList())
            return
        }

        binding.tabSeasons.visibility = View.VISIBLE
        seasons.forEach { season ->
            binding.tabSeasons.addTab(
                binding.tabSeasons.newTab().setText("S${season.number}")
            )
        }

        binding.tabSeasons.clearOnTabSelectedListeners()
        binding.tabSeasons.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val season = seasons.getOrNull(it.position)
                    season?.let { s ->
                        episodeAdapter.submitList(s.episodes)
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Show first season by default
        episodeAdapter.submitList(seasons[0].episodes)
    }

    private fun updateFavoriteButton() {
        series?.let {
            val isFav = repository.isFavoriteSeries(it.id)
            binding.btnFavorite.setImageResource(
                if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
            )
            binding.btnFavorite.setColorFilter(
                if (isFav) getColor(R.color.favorite_active) else getColor(R.color.white)
            )
        }
    }
}
