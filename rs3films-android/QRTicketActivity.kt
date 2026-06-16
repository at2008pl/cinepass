package com.rs3films.app.ui.ticket

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.rs3films.app.data.api.ApiClient
import com.rs3films.app.data.api.models.TicketWithShowtime
import com.rs3films.app.data.prefs.UserPrefs
import com.rs3films.app.databinding.ActivityQrTicketBinding
import com.rs3films.app.databinding.ItemTicketBinding
import com.rs3films.app.ui.home.HomeActivity
import com.rs3films.app.utils.*
import kotlinx.coroutines.launch

class QRTicketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrTicketBinding
    private lateinit var prefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = UserPrefs(this)

        val showList = intent.getBooleanExtra("show_list", false)

        if (showList) {
            // Show all tickets
            showTicketList()
        } else {
            // Show single newly booked ticket
            showSingleTicket()
        }
    }

    private fun showSingleTicket() {
        binding.layoutSingleTicket.show()
        binding.layoutTicketList.hide()

        val movieTitle = intent.getStringExtra("movie_title") ?: Constants.MOVIE_TITLE
        val seats = intent.getStringArrayExtra("seats")?.toList() ?: emptyList()
        val format = intent.getStringExtra("format") ?: ""
        val venue = intent.getStringExtra("venue") ?: ""
        val showTime = intent.getStringExtra("show_time") ?: ""
        val finalPrice = intent.getDoubleExtra("final_price", 0.0)
        val coinsSaved = intent.getIntExtra("coins_saved", 0)
        val totalSaved = intent.getDoubleExtra("total_saved", 0.0)
        val qrData = intent.getStringExtra("qr_code_data") ?: ""

        binding.apply {
            tvMovieTitle.text = movieTitle
            tvSeats.text = "Seats: ${seats.sorted().joinToString(", ")}"
            tvFormat.text = format
            tvVenue.text = venue
            tvShowtime.text = showTime.toDisplayDate()
            tvFinalPrice.text = "Paid: ₹${finalPrice.toInt()}"
            tvTagline.text = Constants.MOVIE_TAGLINE

            if (coinsSaved > 0) {
                tvSavings.text = "🎉 Saved ₹${totalSaved.toInt()} using $coinsSaved coins!"
                tvSavings.show()
            }

            // Generate QR code from the base64 QR data or ticket ID
            generateQR(qrData.take(200)) // truncate for QR generation

            btnBackHome.setOnClickListener {
                startActivityAndClear<HomeActivity>()
            }
        }
    }

    private fun generateQR(data: String) {
        try {
            val writer = MultiFormatWriter()
            val matrix = writer.encode(data.ifEmpty { "RS3FILMS_TICKET" }, BarcodeFormat.QR_CODE, 400, 400)
            val encoder = BarcodeEncoder()
            val bitmap: Bitmap = encoder.createBitmap(matrix)
            binding.ivQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            toast("QR generation failed")
        }
    }

    private fun showTicketList() {
        binding.layoutSingleTicket.hide()
        binding.layoutTicketList.show()
        binding.progressBar.show()

        supportActionBar?.title = "My Tickets"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getMyTickets(prefs.bearerToken)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!

                    if (data.upcoming.isEmpty() && data.past.isEmpty()) {
                        binding.tvNoTickets.show()
                    } else {
                        binding.tvUpcomingHeader.show()
                        binding.rvTickets.apply {
                            layoutManager = LinearLayoutManager(this@QRTicketActivity)
                            adapter = TicketListAdapter(data.upcoming + data.past) { ticket ->
                                // Show QR for selected ticket
                                binding.layoutTicketList.hide()
                                binding.layoutSingleTicket.show()
                                binding.tvMovieTitle.text = ticket.showtime.event.title
                                binding.tvSeats.text = "Seats: ${ticket.seats.joinToString(", ")}"
                                binding.tvVenue.text = ticket.showtime.event.venue
                                binding.tvShowtime.text = ticket.showtime.startTime.toDisplayDate()
                                binding.tvFinalPrice.text = "Paid: ₹${ticket.finalPrice.toInt()}"
                                binding.tvFormat.text = ticket.showtime.format
                                binding.tvTagline.text = Constants.MOVIE_TAGLINE
                                binding.tvSavings.hide()
                                generateQR(ticket.id)
                                binding.btnBackHome.setOnClickListener {
                                    showTicketList()
                                }
                            }
                        }
                    }
                } else {
                    toast("Failed to load tickets")
                }
            } catch (e: Exception) {
                toast("Connection error")
            } finally {
                binding.progressBar.hide()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

// ─── Ticket List Adapter ──────────────────────────────────────────
class TicketListAdapter(
    private val tickets: List<TicketWithShowtime>,
    private val onSelect: (TicketWithShowtime) -> Unit
) : RecyclerView.Adapter<TicketListAdapter.VH>() {

    inner class VH(val binding: ItemTicketBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = tickets.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = tickets[position]
        holder.binding.apply {
            tvMovieName.text = t.showtime.event.title
            tvSeats.text = t.seats.joinToString(", ")
            tvShowtime.text = t.showtime.startTime.toDisplayTime()
            tvVenue.text = t.showtime.event.venue
            tvFormat.text = t.showtime.format
            tvStatus.text = t.status
            tvPrice.text = "₹${t.finalPrice.toInt()}"

            root.setOnClickListener { onSelect(t) }
        }
    }
}
