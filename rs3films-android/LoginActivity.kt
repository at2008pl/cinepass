package com.rs3films.app.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rs3films.app.data.api.ApiClient
import com.rs3films.app.data.api.models.LoginRequest
import com.rs3films.app.data.prefs.UserPrefs
import com.rs3films.app.databinding.ActivityLoginBinding
import com.rs3films.app.ui.home.HomeActivity
import com.rs3films.app.utils.*
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = UserPrefs(this)

        // Already logged in → go home
        if (prefs.isLoggedIn) {
            startActivityAndClear<HomeActivity>()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        binding.apply {
            // RS3 Films branding
            tvMovieTitle.text = Constants.MOVIE_TITLE
            tvTagline.text = Constants.MOVIE_TAGLINE

            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    toast("Please fill all fields")
                    return@setOnClickListener
                }

                login(email, password)
            }

            tvRegister.setOnClickListener {
                startActivity<RegisterActivity>()
            }

            // Pre-fill test credentials for prototype
            etEmail.setText("arjun@test.com")
            etPassword.setText("password123")
        }
    }

    private fun login(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.progressBar.show()

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.login(LoginRequest(email, password))

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    prefs.saveAuthData(
                        accessToken = data.accessToken,
                        refreshToken = data.refreshToken,
                        userId = data.user.id,
                        name = data.user.name,
                        email = data.user.email,
                        phone = data.user.phone,
                        referralCode = data.user.referralCode,
                        coins = data.user.coins,
                        isVerified = data.user.isVerified
                    )
                    toast("Welcome back, ${data.user.name}!")
                    startActivityAndClear<HomeActivity>()
                } else {
                    toast(response.body()?.message ?: "Login failed")
                }
            } catch (e: Exception) {
                toast("Connection error. Is the server running?")
            } finally {
                binding.btnLogin.isEnabled = true
                binding.progressBar.hide()
            }
        }
    }
}
