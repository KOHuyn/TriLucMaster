package com.apero.qrart.data.network

import com.apero.qrart.data.network.exception.NoContentException
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * Created by KO Huyn on 26/06/2023.
 */
sealed interface ApiResponse<out T> {
    data class Success<T>(val response: Response<T>) : ApiResponse<T> {
        val statusCode: Int = response.code()
        val headers: Headers = response.headers()
        val raw: okhttp3.Response = response.raw()
        val data: T by lazy { response.body() ?: throw NoContentException(response.code()) }
        override fun toString(): String = "[ApiResponse.Success](data=$data)"
    }

    sealed interface Failure<T> : ApiResponse<T> {
        data class Error<T>(val response: Response<T>) : Failure<T> {
            val statusCode: Int = response.code()
            val headers: Headers = response.headers()
            val raw: okhttp3.Response = response.raw()
            val errorBody: ResponseBody? = response.errorBody()
            override fun toString(): String {
                val errorBody = errorBody?.string()
                return if (!errorBody.isNullOrEmpty()) {
                    errorBody
                } else {
                    "[ApiResponse.Failure.Error-$statusCode](errorResponse=$response)"
                }
            }
        }

        data class Exception<T>(val exception: Throwable) : Failure<T> {
            val message: String? = exception.localizedMessage
            override fun toString(): String = "[ApiResponse.Failure.Exception](message=$message)"
        }

        companion object {
            fun <T> error(ex: Throwable): Exception<T> = Exception(ex)

            inline fun <T> of(
                crossinline f: () -> Response<T>,
            ): ApiResponse<T> = try {
                val response = f()
                if (response.isSuccessful) {
                    Success(response)
                } else {
                    Error(response)
                }
            } catch (ex: kotlin.Exception) {
                Exception(ex)
            }
        }
    }
}
