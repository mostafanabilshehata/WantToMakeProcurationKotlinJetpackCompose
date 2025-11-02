package com.informatique.tawsekmisr.domain.validation

import com.informatique.tawsekmisr.R
import com.informatique.tawsekmisr.common.util.LocalizationHelper
import com.informatique.tawsekmisr.ui.viewmodels.NationalIdValidationState
import com.informatique.tawsekmisr.ui.viewmodels.ReservationFormState
import javax.inject.Inject

/**
 * Reservation Form Validation Implementation
 */
class ReservationFormValidation @Inject constructor(
    private val localizationHelper: LocalizationHelper
) : BusinessValidation<ReservationFormValidationData> {

    override fun validate(data: ReservationFormValidationData): ValidationResult {
        val errors = mutableMapOf<String, String>()

        // Validate National ID
        when {
            data.formState.nationalId.isBlank() -> {
                errors[ValidationErrorKeys.NATIONAL_ID] = localizationHelper.getString(R.string.error_national_id_required)
            }
            data.formState.nationalId.length != 14 -> {
                errors[ValidationErrorKeys.NATIONAL_ID] = localizationHelper.getString(R.string.error_national_id_length)
            }
            data.nationalIdValidationState !is NationalIdValidationState.Valid -> {
                errors[ValidationErrorKeys.NATIONAL_ID] = localizationHelper.getString(R.string.error_national_id_validation)
            }
        }

        // Validate Classification
        if (data.formState.selectedClassification == null) {
            errors[ValidationErrorKeys.CLASSIFICATION] = localizationHelper.getString(R.string.error_classification_required)
        }

        // Validate Type
        if (data.formState.selectedType == null) {
            errors[ValidationErrorKeys.TYPE] = localizationHelper.getString(R.string.error_type_required)
        }

        // Validate Appointment Date
        if (data.formState.appointmentDate.isBlank()) {
            errors[ValidationErrorKeys.APPOINTMENT_DATE] = localizationHelper.getString(R.string.error_date_required)
        }

        // Validate Appointment Time
        if (data.formState.selectedTime == null) {
            errors[ValidationErrorKeys.APPOINTMENT_TIME] = localizationHelper.getString(R.string.error_time_required)
        }

        // Validate Confirmation
        if (!data.formState.confirmationChecked) {
            errors[ValidationErrorKeys.CONFIRMATION] = localizationHelper.getString(R.string.error_confirmation_required)
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
}

/**
 * Data class for reservation form validation
 */
data class ReservationFormValidationData(
    val formState: ReservationFormState,
    val nationalIdValidationState: NationalIdValidationState
)
