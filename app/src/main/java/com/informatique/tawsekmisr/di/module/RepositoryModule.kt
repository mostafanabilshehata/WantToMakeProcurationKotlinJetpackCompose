package com.informatique.tawsekmisr.di.module

import com.informatique.tawsekmisr.data.repository.LandingRepository
import com.informatique.tawsekmisr.data.repository.LandingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for Repository Layer Dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds LandingRepositoryImpl to LandingRepository interface
     */
    @Binds
    @Singleton
    abstract fun bindLandingRepository(
        landingRepositoryImpl: LandingRepositoryImpl
    ): LandingRepository
}

