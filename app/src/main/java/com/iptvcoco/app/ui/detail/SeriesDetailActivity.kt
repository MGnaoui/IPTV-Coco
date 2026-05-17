package com.iptvcoco.app.ui.detail

import com.iptvcoco.app.IPTVCocoApplication
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.iptvcoco.app.R
import com.iptvcoco.app.adapter.EpisodeAdapter
import com.iptvcoco.app.databinding.ActivitySeriesDetailBinding
import com.iptvcoco.app.model.Series
import com.iptvcoco.app.repository.IPTVRepository
import com.iptvcoco.app.ui.player.PlayerActivity

class SeriesDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeriesDetailBinding
    private lateinit var repository: IPTVRepository
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

        repository = IPTVCocoApplication.instance.repository
        seriesId = intent.getStringExtra(EXTRA_SERIES_ID)

        seriesId?.let { id ->
            series = repository.getSeriesById(id)
            series?.let { displaySeries(it) }
        }
    }

    private fun displaySeries(series: Series) {
        binding.tvTitle.text = series.title
        binding.tvRating.text = series.rating.ifBlank { "N/A" }
        binding.tvYear.text = series.year.ifBlank { "" }
        binding.tvPlot.text = series.plot.ifBlank { "No description available." }
        binding.tvCast.text = series.cast.ifBlank { "N/A" }

        if (!series.banner.isNullOrBlank()) {
            Glide.with(this)
                .load(series.banner)
                .placeholder(R.drawable.ic_tv)
                .into(binding.ivBanner)
        }

        binding.tabSeasons.removeAllTabs()
        series.seasons.forEach { season ->
            binding.tabSeasons.addTab(
                binding.tabSeasons.newTab().setText("S${season.number}")
            )
        }

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
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@SeriesDetailActivity)
            adapter = episodeAdapter
        }

        binding.tabSeasons.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val season = series.seasons.getOrNull(it.position)
                    season?.let { s ->
                        episodeAdapter.submitList(s.episodes)
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        if (series.seasons.isNotEmpty()) {
            episodeAdapter.submitList(series.seasons[0].episodes)
        }
    }
}
