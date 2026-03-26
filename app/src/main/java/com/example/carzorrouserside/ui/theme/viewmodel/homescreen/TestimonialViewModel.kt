package com.example.carzorrouserside.ui.theme.viewmodel.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carzorrouserside.data.model.homescreen.Testimonial
import com.example.carzorrouserside.data.repository.homescreen.TestimonialRepository
import com.example.carzorrouserside.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State data class
data class TestimonialUiState(
    val isLoading: Boolean = false,
    val testimonials: List<Testimonial> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TestimonialViewModel @Inject constructor(
    private val repository: TestimonialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestimonialUiState())
    val uiState: StateFlow<TestimonialUiState> = _uiState.asStateFlow()

    init {
        loadTestimonials()
    }

    fun loadTestimonials() {
        viewModelScope.launch {
            _uiState.value = TestimonialUiState(isLoading = true)
            when (val result = repository.getHomepageTestimonials()) {
                is Resource.Success -> {
                    _uiState.value = TestimonialUiState(
                        isLoading = false,
                        testimonials = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _uiState.value = TestimonialUiState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // This case is handled by the initial isLoading = true
                }
            }
        }
    }

    fun retry() {
        loadTestimonials()
    }
}