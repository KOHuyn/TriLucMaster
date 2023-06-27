package com.apero.qrart.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by KO Huyn on 26/06/2023.
 */
class RequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val request = originalRequest.newBuilder().url(originalRequest.url).build()
        //TODO USING ANY IN INTERCEPTOR
//        Timber.d(request.toString())
        return chain.proceed(request)
    }
}