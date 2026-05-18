package com.iptvcoco.app.ui.series

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
import com.iptvcoco.app.adapter.SeriesAdapter
import com.iptvcoco.app.databinding.FragmentSeriesBinding
import com.iptvcoco.app.ui.detail.SeriesDetailActivity
import com.iptvcoco.app.viewmodel.SeriesViewModel

class SeriesFragment : Fragment() {

    private var _binding: FragmentSeriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SeriesViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var seriesAdapter: SeriesAdapter
    private val searchHandler = Handler(Looper.getMainLooper())
    private var seriesSearchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategories()
        setupSeries()
        setupSearch()

        viewModel.categories.observe(viewLifecycleOwner) {
            categoryAdapter.submitList(it)
        }

        viewModel.series.observe(viewLifecycleOwner) {
            seriesAdapter.submitList(it)
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

    private fun setupSeries() {
        seriesAdapter = SeriesAdapter(
            onSeriesClick = { series ->
                val intent = Intent(requireContext(), SeriesDetailActivity::class.java).apply {
                    putExtra(SeriesDetailActivity.EXTRA_SERIES_ID, series.id)
                }
                startActivity(intent)
            },
            onFavoriteClick = { seriesId, _ ->
                viewModel.toggleFavorite(seriesId)
            },
            isFavorite = { seriesId ->
                viewModel.isFavorite(seriesId)
            }
        )
        binding.rvSeries.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = seriesAdapter
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

        binding.etSearchSeries.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                seriesSearchRunnable?.let { searchHandler.removeCallbacks(it) }
                seriesSearchRunnable = Runnable {
                    viewModel.setSeriesSearch(s?.toString() ?: "")
                }.also { searchHandler.postDelayed(it, 400) }
            }
        })
        binding.etSearchSeries.setOnEditorActionListener { _, _, _ ->
            seriesSearchRunnable?.let { searchHandler.removeCallbacks(it) }
            viewModel.setSeriesSearch(binding.etSearchSeries.text.toString())
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        seriesSearchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }
}
