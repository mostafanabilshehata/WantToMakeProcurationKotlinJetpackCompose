package com.informatique.tawsekmisr.domain.strategy

import com.informatique.tawsekmisr.data.model.ReserveProcRequest
import com.informatique.tawsekmisr.data.model.OfficeAgendaResponse
import java.text.SimpleDateFormat
import java.util.*

/**
 * Strategy Interface for Reservation Processing
 */
interface ReservationStrategy {
    /**
     * Build the reservation request based on office type and data
     */
    fun buildReservationRequest(
        nationalId: String,
        orgUnitId: String,
        categoryCode: String,
        typeCode: String,
        date: String,
        time: String,
        mobileSerialNum: String,
        agenda: OfficeAgendaResponse?
    ): ReserveProcRequest
}

/**
 * Strategy for VIP Flag 1 and 4 (Specific Time Slots)
 */
class VipReservationStrategy : ReservationStrategy {
    override fun buildReservationRequest(
        nationalId: String,
        orgUnitId: String,
        categoryCode: String,
        typeCode: String,
        date: String,
        time: String,
        mobileSerialNum: String,
        agenda: OfficeAgendaResponse?
    ): ReserveProcRequest {
        // Convert date from dd/MM/yyyy to dd-MM-yyyy format for API
        val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val apiDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateForApi = try {
            val parsedDate = displayDateFormat.parse(date)
            parsedDate?.let { apiDateFormat.format(it) } ?: date.replace("/", "-")
        } catch (e: Exception) {
            date.replace("/", "-")
        }

        // VipFlag 1,4: Date + time, empty period
        val reservationSlot = "$dateForApi $time"

        return ReserveProcRequest(
            customerId = nationalId,
            customerIdType = "1",
            orgUnitId = orgUnitId,
            transactionTypeCategory = categoryCode,
            transactionTypeCode = typeCode,
            period = "", // Empty for VIP offices
            requestQueVIPId = null,
            mobileSerialNum = mobileSerialNum,
            reservationSlot = reservationSlot
        )
    }
}

/**
 * Strategy for VIP Flag 2 and 3 (Time Periods)
 */
class StandardReservationStrategy : ReservationStrategy {
    override fun buildReservationRequest(
        nationalId: String,
        orgUnitId: String,
        categoryCode: String,
        typeCode: String,
        date: String,
        time: String,
        mobileSerialNum: String,
        agenda: OfficeAgendaResponse?
    ): ReserveProcRequest {
        // Convert date from dd/MM/yyyy to dd-MM-yyyy format for API
        val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val apiDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateForApi = try {
            val parsedDate = displayDateFormat.parse(date)
            parsedDate?.let { apiDateFormat.format(it) } ?: date.replace("/", "-")
        } catch (e: Exception) {
            date.replace("/", "-")
        }

        // Find the period code for the selected time period
        val periodCode = findPeriodCode(date, time, agenda)

        // VipFlag 2,3: Date + "00:00", with period code
        val reservationSlot = "$dateForApi 00:00"

        return ReserveProcRequest(
            customerId = nationalId,
            customerIdType = "1",
            orgUnitId = orgUnitId,
            transactionTypeCategory = categoryCode,
            transactionTypeCode = typeCode,
            period = periodCode,
            requestQueVIPId = null,
            mobileSerialNum = mobileSerialNum,
            reservationSlot = reservationSlot
        )
    }

    private fun findPeriodCode(date: String, time: String, agenda: OfficeAgendaResponse?): String {
        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val dateToMatch = try {
            val parsedDate = displayDateFormat.parse(date)
            parsedDate?.let { apiDateFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }

        val reservationDate = agenda?.officeAgendaData?.reservationDate
            ?.find { it.datereserved == dateToMatch }

        return reservationDate?.reservePeriods?.find { it.timePeriod == time }?.timePeriodCode ?: ""
    }
}

/**
 * Factory to get the appropriate strategy based on VIP flag
 */
class ReservationStrategyFactory {
    fun getStrategy(vipFlag: String): ReservationStrategy {
        return when (vipFlag) {
            "1", "4" -> VipReservationStrategy()
            "2", "3" -> StandardReservationStrategy()
            else -> StandardReservationStrategy() // Default to standard
        }
    }
}

