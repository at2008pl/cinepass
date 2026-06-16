// ─── SeatSelectionActivity.kt ─────────────────────────────────────
package com.rs3films.app.ui.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rs3films.app.data.prefs.UserPrefs
import com.rs3films.app.databinding.ActivitySeatSelectionBinding
import com.rs3films.app.databinding.ItemSeatBinding
import com.rs3films.app.utils.*

class SeatSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeatSelectionBinding
    private lateinit var prefs: UserPrefs
    private val selectedSeats = mutableListOf<String>()
    private var showtimeId = ""
    private var pricePerSeat = 0.0
    private var format = ""
    private var showTime = ""

    // Generate seats: rows A-G, columns 1-8 (some pre-booked)
    private val bookedSeats = setOf("A1","A2","C3","C4","D6","D7","E1","E8","F2","F5","G3","G7")
    private val allSeats = ('A'..'G').flatMap { row -> (1..8).map { col -> "$row$col" } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeatSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = UserPrefs(this)

        showtimeId = intent.getStringExtra("showtime_id") ?: ""
        pricePerSeat = intent.getDoubleExtra("price", 350.0)
        format = intent.getStringExtra("format") ?: "2D"
        showTime = intent.getStringExtra("time") ?: ""

        supportActionBar?.title = "Select Seats"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupUI()
    }

    private fun setupUI() {
        binding.apply {
            tvFormat.text = format
            tvShowtime.text = showTime.toDisplayDate()
            tvPricePerSeat.text = "₹${pricePerSeat.toInt()} / seat"

            // Screen indicator
            tvScreen.text = "◄─────── SCREEN ───────►"

            // Seat grid
            rvSeats.layoutManager = GridLayoutManager(this@SeatSelectionActivity, 9)
            rvSeats.adapter = SeatAdapter(allSeats, bookedSeats, selectedSeats) {
                updateSummary()
            }

            // Legend
            tvLegendAvailable.text = "○ Available"
            tvLegendSelected.text = "● Selected"
            tvLegendBooked.text = "✕ Booked"

            // Coins toggle
            val coinDiscount = minOf(
                prefs.coins * 0.1,
                pricePerSeat * selectedSeats.size * 0.3
            )
            tvCoinsAvailable.text = "You have ⬡ ${prefs.coins.withCommas()} coins"
            switchCoins.setOnCheckedChangeListener { _, _ -> updateSummary() }

            btnConfirm.setOnClickListener {
                if (selectedSeats.isEmpty()) {
                    toast("Please select at least one seat")
                    return@setOnClickListener
                }
                val coinsToUse = if (switchCoins.isChecked) {
                    val maxCoins = (pricePerSeat * selectedSeats.size * 0.3 / 0.1).toInt()
                    minOf(prefs.coins, maxCoins)
                } else 0

                startActivity<BookingConfirmActivity> {
                    putExtra("showtime_id", showtimeId)
                    putExtra("seats", selectedSeats.toTypedArray())
                    putExtra("coins_to_use", coinsToUse)
                    putExtra("price_per_seat", pricePerSeat)
                    putExtra("format", format)
                    putExtra("show_time", showTime)
                }
            }
        }

        updateSummary()
    }

    private fun updateSummary() {
        binding.apply {
            val total = pricePerSeat * selectedSeats.size
            tvSelectedSeats.text = if (selectedSeats.isEmpty()) "No seats selected"
            else "Selected: ${selectedSeats.sorted().joinToString(", ")}"

            val coinsToUse = if (switchCoins.isChecked) {
                val maxCoins = (total * 0.3 / 0.1).toInt()
                minOf(prefs.coins, maxCoins)
            } else 0

            val coinDiscount = coinsToUse * 0.1
            val finalPrice = total - coinDiscount

            tvTotalPrice.text = "Total: ₹${total.toInt()}"
            tvDiscount.text = if (coinsToUse > 0) "Coin discount: -₹${coinDiscount.toInt()}" else ""
            tvFinalPrice.text = "Pay: ₹${finalPrice.toInt()}"
            btnConfirm.isEnabled = selectedSeats.isNotEmpty()
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

// ─── Seat Adapter ─────────────────────────────────────────────────
class SeatAdapter(
    private val seats: List<String>,
    private val booked: Set<String>,
    private val selected: MutableList<String>,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<SeatAdapter.VH>() {

    inner class VH(val binding: ItemSeatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSeatBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = seats.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val seat = seats[position]
        val isBooked = seat in booked
        val isSelected = seat in selected

        holder.binding.apply {
            // Show row label in first column
            if (seat.endsWith("1")) {
                tvSeatLabel.text = seat.first().toString()
                tvSeatLabel.show()
            } else {
                tvSeatLabel.hide()
            }

            btnSeat.text = if (isBooked) "✕" else seat.drop(1)
            btnSeat.isEnabled = !isBooked

            btnSeat.setBackgroundColor(
                when {
                    isBooked   -> 0xFFCCCCCC.toInt()
                    isSelected -> 0xFFFF3C5F.toInt()
                    else       -> 0xFF1E1E32.toInt()
                }
            )

            btnSeat.setOnClickListener {
                if (isSelected) selected.remove(seat) else {
                    if (selected.size >= 6) {
                        // Max 6 seats
                        return@setOnClickListener
                    }
                    selected.add(seat)
                }
                notifyItemChanged(position)
                onUpdate()
            }
        }
    }
}

// ─── BookingConfirmActivity.kt ────────────────────────────────────
package com.rs3films.app.ui.booking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rs3films.app.data.api.ApiClient
import com.rs3films.app.data.api.models.BookTicketRequest
import com.rs3films.app.data.prefs.UserPrefs
import com.rs3films.app.databinding.ActivityBookingConfirmBinding
import com.rs3films.app.ui.ticket.QRTicketActivity
import com.rs3films.app.utils.*
import kotlinx.coroutines.launch

class BookingConfirmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingConfirmBinding
    private lateinit var prefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = UserPrefs(this)

        val showtimeId = intent.getStringExtra("showtime_id") ?: ""
        val seats = intent.getStringArrayExtra("seats")?.toList() ?: emptyList()
        val coinsToUse = intent.getIntExtra("coins_to_use", 0)
        val pricePerSeat = intent.getDoubleExtra("price_per_seat", 350.0)
        val format = intent.getStringExtra("format") ?: "2D"
        val showTime = intent.getStringExtra("show_time") ?: ""

        supportActionBar?.title = "Confirm Booking"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val total = pricePerSeat * seats.size
        val coinDiscount = coinsToUse * 0.1
        val final = total - coinDiscount

        binding.apply {
            tvMovie.text = Constants.MOVIE_TITLE
            tvFormat.text = format
            tvShowtime.text = showTime.toDisplayDate()
            tvSeats.text = seats.sorted().joinToString(", ")
            tvSeatCount.text = "${seats.size} seat(s)"
            tvOriginalPrice.text = "₹${total.toInt()}"
            tvCoinDiscount.text = if (coinsToUse > 0) "-₹${coinDiscount.toInt()} (${coinsToUse} coins)" else "₹0"
            tvFinalPrice.text = "₹${final.toInt()}"
            tvCoinsUsed.text = "$coinsToUse coins will be deducted from wallet"

            btnConfirmPay.setOnClickListener {
                confirmBooking(showtimeId, seats, coinsToUse)
            }
        }
    }

    private fun confirmBooking(showtimeId: String, seats: List<String>, coinsToUse: Int) {
        binding.btnConfirmPay.isEnabled = false
        binding.progressBar.show()

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.bookTicket(
                    token = prefs.bearerToken,
                    request = BookTicketRequest(
                        showtimeId = showtimeId,
                        seats = seats,
                        coinsToUse = coinsToUse,
                        paymentId = "pay_prototype_${System.currentTimeMillis()}" // Mock payment
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    prefs.coins -= coinsToUse // Update cached coins

                    toastLong("🎟 Booking confirmed! Enjoy the movie!")

                    startActivityAndClear<QRTicketActivity> {
                        putExtra("ticket_id", data.ticket.id)
                        putExtra("qr_code_data", data.ticket.qrCode)
                        putExtra("movie_title", Constants.MOVIE_TITLE)
                        putExtra("seats", seats.toTypedArray())
                        putExtra("format", data.ticket.format)
                        putExtra("venue", data.ticket.venue)
                        putExtra("show_time", data.ticket.showtime)
                        putExtra("final_price", data.ticket.finalPrice)
                        putExtra("coins_saved", data.pricing.coinsSaved)
                        putExtra("total_saved", data.pricing.totalDiscount)
                    }
                } else {
                    toast(response.body()?.message ?: "Booking failed")
                }
            } catch (e: Exception) {
                toast("Connection error")
            } finally {
                binding.btnConfirmPay.isEnabled = true
                binding.progressBar.hide()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
