package com.example.carzorrouserside.data.model.homescreen

data class HomepageService(
    val id: Int,
    val name: String,
    val image: String
)

data class HomepageServiceResponse(
    val success: Boolean,
    val message: String,
    val data: List<HomepageService>
)
data class HomepageServiceUiState(
    val isLoading: Boolean = false,
    val services: List<HomepageService> = emptyList(),
    val error: String? = null,
    val activeBids: List<Bid> = emptyList(),
    val hasActiveBooking: Boolean = false,
    val hasRebid: Boolean = false,
    val activeBookingMessage: String? = null,
    val activeBookingData: ActiveBookingData? = null,  // 👈 add this
    val rebidOffers: Map<String, Any>? = null,
    val bookingStatus: String? = null,
    val backendResponded: Boolean = false
)