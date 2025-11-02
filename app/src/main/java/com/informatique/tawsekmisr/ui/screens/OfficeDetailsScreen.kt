package com.informatique.tawsekmisr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.informatique.tawsekmisr.R
import com.informatique.tawsekmisr.data.model.Office
import com.informatique.tawsekmisr.ui.components.localizedApp
import com.informatique.tawsekmisr.ui.theme.LocalExtraColors
import androidx.core.net.toUri

/**
 * Get office-specific icon based on office type
 */
private fun getOfficeIcon(office: Office): androidx.compose.ui.graphics.vector.ImageVector {
    return if (office.isPremium) {
        Icons.Default.Star // Premium offices
    } else {
        Icons.Default.Apartment // All other offices
    }
}

@Composable
fun OfficeDetailsScreen(
    navController: NavController,
    office: Office,
    distance: Double,
    showBookingButton: Boolean = true // Default to true for backward compatibility
) {
    val extraColors = LocalExtraColors.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Office location
    val officeLocation = LatLng(office.latitude, office.longitude)

    // Camera position state for Google Maps
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(officeLocation, 15f)
    }

    val officeIcon = getOfficeIcon(office)
    val markerState = rememberMarkerState(position = officeLocation)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(extraColors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Google Map Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                // Google Map with custom office marker
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                ) {
                    MarkerComposable(
                        keys = arrayOf(office.id),
                        state = markerState,
                        title = office.name,
                        snippet = office.address
                    ) {
                        // Custom marker icon
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = extraColors.iconDarkBlue,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = officeIcon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Back button
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { navController.navigateUp() },
                    color = extraColors.cardBackground
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = extraColors.textBlue,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // Office Badge on Map - Show for all offices with proper color
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-40).dp)
                        .size(80.dp),
                    shape = CircleShape,
                    color = if (office.isPremium) extraColors.gold else extraColors.iconDarkBlue,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = officeIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )

                        // Verified checkmark
                        if (office.isVerified) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.dp, y = (-8).dp)
                                    .size(24.dp),
                                shape = CircleShape,
                                color = extraColors.cardBackground
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Verified",
                                    tint = extraColors.green,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Sheet Content - Office Info Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = extraColors.cardBackground,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(24.dp)
                ) {
                    // Office Name and Premium Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // Office Name only (removed Premium badge)
                            Text(
                                text = office.name,
                                fontSize = 24.sp,
                                color = extraColors.textBlue,
                                fontWeight = FontWeight.Normal
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // City and Office Type with separate rounded backgrounds
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Government chip
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = extraColors.iconDarkBlue.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Castle,
                                            contentDescription = null,
                                            tint = extraColors.iconDarkBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = office.city,
                                            fontSize = 12.sp,
                                            color = extraColors.textBlue,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Office Type chip
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (office.isPremium)
                                        extraColors.gold.copy(alpha = 0.15f)
                                    else
                                        extraColors.iconDarkBlue.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = officeIcon,
                                            contentDescription = null,
                                            tint = if (office.isPremium) extraColors.gold else extraColors.iconDarkBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = office.type,
                                            fontSize = 12.sp,
                                            color = if (office.isPremium) extraColors.gold else extraColors.textBlue,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Distance Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = extraColors.iconDarkBlue.copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = String.format("%.1f", distance),
                                    fontSize = 20.sp,
                                    color = extraColors.textBlue
                                )
                                Text(
                                    text = localizedApp(R.string.km_unit),
                                    fontSize = 11.sp,
                                    color = extraColors.textGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(color = extraColors.iconLightBackground)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Address Section
                    OfficeDetailRow(
                        icon = Icons.Default.LocationOn,
                        iconColor = extraColors.iconDarkBlue,
                        label = localizedApp(R.string.office_address_label),
                        value = office.address,
                        extraColors = extraColors,
                        onClick = {
                            // No action for address section
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Working Hours Section
                    OfficeDetailRow(
                        icon = Icons.Default.Schedule,
                        iconColor = extraColors.iconDarkBlue,
                        label = localizedApp(R.string.office_working_hours_label),
                        value = office.workingHours,
                        extraColors = extraColors,
                        onClick = {
                            // No action for working hours
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OfficeDetailRow(
                        icon = Icons.Default.Map,
                        iconColor = extraColors.iconDarkBlue,
                        label = localizedApp(R.string.office_action_directions),
                        value = localizedApp(R.string.office_action_directions),
                        extraColors = extraColors,
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                "geo:0,0?q=${office.address}".toUri()
                            )
                            context.startActivity(intent)
                        }
                    )

                    // Book Appointment Button - Only show if enabled
                    if (showBookingButton) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                navController.navigate("booking_form/${office.id}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = extraColors.iconDarkBlue
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = localizedApp(R.string.book_appointment_button),
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun OfficeDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    extraColors: com.informatique.tawsekmisr.ui.theme.ExtraColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(
                onClick = {
                    onClick
                }
            ),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.15f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.padding(12.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = extraColors.textGray,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = extraColors.textBlue
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    backgroundColor: Color,
    iconTint: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = backgroundColor
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
