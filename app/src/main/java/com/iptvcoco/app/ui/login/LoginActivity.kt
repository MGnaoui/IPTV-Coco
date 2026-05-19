package com.iptvcoco.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.iptvcoco.app.R
import com.iptvcoco.app.databinding.ActivityLoginBinding
import com.iptvcoco.app.model.M3UAccount
import com.iptvcoco.app.ui.main.MainActivity
import com.iptvcoco.app.util.AppLogger
import com.iptvcoco.app.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private var navigateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.isLoggedIn()) {
            navigateToMain()
            return
        }

        binding.btnLogin.setOnClickListener {
            val url = binding.etUrl.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (url.isEmpty()) {
                binding.etUrl.error = getString(R.string.url_required)
                return@setOnClickListener
            }
            if (username.isEmpty()) {
                binding.etUsername.error = getString(R.string.username_required)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = getString(R.string.password_required)
                return@setOnClickListener
            }

            val type = when (binding.rgLoginType.checkedRadioButtonId) {
                R.id.rbXtream -> M3UAccount.AccountType.XTREAM
                else -> M3UAccount.AccountType.M3U
            }

            var fixedUrl = url
            if (!fixedUrl.startsWith("http://", ignoreCase = true) && !fixedUrl.startsWith("https://", ignoreCase = true)) {
                fixedUrl = "http://$fixedUrl"
            }

            viewModel.login(fixedUrl, username, password, type)
        }

        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvConnectionStatus.visibility = View.GONE
                    binding.btnLogin.isEnabled = false
                }
                is LoginViewModel.LoginState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.tvConnectionStatus.visibility = View.VISIBLE
                    binding.tvConnectionStatus.text = getString(R.string.connected)
                    binding.tvConnectionStatus.setTextColor(ContextCompat.getColor(this, R.color.accent))
                    navigateRunnable = Runnable { navigateToMain() }
                    binding.tvConnectionStatus.postDelayed(navigateRunnable!!, 600)
                }
                is LoginViewModel.LoginState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.tvConnectionStatus.visibility = View.VISIBLE
                    binding.tvConnectionStatus.text = getString(R.string.connection_error)
                    binding.tvConnectionStatus.setTextColor(ContextCompat.getColor(this, R.color.error_red))
                    AppLogger.logEvent("Login error shown to user: ${state.message}")
                    showRetryDialog(state.message)
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvConnectionStatus.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }

    private fun showRetryDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.connection_failed)
            .setMessage(getString(R.string.connection_failed_message, message))
            .setPositiveButton(R.string.try_again) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        navigateRunnable?.let {
            binding.tvConnectionStatus.removeCallbacks(it)
        }
    }
}
