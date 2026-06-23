package com.cinepass.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cinepass.data.api.ApiClient
import com.cinepass.data.api.models.VerifyOtpRequest
import com.cinepass.utils.*
import kotlinx.coroutines.launch

class OtpActivity : AppCompatActivity() {

    private lateinit var binding: Any // TODO: Replace with ActivityOtpBinding
    private var phone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // binding = ActivityOtpBinding.inflate(layoutInflater)
        // setContentView(binding.root)
        // TODO: Uncomment above and add ViewBinding
        phone = intent.getStringExtra("phone") ?: ""
        // binding.tvPhone.text = "OTP sent to $phone"
        // binding.tvNote.text = "⚠️ Prototype mode: Check server console for OTP"
        // binding.btnVerify.setOnClickListener {
        //     val otp = binding.etOtp.text.toString().trim()
        //     if (otp.length != 6) {
        //         toast("Enter 6-digit OTP")
        //         return@setOnClickListener
        //     }
        //     verifyOtp(otp)
        // }
        // binding.btnSkip.setOnClickListener {
        //     startActivityAndClear<HomeActivity>()
        // }
    }

    private fun verifyOtp(code: String) {
        // binding.btnVerify.isEnabled = false
        // binding.progressBar.show()
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.verifyOtp(VerifyOtpRequest(phone, code))
                if (response.isSuccessful && response.body() != null) {
                    toast("Phone verified! ✅")
                    // startActivityAndClear<HomeActivity>()
                } else {
                    toast("Invalid OTP. Try again.")
                }
            } catch (e: Exception) {
                toast("Connection error")
            } finally {
                // binding.btnVerify.isEnabled = true
                // binding.progressBar.hide()
            }
        }
    }
}

// This Activity is replaced by a Compose-based OTP screen. Remove this file or use Compose navigation.
