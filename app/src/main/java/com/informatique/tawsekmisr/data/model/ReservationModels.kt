package com.informatique.tawsekmisr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * National ID Validation Response
 */
@Serializable
data class NationalIdValidationResponse(
    @SerialName("statusCode")
    val statusCode: String,
    @SerialName("statusDesc")
    val statusDesc: String
)

/**
 * Reservation Classification (Transaction Category)
 */
@Serializable
data class ReservationClassification(
    @SerialName("code")
    val code: String,
    @SerialName("desc")
    val desc: String
)

/**
 * Reservation Type (Transaction Type)
 */
@Serializable
data class ReservationType(
    @SerialName("code")
    val code: String,
    @SerialName("desc")
    val desc: String
)

/**
 * Office Agenda Response
 */
@Serializable
data class OfficeAgendaResponse(
    @SerialName("type")
    val type: String,
    @SerialName("responseStatus")
    val responseStatus: String,
    @SerialName("responseStatusDesc")
    val responseStatusDesc: String,
    @SerialName("channelRequestId")
    val channelRequestId: String,
    @SerialName("correlationId")
    val correlationId: String,
    @SerialName("officeAgendaData")
    val officeAgendaData: OfficeAgendaData,
    @SerialName("originatingChannel")
    val originatingChannel: Int,
    @SerialName("originatingUserIdentifier")
    val originatingUserIdentifier: String,
    @SerialName("originatingUserType")
    val originatingUserType: Int,
    @SerialName("vipFlag")
    val vipFlag: String
)

@Serializable
data class OfficeAgendaData(
    @SerialName("reservationDate")
    val reservationDate: List<ReservationDate>
)

@Serializable
data class ReservationDate(
    @SerialName("datereserved")
    val datereserved: String,
    @SerialName("reservePeriods")
    val reservePeriods: List<ReservePeriod>? = null, // For vipFlag 1,4
    @SerialName("reservationTime")
    val reservationTime: List<ReservationTime>? = null // For vipFlag 2,3
)

@Serializable
data class ReservePeriod(
    @SerialName("timePeriod")
    val timePeriod: String,
    @SerialName("timePeriodCode")
    val timePeriodCode: String
)

@Serializable
data class ReservationTime(
    @SerialName("reserveTime")
    val reserveTime: String
)

/**
 * Reserve Procuration Request
 */
@Serializable
data class ReserveProcRequest(
    @SerialName("customerId")
    val customerId: String, // National ID
    @SerialName("customerIdType")
    val customerIdType: String = "1", // Always "1"
    @SerialName("orgUnitId")
    val orgUnitId: String, // Office org unit id
    @SerialName("transactionTypeCategory")
    val transactionTypeCategory: String, // Category id
    @SerialName("transactionTypeCode")
    val transactionTypeCode: String, // Type code
    @SerialName("period")
    val period: String, // Period id (empty for vipFlag 1,4)
    @SerialName("requestQueVIPId")
    val requestQueVIPId: String? = null, // Always null in add
    @SerialName("mobileSerialNum")
    val mobileSerialNum: String, // Mobile serial number
    @SerialName("reservationSlot")
    val reservationSlot: String // Date + time
)

/**
 * Reserve Procuration Response
 */
@Serializable
data class ReserveProcResponse(
    @SerialName("actualTimeToAttend")
    val actualTimeToAttend: String,
    @SerialName("attendedNo")
    val attendedNo: Int,
    @SerialName("statusCode")
    val statusCode: String,
    @SerialName("statusDesc")
    val statusDesc: String,
    @SerialName("ticketNo")
    val ticketNo: String
)

/**
 * Inquire My Reserve - Single Reservation Item
 */
@Serializable
data class InquireReservation(
    @SerialName("orgId")
    val orgId: String,
    @SerialName("orgName")
    val orgName: String,
    @SerialName("orgVipFlag")
    val orgVipFlag: String,
    @SerialName("periodId")
    val periodId: String? = null, // Optional - not present for vipFlag 1,4
    @SerialName("queVipId")
    val queVipId: String,
    @SerialName("reservationDate")
    val reservationDate: String,
    @SerialName("reserveTime")
    val reserveTime: String,
    @SerialName("transactionCategoryId")
    val transactionCategoryId: String,
    @SerialName("transactionDesc")
    val transactionDesc: String,
    @SerialName("transactionTypeDesc")
    val transactionTypeDesc: String,
    @SerialName("transactionTypeId")
    val transactionTypeId: String
)

/**
 * Inquire My Reserve Response
 */
@Serializable
data class InquireMyReserveResponse(
    @SerialName("inquireReserve")
    val inquireReserve: List<InquireReservation>,
    @SerialName("statusCode")
    val statusCode: String,
    @SerialName("statusMessage")
    val statusMessage: String
)
