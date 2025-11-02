package com.informatique.tawsekmisr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.informatique.tawsekmisr.R
import com.informatique.tawsekmisr.data.model.InquireReservation
import com.informatique.tawsekmisr.ui.components.*
import com.informatique.tawsekmisr.ui.theme.LocalExtraColors
import com.informatique.tawsekmisr.ui.viewmodels.BookingInquiryViewModel
import com.informatique.tawsekmisr.ui.viewmodels.NationalIdValidationState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingInquiryScreen(
    navController: NavController,
    viewModel: BookingInquiryViewModel = hiltViewModel()
) {
    val extraColors = LocalExtraColors.current

    // Collect ViewModel states
    val nationalId by viewModel.nationalId.collectAsState()
    val nationalIdError by viewModel.nationalIdError.collectAsState()
    val nationalIdValidation by viewModel.nationalIdValidation.collectAsState()
    val filteredReservations by viewModel.filteredReservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val hasPerformedInquiry by viewModel.hasPerformedInquiry.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizedApp(R.string.service_booking_inquiry),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(extraColors.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Enhanced Header Card with Icon
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon with gradient background
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(110, 179, 166).copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(110, 179, 166),
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "استعلام عن حجوزاتك",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "أدخل الرقم القومي للاستعلام عن جميع حجوزاتك",
                                fontSize = 13.sp,
                                color = Color(126, 126, 133),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                // National ID Field
                CustomTextField(
                    value = nationalId,
                    onValueChange = { viewModel.updateNationalId(it) },
                    label = localizedApp(R.string.reservation_national_id),
                    isNumeric = true,
                    error = nationalIdError,
                    mandatory = true
                )

                // Show validation status
                if (nationalIdValidation is NationalIdValidationState.Loading) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "جاري التحقق...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else if (nationalIdValidation is NationalIdValidationState.Valid) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Valid",
                            tint = Color(110, 179, 166),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الرقم القومي صحيح",
                            fontSize = 12.sp,
                            color = Color(110, 179, 166)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Inquiry Button
                CommonButton(
                    text = "استعلام",
                    backgroundColor = Color(110, 179, 166),
                    enabled = !isLoading,
                    onClick = { viewModel.fetchReservations() }
                )
            }

            // Loading indicator
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(110, 179, 166)
                        )
                    }
                }
            }

            // Error message
            if (error != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(244, 67, 54).copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(244, 67, 54),
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "خطأ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(244, 67, 54)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = error ?: "حدث خطأ غير متوقع",
                                    fontSize = 12.sp,
                                    color = Color(126, 126, 133)
                                )
                            }
                        }
                    }
                }
            }

            // Reservations List
            if (filteredReservations.isNotEmpty()) {
                item {
                    Text(
                        text = "حجوزاتك (${filteredReservations.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(filteredReservations) { reservation ->
                    ReservationCard(reservation = reservation)
                }
            } else if (!isLoading && error == null && hasPerformedInquiry) {
                // Show empty state only after inquiry button is clicked
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "لا توجد حجوزات",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Text(
                                text = "لم يتم العثور على أي حجوزات لهذا الرقم القومي",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ReservationCard(reservation: InquireReservation) {
    // Parse and format date
    val formattedDate = remember(reservation.reservationDate) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(reservation.reservationDate)
            date?.let { outputFormat.format(it) } ?: reservation.reservationDate
        } catch (e: Exception) {
            reservation.reservationDate
        }
    }

    // Format time based on vipFlag
    val formattedTime = remember(reservation.reserveTime, reservation.orgVipFlag) {
        when (reservation.orgVipFlag) {
            "1", "4" -> {
                // Format: "2023-09-18 12:00:00.0" -> "12:00"
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
                    val time = inputFormat.parse(reservation.reserveTime)
                    time?.let { outputFormat.format(it) } ?: reservation.reserveTime
                } catch (e: Exception) {
                    reservation.reserveTime
                }
            }
            "2", "3" -> {
                // Already formatted as period: "13:30-16:00"
                reservation.reserveTime
            }
            else -> reservation.reserveTime
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with office name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color(110, 179, 166),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = reservation.orgName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // Queue ID Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(98, 138, 236).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "#${reservation.queVipId}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(98, 138, 236),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = Color(0xFFE8E8E8))

            Spacer(modifier = Modifier.height(12.dp))

            // Transaction Category
            InfoRow(
                icon = Icons.Default.Category,
                iconColor = Color(110, 179, 166),
                label = "التصنيف",
                value = reservation.transactionDesc
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Transaction Type
            InfoRow(
                icon = Icons.Default.Description,
                iconColor = Color(98, 138, 236),
                label = "نوع المعاملة",
                value = reservation.transactionTypeDesc
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date
                Box(modifier = Modifier.weight(1f)) {
                    InfoRow(
                        icon = Icons.Default.CalendarMonth,
                        iconColor = Color(255, 159, 10),
                        label = "التاريخ",
                        value = formattedDate
                    )
                }

                // Time
                Box(modifier = Modifier.weight(1f)) {
                    InfoRow(
                        icon = Icons.Default.Schedule,
                        iconColor = Color(110, 179, 166),
                        label = "الوقت",
                        value = formattedTime
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(16.dp),
            color = iconColor.copy(alpha = 0.15f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.padding(6.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(126, 126, 133)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}
