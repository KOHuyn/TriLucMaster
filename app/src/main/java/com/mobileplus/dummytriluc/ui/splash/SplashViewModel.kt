package com.mobileplus.dummytriluc.ui.splash

import com.core.BaseViewModel
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.extensions.isSuccess
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo

class SplashViewModel(dataManager: DataManager, schedulerProvider: SchedulerProvider) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val isLogin = dataManager.isLoggedIn()

    fun deleteTokenPushWhenFirstApp() {
        if (dataManager.isOpenFirstApp) return
        dataManager.removeToken().compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    dataManager.isOpenFirstApp = true
                }
            }, {
                it.logErr()
            }).addTo(disposable)
    }

    fun refreshToken() {
        val dateCurrent = DateTimeUtil.convertCurrentDate()
        if (dataManager.expiredTokenInDay == dateCurrent) return
        if (dataManager.getToken().isNullOrEmpty()) return
        dataManager.refreshToken().compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    dataManager.expiredTokenInDay = dateCurrent
                    try {
                        dataManager.setToken(response.get(ApiConstants.DATA).asString)
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
            }, {
                it.logErr()
            }).addTo(disposable)
    }
}