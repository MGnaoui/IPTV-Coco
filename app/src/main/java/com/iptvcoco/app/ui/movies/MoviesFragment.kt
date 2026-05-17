package com.iptvcoco.app.ui.movies

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
        }
    }

    private fun setupCategories() {
        categoryAdapter = CategoryAdapter { category ->
            viewModel.selectCategory(category)
        }
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun setupMovies() {
        movieAdapter = MovieAdapter { movie ->
            val intent = Intent(requireContext(), MovieDetailActivity::class.java).apply {
                putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.id)
            }
            startActivity(intent)
        }
        binding.rvMovies.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = movieAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearchCategories.setOnEditorActionListener { _, _, _ ->
            viewModel.setCategorySearch(binding.etSearchCategories.text.toString())
            true
        }
        binding.etSearchMovies.setOnEditorActionListener { _, _, _ ->
            viewModel.setMovieSearch(binding.etSearchMovies.text.toString())
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
