/**
 * Application class for Clara Challenge
 *
 * Responsible for initializing all feature modules and setting up
 * global dependencies through Hilt.
 */
package com.carmona.clarachallenge

import android.app.Application
import com.carmona.clarachallenge.core.network.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Main Application class annotated with @HiltAndroidApp to enable
 * Dagger Hilt dependency injection throughout the app.
 *
 * Features are initialized in onCreate() to ensure the provider
 * pattern is set up before any navigation occurs.
 */
@HiltAndroidApp
class ClaraChallengeApp : Application() {

    /**
     * Called when the application is starting.
     * Initializes all feature modules by registering their implementations
     * with the corresponding API providers.
     */
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}