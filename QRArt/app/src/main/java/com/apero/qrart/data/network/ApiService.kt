package com.apero.qrart.data.network

import com.apero.qrart.data.model.QRPreviewModel
import retrofit2.Response

/**
 * Created by KO Huyn on 26/06/2023.
 */
interface ApiService {
    suspend fun getTemplate(): Response<List<QRPreviewModel>>
}