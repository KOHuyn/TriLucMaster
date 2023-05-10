package com.mobileplus.dummytriluc.ui.main.power

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.response.DataPowerResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.lang.Exception

class PowerViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val responsePower: PublishSubject<DataPowerResponse> = PublishSubject.create()

    fun getDataPower(type: String? = ApiConstants.MONTH, page: Int? = 1): Disposable {
        isLoading.onNext(true)
        return dataManager.getPower(type, page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                try {
                    if (response.isSuccess()) {
                        val data = gson.fromJson(
                            response.dataObject(),
                            DataPowerResponse::class.java
                        )
                        logErr("responsePower.onNext(data)")
                        responsePower.onNext(data)
                    } else {
                        rxMessage.onNext(response.message())
                    }
                } catch (e: Exception) {
                    e.logErr()
                    rxMessage.onNext(loadStringRes(R.string.data_not_available))
                }
                logErr("$response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}