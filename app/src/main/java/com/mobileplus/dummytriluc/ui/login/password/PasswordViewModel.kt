package com.mobileplus.dummytriluc.ui.login.password

import android.annotation.SuppressLint
import com.core.BaseViewModel
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class PasswordViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val startCount: PublishSubject<Boolean> = PublishSubject.create()
    val updateSuccess: PublishSubject<Boolean> = PublishSubject.create()

    fun requestCode(email: String): Disposable {
        isLoading.onNext(true)
        return dataManager.requestOTPPassword(email)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe(
                {
                    isLoading.onNext(false)
                    rxMessage.onNext(it.message())
                    startCount.onNext(it.isSuccess())
                }, {
                    isLoading.onNext(false)
                    it.logErr()
                    rxMessage.onNext(it.getErrorMsg())
                }
            )
    }

    @SuppressLint("CheckResult")
    fun changePassword(
        email: String,
        code: String,
        newPassword: String,
        confirmPassword: String
    ) {
        isLoading.onNext(true)
        dataManager.postNewPassword(email, code, newPassword, confirmPassword)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe(
                {
                    isLoading.onNext(false)
                    rxMessage.onNext(it.message())
                    updateSuccess.onNext(it.isSuccess())
                },
                {
                    isLoading.onNext(false)
                    it.logErr()
                    rxMessage.onNext(it.getErrorMsg())
                }
            )
    }

    fun updatePassword(oldPassword: String, newPassword: String): Disposable {
        isLoading.onNext(true)
        return dataManager
            .updatePassword(oldPassword, newPassword)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                updateSuccess.onNext(response.isSuccess())
                if (response.isSuccess()) {
                    try {
                        dataManager.setToken(
                            response.dataObject().get(ApiConstants.TOKEN).asString
                        )
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
                rxMessage.onNext(response.message())
            },
                {
                    isLoading.onNext(false)
                    it.logErr()
                    rxMessage.onNext(it.getErrorMsg())
                })
    }

}