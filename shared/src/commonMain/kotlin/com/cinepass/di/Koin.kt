package com.cinepass.di

import org.koin.core.context.startKoin
import org.koin.dsl.module
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.cinepass.utils.Constants
import com.cinepass.data.prefs.UserPrefs
import com.cinepass.data.api.ApiService
import com.cinepass.data.api.Rs3ApiService
import com.cinepass.data.repository.*
import com.cinepass.ui.home.HomeViewModel
import com.cinepass.ui.wallet.WalletViewModel
import com.cinepass.ui.tickets.TicketViewModel
import com.cinepass.ui.profile.ProfileViewModel
import com.cinepass.ui.profile.ReferralViewModel
import com.cinepass.ui.profile.ReferralTreeViewModel
import com.cinepass.ui.events.EventViewModel
import com.cinepass.ui.offers.OfferDetailViewModel

val appModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get<Json>())
            }
            defaultRequest {
                url(Constants.BASE_URL)
            }
        }
    }

    single { ApiService(get()) }
    single { Rs3ApiService(get()) }
    single { UserPrefs() }

    single { AuthRepository(get(), get()) }
    single { EventRepository(get(), get()) }
    single { HomeFeedRepository(get()) }
    single { ProfileRepository(get(), get()) }
    single { ReferralRepository(get(), get()) }
    single { TicketRepository(get(), get()) }
    single { WalletRepository(get(), get()) }

    // Factories for ViewModels (for common access)
    factory { HomeViewModel(get()) }
    factory { WalletViewModel(get()) }
    factory { TicketViewModel(get(), get(), get()) }
    factory { ProfileViewModel(get()) }
    factory { ReferralViewModel(get()) }
    factory { ReferralTreeViewModel(get()) }
    factory { EventViewModel(get()) }
    factory { OfferDetailViewModel(get()) }
}

fun initKoin() = initKoin {}

fun initKoin(appDeclaration: org.koin.dsl.KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule)
    }
