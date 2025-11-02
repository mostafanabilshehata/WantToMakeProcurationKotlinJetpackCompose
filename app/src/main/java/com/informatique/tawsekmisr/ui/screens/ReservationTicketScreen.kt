package com.informatique.tawsekmisr.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.informatique.tawsekmisr.R
import com.informatique.tawsekmisr.ui.components.CommonButton
import com.informatique.tawsekmisr.ui.components.localizedApp
import com.informatique.tawsekmisr.ui.theme.ExtraColors
import com.informatique.tawsekmisr.ui.theme.LocalExtraColors

@Composable
fun ReservationTicketScreen(
    navController: NavController,
    nationalId: String,
    officeName: String,
    attendanceNo: Int,
    reservationDate: String,
    reservationTime: String
) {
    val extraColors = LocalExtraColors.current
    val scrollState = rememberScrollState()

    // Handle back button press - navigate to homepage
    BackHandler {
        navController.navigate("homepage") {
            popUpTo("homepage") { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extraColors.background)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Success Icon
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(60.dp),
            color = extraColors.green
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = extraColors.cardBackground,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Success Title
        Text(
            text = localizedApp(R.string.reservation_success_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = extraColors.textBlue,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Success Subtitle
        Text(
            text = localizedApp(R.string.reservation_success_subtitle),
            fontSize = 14.sp,
            color = extraColors.textGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Ticket Details Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = extraColors.cardBackground,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // National ID
                TicketInfoRow(
                    label = localizedApp(R.string.reservation_national_id),
                    value = nationalId,
                    extraColors = extraColors
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Office Name
                TicketInfoRow(
                    label = localizedApp(R.string.office_name_label),
                    value = officeName,
                    extraColors = extraColors
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Attendance Number
                TicketInfoRow(
                    label = localizedApp(R.string.attendance_number_label),
                    value = attendanceNo.toString(),
                    extraColors = extraColors
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Reservation Date
                TicketInfoRow(
                    label = localizedApp(R.string.reservation_date),
                    value = reservationDate,
                    extraColors = extraColors
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Reservation Time
                TicketInfoRow(
                    label = localizedApp(R.string.reservation_time),
                    value = reservationTime,
                    extraColors = extraColors
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Important Note Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = extraColors.iconDarkBlue.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = extraColors.iconDarkBlue,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = localizedApp(R.string.important_note_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = extraColors.textBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = localizedApp(R.string.reservation_note_message),
                        fontSize = 12.sp,
                        color = extraColors.textBlue,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // OK Button
        CommonButton(
            text = localizedApp(R.string.ok_button),
            backgroundColor = extraColors.buttonDarkBlue,
            onClick = {
                navController.navigate("homepage") {
                    popUpTo("homepage") { inclusive = true }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TicketInfoRow(
    label: String,
    value: String,
    extraColors: ExtraColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label with fixed width to align all values
        Text(
            text = label,
            fontSize = 14.sp,
            color = extraColors.textGray,
            modifier = Modifier.width(120.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Value
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = extraColors.textBlue,
            modifier = Modifier.weight(1f)
        )
    }
}