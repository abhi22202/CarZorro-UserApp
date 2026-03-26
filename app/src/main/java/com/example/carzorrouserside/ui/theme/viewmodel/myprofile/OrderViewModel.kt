// in package com.example.carzorrouserside.ui.viewmodel.booking

package com.example.carzorrouserside.ui.viewmodel.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.booking.BookingItem
import com.example.carzorrouserside.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingUiState(
    val isLoading: Boolean = false,
    val upcomingBookings: List<BookingItem> = emptyList(),
    val completedBookings: List<BookingItem> = emptyList(),
    val cancelledBookings: List<BookingItem> = emptyList(),
    val ongoingBookings: List<BookingItem> = emptyList(),
    val error: String? = null,
    val selectedStatusTabIndex: Int = 0, // 0: Upcoming, 1: Completed, 2: Cancelled, 3: Ongoing
    val selectedCategoryTabIndex: Int = 1 // 0: Services, 1: Products
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchAllBookings()
    }

    fun fetchAllBookings() {
        fetchBookingsByStatus("upcoming")
        fetchBookingsByStatus("completed")
        fetchBookingsByStatus("cancelled")
        fetchBookingsByStatus("ongoing")
    }

    private fun fetchBookingsByStatus(status: String) {
        viewModelScope.launch {
            // Only set loading if this is the currently selected tab
            val currentTabStatus = when (_uiState.value.selectedStatusTabIndex) {
                0 -> "upcoming"
                1 -> "completed"
                2 -> "cancelled"
                3 -> "ongoing"
                else -> null
            }
            if (status == currentTabStatus) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            
            bookingRepository.getBookingListing(status).fold(
                onSuccess = { bookingData ->
                    _uiState.update { state ->
                        val newState = when (status) {
                            "upcoming" -> state.copy(upcomingBookings = bookingData.bookings)
                            "completed" -> state.copy(completedBookings = bookingData.bookings)
                            "cancelled" -> state.copy(cancelledBookings = bookingData.bookings)
                            "ongoing" -> state.copy(ongoingBookings = bookingData.bookings)
                            else -> state
                        }
                        // Only clear loading if this was the current tab
                        if (status == currentTabStatus) {
                            newState.copy(isLoading = false)
                        } else {
                            newState
                        }
                    }
                },
                onFailure = { exception ->
                    // Only set error if this is the currently selected tab
                    if (status == currentTabStatus) {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to fetch $status bookings"
                            )
                        }
                    }
                }
            )
        }
    }

    fun onStatusTabSelected(index: Int) {
        _uiState.update { it.copy(selectedStatusTabIndex = index, error = null) }
        // Refresh data for the selected tab
        val status = when (index) {
            0 -> "upcoming"
            1 -> "completed"
            2 -> "cancelled"
            3 -> "ongoing"
            else -> null
        }
        status?.let { fetchBookingsByStatus(it) }
    }

    fun onCategoryTabSelected(index: Int) {
        _uiState.update { it.copy(selectedCategoryTabIndex = index) }
    }
}