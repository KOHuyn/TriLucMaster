package com.mobileplus.dummytriluc.data.local.prefs

import com.mobileplus.dummytriluc.data.model.UserInfo
import com.mobileplus.dummytriluc.data.response.HomeListResponse

interface PrefsHelper {
    fun getHomeResponse(): HomeListResponse?
    fun saveHomeResponse(response: HomeListResponse)

    fun getToken(): String?
    fun setToken(token: String)

    fun saveUser(userInfo: UserInfo)
    fun getUserInfo(): UserInfo?
    fun clearUser()

    fun logoutLocal()

    fun setIsLoggedIn(isLogged: Boolean)
    fun isLoggedIn(): Boolean

    fun isFirstConnect(): Boolean
    fun setFirstConnect(isFirst: Boolean)
    var numberHotLine: String

    var isDataSecurityBle: Boolean
    var userName: String
    var password: String
    var isOpenFirstApp: Boolean
    var expiredTokenInDay: String
    var versionUpdateApp: String


}