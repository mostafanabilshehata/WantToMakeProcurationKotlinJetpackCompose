package com.informatique.tawsekmisr.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.informatique.tawsekmisr.R
import com.informatique.tawsekmisr.data.model.Office
import com.informatique.tawsekmisr.ui.components.localizedApp
import com.informatique.tawsekmisr.ui.theme.LocalExtraColors
import com.informatique.tawsekmisr.ui.providers.LocalOffices
import com.informatique.tawsekmisr.ui.providers.LocalGovernments
import com.informatique.tawsekmisr.ui.viewmodels.FindOfficeViewModel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindOfficeScreen(
    navController: NavController,
    viewModel: FindOfficeViewModel = hiltViewModel(),
    showBookingButton: Boolean = true // Default to true for backward compatibility
) {
    val extraColors = LocalExtraColors.current
    // Get offices and governments from CompositionLocal
    val offices = LocalOffices.current
    val governments = LocalGovernments.current

    // Collect state from ViewModel
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGovernment by viewModel.selectedGovernment.collectAsState()
    val filteredOffices by viewModel.filteredOffices.collectAsState()
    val isLoadingLocation by viewModel.isLoadingLocation.collectAsState()
    val locationError by viewModel.locationError.collectAsState()

    var isGovernmentDropdownExpanded by remember { mutableStateOf(false) }
    var showLocationPermissionDialog by remember { mutableStateOf(false) }
    var isMapView by remember { mutableStateOf(false) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // Permission granted, fetch location
            viewModel.fetchUserLocation()
        } else {
            // Permission denied
            showLocationPermissionDialog = false
        }
    }

    // Initialize ViewModel with offices
    LaunchedEffect(offices) {
        viewModel.setOffices(offices)
    }

    // Request location permission on first launch
    LaunchedEffect(Unit) {
        if (viewModel.hasLocationPermission()) {
            // Already has permission, fetch location
            viewModel.fetchUserLocation()
        } else {
            // Request permission
            showLocationPermissionDialog = true
        }
    }

    // Location Permission Dialog
    if (showLocationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showLocationPermissionDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = extraColors.iconDarkBlue
                )
            },
            title = {
                Text(text = localizedApp(R.string.location_permission_title))
            },
            text = {
                Text(text = localizedApp(R.string.location_permission_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationPermissionDialog = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text(localizedApp(R.string.allow))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLocationPermissionDialog = false
                        // Use default location if denied
                        viewModel.fetchUserLocation()
                    }
                ) {
                    Text(localizedApp(R.string.deny))
                }
            }
        )
    }

    // Create government list with "all" option at the start
    val governmentOptions = remember(governments) {
        listOf("all") + governments.map { it.name }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extraColors.backgroundGradient)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = localizedApp(R.string.find_office_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = extraColors.textBlue
                )
                Text(
                    text = "${filteredOffices.size} ${localizedApp(R.string.offices_available)}",
                    fontSize = 14.sp,
                    color = extraColors.textGray
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location refresh button
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (viewModel.hasLocationPermission()) {
                                viewModel.fetchUserLocation()
                            } else {
                                showLocationPermissionDialog = true
                            }
                        },
                    color = if (isLoadingLocation) extraColors.cardBackground else extraColors.cardBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isLoadingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = extraColors.iconDarkBlue,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Get Location",
                                tint = extraColors.iconDarkBlue,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { isMapView = !isMapView },
                    color = if (isMapView) extraColors.cardBackground else extraColors.cardBackground
                ) {
                    Icon(
                        imageVector = if (isMapView) Icons.Default.List else Icons.Default.Map,
                        contentDescription = if (isMapView) "List View" else "Map View",
                        tint = if (isMapView) extraColors.iconDarkBlue else extraColors.iconDarkBlue,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Location error message
        locationError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = extraColors.lightGreen.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = extraColors.gold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = error,
                        fontSize = 12.sp,
                        color = extraColors.textBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Government Dropdown - Only visible when "all" filter is selected
        if (selectedFilter == "all") {
            ExposedDropdownMenuBox(
                expanded = isGovernmentDropdownExpanded,
                onExpandedChange = { isGovernmentDropdownExpanded = !isGovernmentDropdownExpanded }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(16.dp),
                    color = extraColors.cardBackground
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isGovernmentDropdownExpanded = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = extraColors.iconDarkBlue
                            )
                            Text(
                                text = if (selectedGovernment == "all")
                                    localizedApp(R.string.all_governments)
                                else selectedGovernment,
                                fontSize = 16.sp,
                                color = extraColors.textBlue
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = extraColors.textGray
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = isGovernmentDropdownExpanded,
                    onDismissRequest = { isGovernmentDropdownExpanded = false }
                ) {
                    governmentOptions.forEach { government ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (government == "all")
                                        localizedApp(R.string.all_governments)
                                    else government
                                )
                            },
                            onClick = {
                                viewModel.setSelectedGovernment(government)
                                isGovernmentDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = extraColors.cardBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = extraColors.textGray
                )
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = localizedApp(R.string.search_offices),
                            color = extraColors.textGray
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips - Reordered to Nearest, All, Premium
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                label = localizedApp(R.string.filter_nearest),
                icon = Icons.Default.Navigation,
                isSelected = selectedFilter == "nearest",
                onClick = { viewModel.setFilter("nearest") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                label = localizedApp(R.string.filter_all),
                icon = Icons.Default.Apartment,
                isSelected = selectedFilter == "all",
                onClick = { viewModel.setFilter("all") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                label = localizedApp(R.string.filter_premium),
                icon = Icons.Default.Star,
                isSelected = selectedFilter == "premium",
                onClick = { viewModel.setFilter("premium") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content: List View or Map View
        if (isMapView) {
            // Map View
            OfficeMapView(
                offices = filteredOffices,
                navController = navController,
                showBookingButton = showBookingButton,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Office List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOffices) { office ->
                    OfficeCard(
                        office = office,
                        onClick = {
                            // Navigate to office details with calculated distance and booking button visibility
                            navController.navigate("office_details/${office.id}/${office.distance}/$showBookingButton")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extraColors = LocalExtraColors.current

    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        color = if (isSelected) extraColors.iconDarkBlue else extraColors.cardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else extraColors.iconDarkBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else extraColors.iconDarkBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun OfficeCard(
    office: Office,
    onClick: () -> Unit
) {
    val extraColors = LocalExtraColors.current
    val officeIcon = getOfficeIcon(office)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = extraColors.cardBackground,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (office.isPremium) extraColors.gold.copy(alpha = 0.2f)
                        else extraColors.iconDarkBlue.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = officeIcon,
                    contentDescription = null,
                    tint = if (office.isPremium) extraColors.gold else extraColors.iconDarkBlue,
                    modifier = Modifier.size(40.dp)
                )

                if (office.isVerified) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                            .size(24.dp),
                        shape = CircleShape,
                        color = extraColors.iconDarkBlue.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Verified",
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            // Office Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Office Name (removed Premium badge)
                Text(
                    text = office.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = extraColors.textBlue,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = extraColors.textGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = office.type,
                        fontSize = 12.sp,
                        color = extraColors.textGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = extraColors.iconDarkBlue.copy(alpha = 0.15f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = office.address,
                        fontSize = 12.sp,
                        color = extraColors.textGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            // Distance and Arrow
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(80.dp)
            ) {
                // Format distance to 1 decimal place and show on single line with localized km unit
                Text(
                    text = String.format("%.1f %s", office.distance, localizedApp(R.string.km_unit)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = extraColors.textBlue
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = extraColors.textGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun OfficeMapView(
    offices: List<Office>,
    navController: NavController,
    showBookingButton: Boolean,
    modifier: Modifier = Modifier
) {
    val extraColors = LocalExtraColors.current

    // Calculate center position from all offices or use default (Cairo, Egypt)
    val centerPosition = remember(offices) {
        if (offices.isNotEmpty()) {
            val avgLat = offices.map { it.latitude }.average()
            val avgLng = offices.map { it.longitude }.average()
            LatLng(avgLat, avgLng)
        } else {
            LatLng(30.0444, 31.2357) // Cairo, Egypt
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerPosition, 11f)
    }

    var selectedOffice by remember { mutableStateOf<Office?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            // Add markers for each office with custom composable icons
            offices.forEach { office ->
                val position = LatLng(office.latitude, office.longitude)
                val markerState = rememberMarkerState(position = position)
                val officeIcon = getOfficeIcon(office)

                MarkerComposable(
                    keys = arrayOf(office.id),
                    state = markerState,
                    title = office.name,
                    snippet = "${String.format("%.1f", office.distance)} ${localizedApp(R.string.km_unit)}",
                    onClick = {
                        selectedOffice = office
                        true
                    }
                ) {
                    // Custom marker icon
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = if (office.isPremium) extraColors.gold else extraColors.iconDarkBlue,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = officeIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom sheet for selected office
        selectedOffice?.let { office ->
            val officeIcon = getOfficeIcon(office)

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        navController.navigate("office_details/${office.id}/${office.distance}/$showBookingButton")
                    },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Office Icon
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = if (office.isPremium) extraColors.gold else extraColors.iconDarkBlue,
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

                    // Office Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = office.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = extraColors.textBlue,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = extraColors.iconDarkBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = String.format("%.1f %s", office.distance, localizedApp(R.string.km_unit)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = extraColors.iconDarkBlue.copy(alpha = 0.15f)
                            )
                        }

                        if (office.isPremium) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = extraColors.gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = localizedApp(R.string.filter_premium),
                                    fontSize = 12.sp,
                                    color = extraColors.gold
                                )
                            }
                        }
                    }

                    // Arrow
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = extraColors.textGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
