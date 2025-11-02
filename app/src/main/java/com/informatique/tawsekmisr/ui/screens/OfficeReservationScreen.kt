package com.informatique.tawsekmisr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.informatique.tawsekmisr.R
import com.informatique.tawsekmisr.ui.components.*
import com.informatique.tawsekmisr.ui.theme.LocalExtraColors
import com.informatique.tawsekmisr.ui.viewmodels.ReservationViewModel
import com.informatique.tawsekmisr.ui.viewmodels.NationalIdValidationState
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeReservationScreen(
    navController: NavController,
    officeId: String,
    officeName: String = "Central Office",
    orgTypeDesc: String = "",
    viewModel: ReservationViewModel = hiltViewModel()
) {
    val extraColors = LocalExtraColors.current
    val scrollState = rememberScrollState()

    // Collect ViewModel states
    val formState by viewModel.formState.collectAsState()
    val nationalIdValidation by viewModel.nationalIdValidation.collectAsState()
    val classifications by viewModel.classifications.collectAsState()
    val classificationsLoading by viewModel.classificationsLoading.collectAsState()
    val types by viewModel.types.collectAsState()
    val typesLoading by viewModel.typesLoading.collectAsState()
    val officeAgenda by viewModel.officeAgenda.collectAsState()
    val agendaLoading by viewModel.agendaLoading.collectAsState()

    // Reservation submission states
    val reservationSubmitting by viewModel.reservationSubmitting.collectAsState()
    val reservationSuccess by viewModel.reservationSuccess.collectAsState()
    val reservationError by viewModel.reservationError.collectAsState()

    // Get device serial number
    val context = androidx.compose.ui.platform.LocalContext.current
    val mobileSerialNum = remember {
        android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    // Load data on screen launch
    LaunchedEffect(officeId) {
        viewModel.loadClassifications(officeId)
        viewModel.loadOfficeAgenda(officeId)
    }

    // Handle reservation success - navigate to ticket screen
    LaunchedEffect(reservationSuccess) {
        reservationSuccess?.let { response ->
            // URL encode parameters to handle Arabic characters and special characters
            val encodedNationalId = URLEncoder.encode(formState.nationalId, StandardCharsets.UTF_8.toString())
            val encodedOfficeName = URLEncoder.encode(officeName, StandardCharsets.UTF_8.toString())
            val encodedTicketNo = URLEncoder.encode(response.ticketNo, StandardCharsets.UTF_8.toString())
            val encodedDate = URLEncoder.encode(formState.appointmentDate, StandardCharsets.UTF_8.toString())
            val encodedTime = URLEncoder.encode(formState.selectedTime ?: "", StandardCharsets.UTF_8.toString())

            // Navigate to ticket screen with reservation details
            navController.navigate(
                "reservation_ticket/$encodedNationalId/$encodedOfficeName/${response.attendedNo}/$encodedTicketNo/$encodedDate/$encodedTime"
            ) {
                popUpTo("booking_form/$officeId") { inclusive = true }
            }
            viewModel.resetReservationState()
        }
    }

    // Show error dialog if reservation fails
    var showErrorDialog by remember { mutableStateOf(false) }
    LaunchedEffect(reservationError) {
        if (reservationError != null) {
            showErrorDialog = true
        }
    }

    // Get available dates and time slots
    val availableDates = remember(officeAgenda) {
        viewModel.getAvailableDates()
    }

    val availableDatesInMillis = remember(officeAgenda) {
        viewModel.getAvailableDatesInMillis()
    }

    val timeSlots = remember(formState.appointmentDate, officeAgenda) {
        if (formState.appointmentDate.isNotEmpty()) {
            viewModel.getTimeSlotsForDate(formState.appointmentDate)
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = extraColors.textBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(extraColors.backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Blue Header Card with Office Info
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = extraColors.iconDarkBlue
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp , vertical = 8.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = officeName,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = orgTypeDesc,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // Important Information Card (White card below blue header)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    color = extraColors.cardBackground,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = extraColors.iconDarkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localizedApp(R.string.important_notes),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = extraColors.textBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Info item 1
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = extraColors.lightGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localizedApp(R.string.note_1),
                                fontSize = 13.sp,
                                color = extraColors.textGray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Info item 2
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = extraColors.lightGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localizedApp(R.string.note_2),
                                fontSize = 13.sp,
                                color = extraColors.textGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // National ID Field with label and red asterisk
                Column {
                    Row {
                        Text(
                            text = localizedApp(R.string.reservation_national_id),
                            fontSize = 14.sp,
                            color = extraColors.textBlue,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "*",
//                            color = Color.Red,
//                            fontSize = 14.sp
//                        )

                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = formState.nationalId,
                        onValueChange = { viewModel.updateNationalId(it) },
                        label = localizedApp(R.string.reservation_national_id),
                        isNumeric = true,
                        placeholder = localizedApp(R.string.enter_national_id),
                        error = formState.nationalIdError,
                        mandatory = false // Hide asterisk in field since we show it above
                    )
                }

                // Show validation status
                if (nationalIdValidation is NationalIdValidationState.Loading) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = extraColors.accent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "جاري التحقق...",
                            fontSize = 12.sp,
                            color = extraColors.textGray
                        )
                    }
                } else if (nationalIdValidation is NationalIdValidationState.Valid) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Valid",
                            tint = extraColors.green,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localizedApp(R.string.national_id_valid),
                            fontSize = 12.sp,
                            color = extraColors.green
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Classification Dropdown with label and red asterisk
                Column {
                    Row {
                        Text(
                            text = localizedApp(R.string.reservation_classification),
                            fontSize = 14.sp,
                            color = extraColors.textBlue,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "*",
//                            color = Color.Red,
//                            fontSize = 14.sp
//                        )

                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (classificationsLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = extraColors.accent
                            )
                        }
                    } else {
                        CustomDropdown(
                            label = localizedApp(R.string.reservation_classification),
                            options = classifications.map { it.desc },
                            selectedOption = formState.selectedClassification,
                            onOptionSelected = { selected ->
                                classifications.find { it.desc == selected }?.let { classification ->
                                    viewModel.updateClassification(
                                        classification = selected,
                                        code = classification.code,
                                        officeId = officeId
                                    )
                                }
                            },
                            placeholder = localizedApp(R.string.enter_category),
                            error = formState.classificationError,
                            mandatory = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Type Dropdown with label and red asterisk
                Column {
                    Row {
                        Text(
                            text = localizedApp(R.string.reservation_type),
                            fontSize = 14.sp,
                            color = extraColors.textBlue,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "*",
//                            color = Color.Red,
//                            fontSize = 14.sp
//                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (typesLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = extraColors.accent
                            )
                        }
                    } else {
                        CustomDropdown(
                            label = localizedApp(R.string.reservation_type),
                            options = if (types.isNotEmpty()) types.map { it.desc } else listOf("اختر التصنيف أولاً"),
                            selectedOption = formState.selectedType,
                            onOptionSelected = { selected ->
                                types.find { it.desc == selected }?.let { type ->
                                    viewModel.updateType(type = selected, code = type.code)
                                }
                            },
                            placeholder = localizedApp(R.string.enter_type),
                            error = formState.typeError,
                            mandatory = false,
                            enabled = formState.selectedClassification != null && types.isNotEmpty()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker with label and red asterisk
                Column {
                    Row {
                        Text(
                            text = localizedApp(R.string.reservation_date),
                            fontSize = 14.sp,
                            color = extraColors.textBlue,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "*",
//                            color = Color.Red,
//                            fontSize = 14.sp
//                        )

                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (agendaLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = extraColors.accent
                            )
                        }
                    } else if (availableDates.isNotEmpty()) {
                        CustomRestrictedDatePicker(
                            value = formState.appointmentDate,
                            onValueChange = { viewModel.updateAppointmentDate(it) },
                            label = localizedApp(R.string.reservation_date),
                            error = formState.appointmentDateError,
                            mandatory = false,
                            placeholder = localizedApp(R.string.enter_date),
                            allowedDatesInMillis = availableDatesInMillis,
                            enabled = true
                        )
                    } else {
                        Text(
                            text = "لا توجد مواعيد متاحة",
                            fontSize = 12.sp,
                            color = extraColors.textGray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Slots Dropdown with label and red asterisk
                Column {
                    Row {
                        Text(
                            text = localizedApp(R.string.reservation_time),
                            fontSize = 14.sp,
                            color = extraColors.textBlue,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "*",
//                            color = Color.Red,
//                            fontSize = 14.sp
//                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomDropdown(
                        label = localizedApp(R.string.reservation_time),
                        options = if (timeSlots.isNotEmpty()) timeSlots else listOf("اختر التاريخ أولاً"),
                        selectedOption = formState.selectedTime,
                        onOptionSelected = { viewModel.updateAppointmentTime(it) },
                        error = formState.timeError,
                        mandatory = false,
                        placeholder = localizedApp(R.string.enter_time),
                        enabled = formState.appointmentDate.isNotEmpty() && timeSlots.isNotEmpty()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirmation Checkbox
                CustomCheckBox(
                    checked = formState.confirmationChecked,
                    onCheckedChange = { viewModel.updateConfirmation(it) },
                    label = localizedApp(R.string.reservation_confirmation),
                    error = formState.confirmationError
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Submit Button
                CommonButton(
                    text = localizedApp(R.string.submit_reservation),
                    backgroundColor = extraColors.iconDarkBlue,
                    enabled = !reservationSubmitting,
                    onClick = {
                        if (viewModel.validateForm()) {
                            viewModel.submitReservation(
                                nationalId = formState.nationalId,
                                orgUnitId = officeId,
                                categoryCode = formState.selectedClassificationCode ?: "",
                                typeCode = formState.selectedTypeCode ?: "",
                                date = formState.appointmentDate,
                                time = formState.selectedTime ?: "",
                                mobileSerialNum = mobileSerialNum
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Test Button - Simulate successful reservation for testing
                CommonButton(
                    text = "🧪 اختبار الحجز (Test)",
                    backgroundColor = extraColors.green,
                    enabled = true,
                    onClick = {
                        // Simulate successful reservation - navigate directly to ticket screen
                        val testNationalId = "29001010030681"
                        val testOfficeName = officeName
                        val testAttendedNo = 0
                        val testTicketNo = "TEST-${System.currentTimeMillis()}"
                        val testDate = "2025-11-09"
                        val testTime = "الفترة الثانية"

                        // URL encode parameters
                        val encodedNationalId = URLEncoder.encode(testNationalId, StandardCharsets.UTF_8.toString())
                        val encodedOfficeName = URLEncoder.encode(testOfficeName, StandardCharsets.UTF_8.toString())
                        val encodedTicketNo = URLEncoder.encode(testTicketNo, StandardCharsets.UTF_8.toString())
                        val encodedDate = URLEncoder.encode(testDate, StandardCharsets.UTF_8.toString())
                        val encodedTime = URLEncoder.encode(testTime, StandardCharsets.UTF_8.toString())

                        navController.navigate(
                            "reservation_ticket/$encodedNationalId/$encodedOfficeName/$testAttendedNo/$encodedTicketNo/$encodedDate/$encodedTime"
                        ) {
                            popUpTo("booking_form/$officeId") { inclusive = true }
                        }
                    }
                )

                // Show loading indicator while submitting
                if (reservationSubmitting) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = extraColors.accent
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "جاري الإرسال...",
                            fontSize = 14.sp,
                            color = extraColors.textGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.resetReservationState()
            },
            title = {
                Text(
                    text = localizedApp(R.string.error_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(reservationError ?: "Unknown error occurred")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        viewModel.resetReservationState()
                    }
                ) {
                    Text(
                        text = localizedApp(R.string.ok_button),
                        color = Color(110, 179, 166)
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
