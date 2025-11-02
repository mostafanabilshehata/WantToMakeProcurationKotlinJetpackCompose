package com.informatique.tawsekmisr.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class ExtraColors(

    val accent : Color,
    val background : Color,
    val cardBackground : Color,
    val cardDarkBackground : Color,
    val textBlue : Color,
    val textGray : Color,
    val green : Color,
    val lightGreen : Color,
    val gold : Color,
    val iconDarkBackground : Color,
    val iconLightBackground : Color,
    val iconLightBlue : Color,
    val iconDarkBlue : Color,
    val buttonDarkBlue : Color,
    val backgroundGradient : Brush



)
val LightExtraColors = ExtraColors(
    accent = AccentLight,
    background = BackGroundLight,
    cardBackground = CardBackGroundLight,
    cardDarkBackground = CardDarkBackGroundLight,
    textBlue = TextBlueLight,
    textGray = TextGrayLight,
    green = GreenLight,
    lightGreen = LightGreenLight,
    gold = GoldLight,
    iconDarkBackground = IconDarkBackGroundLight,
    iconLightBackground = IconLightBackGroundLight,
    iconLightBlue = IconLightBlueLight,
    iconDarkBlue = IconDarkBlueLight,
    buttonDarkBlue = ButtonDrakBlueLight,
    backgroundGradient = Brush.linearGradient(
        colors = listOf(
            AccentLight.copy(alpha = 0.5f),
            AccentLight.copy(alpha = 0.15f),
            AccentLight.copy(alpha = 0.05f),
            Color.White
        )
    )


)

val DarkExtraColors = ExtraColors(
    accent = AccentDark,
    background = BackGroundDark,
    cardBackground = CardBacKGroundDark,
    cardDarkBackground = CardDarkBackGroundDark,
    textBlue = TextBlueDark,
    textGray = TextGrayDark,
    green = GreenDark,
    lightGreen = LightGreenDark,
    gold = GoldDark,
    iconDarkBackground = IconDarkBackGroundDark,
    iconLightBackground = IconLightBackGroundDark,
    iconLightBlue = IconLightBlueDark,
    iconDarkBlue = IconDarkBlueLDark,
    buttonDarkBlue = ButtonDrakBlueDark,
    backgroundGradient = Brush.linearGradient(
        colors = listOf(
            AccentDark.copy(alpha = 0.5f),
            AccentDark.copy(alpha = 0.15f),
            AccentDark.copy(alpha = 0.05f),
            BackGroundDark
        )
    )

)

val LocalExtraColors = staticCompositionLocalOf { LightExtraColors }