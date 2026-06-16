package com.rs3films.app.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rs3films.app.data.api.ApiClient
import com.rs3films.app.data.api.models.RegisterRequest
import com.rs3films.app.data.prefs.UserPrefs
import com.rs3films.app.databinding.ActivityRegisterBinding
import com.rs3films.app.ui.home.HomeActivity
import com.rs3films.app.utils.*
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var prefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = UserPrefs(this)

        // Pre-fill referral code if passed via intent
        intent.getStringExtra("referral_code")?.let {
            binding.etReferralCode.setText(it)
        }

        setupUI()
    }

    private fun setupUI() {
        binding.apply {
            tvMovieTitle.text = Constants.MOVIE_TITLE

            btnRegister.setOnClickListener {
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val referralCode = etReferralCode.text.toString().trim().ifEmpty { null }

                when {
                    name.isEmpty() -> { etName.error = "Required"; return@setOnClickListener }
                    email.isEmpty() -> { etEmail.error = "Required"; return@setOnClickListener }
                    phone.length < 10 -> { etPhone.error = "Enter valid phone"; return@setOnClickListener }
                    password.length < 8 -> { etPassword.error = "Min 8 characters"; return@setOnClickListener }
                }

                register(name, email, phone, password, referralCode)
            }

            tvLogin.setOnClickListener { finish() }

            // Referral field hint
            tilReferralCode.hint = "Referral code (optional) — earn coins for referrer!"
        }
    }

    private fun register(
        name: String, email: String, phone: String,
        password: String, referralCode: String?
    ) {
        binding.btnRegister.isEnabled = false
        binding.progressBar.show()

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.register(
                    RegisterRequest(name, email, phone, password, referralCode)
                )

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

                    val msg = if (data.referredBy != null)
                        "Welcome! Referred by ${data.referredBy} — they earned coins! 🎉"
                    else "Account created! Welcome to RS³ Films 🎬"

                    toastLong(msg)

                    // Go to OTP verification if phone not verified
                    if (!data.user.isVerified) {
                        startActivity<OtpActivity> {
                            putExtra("phone", phone)
                        }
                        finish()
                    } else {
                        startActivityAndClear<HomeActivity>()
                    }
                } else {
                    toast(response.body()?.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                toast("Connection error. Is the server running?")
            } finally {
                binding.btnRegister.isEnabled = true
                binding.progressBar.hide()
            }
        }
    }
}
