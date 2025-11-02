package com.informatique.tawsekmisr.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRestrictedDatePicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    mandatory: Boolean = false,
    allowedDatesInMillis: Set<Long> = emptySet(),
    enabled: Boolean = true
) {
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        label = {
            Text(
                text = if (mandatory) "$label *" else label,
                color = if (error != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { showDatePicker = true },
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date",
                    tint = if (error != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error
        )
    )

    if (error != null) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDatePicker) {
        RestrictedDatePickerModal(
            onDateSelected = { selectedDate ->
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                onValueChange(formatter.format(Date(selectedDate)))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            allowedDatesInMillis = allowedDatesInMillis,
            initialDate = value
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestrictedDatePickerModal(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    allowedDatesInMillis: Set<Long> = emptySet(),
    initialDate: String? = null
) {
    // Parse initial date if provided
    val initialDateMillis = remember(initialDate) {
        if (initialDate != null && initialDate.isNotEmpty()) {
            try {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatter.parse(initialDate)?.time
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    // Custom SelectableDates that only allows specific dates
    val selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            if (allowedDatesInMillis.isEmpty()) return false

            // Convert UTC timestamp to local date components
            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCalendar.timeInMillis = utcTimeMillis
            val year = utcCalendar.get(Calendar.YEAR)
            val month = utcCalendar.get(Calendar.MONTH)
            val day = utcCalendar.get(Calendar.DAY_OF_MONTH)

            // Check if any allowed date matches this date
            return allowedDatesInMillis.any { allowedMillis ->
                val allowedCalendar = Calendar.getInstance()
                allowedCalendar.timeInMillis = allowedMillis

                allowedCalendar.get(Calendar.YEAR) == year &&
                allowedCalendar.get(Calendar.MONTH) == month &&
                allowedCalendar.get(Calendar.DAY_OF_MONTH) == day
            }
        }

        override fun isSelectableYear(year: Int): Boolean {
            if (allowedDatesInMillis.isEmpty()) return false

            // Allow years that contain any of our allowed dates
            return allowedDatesInMillis.any { dateMillis ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dateMillis
                calendar.get(Calendar.YEAR) == year
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        selectableDates = selectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        onDateSelected(selectedDate)
                    }
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("موافق")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
