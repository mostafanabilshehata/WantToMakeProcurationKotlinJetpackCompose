package com.informatique.tawsekmisr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.informatique.tawsekmisr.ui.theme.LocalExtraColors

/**
 * Reusable custom alert dialog that can be used across the app
 *
 * @param showDialog Controls dialog visibility
 * @param onDismiss Called when dialog should be dismissed
 * @param icon The icon to display at the top (default: Info icon)
 * @param iconTint Color of the icon (default: gold)
 * @param iconBackgroundTint Background color of the icon circle (default: gold with alpha)
 * @param title Main title text (Arabic)
 * @param message Description/message text (Arabic)
 * @param confirmButtonText Text for the confirm/continue button (default: "استمرار")
 * @param dismissButtonText Text for the dismiss/close button (default: "إغلاق")
 * @param onConfirm Called when confirm button is clicked (nullable - if null, button is hidden)
 * @param showDismissButton Whether to show the dismiss button (default: true)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAlertDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    icon: ImageVector = Icons.Default.Info,
    iconTint: Color? = null,
    iconBackgroundTint: Color? = null,
    title: String,
    message: String,
    confirmButtonText: String = "استمرار",
    dismissButtonText: String = "إغلاق",
    onConfirm: (() -> Unit)? = null,
    showDismissButton: Boolean = true
) {
    val extraColors = LocalExtraColors.current

    if (showDialog) {
        BasicAlertDialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = extraColors.black
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = iconBackgroundTint ?: extraColors.gold,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint ?: extraColors.cardBackground,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Title
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = extraColors.textBlue,
                        textAlign = TextAlign.Center
                    )

                    // Message
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = extraColors.textGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Confirm Button (if onConfirm is provided)
                        onConfirm?.let { confirmAction ->
                            Button(
                                onClick = {
                                    confirmAction()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = extraColors.iconDarkBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = confirmButtonText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        // Dismiss Button (if visible)
                        if (showDismissButton) {
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = extraColors.textDarkGray.copy(alpha = 0.2f),
                                    contentColor = extraColors.textGray
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = dismissButtonText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

