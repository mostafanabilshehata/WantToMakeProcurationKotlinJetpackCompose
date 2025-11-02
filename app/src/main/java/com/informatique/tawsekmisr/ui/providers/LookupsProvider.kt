package com.informatique.tawsekmisr.ui.providers

import androidx.compose.runtime.compositionLocalOf
import com.informatique.tawsekmisr.data.model.Government
import com.informatique.tawsekmisr.data.model.Office

/**
 * CompositionLocal for providing offices throughout the app
 * This allows any screen to access offices without passing them as parameters
 */
val LocalOffices = compositionLocalOf<List<Office>> {
    emptyList()
}

/**
 * CompositionLocal for providing governments throughout the app
 * This allows any screen to access governments without passing them as parameters
 */
val LocalGovernments = compositionLocalOf<List<Government>> {
    emptyList()
}
