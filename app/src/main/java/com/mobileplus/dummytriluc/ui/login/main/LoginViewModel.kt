package com.mobileplus.dummytriluc.ui.login.main

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.UserData
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.SocialLoginRequest
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class LoginViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxShouldUpdateInfo: PublishSubject<Boolean> = PublishSubject.create()
    val rxLoginSuccess: PublishSubject<Boolean> = PublishSubject.create()

    fun doSocialLogin(request: SocialLoginRequest): Disposable {
        isLoading.onNext(true)
        return dataManager.loginSocial(request)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                logErr(response.toString())
                logErr(gson.toJson(request))
                rxLoginSuccess.onNext(response.isSuccess())
                when (response.get(ApiConstants.CODE).asInt) {
                    ApiConstants.CODE_SUCCESS -> {
                        dataManager.setIsLoggedIn(true)
                        try {
                            val userData =
                                gson.fromJson(response.dataObject(), UserData::class.java)
                            dataManager.run {
                                saveUser(userData.userInfo)
                                setToken(userData.token)
                            }
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                    ApiConstants.CODE_ERROR_ACCOUNT_MISSING -> {
                        try {
                            val userData =
                                gson.fromJson(response.dataObject(), UserData::class.java)
                            dataManager.run {
                                saveUser(userData.userInfo)
                                setToken(userData.token)
                            }
                        } catch (e: Exception) {
                            e.logErr()
                        }
                        rxShouldUpdateInfo.onNext(true)
                    }
                    else -> {
                        rxMessage.onNext(response.message())
                    }
                }

            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}