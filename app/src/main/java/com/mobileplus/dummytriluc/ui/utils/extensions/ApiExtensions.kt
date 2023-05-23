package com.mobileplus.dummytriluc.ui.utils.extensions

import com.androidnetworking.error.ANError
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CODE_ERROR_ACCOUNT_MISSING
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CODE_ERROR_DATA_EMPTY
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CODE_ERROR_DATA_PROCESS
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CODE_ERROR_GENERAL
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CODE_ERROR_VALIDATE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CODE_UN_AUTHENTICATE
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventUnAuthen
import com.utils.ext.postNormal
import com.utils.ext.toList
import java.net.HttpURLConnection

fun JsonObject.isSuccess(): Boolean {
    when (get(ApiConstants.CODE).asInt) {
        CODE_ERROR_VALIDATE -> {
        }
        CODE_ERROR_GENERAL -> {
        }
        CODE_ERROR_DATA_EMPTY -> {
        }
        CODE_UN_AUTHENTICATE -> {
            postNormal(EventUnAuthen())
        }
        CODE_ERROR_DATA_PROCESS -> {
        }
        CODE_ERROR_ACCOUNT_MISSING -> {
        }
    }
    return get(ApiConstants.CODE).asInt == ApiConstants.CODE_SUCCESS
}

fun JsonObject.isDataEmpty(): Boolean = get(ApiConstants.CODE).asInt == CODE_ERROR_DATA_EMPTY

fun JsonObject.message(): String = get(ApiConstants.MESSAGE).asString

fun JsonObject.code(): Int = get(ApiConstants.CODE).asInt

fun JsonObject.page(): Page = try {
    Page(get(ApiConstants.CURRENT_PAGE).asInt, get(ApiConstants.LAST_PAGE).asInt)
} catch (e: Exception) {
    Page()
}

fun JsonObject.pageOrNull(): Page? = try {
    Page(get(ApiConstants.CURRENT_PAGE).asInt, get(ApiConstants.LAST_PAGE).asInt)
} catch (e: Exception) {
    null
}

fun JsonObject.dataObject(): JsonObject = getAsJsonObject(ApiConstants.DATA)

fun JsonObject.dataObjectSafe(): JsonObject? {
    return try {
        getAsJsonObject(ApiConstants.DATA)
    } catch (e: Exception) {
        null
    }
}

fun JsonObject.dataArray(): JsonArray = getAsJsonArray(ApiConstants.DATA)

fun JsonObject.isEmptyArray(): Boolean = get(ApiConstants.DATA).toString() == "[]"

inline fun <reified T> Gson.fromJson(json: JsonElement) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.fromJsonPretty(json: String): T? {
    return try {
        fromJson<T>(json, object : TypeToken<T>() {}.type)
    } catch (e: JsonSyntaxException) {
        fromJson<T>(
            fromJson(
                json,
                JsonElement::class.java
            ).asJsonPrimitive.asString, object : TypeToken<T>() {}.type
        )
    } catch (e: Exception) {
        null
    }
}

inline fun <reified T> Gson.fromJsonSafe(json: String): T? {
    return try {
        fromJson<T>(json, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        null
    }
}

inline fun <reified T> Gson.fromJsonSafe(json: JsonElement?): T? {
    return try {
        fromJson<T>(json, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        e.logErr()
        null
    }
}

fun Firebase.logJsonError(json: String) {
    crashlytics.log(json)
    crashlytics.recordException(Throwable("Json Fail Exception ${System.currentTimeMillis() / 1000}"))
}

fun Firebase.logJsonSuccess(json: String) {
    crashlytics.log(json)
    crashlytics.recordException(Throwable("Json Success ${System.currentTimeMillis() / 1000}"))
}

fun Throwable?.getErrorMsg(): String = try {
    when {
        this is ANError -> {
            when (this.errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    postNormal(EventUnAuthen())
                    loadStringRes(R.string.session_expired)
                }
                0 -> {
                    if (this.errorDetail == "connectionError")
                        loadStringRes(R.string.server_error_or_no_internet)
                    else loadStringRes(R.string.server_error)
                }
                HttpURLConnection.HTTP_SERVER_ERROR -> loadStringRes(R.string.server_error)
                else -> this.errorBody
            }
        }
        this?.message != null -> this.message!!
        else -> loadStringRes(R.string.server_error_or_no_internet)
    }
} catch (e: Exception) {
    loadStringRes(R.string.server_error_or_no_internet)
}
