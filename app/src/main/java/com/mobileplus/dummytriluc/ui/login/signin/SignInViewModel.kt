package com.mobileplus.dummytriluc.ui.login.signin

import com.core.BaseViewModel
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.UserData
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.LoginRequest
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class SignInViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxShouldUpdateInfo: PublishSubject<Boolean> = PublishSubject.create()
    val rxLoginSuccess: PublishSubject<Boolean> = PublishSubject.create()

    val userName get() = dataManager.userName
    val password get() = dataManager.password

    fun login(request: LoginRequest): Disposable {
        isLoading.onNext(true)
        return dataManager.login(request)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                try {
                    if (response.isSuccess()) {
                        dataManager.userName = request.email ?: ""
                        dataManager.password = request.password ?: ""
                        val userData =
                            gson.fromJson(response.dataObject(), UserData::class.java)
                        dataManager.run {
                            saveUser(userData.userInfo)
                            setToken(userData.token)
                            setIsLoggedIn(true)
                        }
                    } else {
                        if (response.get(ApiConstants.CODE).asInt == ApiConstants.CODE_ERROR_ACCOUNT_MISSING) {
                            dataManager.setToken(
                                response.dataObject().get(ApiConstants.TOKEN).asString
                            )
                            rxShouldUpdateInfo.onNext(true)
                        } else {
                            rxMessage.onNext(response.message())
                        }
                    }
                } catch (e: Exception) {
                    e.logErr()
                }
                rxLoginSuccess.onNext(response.isSuccess())
                logErr(response.toString())
                logErr(gson.toJson(request))
            }, {
                rxMessage.onNext(it.getErrorMsg())
                isLoading.onNext(false)
            })
    }
}