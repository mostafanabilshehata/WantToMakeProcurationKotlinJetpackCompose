package com.informatique.tawsekmisr.di.module

import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager
import com.informatique.tawsekmisr.common.AndroidResourceProvider
import com.informatique.tawsekmisr.common.Const
import com.informatique.tawsekmisr.common.ResourceProvider
import com.informatique.tawsekmisr.common.dispatcher.DispatcherProvider
import com.informatique.tawsekmisr.common.logger.AppLogger
import com.informatique.tawsekmisr.common.logger.Logger
import com.informatique.tawsekmisr.common.networkhelper.NetworkHelper
import com.informatique.tawsekmisr.common.networkhelper.NetworkHelperImpl
import com.informatique.tawsekmisr.di.ApiKey
import com.informatique.tawsekmisr.di.BaseUrl
import com.informatique.tawsekmisr.di.DbName
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationModule {

    companion object {

        @ApiKey
        @Provides
        fun provideApiKey(): String = Const.API_KEY

        @BaseUrl
        @Provides
        fun provideBaseUrl(): String = Const.BASE_URL

        @DbName
        @Provides
        fun provideDbName(): String = Const.DB_NAME

        @Provides
        @Singleton
        fun provideLogger(): Logger = AppLogger()

        @Provides
        @Singleton
        fun provideDispatcherProvider(): DispatcherProvider = object : DispatcherProvider {
            override val main = Dispatchers.Main
            override val io = Dispatchers.IO
            override val default = Dispatchers.Default
        }

        @Provides
        @Singleton
        fun provideNetworkHelper(
            @ApplicationContext context: Context
        ): NetworkHelper {
            return NetworkHelperImpl(context)
        }

        @Provides
        @Singleton
        fun provideWorkManager(
            @ApplicationContext context: Context
        ): WorkManager {
            return WorkManager.getInstance(context)
        }

        @Provides
        @Singleton
        fun provideSharedPreferences(
            @ApplicationContext context: Context
        ): SharedPreferences {
            return context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        }
    }

    /**
     * Binds AndroidResourceProvider implementation to ResourceProvider interface
     */
    @Binds
    @Singleton
    abstract fun bindResourceProvider(
        androidResourceProvider: AndroidResourceProvider
    ): ResourceProvider
}