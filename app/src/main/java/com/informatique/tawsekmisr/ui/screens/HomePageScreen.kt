package com.informatique.tawsekmisr.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.informatique.tawsekmisr.R
import com.informatique.tawsekmisr.ui.components.localizedApp
import com.informatique.tawsekmisr.ui.theme.LocalExtraColors
import com.informatique.tawsekmisr.ui.theme.ExtraColors
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomePageScreen(navController: NavController) {
    val extraColors = LocalExtraColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(extraColors.backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top header row with profile and settings icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Icon and Welcome Text (Start)
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        color = extraColors.iconDarkBlue
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOutline,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Welcome Text beside profile icon
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = localizedApp(R.string.hello_label),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extraColors.textBlue
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = localizedApp(R.string.how_can_help),
                            fontSize = 13.sp,
                            color = extraColors.textGray
                        )
                    }
                }

                // Settings Icon (End)
                Surface(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable { navController.navigate("settings_screen") },
                    color = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = extraColors.textGray,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Feature Box Card (24/7, Safe, Fast)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = extraColors.background),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 24/7 Feature
                    FeatureBox(
                        icon = Icons.Default.AccessTimeFilled,
                        iconColor = extraColors.iconDarkBlue,
                        title = localizedApp(R.string.feature_24_7),
                        subtitle = localizedApp(R.string.feature_24_7_desc),
                        extraColors = extraColors
                    )

                    HorizontalDivider(
                        modifier = Modifier
                            .height(80.dp)
                            .width(1.dp),
                        color = extraColors.iconLightBackground
                    )

                    // Safe Feature
                    FeatureBox(
                        icon = Icons.Default.Shield,
                        iconColor = extraColors.green,
                        title = localizedApp(R.string.feature_safe),
                        subtitle = localizedApp(R.string.feature_safe_desc),
                        extraColors = extraColors
                    )

                    HorizontalDivider(
                        modifier = Modifier
                            .height(80.dp)
                            .width(1.dp),
                        color = extraColors.iconLightBackground
                    )

                    // Fast Feature
                    FeatureBox(
                        icon = Icons.Default.Bolt,
                        iconColor = extraColors.gold,
                        title = localizedApp(R.string.feature_fast),
                        subtitle = localizedApp(R.string.feature_fast_desc),
                        extraColors = extraColors
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Available Services Title
            Text(
                text = localizedApp(R.string.available_services_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = extraColors.textBlue,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Services List (Single Column)
            val services = listOf(
                ServiceData(
                    titleRes = R.string.service_branch_load,
                    descRes = R.string.service_branch_load_desc,
                    icon = Icons.Default.LocationCity,
                    color = extraColors.accent,
                    route = "branch_load"
                ),
                ServiceData(
                    titleRes = R.string.service_book_appointment,
                    descRes = R.string.service_book_appointment_desc,
                    icon = Icons.Default.CalendarMonth,
                    color = extraColors.accent,
                    route = "book_appointment"
                ),
                ServiceData(
                    titleRes = R.string.service_forms,
                    descRes = R.string.service_forms_desc,
                    icon = Icons.Default.Description,
                    color = extraColors.green,
                    route = "forms"
                ),
                ServiceData(
                    titleRes = R.string.service_enotary,
                    descRes = R.string.service_enotary_desc,
                    icon = Icons.Default.VerifiedUser,
                    color = extraColors.green,
                    route = "enotary"
                ),
                ServiceData(
                    titleRes = R.string.service_booking_inquiry,
                    descRes = R.string.service_booking_inquiry_desc,
                    icon = Icons.Default.Search,
                    color = extraColors.gold,
                    route = "booking_inquiry"
                ),
                ServiceData(
                    titleRes = R.string.service_other_services,
                    descRes = R.string.service_other_services_desc,
                    icon = Icons.Default.GridView,
                    color = extraColors.gold,
                    route = "other_services"
                )
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                services.forEach { s ->
                    ServiceCardHorizontal(
                        title = localizedApp(s.titleRes),
                        subtitle = localizedApp(s.descRes),
                        iconTint = extraColors.cardBackground,
                        iconBackgroundColor = extraColors.iconDarkBlue,
                        icon = s.icon,
                        onClick = { navController.navigate(s.route) },
                        extraColors = extraColors
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureBox(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    extraColors: ExtraColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = extraColors.textBlue
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = extraColors.textGray,
            textAlign = TextAlign.Center
        )
    }
}

private data class ServiceData(
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
private fun ServiceCardHorizontal(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackgroundColor: Color,
    onClick: () -> Unit,
    extraColors: ExtraColors
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 100, easing = LinearOutSlowInEasing),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .scale(scale)
            .clickable {
                isPressed = true
                GlobalScope.launch {
                    delay(100)
                    isPressed = false
                    delay(100)
                }
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = extraColors.cardBackground),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 25.dp else 15.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box with animated gradient and decorative circles
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                extraColors.iconDarkBlue.copy(alpha = 0.9f),
                                extraColors.iconDarkBlue
                            ),
                            start = if (isPressed)
                                androidx.compose.ui.geometry.Offset(0f, 0f)
                            else
                                androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                            end = if (isPressed)
                                androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            else
                                androidx.compose.ui.geometry.Offset(0f, 0f)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Decorative Circle 1 (larger, top-left)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = (-20).dp, y = (-20).dp)
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                )

                // Decorative Circle 2 (smaller, bottom-right)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .offset(x = 15.dp, y = 25.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = CircleShape
                        )
                )

                // Icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Text Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = extraColors.textBlue,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = extraColors.textGray,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}