package com.informatique.tawsekmisr.di.module

import com.abanoub.myapp.di.security.Environment
import com.abanoub.myapp.di.security.EnvironmentConfig
import com.informatique.tawsekmisr.di.security.SSLPinning
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
class NetworkModule {

    @Singleton
    @Provides
    fun provideEnvironmentConfig(): Environment {
        return EnvironmentConfig.currentEnvironment
    }

    @Singleton
    @Provides
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
            coerceInputValues = true
            encodeDefaults = true  // Include fields with default values in JSON
            explicitNulls = true   // Include null values in JSON
        }
    }

    @Singleton
    @Provides
    fun provideHttpClient(environment: Environment, json: Json): HttpClient {
        return HttpClient(Android) {
            // Engine configuration
            engine {

                // SSL Pinning
                if (environment.security.enableSSLPinning)
                    this.sslManager = { SSLPinning.provideCertificatePinner() }

            }

            // Default request configuration
            defaultRequest {
                url(environment.baseUrl)
                // Removed contentType to avoid WAF triggering
            }

            install(ContentNegotiation) {
                json(json)
            }

            // Logging
            if (environment.features.enableDebugEndpoints) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL
                }
            }

            // Response caching
            if (environment.features.enableCaching) {
                install(HttpCache)
            }

            // Request retry with exponential backoff
            install(HttpRequestRetry) {
                maxRetries = 3
                retryOnExceptionIf { request, cause ->
                    cause is ConnectTimeoutException
                }
                exponentialDelay()
            }

            // Custom timeout plugin
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000L
                connectTimeoutMillis = 15_000L
                socketTimeoutMillis = 30_000L
            }

        }
    }

    @Singleton
    @Provides
    fun provideAppRepository(httpClient: HttpClient): AppRepository {
        return AppRepository(httpClient)
    }

}