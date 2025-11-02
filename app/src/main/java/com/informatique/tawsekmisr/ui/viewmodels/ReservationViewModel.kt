package com.informatique.tawsekmisr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.informatique.tawsekmisr.data.api.ReservationApiService
import com.informatique.tawsekmisr.data.model.*
import com.informatique.tawsekmisr.domain.strategy.ReservationStrategyFactory
import com.informatique.tawsekmisr.domain.validation.ReservationFormValidation
import com.informatique.tawsekmisr.domain.validation.ReservationFormValidationData
import com.informatique.tawsekmisr.domain.validation.ValidationErrorKeys
import com.informatique.tawsekmisr.domain.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

sealed class NationalIdValidationState {
    object Idle : NationalIdValidationState()
    object Loading : NationalIdValidationState()
    object Valid : NationalIdValidationState()
    data class Invalid(val message: String) : NationalIdValidationState()
    data class Error(val message: String) : NationalIdValidationState()
}

/**
 * Form Validation State for Reservation
 */
data class ReservationFormState(
    val nationalId: String = "",
    val nationalIdError: String? = null,
    val selectedClassification: String? = null,
    val selectedClassificationCode: String? = null,
    val classificationError: String? = null,
    val selectedType: String? = null,
    val selectedTypeCode: String? = null,
    val typeError: String? = null,
    val appointmentDate: String = "",
    val appointmentDateError: String? = null,
    val selectedTime: String? = null,
    val timeError: String? = null,
    val confirmationChecked: Boolean = false,
    val confirmationError: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationApiService: ReservationApiService,
    private val reservationFormValidation: ReservationFormValidation,
    private val reservationStrategyFactory: ReservationStrategyFactory
) : ViewModel() {

    // National ID validation state
    private val _nationalIdValidation = MutableStateFlow<NationalIdValidationState>(NationalIdValidationState.Idle)
    val nationalIdValidation: StateFlow<NationalIdValidationState> = _nationalIdValidation.asStateFlow()

    // Form state
    private val _formState = MutableStateFlow(ReservationFormState())
    val formState: StateFlow<ReservationFormState> = _formState.asStateFlow()

    // Classifications (Reservation Categories)
    private val _classifications = MutableStateFlow<List<ReservationClassification>>(emptyList())
    val classifications: StateFlow<List<ReservationClassification>> = _classifications.asStateFlow()

    private val _classificationsLoading = MutableStateFlow(false)
    val classificationsLoading: StateFlow<Boolean> = _classificationsLoading.asStateFlow()

    private val _classificationsError = MutableStateFlow<String?>(null)
    val classificationsError: StateFlow<String?> = _classificationsError.asStateFlow()

    // Types (Reservation Types based on classification)
    private val _types = MutableStateFlow<List<ReservationType>>(emptyList())
    val types: StateFlow<List<ReservationType>> = _types.asStateFlow()

    private val _typesLoading = MutableStateFlow(false)
    val typesLoading: StateFlow<Boolean> = _typesLoading.asStateFlow()

    private val _typesError = MutableStateFlow<String?>(null)
    val typesError: StateFlow<String?> = _typesError.asStateFlow()

    // Office Agenda (Available dates and time slots)
    private val _officeAgenda = MutableStateFlow<OfficeAgendaResponse?>(null)
    val officeAgenda: StateFlow<OfficeAgendaResponse?> = _officeAgenda.asStateFlow()

    private val _agendaLoading = MutableStateFlow(false)
    val agendaLoading: StateFlow<Boolean> = _agendaLoading.asStateFlow()

    private val _agendaError = MutableStateFlow<String?>(null)
    val agendaError: StateFlow<String?> = _agendaError.asStateFlow()

    // Reservation Submission
    private val _reservationSubmitting = MutableStateFlow(false)
    val reservationSubmitting: StateFlow<Boolean> = _reservationSubmitting.asStateFlow()

    private val _reservationSuccess = MutableStateFlow<ReserveProcResponse?>(null)
    val reservationSuccess: StateFlow<ReserveProcResponse?> = _reservationSuccess.asStateFlow()

    private val _reservationError = MutableStateFlow<String?>(null)
    val reservationError: StateFlow<String?> = _reservationError.asStateFlow()

    /**
     * Validate National ID when user types 14 digits
     */
    fun validateNationalId(nationalId: String) {
        if (nationalId.length != 14) return

        viewModelScope.launch {
            _nationalIdValidation.value = NationalIdValidationState.Loading

            reservationApiService.validateNationalId(nationalId)
                .onSuccess { response ->
                    if (response.statusCode == "1") {
                        _nationalIdValidation.value = NationalIdValidationState.Valid
                    } else {
                        _nationalIdValidation.value = NationalIdValidationState.Invalid(
                            response.statusDesc
                        )
                    }
                }
                .onFailure { error ->
                    _nationalIdValidation.value = NationalIdValidationState.Error(
                        error.message ?: "Failed to validate National ID"
                    )
                }
        }
    }

    /**
     * Load reservation classifications for an office
     */
    fun loadClassifications(orgUnitId: String) {
        viewModelScope.launch {
            _classificationsLoading.value = true
            _classificationsError.value = null

            reservationApiService.getClassificationsByOffice(orgUnitId)
                .onSuccess { classifications ->
                    _classifications.value = classifications
                    _classificationsLoading.value = false
                }
                .onFailure { error ->
                    _classificationsError.value = error.message ?: "Failed to load classifications"
                    _classificationsLoading.value = false
                }
        }
    }

    /**
     * Load reservation types based on selected classification
     */
    fun loadTypes(orgUnitId: String, categoryCode: String) {
        viewModelScope.launch {
            _typesLoading.value = true
            _typesError.value = null
            _types.value = emptyList() // Clear previous types

            reservationApiService.getTypesByClassification(orgUnitId, categoryCode)
                .onSuccess { types ->
                    _types.value = types
                    _typesLoading.value = false
                }
                .onFailure { error ->
                    _typesError.value = error.message ?: "Failed to load reservation types"
                    _typesLoading.value = false
                }
        }
    }

    /**
     * Load office agenda (available dates and time slots)
     */
    fun loadOfficeAgenda(orgUnitId: String) {
        viewModelScope.launch {
            _agendaLoading.value = true
            _agendaError.value = null

            reservationApiService.getOfficeAgenda(orgUnitId)
                .onSuccess { agenda ->
                    _officeAgenda.value = agenda
                    _agendaLoading.value = false
                }
                .onFailure { error ->
                    _agendaError.value = error.message ?: "Failed to load office agenda"
                    _agendaLoading.value = false
                }
        }
    }

    /**
     * Get available dates from agenda
     */
    fun getAvailableDates(): List<String> {
        return _officeAgenda.value?.officeAgendaData?.reservationDate?.map { it.datereserved } ?: emptyList()
    }

    /**
     * Get available dates as milliseconds for date picker
     */
    fun getAvailableDatesInMillis(): Set<Long> {
        val dates = getAvailableDates()
        // API returns dates in yyyy-MM-dd format
        val result = dates.mapNotNull { dateString ->
            try {
                // Parse date in yyyy-MM-dd format
                val parts = dateString.split("-")
                if (parts.size == 3) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1 // Calendar months are 0-based
                    val day = parts[2].toInt()

                    // Create UTC calendar for date picker compatibility
                    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    utcCalendar.clear()
                    utcCalendar.set(year, month, day, 0, 0, 0)
                    utcCalendar.set(Calendar.MILLISECOND, 0)

                    val timestamp = utcCalendar.timeInMillis
                    println("📅 Parsed date: $dateString -> Year: $year, Month: $month, Day: $day -> $timestamp")
                    timestamp
                } else {
                    println("❌ Invalid date format: $dateString")
                    null
                }
            } catch (e: Exception) {
                println("❌ Failed to parse date: $dateString - ${e.message}")
                e.printStackTrace()
                null
            }
        }.toSet()
        println("📅 Total available dates: ${result.size}")
        println("📅 Available dates timestamps: $result")
        return result
    }

    /**
     * Convert date string (dd/MM/yyyy) to milliseconds
     */
    fun dateStringToMillis(dateString: String): Long? {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateFormat.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert milliseconds to date string (dd/MM/yyyy)
     */
    fun millisToDateString(millis: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date(millis))
    }

    /**
     * Get time slots for a specific date
     * Returns either periods (9:00-11:30) or specific times (12:30 PM) based on vipFlag
     */
    fun getTimeSlotsForDate(date: String): List<String> {
        println("🔍 Getting time slots for date: $date")

        // Convert the selected date (dd/MM/yyyy) to API format (yyyy-MM-dd) for comparison
        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val dateToMatch = try {
            val parsedDate = displayDateFormat.parse(date)
            val converted = parsedDate?.let { apiDateFormat.format(it) }
            println("🔍 Converted date: $date -> $converted")
            converted
        } catch (e: Exception) {
            println("❌ Date conversion error: ${e.message}")
            null
        }

        val agenda = _officeAgenda.value
        println("🔍 Agenda available: ${agenda != null}")
        println("🔍 VipFlag: ${agenda?.vipFlag}")
        println("🔍 Available dates in agenda: ${agenda?.officeAgendaData?.reservationDate?.map { it.datereserved }}")

        val reservationDate = agenda?.officeAgendaData?.reservationDate
            ?.find { it.datereserved == dateToMatch }

        println("🔍 Found reservation date: ${reservationDate != null}")
        println("🔍 Reservation date details: ${reservationDate?.datereserved}")
        println("🔍 Has reservePeriods: ${reservationDate?.reservePeriods != null}")
        println("🔍 Has reservationTime: ${reservationDate?.reservationTime != null}")

        if (reservationDate == null) {
            println("❌ No reservation date found for: $dateToMatch")
            return emptyList()
        }

        val vipFlag = agenda?.vipFlag
        println("🔍 Using vipFlag: $vipFlag")

        val result = when (vipFlag) {
            "1", "4" -> {
                // VipFlag 1 and 4 use reservationTime (specific times)
                val times = reservationDate.reservationTime?.map { it.reserveTime } ?: emptyList()
                println("✅ VipFlag $vipFlag: Found ${times.size} time slots from reservationTime")
                times
            }
            "2", "3" -> {
                // VipFlag 2 and 3 use reservePeriods (time periods)
                val periods = reservationDate.reservePeriods?.map { it.timePeriod } ?: emptyList()
                println("✅ VipFlag $vipFlag: Found ${periods.size} time slots from reservePeriods")
                periods
            }
            else -> {
                println("❌ Unknown vipFlag: $vipFlag")
                emptyList()
            }
        }

        println("🔍 Final time slots: $result")
        return result
    }

    /**
     * Reset national ID validation state
     */
    fun resetNationalIdValidation() {
        _nationalIdValidation.value = NationalIdValidationState.Idle
    }

    /**
     * Reset types when classification changes
     */
    fun resetTypes() {
        _types.value = emptyList()
        _typesError.value = null
    }

    /**
     * Update National ID
     */
    fun updateNationalId(nationalId: String) {
        if (nationalId.length <= 14 && nationalId.all { it.isDigit() }) {
            _formState.value = _formState.value.copy(
                nationalId = nationalId,
                nationalIdError = null
            )
            resetNationalIdValidation()

            // Auto-validate when 14 digits are entered
            if (nationalId.length == 14) {
                validateNationalId(nationalId)
            }
        }
    }

    /**
     * Update Classification
     */
    fun updateClassification(classification: String, code: String, officeId: String) {
        _formState.value = _formState.value.copy(
            selectedClassification = classification,
            selectedClassificationCode = code,
            classificationError = null,
            // Reset dependent fields
            selectedType = null,
            selectedTypeCode = null,
            typeError = null
        )
        resetTypes()
        loadTypes(officeId, code)
    }

    /**
     * Update Type
     */
    fun updateType(type: String, code: String) {
        _formState.value = _formState.value.copy(
            selectedType = type,
            selectedTypeCode = code,
            typeError = null
        )
    }

    /**
     * Update Appointment Date
     */
    fun updateAppointmentDate(date: String) {
        _formState.value = _formState.value.copy(
            appointmentDate = date,
            appointmentDateError = null,
            // Reset time when date changes
            selectedTime = null,
            timeError = null
        )
    }

    /**
     * Update Appointment Time
     */
    fun updateAppointmentTime(time: String) {
        _formState.value = _formState.value.copy(
            selectedTime = time,
            timeError = null
        )
    }

    /**
     * Update Confirmation Checkbox
     */
    fun updateConfirmation(checked: Boolean) {
        _formState.value = _formState.value.copy(
            confirmationChecked = checked,
            confirmationError = null
        )
    }

    /**
     * Validate entire form before submission using BusinessValidation
     */
    fun validateForm(): Boolean {
        val validationData = ReservationFormValidationData(
            formState = _formState.value,
            nationalIdValidationState = _nationalIdValidation.value
        )

        val validationResult = reservationFormValidation.validate(validationData)

        if (validationResult is ValidationResult.Error) {
            // Update form state with validation errors
            _formState.value = _formState.value.copy(
                nationalIdError = validationResult.errors[ValidationErrorKeys.NATIONAL_ID],
                classificationError = validationResult.errors[ValidationErrorKeys.CLASSIFICATION],
                typeError = validationResult.errors[ValidationErrorKeys.TYPE],
                appointmentDateError = validationResult.errors[ValidationErrorKeys.APPOINTMENT_DATE],
                timeError = validationResult.errors[ValidationErrorKeys.APPOINTMENT_TIME],
                confirmationError = validationResult.errors[ValidationErrorKeys.CONFIRMATION]
            )
        }

        return validationResult.isValid
    }

    /**
     * Submit reservation using Strategy Pattern
     */
    fun submitReservation(
        nationalId: String,
        orgUnitId: String,
        categoryCode: String,
        typeCode: String,
        date: String,
        time: String,
        mobileSerialNum: String
    ) {
        viewModelScope.launch {
            _reservationSubmitting.value = true
            _reservationError.value = null

            try {
                val agenda = _officeAgenda.value
                val vipFlag = agenda?.vipFlag ?: ""

                // Get the appropriate strategy based on VIP flag
                val strategy = reservationStrategyFactory.getStrategy(vipFlag)

                // Build the request using the strategy
                val request = strategy.buildReservationRequest(
                    nationalId = nationalId,
                    orgUnitId = orgUnitId,
                    categoryCode = categoryCode,
                    typeCode = typeCode,
                    date = date,
                    time = time,
                    mobileSerialNum = mobileSerialNum,
                    agenda = agenda
                )

                println("📤 Submitting reservation with strategy for vipFlag $vipFlag: $request")

                reservationApiService.reserveProc(request)
                    .onSuccess { response ->
                        println("✅ Reservation successful: $response")
                        _reservationSuccess.value = response
                        _reservationSubmitting.value = false
                    }
                    .onFailure { error ->
                        println("❌ Reservation failed: ${error.message}")
                        _reservationError.value = error.message ?: "Failed to create reservation"
                        _reservationSubmitting.value = false
                    }
            } catch (e: Exception) {
                println("❌ Reservation exception: ${e.message}")
                _reservationError.value = e.message ?: "Failed to create reservation"
                _reservationSubmitting.value = false
            }
        }
    }

    /**
     * Reset reservation state
     */
    fun resetReservationState() {
        _reservationSuccess.value = null
        _reservationError.value = null
    }
}
