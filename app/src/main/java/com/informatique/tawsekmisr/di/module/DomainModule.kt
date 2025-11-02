package com.informatique.tawsekmisr.di.module

import com.informatique.tawsekmisr.common.util.LocalizationHelper
import com.informatique.tawsekmisr.domain.strategy.ReservationStrategyFactory
import com.informatique.tawsekmisr.domain.validation.ReservationFormValidation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Hilt Module for Domain Layer Dependencies
 */
@Module
@InstallIn(ViewModelComponent::class)
object DomainModule {

    /**
     * Provides ReservationFormValidation instance
     */
    @Provides
    @ViewModelScoped
    fun provideReservationFormValidation(
        localizationHelper: LocalizationHelper
    ): ReservationFormValidation {
        return ReservationFormValidation(localizationHelper)
    }

    /**
     * Provides ReservationStrategyFactory instance
     */
    @Provides
    @ViewModelScoped
    fun provideReservationStrategyFactory(): ReservationStrategyFactory {
        return ReservationStrategyFactory()
    }
}
