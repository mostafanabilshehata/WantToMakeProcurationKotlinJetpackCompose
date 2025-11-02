package com.informatique.tawsekmisr.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.informatique.tawsekmisr.domain.strategy.LandingStrategy
import com.informatique.tawsekmisr.data.model.Government
import com.informatique.tawsekmisr.data.model.Office
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Landing
 */
sealed class LandingUiState {
    object Loading : LandingUiState()
    data class Success(
        val offices: List<Office>,
        val governments: List<Government>
    ) : LandingUiState()
    data class UpdateRequired(
        val currentVersion: Int,
        val requiredVersion: Int
    ) : LandingUiState()
    data class Error(val message: String) : LandingUiState()
}

/**
 * LandingViewModel - Responsible for initial app setup
 * 1. Check app version
 * 2. Load offices, and governments if version is valid
 * 3. Provide data globally via CompositionLocal
 */
@HiltViewModel
class LandingViewModel @Inject constructor(
    private val strategy: LandingStrategy
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<LandingUiState>(LandingUiState.Loading)
    val uiState: StateFlow<LandingUiState> = _uiState.asStateFlow()

    private val _offices = MutableStateFlow<List<Office>>(emptyList())
    val offices: StateFlow<List<Office>> = _offices.asStateFlow()

    private val _governments = MutableStateFlow<List<Government>>(emptyList())
    val governments: StateFlow<List<Government>> = _governments.asStateFlow()

    init {
        checkVersionAndLoadData()
    }

    /**
     * Retry loading data - can be called from FindOfficeScreen if initial load failed
     */
    fun retryLoadData() {
        checkVersionAndLoadData()
    }

    /**
     * Check version and load initial data (categories, offices, and governments)
     * This is called once when the app starts
     * Offices and governments are loaded concurrently and independently
     */
    private fun checkVersionAndLoadData() {
        viewModelScope.launch {
            _uiState.value = LandingUiState.Loading
            setLoading(true)

            try {
                val result = strategy.checkVersionAndLoad()
                result.fold(
                    onSuccess = { loadResult ->
                        if (loadResult.needsUpdate) {
                            // Version is outdated - show update required dialog
                            _uiState.value = LandingUiState.UpdateRequired(
                                currentVersion = loadResult.currentVersion,
                                requiredVersion = loadResult.requiredVersion
                            )
                        } else {
                            // Version is valid - set categories, offices, and governments
                            _offices.value = loadResult.offices
                            _governments.value = loadResult.governments

                            _uiState.value = LandingUiState.Success(
                                offices = loadResult.offices,
                                governments = loadResult.governments
                            )
                        }
                        setLoading(false)
                    },
                    onFailure = { error ->
                        _uiState.value = LandingUiState.Error(
                            error.message ?: "Failed to load initial data"
                        )
                        setError(error.message ?: "Failed to load initial data")
                        setLoading(false)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = LandingUiState.Error(
                    e.message ?: "An error occurred"
                )
                setError(e.message ?: "An error occurred")
                setLoading(false)
            }
        }
    }
}
