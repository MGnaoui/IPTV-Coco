package com.iptvcoco.app.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iptvcoco.app.adapter.ChannelAdapter
import com.iptvcoco.app.adapter.MovieAdapter
import com.iptvcoco.app.adapter.SeriesAdapter
import com.iptvcoco.app.databinding.FragmentFavoritesBinding
import com.iptvcoco.app.ui.detail.MovieDetailActivity
import com.iptvcoco.app.ui.detail.SeriesDetailActivity
import com.iptvcoco.app.ui.player.PlayerActivity
import com.iptvcoco.app.R
import com.iptvcoco.app.viewmodel.FavoritesViewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesViewModel by viewModels()

    private lateinit var channelAdapter: ChannelAdapter
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var seriesAdapter: SeriesAdapter
    private val sharedPool = RecyclerView.RecycledViewPool()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()

        viewModel.favoriteChannels.observe(viewLifecycleOwner) {
            channelAdapter.submitList(it)
            updateEmptyState()
        }
        viewModel.favoriteMovies.observe(viewLifecycleOwner) {
            movieAdapter.submitList(it)
            updateEmptyState()
        }
        viewModel.favoriteSeries.observe(viewLifecycleOwner) {
            seriesAdapter.submitList(it)
            updateEmptyState()
        }

        viewModel.loadFavorites()
    }

    private fun setupAdapters() {
        channelAdapter = ChannelAdapter(
            onChannelClick = { channel ->
                val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_STREAM_URL, channel.streamUrl)
                    putExtra(PlayerActivity.EXTRA_TITLE, channel.name)
                    putExtra(PlayerActivity.EXTRA_TYPE, PlayerActivity.TYPE_LIVE)
                    putExtra(PlayerActivity.EXTRA_ID, channel.id)
                }
                startActivity(intent)
            },
            onFavoriteClick = { channelId, _ ->
                viewModel.toggleFavoriteChannel(channelId)
                viewModel.loadFavorites()
            },
            isFavorite = { channelId ->
                viewModel.isFavoriteChannel(channelId)
            },
            layoutRes = R.layout.item_channel_horizontal
        )
        binding.rvChannels.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = channelAdapter
            isNestedScrollingEnabled = false
            setRecycledViewPool(sharedPool)
        }

        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val intent = Intent(requireContext(), MovieDetailActivity::class.java).apply {
                    putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.id)
                }
                startActivity(intent)
            },
            onFavoriteClick = { movieId, _ ->
                viewModel.toggleFavoriteMovie(movieId)
                viewModel.loadFavorites()
            },
            isFavorite = { movieId ->
                viewModel.isFavoriteMovie(movieId)
            }
        )
        binding.rvMovies.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = movieAdapter
            isNestedScrollingEnabled = false
            setRecycledViewPool(sharedPool)
        }

        seriesAdapter = SeriesAdapter(
            onSeriesClick = { series ->
                val intent = Intent(requireContext(), SeriesDetailActivity::class.java).apply {
                    putExtra(SeriesDetailActivity.EXTRA_SERIES_ID, series.id)
                }
                startActivity(intent)
            },
            onFavoriteClick = { seriesId, _ ->
                viewModel.toggleFavoriteSeries(seriesId)
                viewModel.loadFavorites()
            },
            isFavorite = { seriesId ->
                viewModel.isFavoriteSeries(seriesId)
            }
        )
        binding.rvSeries.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = seriesAdapter
            isNestedScrollingEnabled = false
            setRecycledViewPool(sharedPool)
        }
    }

    private fun updateEmptyState() {
        val hasAny = viewModel.favoriteChannels.value?.isNotEmpty() == true ||
                viewModel.favoriteMovies.value?.isNotEmpty() == true ||
                viewModel.favoriteSeries.value?.isNotEmpty() == true
        binding.tvEmpty.visibility = if (hasAny) View.GONE else View.VISIBLE
        binding.scrollView.visibility = if (hasAny) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
