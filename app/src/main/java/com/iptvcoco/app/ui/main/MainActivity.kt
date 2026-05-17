package com.iptvcoco.app.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.iptvcoco.app.IPTVCocoApplication
import com.iptvcoco.app.R
import com.iptvcoco.app.databinding.ActivityMainBinding
import com.iptvcoco.app.repository.IPTVRepository
import com.iptvcoco.app.ui.favorites.FavoritesFragment
import com.iptvcoco.app.ui.live.LiveTVFragment
import com.iptvcoco.app.ui.login.LoginActivity
import com.iptvcoco.app.ui.movies.MoviesFragment
import com.iptvcoco.app.ui.series.SeriesFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: IPTVRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = IPTVCocoApplication.instance.repository
        if (repository.isLoggedIn() && repository.getCategories(com.iptvcoco.app.model.ContentType.LIVE).isEmpty()) {
            lifecycleScope.launch {
                repository.reload()
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LiveTVFragment())
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_live -> LiveTVFragment()
                R.id.nav_movies -> MoviesFragment()
                R.id.nav_series -> SeriesFragment()
                R.id.nav_favorites -> FavoritesFragment()
                else -> null
            }
            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, it)
                    .commit()
                true
            } ?: false
        }

        binding.btnSettings.setOnClickListener {
            showUserMenuDialog()
        }
    }

    private fun showUserMenuDialog() {
        val account = repository.getAccount()
        val username = account?.username ?: getString(R.string.not_available)
        val lastRefresh = repository.getLastRefresh()
        val refreshText = if (lastRefresh > 0L) {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(lastRefresh))
        } else {
            getString(R.string.not_available)
        }
        val isConnected = repository.hasPlaylistData()
        val statusText = if (isConnected) "✅ ${getString(R.string.connected)}" else "❌ ${getString(R.string.not_connected)}"

        val message = buildString {
            append(getString(R.string.username_label, username))
            append("\n\n")
            append(getString(R.string.last_refresh_label, refreshText))
            append("\n\n")
            append(getString(R.string.status_label, statusText))
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.user_menu)
            .setMessage(message)
            .setPositiveButton(R.string.refresh_playlist) { _, _ ->
                lifecycleScope.launch {
                    val result = repository.reload()
                    if (result.isSuccess) {
                        showUserMenuDialog()
                    } else {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle(R.string.refresh_failed)
                            .setMessage(result.exceptionOrNull()?.message ?: getString(R.string.unknown_error))
                            .setPositiveButton(R.string.try_again, null)
                            .show()
                    }
                }
            }
            .setNegativeButton(R.string.logout) { _, _ ->
                repository.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNeutralButton(R.string.close, null)
            .show()
    }
}
