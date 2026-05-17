package com.iptvcoco.app.ui.series

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

    private fun setupSeries() {
        seriesAdapter = SeriesAdapter { series ->
            val intent = Intent(requireContext(), SeriesDetailActivity::class.java).apply {
                putExtra(SeriesDetailActivity.EXTRA_SERIES_ID, series.id)
            }
            startActivity(intent)
        }
        binding.rvSeries.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = seriesAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearchCategories.setOnEditorActionListener { _, _, _ ->
            viewModel.setCategorySearch(binding.etSearchCategories.text.toString())
            true
        }
        binding.etSearchSeries.setOnEditorActionListener { _, _, _ ->
            viewModel.setSeriesSearch(binding.etSearchSeries.text.toString())
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
