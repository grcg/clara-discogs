/**
 * Dagger Hilt module providing Coroutine dispatchers for dependency injection.
 * This allows for easier testing by enabling dispatcher substitution.
 */
package com.carmona.clarachallenge.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier annotation for IO dispatcher.
 * Use this to inject [Dispatchers.IO] for background operations.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

/**
 * Qualifier annotation for Main dispatcher.
 * Use this to inject [Dispatchers.Main] for UI operations.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    /**
     * Provides the IO dispatcher for background operations.
     *
     * @return [Dispatchers.IO] for network calls and database operations
     */
    @IoDispatcher
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Provides the Main dispatcher for UI operations.
     *
     * @return [Dispatchers.Main] for UI thread operations
     */
    @MainDispatcher
    @Provides
    @Singleton
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}