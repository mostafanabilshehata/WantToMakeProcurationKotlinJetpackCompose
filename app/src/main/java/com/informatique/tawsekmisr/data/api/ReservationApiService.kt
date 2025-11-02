package com.informatique.tawsekmisr.data.api

import com.informatique.tawsekmisr.data.model.*
import com.informatique.tawsekmisr.di.module.AppRepository
import com.informatique.tawsekmisr.di.module.RepoServiceState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

class ReservationApiService @Inject constructor(
    private val repo: AppRepository,
    private val json: Json
) {
    /**
     * Validate National ID
     */
    suspend fun validateNationalId(nationalId: String): Result<NationalIdValidationResponse> {
        return try {
            val response = repo.onGet(
                "/CityStar/AOAS_RestServiceMobile_v2/api/parties/validateIdentityNumber/$nationalId"
            )
            if (response is RepoServiceState.Success) {
                val responseJson = response.response
                if (!responseJson.jsonObject.isEmpty()) {
                    val data: NationalIdValidationResponse = json.decodeFromJsonElement(responseJson.jsonObject)
                    Result.success(data)
                } else {
                    Result.failure(Exception("Empty response from National ID validation"))
                }
            } else {
                val error = response as RepoServiceState.Error
                Result.failure(Exception("Error ${error.code}: ${error.error}"))
            }
        } catch (e: Exception) {
            println("❌ National ID Validation Error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get Reservation Classifications by Office
     * Returns JSON Array: [{"code": "7", "desc": "التأشير على السجلات التجارية"}, ...]
     */
    suspend fun getClassificationsByOffice(orgUnitId: String): Result<List<ReservationClassification>> {
        return try {
            val response = repo.onGet(
                "/CityStar/AOAS_RestServiceMobile_v2/api/ReservationMobile/getTrxCatByOrg/$orgUnitId"
            )
            if (response is RepoServiceState.Success) {
                val responseJson = response.response
                // API returns a JSON array, so we use jsonArray
                val data: List<ReservationClassification> = json.decodeFromJsonElement(responseJson.jsonArray)
                Result.success(data)
            } else {
                val error = response as RepoServiceState.Error
                Result.failure(Exception("Error ${error.code}: ${error.error}"))
            }
        } catch (e: Exception) {
            println("❌ Classifications API Error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get Reservation Types by Classification
     * Returns JSON Array: [{"code": "1", "desc": "التأشير علي السجلات التجارية"}, ...]
     */
    suspend fun getTypesByClassification(
        orgUnitId: String,
        categoryCode: String
    ): Result<List<ReservationType>> {
        return try {
            val response = repo.onGet(
                "/CityStar/AOAS_RestServiceMobile_v2/api/ReservationMobile/getTrxCatByOrg/$orgUnitId/$categoryCode"
            )
            if (response is RepoServiceState.Success) {
                val responseJson = response.response
                // API returns a JSON array, so we use jsonArray
                val data: List<ReservationType> = json.decodeFromJsonElement(responseJson.jsonArray)
                Result.success(data)
            } else {
                val error = response as RepoServiceState.Error
                Result.failure(Exception("Error ${error.code}: ${error.error}"))
            }
        } catch (e: Exception) {
            println("❌ Types API Error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get Office Agenda (Available time slots)
     */
    suspend fun getOfficeAgenda(orgUnitId: String): Result<OfficeAgendaResponse> {
        return try {
            val response = repo.onGet(
                "/CityStar/AOAS_RestServiceMobile_v2/api/ReservationMobile/getOfficeAgenda/$orgUnitId/1/1/1/1/1"
            )
            if (response is RepoServiceState.Success) {
                val responseJson = response.response
                if (!responseJson.jsonObject.isEmpty()) {
                    val data: OfficeAgendaResponse = json.decodeFromJsonElement(responseJson.jsonObject)
                    Result.success(data)
                } else {
                    Result.failure(Exception("Empty response from Office Agenda"))
                }
            } else {
                val error = response as RepoServiceState.Error
                Result.failure(Exception("Error ${error.code}: ${error.error}"))
            }
        } catch (e: Exception) {
            println("❌ Office Agenda API Error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Reserve Procuration - Create a new reservation
     */
    suspend fun reserveProc(request: ReserveProcRequest): Result<ReserveProcResponse> {
        return try {
            val response = repo.onPostAuth(
                "/CityStar/AOAS_RestServiceMobile_v2/api/ReservationMobile/reserveProc",
                request
            )
            if (response is RepoServiceState.Success) {
                val responseJson = response.response
                if (!responseJson.jsonObject.isEmpty()) {
                    val data: ReserveProcResponse = json.decodeFromJsonElement(responseJson.jsonObject)
                    Result.success(data)
                } else {
                    Result.failure(Exception("Empty response from Reserve Proc"))
                }
            } else {
                val error = response as RepoServiceState.Error
                Result.failure(Exception("Error ${error.code}: ${error.error}"))
            }
        } catch (e: Exception) {
            println("❌ Reserve Proc API Error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Inquire My Reserve - Get all reservations for a national ID
     */
    suspend fun getInquireMyReserve(nationalId: String): Result<InquireMyReserveResponse> {
        return try {
            val response = repo.onGet(
                "/CityStar/AOAS_RestServiceMobile_v2/api/ReservationMobile/getInquireMyReserve/$nationalId"
            )
            if (response is RepoServiceState.Success) {
                val responseJson = response.response
                if (!responseJson.jsonObject.isEmpty()) {
                    val data: InquireMyReserveResponse = json.decodeFromJsonElement(responseJson.jsonObject)
                    Result.success(data)
                } else {
                    Result.failure(Exception("Empty response from Inquire My Reserve"))
                }
            } else {
                val error = response as RepoServiceState.Error
                Result.failure(Exception("Error ${error.code}: ${error.error}"))
            }
        } catch (e: Exception) {
            println("❌ Inquire My Reserve API Error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
