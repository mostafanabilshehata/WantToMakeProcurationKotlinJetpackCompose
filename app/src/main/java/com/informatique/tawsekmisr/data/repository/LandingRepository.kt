package com.informatique.tawsekmisr.data.repository

import com.informatique.tawsekmisr.data.api.LandingApiService
import com.informatique.tawsekmisr.data.model.Government
import com.informatique.tawsekmisr.data.model.Office
import com.informatique.tawsekmisr.data.model.VersionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Landing operations
 */
interface LandingRepository {
    suspend fun checkAppVersion(): Result<VersionResponse>
    suspend fun getOffices(): Result<List<Office>>
    suspend fun getGovernments(): Result<List<Government>>
}

@Singleton
class LandingRepositoryImpl @Inject constructor(
    private val apiService: LandingApiService
) : LandingRepository {

    // Cache for offices
    private var cachedOffices: List<Office>? = null

    // Cache for governments
    private var cachedGovernments: List<Government>? = null

    /**
     * Check app version from server
     */
    override suspend fun checkAppVersion(): Result<VersionResponse> = withContext(Dispatchers.IO) {
        try {
            apiService.checkVersion()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get offices/org units from server
     */
    override suspend fun getOffices(): Result<List<Office>> = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            if (cachedOffices != null) {
                return@withContext Result.success(cachedOffices!!)
            }

            val result = apiService.getOffices()
            result.fold(
                onSuccess = { response ->
                    if (response.orgUnitStatusRes.responseStatus == "200") {
                        // Convert API data to Office models
                        val offices = response.orgUnitStatusRes.orgUnitStatusData.map {
                            Office.fromOrgUnitData(it)
                        }
                        cachedOffices = offices
                        Result.success(offices)
                    } else {
                        Result.failure(Exception(response.orgUnitStatusRes.responseDesc))
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
     * Get governments dropdown from server
     */
    override suspend fun getGovernments(): Result<List<Government>> = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            if (cachedGovernments != null) {
                return@withContext Result.success(cachedGovernments!!)
            }

            val result = apiService.getGovernments()
            result.fold(
                onSuccess = { governments ->
                    cachedGovernments = governments
                    Result.success(governments)
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
     * Clear cache - useful for refresh
     */
    fun clearCache() {
        cachedOffices = null
        cachedGovernments = null
    }
}
