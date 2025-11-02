package com.informatique.tawsekmisr.data.api

import com.informatique.tawsekmisr.data.model.Government
import com.informatique.tawsekmisr.data.model.GovernmentResponse
import com.informatique.tawsekmisr.data.model.OrgUnitStatusResponse
import com.informatique.tawsekmisr.data.model.VersionResponse
import com.informatique.tawsekmisr.di.module.AppRepository
import com.informatique.tawsekmisr.di.module.RepoServiceState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Landing API Service for version check and offices data
 */
@Singleton
class LandingApiService @Inject constructor(
    val repo: AppRepository,
    private val json: Json
)  {

    /**
     * Check app version
     * Endpoint: CityStar/AOAS_RestServiceMobile_v5/api/ReservationMobile/getMobProcVersion
     * Method: GET
     *
     * Note: Server returns valid JSON but with Content-Type: text/html
     * We read as text first, then manually parse to avoid NoTransformationFoundException
     */
    suspend fun checkVersion(): Result<VersionResponse> {
        return try {
            val fullUrl = "https://ms.tawseek.gov.eg/CityStar/AOAS_RestServiceMobile_v5/api/ReservationMobile/getMobProcVersion"

            // Use main client now that NetworkModule headers are fixed
            val response = repo.onGet(fullUrl)
            when (response) {
                is RepoServiceState.Success -> {
                    val response = response.response
                    if (!response.jsonObject.isEmpty()) {
                        val versionResponse: VersionResponse =
                            json.decodeFromJsonElement(response.jsonObject)

                        Result.success(versionResponse)
                    }else{
                        Result.failure(Exception("Empty version response"))
                    }
                }

                is RepoServiceState.Error -> {
                    Result.failure(Exception("Failed to fetch version: ${response.error.toString()}"))

                }
            }

        } catch (e: Exception) {
            println("❌ Version API Error: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Failed to fetch version: ${e.message}"))
        }
    }

    /**
     * Get offices/org units data
     * Endpoint: CityStar/AOAS_RestServiceMobile_v2/api/ReservationMobile/getOrgUnitStatus
     * Method: GET
     *
     * Note: Server returns valid JSON but with Content-Type: text/html
     * We read as text first, then manually parse to avoid NoTransformationFoundException
     */
    suspend fun getOffices(): Result<OrgUnitStatusResponse> {
        return try {
            val fullUrl = "https://ms.tawseek.gov.eg/CityStar/AOAS_RestServiceMobile_v2/api/ReservationMobile/getOrgUnitStatus"

            // Use main client now that NetworkModule headers are fixed
            val response = repo.onGet(fullUrl)
            when (response) {
                is RepoServiceState.Success -> {
                    val response = response.response
                    if (!response.jsonObject.isEmpty()) {
                        val orgUnitStatusResponse: OrgUnitStatusResponse =
                            json.decodeFromJsonElement(response.jsonObject)

                        Result.success(orgUnitStatusResponse)
                    }else{
                        Result.failure(Exception("Failed to fetch offices"))
                    }
                }

                is RepoServiceState.Error -> {
                    Result.failure(Exception("Failed to fetch offices: ${response.error.toString()}"))

                }
            }

        } catch (e: Exception) {
            println("❌ Offices API Error: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Failed to fetch offices: ${e.message}"))
        }
    }

    /**
     * Get governments dropdown data
     * Endpoint: CityStar/AOAS_RestServiceMobile_v2/api/EGovNotarization/getLookups/govTypes/0/1/2/3/4/5
     * Method: GET
     *
     * Note: Server returns valid JSON but with Content-Type: text/html
     * We read as text first, then manually parse to avoid NoTransformationFoundException
     */
    suspend fun getGovernments(): Result<List<Government>> {
        return try {
            val fullUrl = "https://ms.tawseek.gov.eg/CityStar/AOAS_RestServiceMobile_v2/api/EGovNotarization/getLookups/govTypes/0/1/2/3/4/5"

            // Use main client now that NetworkModule headers are fixed
            val response = repo.onGet(fullUrl)
            when (response) {
                is RepoServiceState.Success -> {
                    val response = response.response
                    if (!response.jsonObject.isEmpty()) {
                        val governmentResponse: GovernmentResponse =
                            json.decodeFromJsonElement(response.jsonObject)

                        val governments = governmentResponse.lookupData.map { Government.fromGovernmentData(it) }
                        Result.success(governments)
                    }else{
                        Result.failure(Exception("Failed to fetch governments"))
                    }
                }

                is RepoServiceState.Error -> {
                    Result.failure(Exception("Failed to fetch governments: ${response.error.toString()}"))

                }
            }

        } catch (e: Exception) {
            println("❌ Governments API Error: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Failed to fetch governments: ${e.message}"))
        }
    }
}
