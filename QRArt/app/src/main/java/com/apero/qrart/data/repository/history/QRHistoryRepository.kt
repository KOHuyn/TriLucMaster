package com.apero.qrart.data.repository.history

/**
 * Created by KO Huyn on 26/06/2023.
 */
interface QRHistoryRepository {
    fun getListHistory()
    fun deleteHistory(id: Int)
    fun renameHistory(id: Int, newName: String)
}