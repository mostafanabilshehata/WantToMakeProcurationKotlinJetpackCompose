package com.informatique.tawsekmisr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.informatique.tawsekmisr.data.model.Office
import com.informatique.tawsekmisr.utils.UserLocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

/**
 * ViewModel for Find Office Screen
 * Manages office filtering, search, and distance calculations
 */
@HiltViewModel
class FindOfficeViewModel @Inject constructor(
    private val locationManager: UserLocationManager
) : ViewModel() {

    // User location
    private val _userLatitude = MutableStateFlow<Double?>(null)
    private val _userLongitude = MutableStateFlow<Double?>(null)

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    // Filter states
    private val _selectedFilter = MutableStateFlow("nearest")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGovernment = MutableStateFlow("all")
    val selectedGovernment: StateFlow<String> = _selectedGovernment.asStateFlow()

    // All offices from data source
    private val _allOffices = MutableStateFlow<List<Office>>(emptyList())

    // Filtered offices with calculated distances
    private val _filteredOffices = MutableStateFlow<List<Office>>(emptyList())
    val filteredOffices: StateFlow<List<Office>> = _filteredOffices.asStateFlow()

    /**
     * Set the list of all offices
     */
    fun setOffices(offices: List<Office>) {
        _allOffices.value = offices
        updateFilteredOffices()
    }

    /**
     * Update selected filter
     */
    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        // Reset government filter when switching away from "all"
        if (filter != "all") {
            _selectedGovernment.value = "all"
        }
        updateFilteredOffices()
    }

    /**
     * Update search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredOffices()
    }

    /**
     * Update selected government
     */
    fun setSelectedGovernment(government: String) {
        _selectedGovernment.value = government
        updateFilteredOffices()
    }

    /**
     * Fetch current user location
     */
    fun fetchUserLocation() {
        viewModelScope.launch {
            _isLoadingLocation.value = true
            _locationError.value = null

            try {
                val location = locationManager.getCurrentLocation()
                if (location != null) {
                    _userLatitude.value = location.latitude
                    _userLongitude.value = location.longitude
                    updateFilteredOffices()
                } else {
                    _locationError.value = "Unable to get location"
                    // Use default Cairo location as fallback
                    _userLatitude.value = 30.0444
                    _userLongitude.value = 31.2357
                    updateFilteredOffices()
                }
            } catch (e: Exception) {
                _locationError.value = "Location error: ${e.message}"
                // Use default Cairo location as fallback
                _userLatitude.value = 30.0444
                _userLongitude.value = 31.2357
                updateFilteredOffices()
            } finally {
                _isLoadingLocation.value = false
            }
        }
    }

    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(): Boolean {
        return locationManager.hasLocationPermission()
    }

    /**
     * Check if location services are enabled
     */
    fun isLocationEnabled(): Boolean {
        return locationManager.isLocationEnabled()
    }

    /**
     * Calculate distance between two geographic coordinates using Haversine formula
     * Returns distance in kilometers
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 || lon1 == 0.0 || lat2 == 0.0 || lon2 == 0.0) {
            return Double.MAX_VALUE // Return max value if coordinates are invalid
        }

        val earthRadius = 6371.0 // Earth's radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Update filtered offices based on current state
     */
    private fun updateFilteredOffices() {
        viewModelScope.launch {
            val offices = _allOffices.value
            val filter = _selectedFilter.value
            val search = _searchQuery.value
            val government = _selectedGovernment.value
            val userLat = _userLatitude.value ?: 30.0444 // Default Cairo
            val userLon = _userLongitude.value ?: 31.2357

            // First, calculate distances for all offices
            val officesWithDistance = offices.map { office ->
                office.copy(
                    distance = calculateDistance(
                        userLat,
                        userLon,
                        office.latitude,
                        office.longitude
                    )
                )
            }

            // Apply filters
            val filtered = officesWithDistance.filter { office ->
                val matchesSearch = search.isEmpty() ||
                        office.name.contains(search, ignoreCase = true) ||
                        office.address.contains(search, ignoreCase = true)

                val matchesGovernment = government == "all" || office.government == government

                val matchesFilter = when (filter) {
                    "all" -> true
                    "nearest" -> true // Will limit to 10 nearest after filtering
                    "premium" -> office.isPremium
                    else -> true
                }

                matchesSearch && matchesGovernment && matchesFilter
            }

            // Sort by distance and apply specific filter logic
            _filteredOffices.value = when (filter) {
                "nearest" -> filtered.sortedBy { it.distance }.take(10)
                else -> filtered.sortedBy { it.distance }
            }
        }
    }
}
