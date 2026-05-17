package com.iptvcoco.app.ui.live

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.iptvcoco.app.adapter.CategoryAdapter
import com.iptvcoco.app.adapter.ChannelAdapter
import com.iptvcoco.app.adapter.EpgAdapter
import com.iptvcoco.app.databinding.FragmentLiveTvBinding
import com.iptvcoco.app.model.Channel
import com.iptvcoco.app.ui.player.PlayerActivity
import com.iptvcoco.app.viewmodel.LiveTVViewModel

class LiveTVFragment : Fragment() {

    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LiveTVViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var channelAdapter: ChannelAdapter
    private lateinit var epgAdapter: EpgAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveTvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategories()
        setupChannels()
        setupEPG()
        setupPreview()
        setupSearch()

        viewModel.categories.observe(viewLifecycleOwner) {
            categoryAdapter.submitList(it)
        }

        viewModel.channels.observe(viewLifecycleOwner) {
            channelAdapter.submitList(it)
            if (viewModel.selectedChannel.value == null && it.isNotEmpty()) {
                viewModel.selectChannel(it.first())
            }
        }

        viewModel.selectedChannel.observe(viewLifecycleOwner) { channel ->
            channel?.let { updatePreview(it) }
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

    private fun setupChannels() {
        channelAdapter = ChannelAdapter { channel ->
            viewModel.selectChannel(channel)
            openPlayer(channel)
        }
        binding.rvChannels.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = channelAdapter
        }
    }

    private fun setupEPG() {
        epgAdapter = EpgAdapter()
        binding.rvEpg.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = epgAdapter
        }
    }

    private fun setupPreview() {
        binding.previewContainer.setOnClickListener {
            viewModel.selectedChannel.value?.let { channel ->
                openPlayer(channel)
            }
        }
    }

    private fun setupSearch() {
        binding.etSearchCategories.setOnEditorActionListener { _, _, _ ->
            viewModel.setCategorySearch(binding.etSearchCategories.text.toString())
            true
        }
        binding.etSearchChannels.setOnEditorActionListener { _, _, _ ->
            viewModel.setChannelSearch(binding.etSearchChannels.text.toString())
            true
        }
    }

    private fun updatePreview(channel: Channel) {
        binding.tvPreviewName.text = channel.name
        if (!channel.logo.isNullOrBlank()) {
            Glide.with(this)
                .load(channel.logo)
                .into(binding.ivPreview)
        }
        epgAdapter.submitList(channel.epg)
    }

    private fun openPlayer(channel: Channel) {
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_STREAM_URL, channel.streamUrl)
            putExtra(PlayerActivity.EXTRA_TITLE, channel.name)
            putExtra(PlayerActivity.EXTRA_TYPE, PlayerActivity.TYPE_LIVE)
            putExtra(PlayerActivity.EXTRA_ID, channel.id)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
