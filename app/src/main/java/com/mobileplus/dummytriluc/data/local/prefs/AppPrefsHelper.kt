package com.mobileplus.dummytriluc.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.mobileplus.dummytriluc.BuildConfig
import com.mobileplus.dummytriluc.data.model.MachineInfo
import com.mobileplus.dummytriluc.data.model.UserInfo
import com.mobileplus.dummytriluc.data.response.HomeListResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.fromJsonSafe
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr

class AppPrefsHelper constructor(context: Context, prefsName: String, private val gson: Gson) :
    PrefsHelper {
    companion object {
        const val KEY_HOME_LIST_RESPONSE = "KEY_HOME_LIST_RESPONSE"
        const val KEY_USER_INFO = "KEY_USER_INFO"
        const val KEY_TOKEN = "KEY_TOKEN"
        const val KEY_IS_LOGGED_IN = "KEY_IS_LOGGED_IN"
        const val KEY_HOT_LINE_NUMBER = "KEY_HOT_LINE_NUMBER"
        const val KEY_DATA_SECURITY = "KEY_DATA_SECURITY"
        const val KEY_USER_NAME = "KEY_USER_NAME"
        const val KEY_PASSWORD = "KEY_PASSWORD"
        const val KEY_EXPIRED_DAY_TOKEN = "KEY_EXPIRED_DAY_TOKEN"
        const val KEY_OPEN_FIRST_APP = "KEY_OPEN_FIRST_APP"
        const val KEY_UPDATE_APP = "KEY_UPDATE_APP"
        const val KEY_MACHINE_CODE_LASTED = "KEY_MACHINE_CODE_LASTED"
        const val KEY_LAST_STATUS_CONNECT_MACHINE = "KEY_LAST_STATUS_CONNECT_MACHINE"
    }

    private val userPrefs: SharedPreferences =
        context.getSharedPreferences(prefsName + "user", Context.MODE_PRIVATE)
    private val appPrefs: SharedPreferences =
        context.getSharedPreferences(prefsName + "app", Context.MODE_PRIVATE)

    override fun getHomeResponse(): HomeListResponse? {
        val response = userPrefs.getString(KEY_HOME_LIST_RESPONSE, "")
        return try {
            if (response?.isNotEmpty() == true)
                gson.fromJson(response, HomeListResponse::class.java) else null
        } catch (e: Exception) {
            e.logErr()
            null
        }
    }

    override fun saveHomeResponse(response: HomeListResponse) {
        userPrefs.edit().putString(KEY_HOME_LIST_RESPONSE, gson.toJson(response)).apply()
    }

    override fun getToken(): String? {
        val token = userPrefs.getString(KEY_TOKEN, "")
        return if (token.isNullOrEmpty()) null else token
    }

    override fun setToken(token: String) {
        userPrefs.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun saveUser(userInfo: UserInfo) {
        userPrefs.edit().putString(KEY_USER_INFO, gson.toJson(userInfo)).apply()
    }

    override fun getUserInfo(): UserInfo? {
        val data = userPrefs.getString(KEY_USER_INFO, "")
        return try {
            if (data?.isNotEmpty() == true)
                gson.fromJson(data, UserInfo::class.java) else null
        } catch (e: Exception) {
            e.logErr()
            null
        }
    }

    override fun clearUser() {
        userPrefs.edit().remove(KEY_USER_INFO).apply()
    }

    override fun logoutLocal() {
        userPrefs.edit().clear().apply()
    }

    override fun setIsLoggedIn(isLogged: Boolean) {
        userPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLogged).apply()
    }

    override fun isLoggedIn(): Boolean {
        return userPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    override var numberHotLine: String
        get() = appPrefs.getString(KEY_HOT_LINE_NUMBER, "") ?: ""
        set(value) {
            appPrefs.edit().putString(KEY_HOT_LINE_NUMBER, value).apply()
        }
    override var isDataSecurityBle: Boolean
        get() = appPrefs.getBoolean(KEY_DATA_SECURITY, false)
        set(value) {
            appPrefs.edit().putBoolean(KEY_DATA_SECURITY, value).apply()
        }
    override var userName: String
        get() =
            appPrefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) {
            appPrefs.edit().putString(KEY_USER_NAME, value).apply()
        }
    override var password: String
        get() = appPrefs.getString(KEY_PASSWORD, "") ?: ""
        set(value) {
            appPrefs.edit().putString(KEY_PASSWORD, value).apply()
        }
    override var isOpenFirstApp: Boolean
        get() = appPrefs.getBoolean(KEY_OPEN_FIRST_APP, false)
        set(value) {
            appPrefs.edit().putBoolean(KEY_OPEN_FIRST_APP, value).apply()
        }
    override var expiredTokenInDay: String
        get() = appPrefs.getString(KEY_EXPIRED_DAY_TOKEN, "") ?: ""
        set(value) {
            appPrefs.edit().putString(KEY_EXPIRED_DAY_TOKEN, value).apply()
        }
    override var versionUpdateApp: String
        get() = appPrefs.getString(KEY_UPDATE_APP, BuildConfig.VERSION_NAME)
            ?: BuildConfig.VERSION_NAME
        set(value) {
            appPrefs.edit().putString(KEY_UPDATE_APP, value).apply()
        }
    override var machineCodeConnectLasted: MachineInfo?
        get() {
            val json = userPrefs.getString(KEY_MACHINE_CODE_LASTED, "") ?: ""
            return if (json.isBlank()) {
                null
            } else {
                gson.fromJsonSafe<MachineInfo>(json)
            }
        }
        set(value) {
            if (value == null) {
                userPrefs.edit().remove(KEY_MACHINE_CODE_LASTED).apply()
            } else {
                userPrefs.edit().putString(KEY_MACHINE_CODE_LASTED, gson.toJson(value)).apply()
            }
        }
    override var isConnectedMachine: Boolean
        get() = userPrefs.getBoolean(KEY_LAST_STATUS_CONNECT_MACHINE, false)
        set(value) {
            userPrefs.edit().putBoolean(KEY_LAST_STATUS_CONNECT_MACHINE, value).apply()
        }
}