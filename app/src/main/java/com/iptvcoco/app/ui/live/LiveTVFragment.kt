package com.iptvcoco.app.ui.live

import android.content.Intent
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.iptvcoco.app.adapter.CategoryAdapter
import com.iptvcoco.app.adapter.ChannelAdapter
import com.iptvcoco.app.adapter.EpgAdapter
import com.iptvcoco.app.databinding.FragmentLiveTvBinding
import com.iptvcoco.app.model.Channel
import com.iptvcoco.app.ui.player.PlayerActivity
import com.iptvcoco.app.R
import com.iptvcoco.app.util.DeviceUtils
import com.iptvcoco.app.viewmodel.LiveTVViewModel

class LiveTVFragment : Fragment() {

    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LiveTVViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var channelAdapter: ChannelAdapter
    private lateinit var epgAdapter: EpgAdapter
    private val searchHandler = Handler(Looper.getMainLooper())
    private var categorySearchRunnable: Runnable? = null
    private var channelSearchRunnable: Runnable? = null

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
            binding.tvEmptyChannels.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.selectedChannel.observe(viewLifecycleOwner) { channel ->
            channel?.let { updatePreview(it) }
        }
    }

    private fun setupCategories() {
        categoryAdapter = CategoryAdapter(
            onCategorySelected = { category ->
                viewModel.selectCategory(category)
            },
            layoutRes = R.layout.item_category_chip
        )
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupChannels() {
        channelAdapter = ChannelAdapter(
            onChannelClick = { channel ->
                viewModel.selectChannel(channel)
                openPlayer(channel)
            },
            onFavoriteClick = { channelId, _ ->
                viewModel.toggleFavorite(channelId)
            },
            isFavorite = { channelId ->
                viewModel.isFavorite(channelId)
            }
        )
        binding.rvChannels.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = channelAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(DeviceUtils.getRecyclerViewCacheSize(requireContext()))
            post {
                if (width > 0) {
                    val itemWidth = resources.getDimensionPixelSize(R.dimen.channel_item_width_compact) +
                            resources.getDimensionPixelSize(R.dimen.margin_small) * 2
                    val spanCount = (width / itemWidth).coerceAtLeast(4)
                    (layoutManager as GridLayoutManager).spanCount = spanCount
                }
            }
        }
    }

    private fun setupEPG() {
        epgAdapter = EpgAdapter()
        binding.rvEpg.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = epgAdapter
            setHasFixedSize(true)
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
        binding.etSearchCategories.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                categorySearchRunnable?.let { searchHandler.removeCallbacks(it) }
                categorySearchRunnable = Runnable {
                    viewModel.setCategorySearch(s?.toString() ?: "")
                }.also { searchHandler.postDelayed(it, 400) }
            }
        })
        binding.etSearchCategories.setOnEditorActionListener { _, _, _ ->
            categorySearchRunnable?.let { searchHandler.removeCallbacks(it) }
            viewModel.setCategorySearch(binding.etSearchCategories.text.toString())
            true
        }

        binding.etSearchChannels.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                channelSearchRunnable?.let { searchHandler.removeCallbacks(it) }
                channelSearchRunnable = Runnable {
                    viewModel.setChannelSearch(s?.toString() ?: "")
                }.also { searchHandler.postDelayed(it, 400) }
            }
        })
        binding.etSearchChannels.setOnEditorActionListener { _, _, _ ->
            channelSearchRunnable?.let { searchHandler.removeCallbacks(it) }
            viewModel.setChannelSearch(binding.etSearchChannels.text.toString())
            true
        }
    }

    private fun updatePreview(channel: Channel) {
        binding.tvPreviewName.text = channel.name
        if (!channel.logo.isNullOrBlank()) {
            Glide.with(this)
                .load(channel.logo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_live)
                .error(R.drawable.ic_live)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(binding.ivPreview)
        } else {
            binding.ivPreview.setImageResource(R.drawable.ic_live)
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
        categorySearchRunnable?.let { searchHandler.removeCallbacks(it) }
        channelSearchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }
}
