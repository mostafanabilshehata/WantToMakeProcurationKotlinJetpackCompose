package com.informatique.tawsekmisr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Version Check Response Model
 */
@Serializable
data class VersionResponse(
    @SerialName("android")
    val android: String,
    @SerialName("ios")
    val ios: String
)

/**
 * Office/OrgUnit Response Models
 */
@Serializable
data class OrgUnitStatusResponse(
    @SerialName("orgUnitStatusRes")
    val orgUnitStatusRes: OrgUnitStatusRes
)

@Serializable
data class OrgUnitStatusRes(
    @SerialName("orgUnitStatusData")
    val orgUnitStatusData: List<OrgUnitStatusData>,
    @SerialName("responseDesc")
    val responseDesc: String,
    @SerialName("responseStatus")
    val responseStatus: String
)

@Serializable
data class OrgUnitStatusData(
    @SerialName("activeFlag")
    val activeFlag: Int,
    @SerialName("address")
    val address: String,
    @SerialName("cnt")
    val cnt: Int,
    @SerialName("empNum")
    val empNum: Int,
    @SerialName("fromTime")
    val fromTime: String,
    @SerialName("govDesc")
    val govDesc: String,
    @SerialName("isopened")
    val isOpened: Int,
    @SerialName("latitude")
    val latitude: String,
    @SerialName("longtiude")
    val longitude: String,
    @SerialName("notServicedAm")
    val notServicedAm: Int,
    @SerialName("notServicedPm")
    val notServicedPm: Int,
    @SerialName("notServicedYet")
    val notServicedYet: Int,
    @SerialName("orgDesc")
    val orgDesc: String,
    @SerialName("orgType")
    val orgType: Int,
    @SerialName("orgTypeDesc")
    val orgTypeDesc: String,
    @SerialName("orgUnitId")
    val orgUnitId: String,
    @SerialName("orgUnitName")
    val orgUnitName: String,
    @SerialName("pluscodes")
    val pluscodes: String,
    @SerialName("prcCntPerDay")
    val prcCntPerDay: String,
    @SerialName("ticketEnabled")
    val ticketEnabled: String,
    @SerialName("toTime")
    val toTime: String,
    @SerialName("windowsNo")
    val windowsNo: String
)

/**
 * Office Model - Updated to work with API data
 */
data class Office(
    val id: String,
    val name: String,
    val type: String,
    val address: String,
    val city: String,
    val distance: Double = 0.0,
    val isPremium: Boolean = false,
    val isVerified: Boolean = false,
    val government: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val phone: String = "",
    val workingHours: String = "",
    val orgType: Int = 0,
    val activeFlag: Int = 0,
    val isOpened: Int = 0,
    val fromTime: String = "",
    val toTime: String = ""
) {
    companion object {
        /**
         * Convert API OrgUnitStatusData to Office model
         */
        fun fromOrgUnitData(data: OrgUnitStatusData): Office {
            return Office(
                id = data.orgUnitId,
                name = data.orgUnitName,
                type = data.orgTypeDesc,
                address = data.address,
                city = data.govDesc,
                distance = 0.0, // Will be calculated based on user location
                isPremium = data.orgType == 1, // Type 1 is premium service
                isVerified = data.activeFlag == 2, // activeFlag 2 means verified
                government = data.govDesc,
                latitude = data.latitude.toDoubleOrNull() ?: 0.0,
                longitude = data.longitude.toDoubleOrNull() ?: 0.0,
                phone = "",
                workingHours = "${data.fromTime} - ${data.toTime}",
                orgType = data.orgType,
                activeFlag = data.activeFlag,
                isOpened = data.isOpened,
                fromTime = data.fromTime,
                toTime = data.toTime
            )
        }
    }
}
