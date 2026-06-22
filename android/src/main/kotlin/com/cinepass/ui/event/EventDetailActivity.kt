package com.cinepass.ui.event

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cinepass.data.api.ApiClient
import com.cinepass.data.api.models.ShowtimeData
import com.cinepass.data.prefs.UserPrefs
import com.cinepass.utils.*
import kotlinx.coroutines.launch

class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: Any // TODO: Replace with ActivityEventDetailBinding
    private lateinit var prefs: UserPrefs
    private var eventId = ""
    private var selectedShowtime: ShowtimeData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // binding = ActivityEventDetailBinding.inflate(layoutInflater)
        // setContentView(binding.root)
        // TODO: Uncomment above and add ViewBinding
        prefs = UserPrefs(this)
        eventId = intent.getStringExtra("event_id") ?: Constants.MOVIE_EVENT_ID
        // supportActionBar?.title = "Movie Event"
        // supportActionBar?.setDisplayHomeAsUpEnabled(true)
        loadEvent()
        registerForEvent()
    }

    private fun loadEvent() {
        // binding.progressBar.show()
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getEvent(eventId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val event = response.body()!!.data!!
                    displayEvent(event.showtimes ?: emptyList())
                    // binding.apply { ... UI updates ... }
                } else {
                    toast("Failed to load event")
                    finish()
                }
            } catch (e: Exception) {
                toast("Failed to load event")
                finish()
            } finally {
                // binding.progressBar.hide()
            }
        }
    }

    private fun displayEvent(showtimes: List<ShowtimeData>) {
        if (showtimes.isEmpty()) {
            // binding.tvNoShowtimes.show()
            return
        }
        // binding.rvShowtimes.apply { ... setup adapter ... }
        // binding.btnBookNow.setOnClickListener { ... }
    }

    private fun registerForEvent() {
        lifecycleScope.launch {
            try {
                ApiClient.apiService.registerForEvent(eventId, prefs.bearerToken)
            } catch (e: Exception) { /* Silent */ }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

// TODO: Implement ShowtimeAdapter as inner class or separate file as needed
