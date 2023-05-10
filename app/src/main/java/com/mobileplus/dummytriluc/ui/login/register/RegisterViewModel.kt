package com.mobileplus.dummytriluc.ui.login.register

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.SubjectItem
import com.mobileplus.dummytriluc.data.model.UserData
import com.mobileplus.dummytriluc.data.model.UserInfo
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CODE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CODE_ERROR_ACCOUNT_MISSING
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CODE_SUCCESS
import com.mobileplus.dummytriluc.data.remote.ApiConstants.SRC
import com.mobileplus.dummytriluc.data.remote.ApiConstants.TOKEN
import com.mobileplus.dummytriluc.data.request.RegisterRequest
import com.mobileplus.dummytriluc.data.request.UpdateInfo
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.io.File

class RegisterViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxRegisterNextStep: PublishSubject<Boolean> = PublishSubject.create()
    val rxOpenMain: PublishSubject<Boolean> = PublishSubject.create()
    val rxUploadImageSuccess: PublishSubject<Boolean> = PublishSubject.create()
    val rxUpdateInfoSuccess: PublishSubject<Boolean> = PublishSubject.create()
    val subjectData: PublishSubject<List<SubjectItem>> = PublishSubject.create()

    fun setFirstConnect(isFirstConnect: Boolean) = dataManager.setFirstConnect(isFirstConnect)
    fun getUserInfo() = dataManager.getUserInfo()

    fun registerUser(request: RegisterRequest): Disposable {
        isLoading.onNext(true)
        return dataManager.register(request)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                when (response.get(CODE).asInt) {
                    CODE_SUCCESS -> {
                        dataManager.userName = request.email ?: ""
                        dataManager.password = request.password ?: ""
                        dataManager.setToken(response.dataObject().get(TOKEN).asString)
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
                        rxOpenMain.onNext(true)
                    }
                    CODE_ERROR_ACCOUNT_MISSING -> {
                        rxRegisterNextStep.onNext(true)
                        dataManager.setToken(response.dataObject().get(TOKEN).asString)
                    }
                    else -> {
                        rxMessage.onNext(response.message())
                    }
                }
            }, {
                isLoading.onNext(false)
                it.printStackTrace()
                rxMessage.onNext(it.getErrorMsg())
            })
    }


    fun updateAvatar(imageAvatar: File): Disposable {
        isLoading.onNext(true)
        return dataManager.uploadAvatar(imageAvatar)
            .flatMap { response ->
                dataManager.updateAvatar(response.dataObject().get(SRC).asString)
            }
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                rxUploadImageSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
                it.logErr()
            })
    }


    fun getSubjectList(): Disposable {
        isLoading.onNext(true)
        return dataManager.getSubjectData()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        subjectData.onNext(emptyList())
                    } else {
                        try {
                            subjectData.onNext(gson.toList(response.dataArray()))
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                } else {
                    rxMessage.onNext(response.message())
                }
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
                logErr("${it.message}")
                it.printStackTrace()
            })
    }

    fun updateUserInfo(user: UpdateInfo): Disposable {
        isLoading.onNext(true)
        return dataManager.updateUserInfo(user)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                rxUpdateInfoSuccess.onNext(response.isSuccess())
                if (response.isSuccess()) {
                    val userInfo = gson.fromJson(response.dataObject(), UserInfo::class.java)
                    dataManager.saveUser(userInfo)
                    dataManager.setIsLoggedIn(true)
                } else {
                    rxMessage.onNext(response.message())
                }
            }, {
                isLoading.onNext(false)
                logErr("${it.message}")
                rxMessage.onNext(it.getErrorMsg())
                it.printStackTrace()
            })
    }
}