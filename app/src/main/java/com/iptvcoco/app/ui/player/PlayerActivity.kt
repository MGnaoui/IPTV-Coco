package com.iptvcoco.app.ui.player

import com.iptvcoco.app.IPTVCocoApplication
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.iptvcoco.app.R
import com.iptvcoco.app.databinding.ActivityPlayerBinding
import com.iptvcoco.app.model.Channel
import com.iptvcoco.app.repository.IPTVRepository

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private lateinit var repository: IPTVRepository

    private val handler = Handler(Looper.getMainLooper())
    private var controlsVisible = true
    private var isLive = true
    private var contentId: String? = null
    private var streamUrl: String? = null
    private var title: String? = null
    private var contentType: String = TYPE_LIVE

    private var channelList: List<Channel> = emptyList()
    private var currentChannelIndex: Int = -1

    private lateinit var audioManager: AudioManager
    private var maxVolume = 0

    companion object {
        const val EXTRA_STREAM_URL = "stream_url"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TYPE = "type"
        const val EXTRA_ID = "content_id"
        const val TYPE_LIVE = "live"
        const val TYPE_MOVIE = "movie"
        const val TYPE_SERIES = "series"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = IPTVCocoApplication.instance.repository
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        streamUrl = intent.getStringExtra(EXTRA_STREAM_URL)
        title = intent.getStringExtra(EXTRA_TITLE)
        contentType = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_LIVE
        contentId = intent.getStringExtra(EXTRA_ID)
        isLive = contentType == TYPE_LIVE

        if (isLive) {
            channelList = repository.getChannels()
            currentChannelIndex = channelList.indexOfFirst { it.id == contentId }
        }

        binding.tvTitle.text = title

        setupUI()
        initializePlayer()
        setupControls()
        setupTouchHandling()
        resetControlsTimer()
    }

    private fun setupUI() {
        if (isLive) {
            binding.btnSkipForward.visibility = View.GONE
            binding.btnSkipBackward.visibility = View.GONE
            binding.btnChannelUp.visibility = View.VISIBLE
            binding.btnChannelDown.visibility = View.VISIBLE
        } else {
            binding.btnSkipForward.visibility = View.VISIBLE
            binding.btnSkipBackward.visibility = View.VISIBLE
            binding.btnChannelUp.visibility = View.GONE
            binding.btnChannelDown.visibility = View.GONE
        }
    }

    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        player = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(audioAttributes, true)
            binding.playerView.player = this
            streamUrl?.let { url ->
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                play()

                contentId?.let { id ->
                    val resumePos = repository.getResumePosition(id)
                    if (resumePos > 0 && !isLive) {
                        seekTo(resumePos)
                    }
                }
            }
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    binding.progressBar.visibility =
                        if (playbackState == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                }
            })
        }
    }

    private fun setupControls() {
        binding.btnPlayPause.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    binding.btnPlayPause.setImageResource(R.drawable.ic_play)
                } else {
                    it.play()
                    binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
                }
            }
            resetControlsTimer()
        }

        binding.btnChannelUp.setOnClickListener {
            if (isLive && channelList.isNotEmpty()) {
                currentChannelIndex = (currentChannelIndex + 1) % channelList.size
                switchToChannel(channelList[currentChannelIndex])
            }
            resetControlsTimer()
        }

        binding.btnChannelDown.setOnClickListener {
            if (isLive && channelList.isNotEmpty()) {
                currentChannelIndex = (currentChannelIndex - 1 + channelList.size) % channelList.size
                switchToChannel(channelList[currentChannelIndex])
            }
            resetControlsTimer()
        }

        binding.btnSkipForward.setOnClickListener {
            player?.let {
                it.seekTo(it.currentPosition + 30000)
            }
            resetControlsTimer()
        }

        binding.btnSkipBackward.setOnClickListener {
            player?.let {
                it.seekTo((it.currentPosition - 30000).coerceAtLeast(0))
            }
            resetControlsTimer()
        }

        binding.btnVolumeUp.setOnClickListener {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
            resetControlsTimer()
        }

        binding.btnVolumeDown.setOnClickListener {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )
            resetControlsTimer()
        }

        binding.btnBrightnessUp.setOnClickListener {
            adjustBrightness(0.1f)
            resetControlsTimer()
        }

        binding.btnBrightnessDown.setOnClickListener {
            adjustBrightness(-0.1f)
            resetControlsTimer()
        }

        updateFavoriteButton()
        binding.btnFavorite.setOnClickListener {
            contentId?.let { id ->
                when {
                    isLive -> repository.toggleFavoriteChannel(id)
                    contentType == TYPE_MOVIE -> repository.toggleFavoriteMovie(id)
                    contentType == TYPE_SERIES -> repository.toggleFavoriteSeries(id)
                }
                updateFavoriteButton()
            }
            resetControlsTimer()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && !isLive) {
                    player?.let {
                        val duration = it.duration
                        if (duration > 0) {
                            it.seekTo(duration * progress / 1000)
                        }
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        updateSeekBar()
    }

    private fun setupTouchHandling() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                toggleControls()
                return true
            }
        })

        binding.controlsOverlay.setOnClickListener {
            toggleControls()
        }

        binding.playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun toggleControls() {
        if (controlsVisible) {
            hideControls()
        } else {
            showControls()
        }
    }

    private fun showControls() {
        binding.controlsOverlay.visibility = View.VISIBLE
        controlsVisible = true
        resetControlsTimer()
    }

    private fun hideControls() {
        binding.controlsOverlay.visibility = View.GONE
        controlsVisible = false
        handler.removeCallbacks(hideControlsRunnable)
    }

    private val hideControlsRunnable = Runnable {
        hideControls()
    }

    private fun resetControlsTimer() {
        handler.removeCallbacks(hideControlsRunnable)
        handler.postDelayed(hideControlsRunnable, 5000)
    }

    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                player?.let {
                    if (!isLive && it.duration > 0) {
                        val progress = (it.currentPosition * 1000 / it.duration).toInt()
                        binding.seekBar.progress = progress
                    }
                }
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun switchToChannel(channel: Channel) {
        streamUrl = channel.streamUrl
        title = channel.name
        contentId = channel.id
        binding.tvTitle.text = title

        player?.let { exoPlayer ->
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(MediaItem.fromUri(channel.streamUrl))
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    private fun adjustBrightness(delta: Float) {
        val layoutParams = window.attributes
        var brightness = layoutParams.screenBrightness
        if (brightness < 0) brightness = 0.5f
        brightness += delta
        brightness = brightness.coerceIn(0.1f, 1.0f)
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
    }

    override fun onPause() {
        super.onPause()
        if (!isLive) {
            player?.currentPosition?.let { pos ->
                contentId?.let { id ->
                    repository.saveResumePosition(id, pos)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        player?.release()
        player = null
    }

    private fun updateFavoriteButton() {
        contentId?.let { id ->
            val isFav = when {
                isLive -> repository.isFavoriteChannel(id)
                contentType == TYPE_MOVIE -> repository.isFavoriteMovie(id)
                contentType == TYPE_SERIES -> repository.isFavoriteSeries(id)
                else -> false
            }
            binding.btnFavorite.setImageResource(
                if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
            )
            binding.btnFavorite.setColorFilter(
                if (isFav) getColor(R.color.red_netflix) else getColor(R.color.white)
            )
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            // When controls are visible, allow normal focus navigation between buttons.
            // Only intercept directional keys for media actions when controls are hidden.
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_BUTTON_A -> {
                    if (!controlsVisible) {
                        showControls()
                        return true
                    }
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (!controlsVisible && !isLive) {
                        player?.let { it.seekTo((it.currentPosition - 10000).coerceAtLeast(0)) }
                        showControls()
                        return true
                    }
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (!controlsVisible && !isLive) {
                        player?.let { it.seekTo(it.currentPosition + 10000) }
                        showControls()
                        return true
                    }
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (!controlsVisible && isLive && channelList.isNotEmpty()) {
                        currentChannelIndex = (currentChannelIndex + 1) % channelList.size
                        switchToChannel(channelList[currentChannelIndex])
                        showControls()
                        return true
                    }
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (!controlsVisible && isLive && channelList.isNotEmpty()) {
                        currentChannelIndex = (currentChannelIndex - 1 + channelList.size) % channelList.size
                        switchToChannel(channelList[currentChannelIndex])
                        showControls()
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
