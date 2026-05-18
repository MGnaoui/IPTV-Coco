package com.iptvcoco.app.ui.detail

import com.iptvcoco.app.IPTVCocoApplication
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.iptvcoco.app.R
import com.iptvcoco.app.databinding.ActivityMovieDetailBinding
import com.iptvcoco.app.repository.IPTVRepository
import com.iptvcoco.app.ui.player.PlayerActivity

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var repository: IPTVRepository
    private var movieId: String? = null
    private var movie: com.iptvcoco.app.model.Movie? = null

    companion object {
        const val EXTRA_MOVIE_ID = "movie_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = IPTVCocoApplication.instance.repository
        movieId = intent.getStringExtra(EXTRA_MOVIE_ID)

        movieId?.let { id ->
            movie = repository.getMovieById(id)
            movie?.let { displayMovie(it) }
        }
    }

    private fun displayMovie(movie: com.iptvcoco.app.model.Movie) {
        this.movie = movie
        binding.tvTitle.text = movie.title
        binding.tvRuntime.text = movie.runtime.ifBlank { "N/A" }
        binding.tvRating.text = movie.rating.ifBlank { "N/A" }
        binding.tvYear.text = movie.year.ifBlank { "" }
        binding.tvPlot.text = movie.plot.ifBlank { "No description available." }
        binding.tvCast.text = movie.cast.ifBlank { "N/A" }

        if (!movie.banner.isNullOrBlank()) {
            Glide.with(this)
                .load(movie.banner)
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_movie)
                .error(R.drawable.ic_movie)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(binding.ivBanner)
        } else {
            binding.ivBanner.setImageResource(R.drawable.ic_movie)
        }

        val resumePos = repository.getResumePosition(movie.id)
        if (resumePos > 10000) {
            binding.btnContinue.visibility = View.VISIBLE
        }

        updateFavoriteButton()
        binding.btnFavorite.setOnClickListener {
            repository.toggleFavoriteMovie(movie.id)
            updateFavoriteButton()
        }

        binding.btnStart.setOnClickListener {
            playMovie(movie, 0L)
        }

        binding.btnContinue.setOnClickListener {
            playMovie(movie, resumePos)
        }
    }

    private fun updateFavoriteButton() {
        movie?.let {
            val isFav = repository.isFavoriteMovie(it.id)
            binding.btnFavorite.setImageResource(
                if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
            )
            binding.btnFavorite.setColorFilter(
                if (isFav) getColor(R.color.favorite_active) else getColor(R.color.white)
            )
        }
    }

    private fun playMovie(movie: com.iptvcoco.app.model.Movie, position: Long) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_STREAM_URL, movie.streamUrl)
            putExtra(PlayerActivity.EXTRA_TITLE, movie.title)
            putExtra(PlayerActivity.EXTRA_TYPE, PlayerActivity.TYPE_MOVIE)
            putExtra(PlayerActivity.EXTRA_ID, movie.id)
            // Clear any existing resume position if starting from beginning
            if (position == 0L) {
                // Save 0 to override existing resume position
                repository.saveResumePosition(movie.id, 0L)
            }
        }
        startActivity(intent)
    }
}
