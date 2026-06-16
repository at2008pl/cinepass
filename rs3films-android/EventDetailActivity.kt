package com.rs3films.app.ui.event

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rs3films.app.data.api.ApiClient
import com.rs3films.app.data.api.models.ShowtimeData
import com.rs3films.app.data.prefs.UserPrefs
import com.rs3films.app.databinding.ActivityEventDetailBinding
import com.rs3films.app.ui.booking.SeatSelectionActivity
import com.rs3films.app.utils.*
import kotlinx.coroutines.launch

class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding
    private lateinit var prefs: UserPrefs
    private var eventId = ""
    private var selectedShowtime: ShowtimeData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = UserPrefs(this)

        eventId = intent.getStringExtra("event_id") ?: Constants.MOVIE_EVENT_ID
        supportActionBar?.title = "Movie Event"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadEvent()
        registerForEvent()
    }

    private fun loadEvent() {
        binding.progressBar.show()
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getEvent(eventId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val event = response.body()!!.data!!
                    displayEvent(event.showtimes ?: emptyList())

                    binding.apply {
                        tvTitle.text = Constants.MOVIE_TITLE
                        tvVenue.text = "📍 ${event.venue}, ${event.city}"
                        tvOrganizer.text = "By ${event.organizer.name}"
                        tvRegistered.text = "👥 ${event.count?.registrations ?: 0} fans registered"
                        tvCoinsPerRef.text = "⬡ Earn ${event.coinsPerReferral} coins per referral"
                        tvAttendBonus.text = "⬡ +${event.attendanceBonus} bonus for attending"
                        tvDescription.text = event.description ?: ""
                        tvTagline.text = Constants.MOVIE_TAGLINE
                        tvMovieInfo.text = "🎬 Dr V Ravichandran • Written & Directed by S Supreeth"
                        tvCast.text = "🎵 Shreya Ghoshal & Sonu Nigam • 🎬 Feb 2026"
                    }
                } else {
                    toast("Failed to load event")
                    finish()
                }
            } catch (e: Exception) {
                toast("Connection error")
            } finally {
                binding.progressBar.hide()
            }
        }
    }

    private fun displayEvent(showtimes: List<ShowtimeData>) {
        if (showtimes.isEmpty()) {
            binding.tvNoShowtimes.show()
            return
        }

        binding.rvShowtimes.apply {
            layoutManager = LinearLayoutManager(this@EventDetailActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = ShowtimeAdapter(showtimes) { showtime ->
                selectedShowtime = showtime
                binding.btnBookNow.isEnabled = true
                binding.btnBookNow.text = "Book ${showtime.format} — ${showtime.startTime.toDisplayTime()}"
            }
        }

        binding.btnBookNow.setOnClickListener {
            selectedShowtime?.let { showtime ->
                startActivity<SeatSelectionActivity> {
                    putExtra("showtime_id", showtime.id)
                    putExtra("format", showtime.format)
                    putExtra("price", showtime.price)
                    putExtra("time", showtime.startTime)
                    putExtra("available_seats", showtime.availableSeats)
                    putExtra("event_title", Constants.MOVIE_TITLE)
                }
            } ?: toast("Please select a showtime")
        }
    }

    private fun registerForEvent() {
        // Auto-register fan for priority access
        lifecycleScope.launch {
            try {
                ApiClient.apiService.registerForEvent(eventId, prefs.bearerToken)
                // If already registered, API returns 409 — that's fine
            } catch (e: Exception) { /* Silent */ }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

// ─── Showtime RecyclerView Adapter ────────────────────────────────
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rs3films.app.databinding.ItemShowtimeBinding

class ShowtimeAdapter(
    private val showtimes: List<ShowtimeData>,
    private val onSelect: (ShowtimeData) -> Unit
) : RecyclerView.Adapter<ShowtimeAdapter.VH>() {

    private var selectedPos = -1

    inner class VH(val binding: ItemShowtimeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemShowtimeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = showtimes.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = showtimes[position]
        holder.binding.apply {
            tvTime.text = s.startTime.toDisplayTime()
            tvFormat.text = s.format
            tvPrice.text = "₹${s.price.toInt()}"
            tvSeats.text = "${s.availableSeats} seats"

            root.isSelected = selectedPos == position
            root.setOnClickListener {
                val prev = selectedPos
                selectedPos = position
                notifyItemChanged(prev)
                notifyItemChanged(position)
                onSelect(s)
            }
        }
    }
}
