package com.informatique.tawsekmisr.ui.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color


@Composable
fun CommonButton(
    text: String,
    isPrimary: Boolean = true,
    backgroundColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
//    val backgroundColor = if (isPrimary) colorResource(R.color.login_button) else colorResource(R.color.login_button)
    val contentColor = if (isPrimary) Color.White else Color.Black
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = ButtonDefaults.buttonElevation(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = text, color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
