package com.rs3films.app.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rs3films.app.data.api.ApiClient
import com.rs3films.app.data.api.models.VerifyOtpRequest
import com.rs3films.app.databinding.ActivityOtpBinding
import com.rs3films.app.ui.home.HomeActivity
import com.rs3films.app.utils.*
import kotlinx.coroutines.launch

class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private var phone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phone = intent.getStringExtra("phone") ?: ""

        binding.tvPhone.text = "OTP sent to $phone"
        // Prototype note: OTP is printed in server logs since Twilio not configured
        binding.tvNote.text = "⚠️ Prototype mode: Check server console for OTP"

        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()
            if (otp.length != 6) {
                toast("Enter 6-digit OTP")
                return@setOnClickListener
            }
            verifyOtp(otp)
        }

        binding.btnSkip.setOnClickListener {
            // Allow skip in prototype mode
            startActivityAndClear<HomeActivity>()
        }
    }

    private fun verifyOtp(code: String) {
        binding.btnVerify.isEnabled = false
        binding.progressBar.show()

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.verifyOtp(VerifyOtpRequest(phone, code))
                if (response.isSuccessful && response.body()?.success == true) {
                    toast("Phone verified! ✅")
                    startActivityAndClear<HomeActivity>()
                } else {
                    toast("Invalid OTP. Try again.")
                }
            } catch (e: Exception) {
                toast("Connection error")
            } finally {
                binding.btnVerify.isEnabled = true
                binding.progressBar.hide()
            }
        }
    }
}
