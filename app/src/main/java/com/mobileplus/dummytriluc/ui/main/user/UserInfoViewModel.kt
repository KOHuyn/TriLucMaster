package com.mobileplus.dummytriluc.ui.main.user

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.UserInfo
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.UpdateInfo
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.io.File

class UserInfoViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxUserInfo: PublishSubject<UserInfo> = PublishSubject.create()
    val updateSuccess: PublishSubject<UserInfo> = PublishSubject.create()

    val rxStatusReceiveMaster: PublishSubject<Boolean> = PublishSubject.create()
    val rxStatusUnReceiveMaster: PublishSubject<Boolean> = PublishSubject.create()

    val homeResponse = dataManager.getHomeResponse()

    fun updateUserInfo(updateInfo: UpdateInfo): Disposable {
        isLoading.onNext(true)
        return dataManager.updateUserInfo(updateInfo)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    val userInfo = gson.fromJson(
                        response.dataObject(),
                        UserInfo::class.java
                    )
                    rxUserInfo.onNext(userInfo)
                    dataManager.saveUser(userInfo)
                    updateSuccess.onNext(userInfo)
                } else {
                    rxMessage.onNext(response.message())
                }
            }, {
                isLoading.onNext(false)
                it.printStackTrace()
            })
    }

    fun getUserGuest(id: Int): Disposable {
        return dataManager.getUserGuest(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    val info = gson.fromJson(response.dataObject(), UserInfo::class.java)
                    rxUserInfo.onNext(info)
                } else {
                    rxMessage.onNext(response.message())
                }
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun requestMaster(msg: String, masterId: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.postTrainerRequest(msg, masterId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                rxStatusReceiveMaster.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                rxStatusReceiveMaster.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun unReceiverMaster(masterId: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.postTrainerRequestRemove(masterId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                rxStatusUnReceiveMaster.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                rxStatusUnReceiveMaster.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getUserInfo(): Disposable {
        isLoading.onNext(true)
        return dataManager.getUserInfoSever()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    val info = gson.fromJson(response.dataObject(), UserInfo::class.java)
                    var user = dataManager.getUserInfo()
                    if (user != null) {
                        user = info
                        dataManager.saveUser(user)
                        rxUserInfo.onNext(user)
                    }
                } else {
                    rxMessage.onNext(response.message())
                }
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun updateAvatar(imageAvatar: File): Disposable {
        var avatarPath: String? = null
        isLoading.onNext(true)
        return dataManager.uploadAvatar(imageAvatar)
            .flatMap { response ->
                avatarPath = response.dataObject().get(ApiConstants.FULL_WIDTH).asString
                dataManager.updateAvatar(response.dataObject().get(ApiConstants.SRC).asString)
            }
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    val user = dataManager.getUserInfo()
                    if (user != null) {
                        user.avatarPath = avatarPath
                        dataManager.saveUser(user)
                        updateSuccess.onNext(user)
                    }
                }
                rxMessage.onNext(response.message())
            },
                {
                    isLoading.onNext(false)
                    rxMessage.onNext(it.getErrorMsg())
                    logErr("${it.message}")
                    it.printStackTrace()
                }
            )
    }

}