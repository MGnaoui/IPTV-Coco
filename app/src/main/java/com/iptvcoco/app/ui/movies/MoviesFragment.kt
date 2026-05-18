package com.iptvcoco.app.ui.movies

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.iptvcoco.app.R
import com.iptvcoco.app.adapter.CategoryAdapter
import com.iptvcoco.app.adapter.MovieAdapter
import com.iptvcoco.app.databinding.FragmentMoviesBinding
import com.iptvcoco.app.ui.detail.MovieDetailActivity
import com.iptvcoco.app.viewmodel.MoviesViewModel

class MoviesFragment : Fragment() {

    private var _binding: FragmentMoviesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MoviesViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var movieAdapter: MovieAdapter
    private val searchHandler = Handler(Looper.getMainLooper())
    private var movieSearchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategories()
        setupMovies()
        setupSearch()

        viewModel.categories.observe(viewLifecycleOwner) {
            categoryAdapter.submitList(it)
        }

        viewModel.movies.observe(viewLifecycleOwner) {
            movieAdapter.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupCategories() {
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        categoryAdapter = CategoryAdapter(
            onCategorySelected = { category ->
                viewModel.selectCategory(category)
            },
            layoutRes = if (isPortrait) R.layout.item_category_chip else R.layout.item_category
        )
        binding.rvCategories.apply {
            layoutManager = if (isPortrait) {
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            } else {
                LinearLayoutManager(requireContext())
            }
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupMovies() {
        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val intent = Intent(requireContext(), MovieDetailActivity::class.java).apply {
                    putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.id)
                }
                startActivity(intent)
            },
            onFavoriteClick = { movieId, _ ->
                viewModel.toggleFavorite(movieId)
            },
            isFavorite = { movieId ->
                viewModel.isFavorite(movieId)
            }
        )
        binding.rvMovies.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = movieAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            post {
                if (width > 0) {
                    val itemWidth = resources.getDimensionPixelSize(R.dimen.movie_item_width) +
                            resources.getDimensionPixelSize(R.dimen.margin_small) * 2
                    val spanCount = (width / itemWidth).coerceAtLeast(2)
                    (layoutManager as GridLayoutManager).spanCount = spanCount
                }
            }
        }
    }

    private fun setupSearch() {
        // Category search only exists in landscape layout
        binding.etSearchCategories?.setOnEditorActionListener { _, _, _ ->
            val query = binding.etSearchCategories?.text?.toString() ?: ""
            viewModel.setCategorySearch(query)
            true
        }

        binding.etSearchMovies.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                movieSearchRunnable?.let { searchHandler.removeCallbacks(it) }
                movieSearchRunnable = Runnable {
                    viewModel.setMovieSearch(s?.toString() ?: "")
                }.also { searchHandler.postDelayed(it, 400) }
            }
        })
        binding.etSearchMovies.setOnEditorActionListener { _, _, _ ->
            movieSearchRunnable?.let { searchHandler.removeCallbacks(it) }
            viewModel.setMovieSearch(binding.etSearchMovies.text.toString())
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        movieSearchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }
}
