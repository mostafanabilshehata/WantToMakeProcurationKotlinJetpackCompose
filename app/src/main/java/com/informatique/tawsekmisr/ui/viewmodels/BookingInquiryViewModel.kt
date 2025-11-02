package com.informatique.tawsekmisr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.informatique.tawsekmisr.data.model.InquireReservation
import com.informatique.tawsekmisr.domain.strategy.BookingInquiryStrategy
import com.informatique.tawsekmisr.domain.strategy.ReservationFilter
import com.informatique.tawsekmisr.domain.strategy.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingInquiryViewModel @Inject constructor(
    private val bookingInquiryStrategy: BookingInquiryStrategy
) : ViewModel() {

    // National ID validation state
    private val _nationalIdValidation = MutableStateFlow<NationalIdValidationState>(NationalIdValidationState.Idle)
    val nationalIdValidation: StateFlow<NationalIdValidationState> = _nationalIdValidation.asStateFlow()

    // National ID input
    private val _nationalId = MutableStateFlow("")
    val nationalId: StateFlow<String> = _nationalId.asStateFlow()

    private val _nationalIdError = MutableStateFlow<String?>(null)
    val nationalIdError: StateFlow<String?> = _nationalIdError.asStateFlow()

    // Reservations list
    private val _reservations = MutableStateFlow<List<InquireReservation>>(emptyList())
    val reservations: StateFlow<List<InquireReservation>> = _reservations.asStateFlow()

    // Current filter
    private val _currentFilter = MutableStateFlow(ReservationFilter.ALL)
    val currentFilter: StateFlow<ReservationFilter> = _currentFilter.asStateFlow()

    // Filtered reservations
    val filteredReservations: StateFlow<List<InquireReservation>> = combine(
        _reservations,
        _currentFilter
    ) { reservations, filter ->
        bookingInquiryStrategy.filterReservations(reservations, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Track if inquiry has been performed
    private val _hasPerformedInquiry = MutableStateFlow(false)
    val hasPerformedInquiry: StateFlow<Boolean> = _hasPerformedInquiry.asStateFlow()

    /**
     * Update National ID
     */
    fun updateNationalId(nationalId: String) {
        if (nationalId.length <= 14 && nationalId.all { it.isDigit() }) {
            _nationalId.value = nationalId
            _nationalIdError.value = null
            resetNationalIdValidation()

            // Reset inquiry state when national ID changes
            _hasPerformedInquiry.value = false

            // Auto-validate when 14 digits are entered
            if (nationalId.length == 14) {
                validateNationalId(nationalId)
            }
        }
    }

    /**
     * Validate National ID when user types 14 digits
     */
    private fun validateNationalId(nationalId: String) {
        if (nationalId.length != 14) return

        viewModelScope.launch {
            _nationalIdValidation.value = NationalIdValidationState.Loading

            bookingInquiryStrategy.validateNationalIdWithApi(nationalId)
                .onSuccess {
                    _nationalIdValidation.value = NationalIdValidationState.Valid
                }
                .onFailure { error ->
                    _nationalIdValidation.value = NationalIdValidationState.Error(
                        error.message ?: "فشل في التحقق من الرقم القومي"
                    )
                }
        }
    }

    /**
     * Reset national ID validation state
     */
    private fun resetNationalIdValidation() {
        _nationalIdValidation.value = NationalIdValidationState.Idle
    }

    /**
     * Validate form before inquiry
     */
    fun validateForm(): Boolean {
        val validationResult = bookingInquiryStrategy.validateNationalIdFormat(_nationalId.value)

        return when (validationResult) {
            is ValidationResult.Success -> {
                if (_nationalIdValidation.value !is NationalIdValidationState.Valid) {
                    _nationalIdError.value = "يرجى الانتظار للتحقق من صحة الرقم القومي"
                    false
                } else {
                    true
                }
            }
            is ValidationResult.Error -> {
                _nationalIdError.value = validationResult.message
                false
            }
        }
    }

    /**
     * Fetch reservations for the national ID
     */
    fun fetchReservations() {
        if (!validateForm()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _reservations.value = emptyList()
            _hasPerformedInquiry.value = true // Mark that inquiry has been performed

            bookingInquiryStrategy.fetchReservations(_nationalId.value)
                .onSuccess { reservations ->
                    // Sort by date (newest first)
                    val sortedReservations = bookingInquiryStrategy.sortReservationsByDate(reservations)
                    _reservations.value = sortedReservations
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _error.value = error.message ?: "فشل في جلب الحجوزات"
                    _isLoading.value = false
                }
        }
    }

    /**
     * Change reservation filter
     */
    fun setFilter(filter: ReservationFilter) {
        _currentFilter.value = filter
    }

    /**
     * Get reservation statistics
     */
    fun getReservationStats() = bookingInquiryStrategy.getReservationStats(_reservations.value)

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}
