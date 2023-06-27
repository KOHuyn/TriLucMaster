package com.apero.qrart.data.prefs

import kotlinx.coroutines.flow.Flow

/**
 * Created by KO Huyn on 26/06/2023.
 */
interface AppDataStore {
    fun isOpenAppFirst(): Flow<Boolean>
    suspend fun setOpenAppFirst(isFirst: Boolean)
}