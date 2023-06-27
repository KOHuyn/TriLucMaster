package com.apero.qrart.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.apero.qrart.data.prefs.AppDataStore
import com.apero.qrart.data.prefs.AppDataStoreImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by KO Huyn on 26/06/2023.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    private const val APP_PREFERENCES_NAME = "app_preferences"

    private val Context.dataStore by preferencesDataStore(name = APP_PREFERENCES_NAME,
        produceMigrations = { context ->
            // Since we're migrating from SharedPreferences, add a migration based on the
            // SharedPreferences name
            listOf(SharedPreferencesMigration(context, APP_PREFERENCES_NAME))
        })

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideAppDataStore(dataStore: DataStore<Preferences>): AppDataStore {
        return AppDataStoreImpl(dataStore)
    }
}