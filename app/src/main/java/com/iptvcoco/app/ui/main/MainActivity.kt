package com.iptvcoco.app.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
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
import com.iptvcoco.app.util.AppLogger
import com.iptvcoco.app.util.DeviceUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: IPTVRepository
    private var currentNavItemId: Int = -1
    private var isTvMode = false

    private val sidebarButtons = mutableMapOf<Int, ImageButton?>()
    private val sidebarIndicators = mutableMapOf<Int, View?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isTvMode = DeviceUtils.isTv(this)

        repository = (application as IPTVCocoApplication).repository
        if (repository.isLoggedIn() && repository.getCategories(com.iptvcoco.app.model.ContentType.LIVE).isEmpty()) {
            lifecycleScope.launch {
                repository.reload()
            }
        }

        if (savedInstanceState == null) {
            switchTab(R.id.nav_live)
        } else {
            currentNavItemId = savedInstanceState.getInt("currentNavItemId", R.id.nav_live)
            val transaction = supportFragmentManager.beginTransaction()
            listOf(R.id.nav_live, R.id.nav_movies, R.id.nav_series, R.id.nav_favorites).forEach { id ->
                supportFragmentManager.findFragmentByTag(tagFor(id))?.let { fragment ->
                    if (id == currentNavItemId) transaction.show(fragment) else transaction.hide(fragment)
                }
            }
            transaction.commit()
        }

        if (isTvMode) {
            setupTvSidebar()
        } else {
            setupPhoneBottomNav()
        }
    }

    private fun setupPhoneBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            switchTab(item.itemId)
            true
        }
        binding.btnSettings.setOnClickListener {
            showUserMenuDialog()
        }
    }

    private fun setupTvSidebar() {
        sidebarButtons[R.id.nav_live] = findViewById(R.id.btnSidebarLive)
        sidebarButtons[R.id.nav_movies] = findViewById(R.id.btnSidebarMovies)
        sidebarButtons[R.id.nav_series] = findViewById(R.id.btnSidebarSeries)
        sidebarButtons[R.id.nav_favorites] = findViewById(R.id.btnSidebarFavorites)

        sidebarIndicators[R.id.nav_live] = findViewById(R.id.indicatorLive)
        sidebarIndicators[R.id.nav_movies] = findViewById(R.id.indicatorMovies)
        sidebarIndicators[R.id.nav_series] = findViewById(R.id.indicatorSeries)
        sidebarIndicators[R.id.nav_favorites] = findViewById(R.id.indicatorFavorites)

        sidebarButtons.forEach { (itemId, btn) ->
            btn?.setOnClickListener {
                switchTab(itemId)
            }
        }

        findViewById<ImageButton?>(R.id.btnSidebarSettings)?.setOnClickListener {
            showUserMenuDialog()
        }

        updateSidebarSelection()
    }

    private fun updateSidebarSelection() {
        if (!isTvMode) return
        sidebarButtons.forEach { (itemId, btn) ->
            val isSelected = itemId == currentNavItemId
            btn?.setColorFilter(
                if (isSelected) getColor(R.color.accent) else getColor(R.color.gray_text)
            )
            sidebarIndicators[itemId]?.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentNavItemId", currentNavItemId)
    }

    private fun switchTab(itemId: Int) {
        if (itemId == currentNavItemId) return

        val transaction = supportFragmentManager.beginTransaction()

        supportFragmentManager.findFragmentByTag(tagFor(currentNavItemId))?.let {
            transaction.hide(it)
        }

        val tag = tagFor(itemId)
        var fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = createFragment(itemId)
            transaction.add(R.id.container, fragment, tag)
        }
        transaction.show(fragment)
        transaction.commit()

        currentNavItemId = itemId

        if (isTvMode) {
            updateSidebarSelection()
        } else {
            binding.bottomNav.selectedItemId = itemId
        }
    }

    private fun createFragment(itemId: Int): Fragment {
        return when (itemId) {
            R.id.nav_live -> LiveTVFragment()
            R.id.nav_movies -> MoviesFragment()
            R.id.nav_series -> SeriesFragment()
            R.id.nav_favorites -> FavoritesFragment()
            else -> throw IllegalArgumentException("Unknown tab: $itemId")
        }
    }

    private fun tagFor(itemId: Int): String = "tab_$itemId"

    private fun shareLogs() {
        val logFile = AppLogger.getLogFile()
        if (logFile == null || !logFile.exists()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.user_menu)
                .setMessage("No logs available yet.")
                .setPositiveButton(R.string.close, null)
                .show()
            return
        }

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            logFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "IPTV Coco Logs")
            putExtra(Intent.EXTRA_TEXT, "Please find the attached log file.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Logs"))
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
            .setNeutralButton(R.string.share_logs) { _, _ ->
                shareLogs()
            }
            .show()
    }
}
