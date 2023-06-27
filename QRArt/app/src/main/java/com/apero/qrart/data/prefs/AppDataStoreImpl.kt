package com.apero.qrart.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by KO Huyn on 26/06/2023.
 */
class AppDataStoreImpl(
    private val dataStore: DataStore<Preferences>
) : AppDataStore {
    override fun isOpenAppFirst(): Flow<Boolean> {
        return dataStore.get(PreferencesKeys.KEY_OPEN_APP_FIRST).map { it ?: false }
    }

    override suspend fun setOpenAppFirst(isFirst: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_OPEN_APP_FIRST] = isFirst
        }
    }
}