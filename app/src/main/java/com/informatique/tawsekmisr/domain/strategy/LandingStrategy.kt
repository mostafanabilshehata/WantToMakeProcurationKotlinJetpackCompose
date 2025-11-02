package com.informatique.tawsekmisr.domain.strategy

import android.content.Context
import android.content.pm.PackageManager
import com.informatique.tawsekmisr.data.model.Government
import com.informatique.tawsekmisr.data.model.Office
import com.informatique.tawsekmisr.data.repository.LandingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Strategy interface for Landing operations
 * Responsible for initial app setup, version check, loading categories, offices, and governments
 */
interface LandingStrategyInterface {
    suspend fun checkVersionAndLoad(): Result<InitialLoadResult>
}

/**
 * Result of initial app load
 */
data class InitialLoadResult(
    val needsUpdate: Boolean,
    val currentVersion: Int,
    val requiredVersion: Int,
    val offices: List<Office>,
    val governments: List<Government>
)

/**
 * Implementation of Landing Strategy
 * Handles version check, loading offices and governments concurrently at app startup
 */
class LandingStrategy @Inject constructor(
    private val repository: LandingRepository,
    @ApplicationContext private val context: Context
) : LandingStrategyInterface {

    /**
     * Check app version and load initial data (offices and governments)
     * This is called once when the app starts
     *
     * Flow:
     * 1. Check app version - if outdated, return with needsUpdate = true
     * 2. If version is valid, load offices and governments CONCURRENTLY and INDEPENDENTLY
     * 3. Each API call is independent - if one fails, others can still succeed
     * 4. Return all data for initialization
     */
    override suspend fun checkVersionAndLoad(): Result<InitialLoadResult> {
        return try {
            // Step 1: Check app version
            val versionResult = repository.checkAppVersion()

            versionResult.fold(
                onSuccess = { versionResponse ->
                    val currentVersion = getCurrentAppVersion()
                    val requiredVersion = versionResponse.android.toIntOrNull() ?: 0

                    // If current version is lower than required, return immediately with update flag
                    if (currentVersion < requiredVersion) {
                        return Result.success(
                            InitialLoadResult(
                                needsUpdate = true,
                                currentVersion = currentVersion,
                                requiredVersion = requiredVersion,
                                offices = emptyList(),
                                governments = emptyList()
                            )
                        )
                    }

                    // Step 2: Version is valid, load offices and governments CONCURRENTLY
                    coroutineScope {
                        // Launch both API calls concurrently using async
                        val officesDeferredResult = async { repository.getOffices() }
                        val governmentsDeferredResult = async { repository.getGovernments() }

                        // Wait for both to complete
                        val officesResult = officesDeferredResult.await()
                        val governmentsResult = governmentsDeferredResult.await()

                        // Each API is independent - extract data or use empty list on failure
                        val offices = officesResult.getOrElse {
                            println("⚠️ Offices API failed, using empty list")
                            emptyList()
                        }

                        val governments = governmentsResult.getOrElse {
                            println("⚠️ Governments API failed, using empty list")
                            emptyList()
                        }

                        // Return success with whatever data we got
                        Result.success(
                            InitialLoadResult(
                                needsUpdate = false,
                                currentVersion = currentVersion,
                                requiredVersion = requiredVersion,
                                offices = offices,
                                governments = governments
                            )
                        )
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
     * Get current app version code
     */
    private fun getCurrentAppVersion(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }
}

