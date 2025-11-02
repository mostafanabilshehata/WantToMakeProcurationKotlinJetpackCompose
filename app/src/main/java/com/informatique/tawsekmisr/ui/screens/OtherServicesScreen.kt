package com.informatique.tawsekmisr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPasteSearch
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.informatique.tawsekmisr.R
import com.informatique.tawsekmisr.ui.components.localizedApp
import com.informatique.tawsekmisr.ui.theme.LocalExtraColors
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherServicesScreen(navController: NavController) {
    val extraColors = LocalExtraColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizedApp(R.string.other_services_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = extraColors.textBlue
                    )
                },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(extraColors.backgroundGradient)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Hero Icon
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(32.dp),
                color = extraColors.green.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector =  Icons.Default.GridView,
                    contentDescription = null,
                    tint = extraColors.green,
                    modifier = Modifier.padding(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = localizedApp(R.string.other_services_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = extraColors.textBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = localizedApp(R.string.other_services_subtitle),
                fontSize = 14.sp,
                color = extraColors.textGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Service Items - Get localized titles OUTSIDE the onClick
            val translationTitle = localizedApp(R.string.other_service_translation)
            val realEstateTitle = localizedApp(R.string.other_service_real_estate)
            val contractCopyTitle = localizedApp(R.string.other_service_contract_copy)
            val subtitle = localizedApp(R.string.other_service_access_gov)

            val services = listOf(
                OtherServiceItem(
                    title = translationTitle,
                    subtitle = subtitle,
                    iconColor = extraColors.accent,
                    icon = Icons.Default.Translate
                ),
                OtherServiceItem(
                    title = realEstateTitle,
                    subtitle = subtitle,
                    iconColor = extraColors.green,
                    icon = Icons.Default.ContentPasteSearch
                ),
                OtherServiceItem(
                    title = contractCopyTitle,
                    subtitle = subtitle,
                    icon = Icons.Default.Description,
                    iconColor = extraColors.gold
                )
            )

            services.forEach { service ->
                OtherServiceCard(
                    title = service.title,
                    subtitle = service.subtitle,
                    iconColor = service.iconColor,
                    icon = service.icon,
                            onClick = {
                        val encodedUrl = URLEncoder.encode("https://rern.gov.eg/", StandardCharsets.UTF_8.toString())
                        val encodedTitle = URLEncoder.encode(service.title, StandardCharsets.UTF_8.toString())
                        navController.navigate("webview/$encodedUrl/$encodedTitle")
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private data class OtherServiceItem(
    val title: String,
    val subtitle: String,
    val iconColor: Color,
    val icon : ImageVector
)

@Composable
private fun OtherServiceCard(
    title: String,
    subtitle: String,
    iconColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val extraColors = LocalExtraColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = extraColors.cardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = iconColor.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = extraColors.textBlue
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = extraColors.textGray
                )
            }
        }
    }
}
