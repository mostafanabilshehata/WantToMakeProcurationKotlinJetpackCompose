package com.informatique.tawsekmisr.domain.validation

/**
 * Generic Business Validation Interface
 */
interface BusinessValidation<T> {
    /**
     * Validates the given data
     * @param data The data to validate
     * @return ValidationResult with success/failure state and error messages
     */
    fun validate(data: T): ValidationResult
}

/**
 * Validation Result
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val errors: Map<String, String>) : ValidationResult()

    val isValid: Boolean
        get() = this is Success

    val errorMessages: Map<String, String>
        get() = when (this) {
            is Success -> emptyMap()
            is Error -> errors
        }
}

/**
 * Validation Error Keys
 */
object ValidationErrorKeys {
    const val NATIONAL_ID = "nationalId"
    const val CLASSIFICATION = "classification"
    const val TYPE = "type"
    const val APPOINTMENT_DATE = "appointmentDate"
    const val APPOINTMENT_TIME = "appointmentTime"
    const val CONFIRMATION = "confirmation"
}

