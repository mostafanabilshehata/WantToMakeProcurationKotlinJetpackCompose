package com.informatique.tawsekmisr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Government/Lookup Response Models
 */
@Serializable
data class GovernmentResponse(
    @SerialName("type")
    val type: String,
    @SerialName("responseStatus")
    val responseStatus: String,
    @SerialName("responseStatusDesc")
    val responseStatusDesc: String,
    @SerialName("channelRequestId")
    val channelRequestId: String? = null,
    @SerialName("correlationId")
    val correlationId: String? = null,
    @SerialName("lookupData")
    val lookupData: List<GovernmentData>,
    @SerialName("minutesOfValidity")
    val minutesOfValidity: Int? = null,
    @SerialName("originatingChannel")
    val originatingChannel: Int? = null,
    @SerialName("originatingUserIdentifier")
    val originatingUserIdentifier: String? = null,
    @SerialName("originatingUserType")
    val originatingUserType: Int? = null
)

@Serializable
data class GovernmentData(
    @SerialName("code")
    val code: String,
    @SerialName("desc")
    val desc: String
)

/**
 * Simple Government model for UI
 */
data class Government(
    val code: String,
    val name: String
) {
    companion object {
        fun fromGovernmentData(data: GovernmentData): Government {
            return Government(
                code = data.code,
                name = data.desc.trim()
            )
        }
    }
}

