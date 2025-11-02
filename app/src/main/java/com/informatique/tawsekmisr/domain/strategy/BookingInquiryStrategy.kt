package com.informatique.tawsekmisr.domain.strategy

import com.informatique.tawsekmisr.data.api.ReservationApiService
import com.informatique.tawsekmisr.data.model.InquireReservation
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Strategy for handling booking inquiry business logic
 */
class BookingInquiryStrategy @Inject constructor(
    private val reservationApiService: ReservationApiService
) {

    /**
     * Validate National ID format
     */
    fun validateNationalIdFormat(nationalId: String): ValidationResult {
        return when {
            nationalId.isBlank() -> ValidationResult.Error("الرقم القومي مطلوب")
            nationalId.length != 14 -> ValidationResult.Error("يجب أن يكون الرقم القومي 14 رقماً")
            !nationalId.all { it.isDigit() } -> ValidationResult.Error("يجب أن يحتوي الرقم القومي على أرقام فقط")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validate National ID with API
     */
    suspend fun validateNationalIdWithApi(nationalId: String): Result<Boolean> {
        return try {
            val result = reservationApiService.validateNationalId(nationalId)
            result.fold(
                onSuccess = { response ->
                    if (response.statusCode == "1") {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(response.statusDesc))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch reservations for the given national ID
     */
    suspend fun fetchReservations(nationalId: String): Result<List<InquireReservation>> {
        return try {
            val result = reservationApiService.getInquireMyReserve(nationalId)
            result.fold(
                onSuccess = { response ->
                    if (response.statusCode == "200") {
                        Result.success(response.inquireReserve)
                    } else {
                        Result.failure(Exception(response.statusMessage))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Format reservation date for display
     * Input: "2023-09-20 12:00:00.0"
     * Output: "20/09/2023"
     */
    fun formatReservationDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Format reservation time based on office vipFlag
     * - VipFlag 1,4: Format "2023-09-18 12:00:00.0" -> "12:00 PM"
     * - VipFlag 2,3: Already formatted as period "13:30-16:00"
     */
    fun formatReservationTime(timeString: String, vipFlag: String): String {
        return try {
            when (vipFlag) {
                "1", "4" -> {
                    // Format time from datetime string
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
                    val time = inputFormat.parse(timeString)
                    time?.let { outputFormat.format(it) } ?: timeString
                }
                "2", "3" -> {
                    // Already formatted as period
                    timeString
                }
                else -> timeString
            }
        } catch (e: Exception) {
            timeString
        }
    }

    /**
     * Sort reservations by date (newest first)
     */
    fun sortReservationsByDate(reservations: List<InquireReservation>): List<InquireReservation> {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault())
            reservations.sortedByDescending { reservation ->
                try {
                    dateFormat.parse(reservation.reservationDate)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
        } catch (e: Exception) {
            reservations
        }
    }

    /**
     * Filter reservations by status (future, past, all)
     */
    fun filterReservations(
        reservations: List<InquireReservation>,
        filterType: ReservationFilter
    ): List<InquireReservation> {
        if (filterType == ReservationFilter.ALL) return reservations

        val now = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault())

        return reservations.filter { reservation ->
            try {
                val reservationDate = dateFormat.parse(reservation.reservationDate)
                when (filterType) {
                    ReservationFilter.UPCOMING -> reservationDate?.after(now) ?: false
                    ReservationFilter.PAST -> reservationDate?.before(now) ?: false
                    else -> true
                }
            } catch (e: Exception) {
                true
            }
        }
    }

    /**
     * Get reservation statistics
     */
    fun getReservationStats(reservations: List<InquireReservation>): ReservationStats {
        val now = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault())

        var upcoming = 0
        var past = 0

        reservations.forEach { reservation ->
            try {
                val reservationDate = dateFormat.parse(reservation.reservationDate)
                if (reservationDate?.after(now) == true) {
                    upcoming++
                } else {
                    past++
                }
            } catch (e: Exception) {
                // Skip if date parsing fails
            }
        }

        return ReservationStats(
            total = reservations.size,
            upcoming = upcoming,
            past = past
        )
    }
}

/**
 * Validation result sealed class
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

/**
 * Reservation filter types
 */
enum class ReservationFilter {
    ALL,
    UPCOMING,
    PAST
}

/**
 * Reservation statistics
 */
data class ReservationStats(
    val total: Int,
    val upcoming: Int,
    val past: Int
)

