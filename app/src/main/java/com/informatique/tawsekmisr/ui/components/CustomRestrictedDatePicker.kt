package com.informatique.tawsekmisr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.informatique.tawsekmisr.ui.theme.LocalExtraColors
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
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    placeholder: String? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val extraColors = LocalExtraColors.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) {
                    showDatePicker = true
                }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = extraColors.cardBackground,
                    unfocusedContainerColor = extraColors.cardBackground,
                    disabledContainerColor = extraColors.cardBackground,
                    focusedBorderColor = if (error != null) Color(0xFFE74C3C) else Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedTextColor = extraColors.textBlue,
                    unfocusedTextColor = extraColors.textBlue,
                    disabledTextColor = extraColors.textBlue
                ),
                readOnly = true,
                enabled = false,
                placeholder = {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            color = extraColors.textGray,
                            fontSize = 16.sp
                        )
                    }
                },
                leadingIcon = if (leadingIcon != null) {
                    {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = extraColors.iconDarkBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else null,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date",
                        tint = if (enabled) extraColors.textGray
                        else extraColors.textGray.copy(alpha = 0.3f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = error != null,
                singleLine = true
            )
        }

        if (error != null) {
            Text(
                text = error,
                fontSize = 12.sp,
                color = Color(0xFFE74C3C),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
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